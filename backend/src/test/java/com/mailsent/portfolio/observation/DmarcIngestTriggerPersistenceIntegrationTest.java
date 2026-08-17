package com.mailsent.portfolio.observation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.UUID;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.test.context.ActiveProfiles;

import com.mailsent.portfolio.domain.application.DomainService;
import com.mailsent.portfolio.domain.domain.Domain;
import com.mailsent.diagnosis.application.AlignmentDerivationService;
import com.mailsent.diagnosis.domain.Alignment;
import com.mailsent.diagnosis.infrastructure.Diag001AlignmentDerivationAdapter;
import com.mailsent.portfolio.observation.application.Diag001AlignmentDerivationPort;
import com.mailsent.portfolio.observation.application.DmarcAggregateEvidenceService;
import com.mailsent.portfolio.observation.application.DmarcIngestTriggerService;
import com.mailsent.portfolio.observation.application.DmarcReportReceiveService;
import com.mailsent.portfolio.observation.domain.DmarcAggregateEvidence;
import com.mailsent.portfolio.observation.domain.DmarcReportReceipt;
import com.mailsent.portfolio.organization.application.OrganizationService;

/**
 * Real-PostgreSQL integration tests for INT-006: parse → store → DIAG-001
 * port (real derivation). No Alignment table. Gated on {@code mailsent.it.postgres}.
 */
@SpringBootTest
@ActiveProfiles("it")
@EnabledIfSystemProperty(named = "mailsent.it.postgres", matches = "true")
class DmarcIngestTriggerPersistenceIntegrationTest {

    private static final String XML = """
        <?xml version="1.0" encoding="UTF-8"?>
        <feedback>
          <report_metadata>
            <org_name>google.com</org_name>
            <report_id>it-trigger-1</report_id>
            <date_range><begin>1609459200</begin><end>1609545599</end></date_range>
          </report_metadata>
          <policy_published><domain>other.example</domain><p>none</p></policy_published>
          <record>
            <row>
              <source_ip>192.0.2.1</source_ip>
              <count>3</count>
              <policy_evaluated><disposition>none</disposition><dkim>pass</dkim><spf>fail</spf></policy_evaluated>
            </row>
            <identifiers><header_from>example.com</header_from></identifiers>
          </record>
        </feedback>
        """;

    @Autowired private OrganizationService organizationService;
    @Autowired private DomainService domainService;
    @Autowired private DmarcReportReceiveService receiveService;
    @Autowired private DmarcIngestTriggerService triggerService;
    @Autowired private DmarcAggregateEvidenceService aggregateService;
    @Autowired private Diag001AlignmentDerivationPort diag001;
    @Autowired private AlignmentDerivationService alignmentDerivation;
    @Autowired private DataSource dataSource;

    @Test
    void processStoresNewEvidenceAndWiresRealDiag001Port() {
        String orgId = organizationService.create("DMARC-Trigger " + UUID.randomUUID()).id();
        Domain domain = domainService.register(orgId, "it-trigger-" + UUID.randomUUID() + ".com");
        DmarcReportReceipt receipt = receiveService.receive(domain.id(), XML.getBytes(StandardCharsets.UTF_8))
            .orElseThrow();

        triggerService.process(receipt.id());

        DmarcAggregateEvidence stored = aggregateService.findByDomainId(domain.id()).get(0);
        assertEquals(1, aggregateService.findByDomainId(domain.id()).size());
        assertEquals(receipt.id(), stored.receiptId());
        assertInstanceOf(Diag001AlignmentDerivationAdapter.class, diag001);
        assertEquals(1, alignmentDerivation.lastDerived().size());
        Alignment alignment = alignmentDerivation.lastDerived().get(0);
        assertEquals(receipt.id(), alignment.receiptId());
        assertEquals(stored.id(), alignment.evidenceId());
        assertEquals(orgId, alignment.organizationId());
        assertEquals(domain.id(), alignment.domainId());
        assertEquals(domain.name(), alignment.domainName());
        assertEquals("fail", alignment.spfAligned());
        assertEquals("pass", alignment.dkimAligned());
        assertEquals("example.com", alignment.headerFrom());

        triggerService.process(receipt.id());
        assertEquals(1, aggregateService.findByDomainId(domain.id()).size());
        assertEquals(1, alignmentDerivation.lastDerived().size());
    }

    @Test
    void schemaHasNoAlignmentTable() throws Exception {
        try (Connection con = DataSourceUtils.getConnection(dataSource)) {
            DatabaseMetaData meta = con.getMetaData();
            boolean alignmentTable = false;
            try (ResultSet tables = meta.getTables(null, null, "%", new String[] {"TABLE"})) {
                while (tables.next()) {
                    String name = tables.getString("TABLE_NAME").toLowerCase();
                    if (name.contains("alignment")) {
                        alignmentTable = true;
                    }
                }
            }
            assertFalse(alignmentTable);
        }
    }
}
