package com.mailsent.portfolio.observation.application;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mailsent.portfolio.domain.domain.Domain;
import com.mailsent.portfolio.domain.infrastructure.InMemoryDomainRepository;
import com.mailsent.portfolio.observation.domain.DmarcReportReceipt;
import com.mailsent.portfolio.observation.domain.ParsedDmarcRecord;
import com.mailsent.portfolio.observation.domain.ParsedDmarcReport;
import com.mailsent.portfolio.observation.infrastructure.InMemoryDmarcReportReceiptRepository;
import com.mailsent.portfolio.observation.infrastructure.InMemoryObservationEvidenceRepository;
import com.mailsent.portfolio.observation.infrastructure.Rfc7489DmarcAggregateXmlParser;

/**
 * Unit tests for {@link DmarcReportParseService} (INT-004) — RFC 7489 aggregate
 * XML facts, zip/gz wrappers, unusable outcomes, Receipt identity, and no
 * aggregate/OE/alignment/health/diagnosis side effects.
 */
class DmarcReportParseServiceTest {

    private static final Instant NOW = Instant.parse("2026-08-15T10:00:00Z");
    private static final String VALID_XML = """
        <?xml version="1.0" encoding="UTF-8"?>
        <feedback>
          <report_metadata>
            <org_name>google.com</org_name>
            <email>noreply-dmarc-support@google.com</email>
            <report_id>1.2.3.4.999</report_id>
            <date_range>
              <begin>1609459200</begin>
              <end>1609545599</end>
            </date_range>
          </report_metadata>
          <policy_published>
            <domain>other.example</domain>
            <adkim>r</adkim>
            <aspf>s</aspf>
            <p>quarantine</p>
            <sp>none</sp>
            <pct>100</pct>
          </policy_published>
          <record>
            <row>
              <source_ip>192.0.2.1</source_ip>
              <count>3</count>
              <policy_evaluated>
                <disposition>none</disposition>
                <dkim>pass</dkim>
                <spf>fail</spf>
              </policy_evaluated>
            </row>
            <identifiers>
              <envelope_from>bounce.mail.example</envelope_from>
              <header_from>example.com</header_from>
            </identifiers>
            <auth_results>
              <dkim>
                <domain>sel.example.com</domain>
                <result>pass</result>
                <selector>s1</selector>
              </dkim>
              <dkim>
                <domain>ex.example.com</domain>
                <result>fail</result>
              </dkim>
              <spf>
                <domain>example.com</domain>
                <result>fail</result>
              </spf>
            </auth_results>
          </record>
        </feedback>
        """;

    private InMemoryDomainRepository domains;
    private InMemoryDmarcReportReceiptRepository receipts;
    private InMemoryObservationEvidenceRepository evidence;
    private DmarcReportReceiveService receive;
    private DmarcReportParseService parse;
    private Domain domain;

    @BeforeEach
    void setUp() {
        domains = new InMemoryDomainRepository();
        receipts = new InMemoryDmarcReportReceiptRepository();
        evidence = new InMemoryObservationEvidenceRepository();
        receive = new DmarcReportReceiveService(receipts, domains);
        parse = new DmarcReportParseService(receipts, new Rfc7489DmarcAggregateXmlParser());
        domain = Domain.register("org-1", "example.com", NOW);
        domains.save(domain);
    }

    @Test
    void validAggregateXmlExtractsMetadataRecordsAndAssociatesReportId() {
        DmarcReportReceipt receipt = store(VALID_XML.getBytes(StandardCharsets.UTF_8));
        assertNull(receipt.reportId());

        DmarcReportParseOutcome outcome = parse.parse(receipt.id());

        assertTrue(outcome.usable());
        ParsedDmarcReport parsed = outcome.parsed().orElseThrow();
        assertEquals("1.2.3.4.999", parsed.reportId());
        assertEquals("google.com", parsed.reportingOrg());
        assertEquals(Instant.ofEpochSecond(1609459200L), parsed.dateRangeBegin());
        assertEquals(Instant.ofEpochSecond(1609545599L), parsed.dateRangeEnd());
        assertEquals("other.example", parsed.policyDomain());
        assertEquals("quarantine", parsed.policyDisposition());
        assertEquals("r", parsed.policyDkim());
        assertEquals("s", parsed.policySpf());
        assertEquals(1, parsed.records().size());
        ParsedDmarcRecord record = parsed.records().get(0);
        assertEquals("192.0.2.1", record.sourceIp());
        assertEquals(3L, record.count());
        assertEquals("fail", record.spfResult());
        assertEquals("pass", record.dkimResult());
        assertEquals("fail", record.spfAligned());
        assertEquals("pass", record.dkimAligned());
        assertEquals("bounce.mail.example", record.envelopeFrom());
        assertEquals("example.com", record.headerFrom());
        assertEquals(List.of("sel.example.com", "ex.example.com"), record.dkimDomains());
        assertEquals("none", record.disposition());
        assertEquals("1.2.3.4.999", receipts.findById(receipt.id()).orElseThrow().reportId());
    }

