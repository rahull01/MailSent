package com.mailsent.portfolio.observation.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.mailsent.portfolio.observation.application.DmarcAggregateXmlExtraction;
import com.mailsent.portfolio.observation.application.DmarcIngestLimits;
import com.mailsent.portfolio.observation.application.DmarcReportParseFailure;

/**
 * INT-004 ingest hardening: bombs, malformed wrappers, XXE. Tight limits keep
 * tests small; production defaults remain on the no-arg parser.
 */
class Rfc7489DmarcAggregateXmlParserSecurityTest {

    private static final String VALID_XML = """
        <?xml version="1.0" encoding="UTF-8"?>
        <feedback>
          <report_metadata>
            <org_name>google.com</org_name>
            <report_id>sec-1</report_id>
            <date_range><begin>1</begin><end>2</end></date_range>
          </report_metadata>
          <record><row><source_ip>192.0.2.1</source_ip><count>1</count></row></record>
        </feedback>
        """;

    @Test
    void normalXmlStillParsesWithProductionLimits() {
        DmarcAggregateXmlExtraction extraction =
            new Rfc7489DmarcAggregateXmlParser().extract(VALID_XML.getBytes(StandardCharsets.UTF_8));
        assertTrue(extraction.extracted());
        assertEquals("sec-1", extraction.reportId());
    }

    @Test
    void normalGzipStillParsesWithProductionLimits() throws Exception {
        DmarcAggregateXmlExtraction extraction =
            new Rfc7489DmarcAggregateXmlParser().extract(gzip(VALID_XML.getBytes(StandardCharsets.UTF_8)));
        assertTrue(extraction.extracted());
        assertEquals("sec-1", extraction.reportId());
    }

    @Test
    void normalZipStillParsesWithProductionLimits() throws Exception {
        DmarcAggregateXmlExtraction extraction =
            new Rfc7489DmarcAggregateXmlParser().extract(zip("report.xml", VALID_XML.getBytes(StandardCharsets.UTF_8)));
        assertTrue(extraction.extracted());
        assertEquals("sec-1", extraction.reportId());
    }

    @Test
    void oversizedDecompressedGzipIsUnsupportedFormat() throws Exception {
        DmarcIngestLimits tight = new DmarcIngestLimits(1024, 64, 10_000, 4);
        byte[] gzipped = gzip(new byte[512]);
        DmarcAggregateXmlExtraction extraction = new Rfc7489DmarcAggregateXmlParser(tight).extract(gzipped);
        assertFalse(extraction.extracted());
        assertEquals(DmarcReportParseFailure.UNSUPPORTED_FORMAT, extraction.failure());
    }

    @Test
    void oversizedDecompressedZipIsUnsupportedFormat() throws Exception {
        DmarcIngestLimits tight = new DmarcIngestLimits(1024, 64, 10_000, 4);
        byte[] zipped = zip("report.xml", new byte[512]);
        DmarcAggregateXmlExtraction extraction = new Rfc7489DmarcAggregateXmlParser(tight).extract(zipped);
        assertFalse(extraction.extracted());
        assertEquals(DmarcReportParseFailure.UNSUPPORTED_FORMAT, extraction.failure());
    }

    @Test
    void excessiveCompressionRatioIsUnsupportedFormat() throws Exception {
        DmarcIngestLimits tight = new DmarcIngestLimits(1024, 20 * 1024 * 1024, 2, 4);
        byte[] zeros = new byte[400];
        DmarcAggregateXmlExtraction gzipOutcome = new Rfc7489DmarcAggregateXmlParser(tight).extract(gzip(zeros));
        assertFalse(gzipOutcome.extracted());
        assertEquals(DmarcReportParseFailure.UNSUPPORTED_FORMAT, gzipOutcome.failure());

        DmarcAggregateXmlExtraction zipOutcome = new Rfc7489DmarcAggregateXmlParser(tight).extract(zip("a.xml", zeros));
        assertFalse(zipOutcome.extracted());
        assertEquals(DmarcReportParseFailure.UNSUPPORTED_FORMAT, zipOutcome.failure());
    }

    @Test
    void malformedGzipIsUnsupportedFormat() {
        byte[] malformed = new byte[] {(byte) 0x1f, (byte) 0x8b, 0x08, 0x00, 0x00};
        DmarcAggregateXmlExtraction extraction = new Rfc7489DmarcAggregateXmlParser().extract(malformed);
        assertFalse(extraction.extracted());
        assertEquals(DmarcReportParseFailure.UNSUPPORTED_FORMAT, extraction.failure());
    }

