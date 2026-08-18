package com.mailsent.delivery.ingest;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * DmarcAggregateParser — RFC 7489 RUA XML aggregate report parser (INT-004).
 */
public final class DmarcAggregateParser {

    private DmarcAggregateParser() {}

    public static ParsedDmarcReport parse(byte[] xmlBytes) throws Exception {
        if (xmlBytes == null || xmlBytes.length == 0) {
            throw new IllegalArgumentException("XML payload must not be empty.");
        }

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // Prevent XXE
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new ByteArrayInputStream(xmlBytes));
        doc.getDocumentElement().normalize();

        // 1. Report Metadata
        NodeList metaNodes = doc.getElementsByTagName("report_metadata");
        if (metaNodes.getLength() == 0) {
            throw new IllegalArgumentException("Missing report_metadata in DMARC RUA report.");
        }
        Element metaElem = (Element) metaNodes.item(0);

        String orgName = getTagValue(metaElem, "org_name");
        String email = getTagValue(metaElem, "email");
        String reportId = getTagValue(metaElem, "report_id");

        if (reportId == null || reportId.isBlank()) {
            throw new IllegalArgumentException("report_id is missing: unusable report (INT-004)");
        }

        long beginEpoch = 0L;
        long endEpoch = 0L;
        NodeList dateRangeNodes = metaElem.getElementsByTagName("date_range");
        if (dateRangeNodes.getLength() > 0) {
            Element dateElem = (Element) dateRangeNodes.item(0);
            try {
                beginEpoch = Long.parseLong(getTagValue(dateElem, "begin"));
                endEpoch = Long.parseLong(getTagValue(dateElem, "end"));
            } catch (Exception ignored) {
            }
        }
        Instant begin = beginEpoch > 0 ? Instant.ofEpochSecond(beginEpoch) : Instant.now();
        Instant end = endEpoch > 0 ? Instant.ofEpochSecond(endEpoch) : Instant.now();

        // 2. Records
        List<ParsedDmarcRecord> recordList = new ArrayList<>();
        NodeList recordNodes = doc.getElementsByTagName("record");
        for (int i = 0; i < recordNodes.getLength(); i++) {
            Element recElem = (Element) recordNodes.item(i);

            String sourceIp = "0.0.0.0";
            int count = 1;
            NodeList rowNodes = recElem.getElementsByTagName("row");
            if (rowNodes.getLength() > 0) {
                Element rowElem = (Element) rowNodes.item(0);
                String ip = getTagValue(rowElem, "source_ip");
                if (ip != null) sourceIp = ip;
                String cntStr = getTagValue(rowElem, "count");
                if (cntStr != null) {
                    try { count = Integer.parseInt(cntStr); } catch (Exception ignored) {}
                }
            }

            String disposition = "none";
            String dkimResult = "pass";
            String spfResult = "pass";
            NodeList policyNodes = recElem.getElementsByTagName("policy_evaluated");
            if (policyNodes.getLength() > 0) {
                Element polElem = (Element) policyNodes.item(0);
                String disp = getTagValue(polElem, "disposition");
                if (disp != null) disposition = disp;
                String dkim = getTagValue(polElem, "dkim");
                if (dkim != null) dkimResult = dkim;
                String spf = getTagValue(polElem, "spf");
                if (spf != null) spfResult = spf;
            }

            boolean dkimAligned = "pass".equalsIgnoreCase(dkimResult);
            boolean spfAligned = "pass".equalsIgnoreCase(spfResult);
            String headerFrom = "";

            NodeList identNodes = recElem.getElementsByTagName("identifiers");
            if (identNodes.getLength() > 0) {
                Element idElem = (Element) identNodes.item(0);
                String hf = getTagValue(idElem, "header_from");
                if (hf != null) headerFrom = hf;
            }

            recordList.add(new ParsedDmarcRecord(sourceIp, count, disposition, dkimResult, spfResult, dkimAligned, spfAligned, headerFrom));
        }

        return new ParsedDmarcReport(reportId, orgName != null ? orgName : "Unknown", email, begin, end, recordList);
    }

    private static String getTagValue(Element parent, String tagName) {
        NodeList nl = parent.getElementsByTagName(tagName);
        if (nl.getLength() > 0 && nl.item(0).getFirstChild() != null) {
            return nl.item(0).getFirstChild().getNodeValue();
        }
        return null;
    }
}