    @Test
    void tenantAndDomainRemainReceiptIdentityWhenPolicyDomainDiffers() {
        DmarcReportReceipt receipt = store(VALID_XML.getBytes(StandardCharsets.UTF_8));
        ParsedDmarcReport parsed = parse.parse(receipt.id()).parsed().orElseThrow();

        assertEquals("org-1", parsed.organizationId());
        assertEquals(domain.id(), parsed.domainId());
        assertEquals("example.com", parsed.domainName());
        assertEquals(receipt.id(), parsed.receiptId());
        assertEquals("other.example", parsed.policyDomain());
        assertFalse(parsed.domainName().equals(parsed.policyDomain()));
    }

    @Test
    void gzipWrapperIsAccepted() throws Exception {
        DmarcReportReceipt receipt = store(gzip(VALID_XML.getBytes(StandardCharsets.UTF_8)));
        ParsedDmarcReport parsed = parse.parse(receipt.id()).parsed().orElseThrow();
        assertEquals("1.2.3.4.999", parsed.reportId());
        assertEquals("192.0.2.1", parsed.records().get(0).sourceIp());
    }

    @Test
    void zipWrapperIsAccepted() throws Exception {
        DmarcReportReceipt receipt = store(zip("google.com!example.com!1!2.xml",
            VALID_XML.getBytes(StandardCharsets.UTF_8)));
        ParsedDmarcReport parsed = parse.parse(receipt.id()).parsed().orElseThrow();
        assertEquals("1.2.3.4.999", parsed.reportId());
        assertEquals(3L, parsed.records().get(0).count());
    }

    @Test
    void malformedXmlIsUnusableAndDoesNotAssociateReportId() {
        DmarcReportReceipt receipt = store("<feedback><report_metadata>".getBytes(StandardCharsets.UTF_8));
        DmarcReportParseOutcome outcome = parse.parse(receipt.id());

        assertFalse(outcome.usable());
        assertEquals(DmarcReportParseFailure.MALFORMED_XML, outcome.failure().orElseThrow());
        assertNull(receipts.findById(receipt.id()).orElseThrow().reportId());
    }

    @Test
    void missingReportIdIsUnusable() {
        byte[] xml = """
            <feedback>
              <report_metadata>
                <org_name>google.com</org_name>
                <date_range><begin>1</begin><end>2</end></date_range>
              </report_metadata>
              <record><row><source_ip>192.0.2.1</source_ip><count>1</count></row></record>
            </feedback>
            """.getBytes(StandardCharsets.UTF_8);
        DmarcReportReceipt receipt = store(xml);
        DmarcReportParseOutcome outcome = parse.parse(receipt.id());

        assertFalse(outcome.usable());
        assertEquals(DmarcReportParseFailure.MISSING_REPORT_ID, outcome.failure().orElseThrow());
        assertNull(receipts.findById(receipt.id()).orElseThrow().reportId());
    }

    @Test
    void unsupportedInputIsUnusable() {
        DmarcReportReceipt receipt = store(new byte[] {0x00, 0x01, 0x02, '%', 'P', 'D', 'F'});
        DmarcReportParseOutcome outcome = parse.parse(receipt.id());

        assertFalse(outcome.usable());
        assertEquals(DmarcReportParseFailure.UNSUPPORTED_FORMAT, outcome.failure().orElseThrow());
        assertNull(receipts.findById(receipt.id()).orElseThrow().reportId());
    }

    @Test
    void structurallyInvalidAggregateIsUnusable() {
        DmarcReportReceipt receipt = store("<not-feedback><report_id>x</report_id></not-feedback>"
            .getBytes(StandardCharsets.UTF_8));
        DmarcReportParseOutcome outcome = parse.parse(receipt.id());

        assertFalse(outcome.usable());
        assertEquals(DmarcReportParseFailure.INVALID_AGGREGATE, outcome.failure().orElseThrow());
    }

    @Test
    void unknownReceiptIsUnusable() {
        DmarcReportParseOutcome outcome = parse.parse(UUID.randomUUID().toString());
        assertFalse(outcome.usable());
        assertEquals(DmarcReportParseFailure.MISSING_RECEIPT, outcome.failure().orElseThrow());
    }

    @Test
    void receiveDoesNotParseValidXml() {
        DmarcReportReceipt stored = receive.receive(domain.id(), VALID_XML.getBytes(StandardCharsets.UTF_8))
            .orElseThrow();
        assertNull(stored.reportId());
        assertArrayEquals(VALID_XML.getBytes(StandardCharsets.UTF_8), stored.payload());
    }