    @Test
    void malformedZipIsUnsupportedFormat() {
        byte[] malformed = new byte[] {'P', 'K', 0x03, 0x04, 0x00};
        DmarcAggregateXmlExtraction extraction = new Rfc7489DmarcAggregateXmlParser().extract(malformed);
        assertFalse(extraction.extracted());
        assertEquals(DmarcReportParseFailure.UNSUPPORTED_FORMAT, extraction.failure());
    }

    @Test
    void excessiveZipEntriesAreUnsupportedFormat() throws Exception {
        DmarcIngestLimits tight = new DmarcIngestLimits(1024, 1024, 50, 2);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(out)) {
            zip.putNextEntry(new ZipEntry("a.bin"));
            zip.write(new byte[] {1});
            zip.closeEntry();
            zip.putNextEntry(new ZipEntry("b.bin"));
            zip.write(new byte[] {1});
            zip.closeEntry();
            zip.putNextEntry(new ZipEntry("report.xml"));
            zip.write(VALID_XML.getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
        }
        DmarcAggregateXmlExtraction extraction = new Rfc7489DmarcAggregateXmlParser(tight).extract(out.toByteArray());
        assertFalse(extraction.extracted());
        assertEquals(DmarcReportParseFailure.UNSUPPORTED_FORMAT, extraction.failure());
    }

    @Test
    void zipBombOfEmptyEntriesIsRejected() throws Exception {
        DmarcIngestLimits tight = new DmarcIngestLimits(1024, 256, 50, 2);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(out)) {
            zip.putNextEntry(new ZipEntry("a.bin"));
            zip.write(new byte[] {1});
            zip.closeEntry();
            zip.putNextEntry(new ZipEntry("b.bin"));
            zip.write(new byte[] {1});
            zip.closeEntry();
            zip.putNextEntry(new ZipEntry("c.bin"));
            zip.write(new byte[] {1});
            zip.closeEntry();
        }
        DmarcAggregateXmlExtraction extraction = new Rfc7489DmarcAggregateXmlParser(tight).extract(out.toByteArray());
        assertFalse(extraction.extracted());
        assertEquals(DmarcReportParseFailure.UNSUPPORTED_FORMAT, extraction.failure());
    }

    @Test
    void xxePayloadIsMalformedXmlAndDoesNotReadLocalFiles(@TempDir Path temp) throws Exception {
        Path secret = temp.resolve("secret.txt");
        Files.writeString(secret, "should-not-be-read");
        String xxe = """
            <?xml version="1.0" encoding="UTF-8"?>
            <!DOCTYPE foo [
              <!ENTITY xxe SYSTEM "%s">
            ]>
            <feedback>
              <report_metadata>
                <org_name>evil</org_name>
                <report_id>&xxe;</report_id>
                <date_range><begin>1</begin><end>2</end></date_range>
              </report_metadata>
            </feedback>
            """.formatted(secret.toUri());
        DmarcAggregateXmlExtraction extraction =
            new Rfc7489DmarcAggregateXmlParser().extract(xxe.getBytes(StandardCharsets.UTF_8));
        assertFalse(extraction.extracted());
        assertEquals(DmarcReportParseFailure.MALFORMED_XML, extraction.failure());
        assertTrue(extraction.reportId() == null || !extraction.reportId().contains("should-not-be-read"));
    }

    @Test
    void externalDtdAndEntityResolutionIsMalformedXml() {
        String external = """
            <?xml version="1.0" encoding="UTF-8"?>
            <!DOCTYPE feedback SYSTEM "file:///this-must-not-be-fetched.dtd">
            <feedback>
              <report_metadata>
                <org_name>evil</org_name>
                <report_id>ext-1</report_id>
                <date_range><begin>1</begin><end>2</end></date_range>
              </report_metadata>
            </feedback>
            """;
        DmarcAggregateXmlExtraction extraction =
            new Rfc7489DmarcAggregateXmlParser().extract(external.getBytes(StandardCharsets.UTF_8));
        assertFalse(extraction.extracted());
        assertEquals(DmarcReportParseFailure.MALFORMED_XML, extraction.failure());
    }

    @Test
    void zipPathTraversalIsUnsupportedFormat() throws Exception {
        DmarcAggregateXmlExtraction extraction =
            new Rfc7489DmarcAggregateXmlParser().extract(zip("../report.xml", VALID_XML.getBytes(StandardCharsets.UTF_8)));
        assertFalse(extraction.extracted());
        assertEquals(DmarcReportParseFailure.UNSUPPORTED_FORMAT, extraction.failure());
    }

    private static byte[] gzip(byte[] xml) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(out)) {
            gzip.write(xml);
        }
        return out.toByteArray();
    }

    private static byte[] zip(String name, byte[] xml) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(out)) {
            zip.putNextEntry(new ZipEntry(name));
            zip.write(xml);
            zip.closeEntry();
        }
        return out.toByteArray();
    }
}
