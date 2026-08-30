package com.mailsent.delivery.ingest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

import com.mailsent.alerts.domain.Alert;
import com.mailsent.alerts.domain.AlertSeverity;
import com.mailsent.alerts.domain.SlackNotificationPayload;
import com.mailsent.resolution.domain.DnsProviderGuide;

class DmarcIngestAndIntegrationsTest {

    private static final String SAMPLE_RUA_XML = """
            <?xml version="1.0"?>
            <feedback>
              <report_metadata>
                <org_name>google.com</org_name>
                <email>noreply-dmarc-support@google.com</email>
                <report_id>1234567890123456789</report_id>
                <date_range>
                  <begin>1771113600</begin>
                  <end>1771199999</end>
                </date_range>
              </report_metadata>
              <record>
                <row>
                  <source_ip>192.0.2.1</source_ip>
                  <count>150</count>
                  <policy_evaluated>
                    <disposition>none</disposition>
                    <dkim>pass</dkim>
                    <spf>pass</spf>
                  </policy_evaluated>
                </row>
                <identifiers>
                  <header_from>acme.com</header_from>
                </identifiers>
              </record>
            </feedback>
            """;

    @Test
    void receivesRuaReportReceipt() {
        byte[] bytes = SAMPLE_RUA_XML.getBytes(StandardCharsets.UTF_8);
        DmarcReportReceipt receipt = DmarcReportReceipt.receive("dom-1", "org-1", "google.com!acme.com!1234.xml", bytes, Instant.now());

        assertNotNull(receipt.id());
        assertEquals("dom-1", receipt.domainId());
        assertEquals("org-1", receipt.organizationId());
        assertEquals(DmarcReportReceipt.IngestStatus.RECEIVED, receipt.status());
    }

    @Test
    void parsesRuaXmlReport() throws Exception {
        byte[] bytes = SAMPLE_RUA_XML.getBytes(StandardCharsets.UTF_8);
        ParsedDmarcReport report = DmarcAggregateParser.parse(bytes);

        assertEquals("1234567890123456789", report.reportId());
        assertEquals("google.com", report.orgName());
        assertEquals(1, report.records().size());

        ParsedDmarcRecord rec = report.records().get(0);
        assertEquals("192.0.2.1", rec.sourceIp());
        assertEquals(150, rec.count());
        assertTrue(rec.isDkimAligned());
        assertTrue(rec.isSpfAligned());
    }

    @Test
    void aggregatesAndTriggersInProcessIdempotently() throws Exception {
        byte[] bytes = SAMPLE_RUA_XML.getBytes(StandardCharsets.UTF_8);
        DmarcReportReceipt receipt = DmarcReportReceipt.receive("dom-1", "org-1", "report.xml", bytes, Instant.now());
        ParsedDmarcReport report = DmarcAggregateParser.parse(bytes);

        DmarcAggregateEvidence evidence = DmarcAggregateEvidence.aggregate(receipt, report);
        assertNotNull(evidence.id());
        assertEquals(receipt.id(), evidence.receiptId());
        assertEquals(150, evidence.totalMessages());
        assertEquals(150, evidence.fullyCompliantMessages());
        assertEquals(150, evidence.volumeBySourceIp().get("192.0.2.1"));

        AtomicBoolean triggered = new AtomicBoolean(false);
        DmarcIngestTrigger trigger = new DmarcIngestTrigger((domainId, orgId, evId) -> {
            triggered.set(true);
            assertEquals("dom-1", domainId);
        });

        boolean firstTrigger = trigger.evaluateAndTrigger(evidence);
        assertTrue(firstTrigger);
        assertTrue(triggered.get());

        // Duplicate reportId under same domain must NOT re-trigger (idempotency)
        boolean secondTrigger = trigger.evaluateAndTrigger(evidence);
        assertFalse(secondTrigger);
    }

    @Test
    void formatsSlackWebhookPayload() {
        Alert alert = Alert.generate("org-1", "inc-1", AlertSeverity.CRITICAL, "DMARC Failure", "Spoofed traffic detected", "Enforce p=reject", Instant.now());
        SlackNotificationPayload slack = SlackNotificationPayload.fromAlert("https://hooks.slack.com/services/xxx", "#sec-ops", alert);

        assertNotNull(slack.id());
        assertTrue(slack.jsonBody().contains("Spoofed traffic detected"));
    }

    @Test
    void generatesDnsProviderFixGuides() {
        DnsProviderGuide cfGuide = DnsProviderGuide.generate(DnsProviderGuide.DnsProvider.CLOUDFLARE, "TXT", "@", "v=spf1 ~all");
        assertNotNull(cfGuide.id());
        assertEquals(DnsProviderGuide.DnsProvider.CLOUDFLARE, cfGuide.provider());
        assertTrue(cfGuide.stepByStepInstructions().get(0).contains("Cloudflare"));
    }
}