    @Test
    void observationEvidenceIsUntouched() {
        DmarcReportReceipt receipt = store(VALID_XML.getBytes(StandardCharsets.UTF_8));
        parse.parse(receipt.id());

        assertTrue(evidence.findByDomainId(domain.id()).isEmpty());
        assertTrue(evidence.findByOrganizationId("org-1").isEmpty());
        assertTrue(Arrays.stream(DmarcReportParseService.class.getDeclaredFields())
            .noneMatch(f -> f.getType().getSimpleName().contains("ObservationEvidence")));
    }

    @Test
    void doesNotCreateAggregateAlignmentHealthOrDiagnosis() {
        parse.parse(store(VALID_XML.getBytes(StandardCharsets.UTF_8)).id());

        assertTrue(Arrays.stream(DmarcReportParseService.class.getDeclaredFields())
            .noneMatch(f -> f.getType().getSimpleName().contains("DmarcAggregateEvidence")));
        assertTrue(Arrays.stream(DmarcReportParseService.class.getConstructors())
            .flatMap(c -> Arrays.stream(c.getParameterTypes()))
            .noneMatch(t -> t.getSimpleName().contains("DmarcAggregateEvidence")));
        assertTrue(Arrays.stream(DmarcReportParseService.class.getDeclaredFields())
            .noneMatch(f -> f.getType().getSimpleName().contains("Alignment")
                || f.getType().getSimpleName().contains("Health")
                || f.getType().getSimpleName().contains("Diagnosis")));
        assertTrue(Arrays.stream(DmarcReportParseService.class.getDeclaredFields())
            .noneMatch(f -> f.getType().getSimpleName().contains("Trigger")
                || f.getType().getSimpleName().contains("Diag001")));
        Set<String> ctorParams = Arrays.stream(DmarcReportParseService.class.getConstructors())
            .flatMap(c -> Arrays.stream(c.getParameterTypes()))
            .map(Class::getSimpleName)
            .collect(Collectors.toSet());
        assertEquals(Set.of("DmarcReportReceiptRepository", "DmarcAggregateXmlParser"), ctorParams);
        assertTrue(Arrays.stream(DmarcReportParseService.class.getDeclaredMethods())
            .map(Method::getName)
            .noneMatch(n -> n.toLowerCase().contains("diagnos") || n.toLowerCase().contains("align")
                || n.toLowerCase().contains("aggregat") || n.toLowerCase().contains("health")));
    }

    @Test
    void parseIsNotAnHttpEndpoint() {
        assertTrue(Arrays.stream(DmarcReportParseService.class.getDeclaredAnnotations())
            .noneMatch(a -> a.annotationType().getSimpleName().contains("Controller")
                || a.annotationType().getSimpleName().contains("RequestMapping")));
        assertTrue(Arrays.stream(DmarcReportParseService.class.getDeclaredMethods())
            .noneMatch(m -> Arrays.stream(m.getDeclaredAnnotations())
                .anyMatch(a -> a.annotationType().getSimpleName().contains("Mapping"))));
    }

    @Test
    void gzipBombIsUnusableUnsupportedFormat() throws Exception {
        DmarcReportParseService tightParse = new DmarcReportParseService(
            receipts, new Rfc7489DmarcAggregateXmlParser(new DmarcIngestLimits(1024, 64, 4, 4)));
        DmarcReportReceipt receipt = store(gzip(new byte[512]));
        DmarcReportParseOutcome outcome = tightParse.parse(receipt.id());
        assertFalse(outcome.usable());
        assertEquals(DmarcReportParseFailure.UNSUPPORTED_FORMAT, outcome.failure().orElseThrow());
        assertNull(receipts.findById(receipt.id()).orElseThrow().reportId());
    }

    @Test
    void xxePayloadIsUnusableMalformedXml() {
        String xxe = """
            <?xml version="1.0"?>
            <!DOCTYPE foo [<!ENTITY xxe SYSTEM "file:///etc/passwd">]>
            <feedback><report_metadata><report_id>&xxe;</report_id></report_metadata></feedback>
            """;
        DmarcReportReceipt receipt = store(xxe.getBytes(StandardCharsets.UTF_8));
        DmarcReportParseOutcome outcome = parse.parse(receipt.id());
        assertFalse(outcome.usable());
        assertEquals(DmarcReportParseFailure.MALFORMED_XML, outcome.failure().orElseThrow());
        assertNull(receipts.findById(receipt.id()).orElseThrow().reportId());
    }

    private DmarcReportReceipt store(byte[] payload) {
        return receive.receive(domain.id(), payload).orElseThrow();
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
