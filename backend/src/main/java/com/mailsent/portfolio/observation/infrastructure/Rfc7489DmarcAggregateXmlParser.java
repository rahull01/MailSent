package com.mailsent.portfolio.observation.infrastructure;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.mailsent.portfolio.observation.application.DmarcAggregateXmlExtraction;
import com.mailsent.portfolio.observation.application.DmarcAggregateXmlParser;
import com.mailsent.portfolio.observation.application.DmarcIngestLimits;
import com.mailsent.portfolio.observation.application.DmarcReportParseFailure;
import com.mailsent.portfolio.observation.domain.ParsedDmarcRecord;

/**
 * JDK XML reader for RFC 7489 aggregate reports. Zip and gzip are transport
 * wrappers, not a second schema. RUF is out of scope.
 */
public class Rfc7489DmarcAggregateXmlParser implements DmarcAggregateXmlParser {

    private static final byte[] GZIP_MAGIC = new byte[] {(byte) 0x1f, (byte) 0x8b};
    private static final byte[] ZIP_MAGIC = new byte[] {'P', 'K'};

    private final DmarcIngestLimits limits;

    public Rfc7489DmarcAggregateXmlParser() {
        this(DmarcIngestLimits.production());
    }

    public Rfc7489DmarcAggregateXmlParser(DmarcIngestLimits limits) {
        if (limits == null) {
            throw new IllegalArgumentException("limits");
        }
        this.limits = limits;
    }

    @Override
    public DmarcAggregateXmlExtraction extract(byte[] payload) {
        if (payload == null || payload.length == 0) {
            return DmarcAggregateXmlExtraction.unusable(DmarcReportParseFailure.UNSUPPORTED_FORMAT);
        }
        byte[] xmlBytes;
        try {
            xmlBytes = unwrap(payload);
        } catch (UnsupportedFormatException | InflationLimitException ex) {
            return DmarcAggregateXmlExtraction.unusable(DmarcReportParseFailure.UNSUPPORTED_FORMAT);
        } catch (IOException ex) {
            return DmarcAggregateXmlExtraction.unusable(DmarcReportParseFailure.UNSUPPORTED_FORMAT);
        }
        Document document;
        try {
            document = parseXml(xmlBytes);
        } catch (InflationLimitException ex) {
            return DmarcAggregateXmlExtraction.unusable(DmarcReportParseFailure.UNSUPPORTED_FORMAT);
        } catch (SAXException | IOException | ParserConfigurationException ex) {
            return DmarcAggregateXmlExtraction.unusable(DmarcReportParseFailure.MALFORMED_XML);
        }
        Element root = document.getDocumentElement();
        if (root == null || !equalsIgnoreCase(root.getTagName(), "feedback")) {
            return DmarcAggregateXmlExtraction.unusable(DmarcReportParseFailure.INVALID_AGGREGATE);
        }
        Element metadata = firstChild(root, "report_metadata");
        if (metadata == null) {
            return DmarcAggregateXmlExtraction.unusable(DmarcReportParseFailure.INVALID_AGGREGATE);
        }
        String reportId = childText(metadata, "report_id");
        if (reportId == null) {
            return DmarcAggregateXmlExtraction.unusable(DmarcReportParseFailure.MISSING_REPORT_ID);
        }
        Element dateRange = firstChild(metadata, "date_range");
        Instant begin = dateRange == null ? null : epochSeconds(childText(dateRange, "begin"));
        Instant end = dateRange == null ? null : epochSeconds(childText(dateRange, "end"));
        Element published = firstChild(root, "policy_published");
        String policyDomain = published == null ? null : childText(published, "domain");
        String policyDisposition = published == null ? null : childText(published, "p");
        String policyDkim = published == null ? null : childText(published, "adkim");
        String policySpf = published == null ? null : childText(published, "aspf");
        List<ParsedDmarcRecord> records = new ArrayList<>();
        for (Element record : children(root, "record")) {
            records.add(readRecord(record));
        }
        return DmarcAggregateXmlExtraction.extracted(
            reportId,
            childText(metadata, "org_name"),
            begin,
            end,
            policyDomain,
            policyDisposition,
            policyDkim,
            policySpf,
            records);
    }

    private static ParsedDmarcRecord readRecord(Element record) {
        Element row = firstChild(record, "row");
        Element evaluated = row == null ? null : firstChild(row, "policy_evaluated");
        Element identifiers = firstChild(record, "identifiers");
        Element auth = firstChild(record, "auth_results");
        List<Element> dkimAuth = auth == null ? List.of() : children(auth, "dkim");
        List<String> dkimDomains = new ArrayList<>();
        String dkimResult = null;
        for (Element dkim : dkimAuth) {
            String domain = childText(dkim, "domain");
            if (domain != null) {
                dkimDomains.add(domain);
            }
            if (dkimResult == null) {
                dkimResult = childText(dkim, "result");
            }
        }
        Element spfAuth = auth == null ? null : firstChild(auth, "spf");
        return new ParsedDmarcRecord(
            row == null ? null : childText(row, "source_ip"),
            parseCount(row == null ? null : childText(row, "count")),
            spfAuth == null ? null : childText(spfAuth, "result"),
            dkimResult,
            evaluated == null ? null : childText(evaluated, "spf"),
            evaluated == null ? null : childText(evaluated, "dkim"),
            identifiers == null ? null : childText(identifiers, "envelope_from"),
            identifiers == null ? null : childText(identifiers, "header_from"),
            dkimDomains,
            evaluated == null ? null : childText(evaluated, "disposition"));
    }

    private byte[] unwrap(byte[] payload) throws IOException {
        byte[] current = payload;
        if (startsWith(current, GZIP_MAGIC)) {
            current = gunzip(current);
        }
        if (startsWith(current, ZIP_MAGIC)) {
            current = unzipFirstXml(current);
            if (startsWith(current, GZIP_MAGIC)) {
                current = gunzip(current);
            }
        }
        if (looksLikeXml(current)) {
            return current;
        }
        throw new UnsupportedFormatException();
    }

    private byte[] gunzip(byte[] gzipped) throws IOException {
        try (GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(gzipped))) {
            return new BoundedInflatingInputStream(gzip, gzipped.length, limits).readAllBytes();
        }
    }

    private byte[] unzipFirstXml(byte[] zipped) throws IOException {
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(zipped))) {
            byte[] xmlCandidate = null;
            byte[] firstFile = null;
            int entries = 0;
            int inflated = 0;
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                entries++;
                if (entries > limits.maxZipEntries()) {
                    throw new InflationLimitException();
                }
                String name = entry.getName() == null ? "" : entry.getName();
                if (name.contains("..") || name.startsWith("/") || name.startsWith("\\")) {
                    throw new UnsupportedFormatException();
                }
                if (entry.isDirectory()) {
                    continue;
                }
                int remaining = limits.maxDecompressedBytes() - inflated;
                if (remaining <= 0) {
                    throw new InflationLimitException();
                }
                int ceiling = Math.min(remaining, limits.inflationCeiling(compressedSize(entry, zipped.length)));
                byte[] content = new BoundedInflatingInputStream(zip, ceiling).readAllBytes();
                inflated += content.length;
                if (name.contains("__MACOSX") || name.endsWith(".DS_Store")) {
                    continue;
                }
                if (firstFile == null && content.length > 0) {
                    firstFile = content;
                }
                if (name.toLowerCase().endsWith(".xml")) {
                    xmlCandidate = content;
                    break;
                }
            }
            byte[] chosen = xmlCandidate != null ? xmlCandidate : firstFile;
            if (chosen == null || chosen.length == 0) {
                throw new UnsupportedFormatException();
            }
            return chosen;
        }
    }

    private static int compressedSize(ZipEntry entry, int archiveLength) {
        long size = entry.getCompressedSize();
        if (size <= 0 || size > Integer.MAX_VALUE) {
            return archiveLength;
        }
        return (int) size;
    }

    private static boolean looksLikeXml(byte[] bytes) {
        int i = 0;
        if (bytes.length >= 3 && bytes[0] == (byte) 0xEF && bytes[1] == (byte) 0xBB && bytes[2] == (byte) 0xBF) {
            i = 3;
        }
        while (i < bytes.length && Character.isWhitespace(bytes[i] & 0xFF)) {
            i++;
        }
        return i < bytes.length && bytes[i] == '<';
    }

    private static boolean startsWith(byte[] bytes, byte[] magic) {
        if (bytes.length < magic.length) {
            return false;
        }
        for (int i = 0; i < magic.length; i++) {
            if (bytes[i] != magic[i]) {
                return false;
            }
        }
        return true;
    }

    private Document parseXml(byte[] xmlBytes)
            throws ParserConfigurationException, SAXException, IOException {
        if (xmlBytes.length > limits.maxDecompressedBytes()) {
            throw new InflationLimitException();
        }
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        factory.setExpandEntityReferences(false);
        factory.setXIncludeAware(false);
        factory.setValidating(false);
        requireFeature(factory, XMLConstants.FEATURE_SECURE_PROCESSING, true);
        requireFeature(factory, "http://apache.org/xml/features/disallow-doctype-decl", true);
        requireFeature(factory, "http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        requireFeature(factory, "http://xml.org/sax/features/external-general-entities", false);
        requireFeature(factory, "http://xml.org/sax/features/external-parameter-entities", false);
        try {
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        } catch (IllegalArgumentException ex) {
            throw new ParserConfigurationException("external DTD/schema access must be disabled");
        }
        DocumentBuilder builder = factory.newDocumentBuilder();
        builder.setEntityResolver((publicId, systemId) -> {
            throw new SAXException("External entity/resource resolution is disabled");
        });
        return builder.parse(new ByteArrayInputStream(xmlBytes));
    }

    private static void requireFeature(DocumentBuilderFactory factory, String feature, boolean value)
            throws ParserConfigurationException {
        factory.setFeature(feature, value);
    }

    private static List<Element> children(Element parent, String name) {
        List<Element> found = new ArrayList<>();
        for (Node node = parent.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (node instanceof Element element && equalsIgnoreCase(element.getTagName(), name)) {
                found.add(element);
            }
        }
        return found;
    }

    private static Element firstChild(Element parent, String name) {
        List<Element> found = children(parent, name);
        return found.isEmpty() ? null : found.get(0);
    }

    private static String childText(Element parent, String name) {
        Element child = firstChild(parent, name);
        if (child == null) {
            return null;
        }
        String text = child.getTextContent();
        if (text == null) {
            return null;
        }
        String trimmed = text.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static boolean equalsIgnoreCase(String left, String right) {
        return left != null && left.equalsIgnoreCase(right);
    }

    private static Instant epochSeconds(String value) {
        if (value == null) {
            return null;
        }
        try {
            return Instant.ofEpochSecond(Long.parseLong(value));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static Long parseCount(String value) {
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static final class UnsupportedFormatException extends IOException {
        private static final long serialVersionUID = 1L;
    }
}
