package com.mailsent.portfolio.observation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.test.context.ActiveProfiles;

import com.mailsent.portfolio.domain.application.DomainService;
import com.mailsent.portfolio.domain.domain.Domain;
import com.mailsent.portfolio.observation.application.DmarcAggregateEvidenceService;
import com.mailsent.portfolio.observation.application.DmarcReportParseService;
import com.mailsent.portfolio.observation.application.DmarcReportReceiveService;
import com.mailsent.portfolio.observation.application.ObservationEvidenceRepository;
import com.mailsent.portfolio.observation.domain.DmarcAggregateEvidence;
import com.mailsent.portfolio.observation.domain.DmarcReportReceipt;
import com.mailsent.portfolio.observation.domain.ParsedDmarcReport;
import com.mailsent.portfolio.organization.application.OrganizationService;

/**
 * Real-PostgreSQL integration tests for the INT-005 DMARC Aggregate Evidence
 * persistence path.
 *
 * <p>Runs against a live PostgreSQL on port 5433 (see {@code application-it.yml}).
 * Flyway applies V1–V17 at context startup, and {@code ddl-auto: validate}
 * verifies the schema against the JPA entity. Gated on
 * {@code mailsent.it.postgres}.</p>
 */
@SpringBootTest
@ActiveProfiles("it")
@EnabledIfSystemProperty(named = "mailsent.it.postgres", matches = "true")
class DmarcAggregateEvidencePersistenceIntegrationTest {

    private static final String XML = """
        <?xml version="1.0" encoding="UTF-8"?>
        <feedback>
          <report_metadata>
            <org_name>google.com</org_name>
            <report_id>it-report-1</report_id>
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
            <auth_results>
              <dkim><domain>sel.example.com</domain><result>pass</result></dkim>
              <spf><domain>example.com</domain><result>fail</result></spf>
            </auth_results>
          </record>
          <record>
            <row>
              <source_ip>192.0.2.2</source_ip>
              <count>1</count>
              <policy_evaluated><disposition>none</disposition><dkim>pass</dkim><spf>fail</spf></policy_evaluated>
            </row>
            <identifiers><header_from>example.com</header_from></identifiers>
            <auth_results>
              <dkim><domain>sel.example.com</domain><result>pass</result></dkim>
              <spf><domain>example.com</domain><result>fail</result></spf>
            </auth_results>
          </record>
        </feedback>
        """;

    @Autowired private OrganizationService organizationService;
    @Autowired private DomainService domainService;
    @Autowired private DmarcReportReceiveService receiveService;
    @Autowired private DmarcReportParseService parseService;
    @Autowired private DmarcAggregateEvidenceService aggregateService;
    @Autowired private ObservationEvidenceRepository observationEvidence;
    @Autowired private DataSource dataSource;

    private Domain registeredDomain() {
        String orgId = organizationService.create("DMARC-Agg " + UUID.randomUUID()).id();
        return domainService.register(orgId, "it-" + UUID.randomUUID() + ".com");
    }

    @Test
    void parseThenStoreRoundTripsThroughRealPostgres() {
        Domain domain = registeredDomain();
        DmarcReportReceipt receipt = receiveService.receive(domain.id(), XML.getBytes(StandardCharsets.UTF_8))
            .orElseThrow();
        ParsedDmarcReport parsed = parseService.parse(receipt.id()).parsed().orElseThrow();

        List<DmarcAggregateEvidence> stored = aggregateService.store(parsed);
        assertEquals(2, stored.size());

        DmarcAggregateEvidence reloaded = aggregateService.findById(stored.get(0).id()).orElseThrow();
        assertEquals(domain.organizationId(), reloaded.organizationId());
        assertEquals(domain.id(), reloaded.domainId());
        assertEquals(domain.name(), reloaded.domainName());
        assertEquals(receipt.id(), reloaded.receiptId());
        assertEquals("it-report-1", reloaded.reportId());
        assertEquals(LocalDate.of(2021, 1, 1), reloaded.utcDay());
        assertEquals(2, aggregateService.findByDomainId(domain.id()).size());
        assertTrue(observationEvidence.findByDomainId(domain.id()).isEmpty());

        List<DmarcAggregateEvidence> duplicate = aggregateService.store(parsed);
        assertEquals(stored.stream().map(DmarcAggregateEvidence::id).collect(Collectors.toSet()),
            duplicate.stream().map(DmarcAggregateEvidence::id).collect(Collectors.toSet()));
        assertEquals(2, aggregateService.findByDomainId(domain.id()).size());
    }

    @Test
    void schemaEnforcesKeysIndexesAndAllowsMultiIpReports() throws Exception {
        try (Connection con = DataSourceUtils.getConnection(dataSource)) {
            DatabaseMetaData meta = con.getMetaData();

            boolean pkFound = false;
            try (ResultSet pk = meta.getPrimaryKeys(null, null, "dmarc_aggregate_evidence")) {
                while (pk.next()) {
                    if ("pk_dmarc_aggregate_evidence".equalsIgnoreCase(pk.getString("PK_NAME"))
                            && "id".equalsIgnoreCase(pk.getString("COLUMN_NAME"))) {
                        pkFound = true;
                    }
                }
            }
            assertTrue(pkFound, "PostgreSQL enforces pk_dmarc_aggregate_evidence PRIMARY KEY (id)");

            boolean orgFk = false;
            boolean domainFk = false;
            boolean receiptFk = false;
            try (ResultSet fk = meta.getImportedKeys(null, null, "dmarc_aggregate_evidence")) {
                while (fk.next()) {
                    String name = fk.getString("FK_NAME");
                    String column = fk.getString("FKCOLUMN_NAME");
                    String pkTable = fk.getString("PKTABLE_NAME");
                    if ("fk_dmarc_aggregate_evidence_organization".equalsIgnoreCase(name)
                            && "organization_id".equalsIgnoreCase(column)
                            && "organizations".equalsIgnoreCase(pkTable)) {
                        orgFk = true;
                    }
                    if ("fk_dmarc_aggregate_evidence_domain".equalsIgnoreCase(name)
                            && "domain_id".equalsIgnoreCase(column)
                            && "domains".equalsIgnoreCase(pkTable)) {
                        domainFk = true;
                    }
                    if ("fk_dmarc_aggregate_evidence_receipt".equalsIgnoreCase(name)
                            && "receipt_id".equalsIgnoreCase(column)
                            && "dmarc_report_receipt".equalsIgnoreCase(pkTable)) {
                        receiptFk = true;
                    }
                }
            }
            assertTrue(orgFk);
            assertTrue(domainFk);
            assertTrue(receiptFk);

            boolean orgIdx = false;
            boolean domainIdx = false;
            boolean uniqueOnDomainId = false;
            boolean uniqueOnDomainReportOnly = false;
            try (ResultSet ri = meta.getIndexInfo(null, null, "dmarc_aggregate_evidence", false, false)) {
                while (ri.next()) {
                    String indexName = ri.getString("INDEX_NAME");
                    String column = ri.getString("COLUMN_NAME");
                    boolean unique = !ri.getBoolean("NON_UNIQUE");
                    if ("idx_dmarc_aggregate_evidence_organization_id".equalsIgnoreCase(indexName)) {
                        orgIdx = true;
                    }
                    if ("idx_dmarc_aggregate_evidence_domain_id".equalsIgnoreCase(indexName)) {
                        domainIdx = true;
                    }
                    if (unique && "domain_id".equalsIgnoreCase(column)
                            && !"pk_dmarc_aggregate_evidence".equalsIgnoreCase(indexName)) {
                        uniqueOnDomainId = true;
                    }
                    if (unique && "idx_dmarc_aggregate_evidence_domain_report".equalsIgnoreCase(indexName)) {
                        uniqueOnDomainReportOnly = true;
                    }
                }
            }
            assertTrue(orgIdx);
            assertTrue(domainIdx);
            assertFalse(uniqueOnDomainId, "domain_id must not be UNIQUE (append-only + multi-IP)");
            assertFalse(uniqueOnDomainReportOnly, "domain_id+report_id must not be UNIQUE (multi-IP reports)");

            Set<String> columns = new HashSet<>();
            try (ResultSet cols = meta.getColumns(null, null, "dmarc_aggregate_evidence", null)) {
                while (cols.next()) {
                    columns.add(cols.getString("COLUMN_NAME").toLowerCase());
                }
            }
            assertEquals(Set.of(
                "id", "organization_id", "domain_id", "domain_name", "receipt_id", "report_id",
                "source_ip", "utc_day", "message_count", "spf_result", "dkim_result", "spf_aligned",
                "dkim_aligned", "envelope_from", "header_from", "dkim_domains", "disposition"),
                columns);

            boolean occupancyPk = false;
            try (ResultSet pk = meta.getPrimaryKeys(null, null, "dmarc_aggregate_report_occupancy")) {
                Set<String> pkCols = new HashSet<>();
                while (pk.next()) {
                    pkCols.add(pk.getString("COLUMN_NAME").toLowerCase());
                    occupancyPk = true;
                }
                assertEquals(Set.of("domain_id", "report_id"), pkCols);
            }
            assertTrue(occupancyPk, "occupancy PRIMARY KEY (domain_id, report_id)");
        }
    }

    @Test
    void laterGroupConflictPersistsZeroRowsThenRetrySucceeds() {
        Domain domain = registeredDomain();
        String reportId = "it-conflict-" + UUID.randomUUID();
        String conflictXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <feedback>
              <report_metadata>
                <org_name>google.com</org_name>
                <report_id>%s</report_id>
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
                <auth_results>
                  <dkim><domain>sel.example.com</domain><result>pass</result></dkim>
                  <spf><domain>example.com</domain><result>fail</result></spf>
                </auth_results>
              </record>
              <record>
                <row>
                  <source_ip>192.0.2.2</source_ip>
                  <count>1</count>
                  <policy_evaluated><disposition>none</disposition><dkim>pass</dkim><spf>fail</spf></policy_evaluated>
                </row>
                <identifiers><header_from>example.com</header_from></identifiers>
                <auth_results>
                  <dkim><domain>sel.example.com</domain><result>pass</result></dkim>
                  <spf><domain>example.com</domain><result>fail</result></spf>
                </auth_results>
              </record>
              <record>
                <row>
                  <source_ip>192.0.2.2</source_ip>
                  <count>2</count>
                  <policy_evaluated><disposition>quarantine</disposition><dkim>fail</dkim><spf>pass</spf></policy_evaluated>
                </row>
                <identifiers><header_from>other.example</header_from></identifiers>
              </record>
            </feedback>
            """.formatted(reportId);
        DmarcReportReceipt bad = receiveService.receive(domain.id(), conflictXml.getBytes(StandardCharsets.UTF_8))
            .orElseThrow();
        ParsedDmarcReport parsed = parseService.parse(bad.id()).parsed().orElseThrow();
        assertThrows(IllegalStateException.class, () -> aggregateService.store(parsed));
        assertTrue(aggregateService.findByDomainId(domain.id()).isEmpty());
        assertEquals(0, occupancyCount(domain.id(), reportId));

        String goodXml = XML.replace("it-report-1", reportId);
        DmarcReportReceipt good = receiveService.receive(domain.id(), goodXml.getBytes(StandardCharsets.UTF_8))
            .orElseThrow();
        List<DmarcAggregateEvidence> stored = aggregateService.store(parseService.parse(good.id()).parsed().orElseThrow());
        assertEquals(2, stored.size());
        assertEquals(1, occupancyCount(domain.id(), reportId));
    }

    @Test
    void concurrentStoresForSameReportPersistOneSnapshotSet() throws Exception {
        Domain domain = registeredDomain();
        String reportId = "it-concurrent-" + UUID.randomUUID();
        String xml = XML.replace("it-report-1", reportId);
        DmarcReportReceipt receiptA = receiveService.receive(domain.id(), xml.getBytes(StandardCharsets.UTF_8))
            .orElseThrow();
        DmarcReportReceipt receiptB = receiveService.receive(domain.id(), xml.getBytes(StandardCharsets.UTF_8))
            .orElseThrow();
        ParsedDmarcReport parsedA = parseService.parse(receiptA.id()).parsed().orElseThrow();
        ParsedDmarcReport parsedB = parseService.parse(receiptB.id()).parsed().orElseThrow();
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch go = new CountDownLatch(1);
        try {
            Future<List<DmarcAggregateEvidence>> a = pool.submit(() -> {
                ready.countDown();
                go.await();
                return aggregateService.store(parsedA);
            });
            Future<List<DmarcAggregateEvidence>> b = pool.submit(() -> {
                ready.countDown();
                go.await();
                return aggregateService.store(parsedB);
            });
            assertTrue(ready.await(5, TimeUnit.SECONDS));
            go.countDown();
            List<DmarcAggregateEvidence> left = a.get(15, TimeUnit.SECONDS);
            List<DmarcAggregateEvidence> right = b.get(15, TimeUnit.SECONDS);
            assertEquals(2, left.size());
            assertEquals(2, right.size());
            assertEquals(left.stream().map(DmarcAggregateEvidence::id).collect(Collectors.toSet()),
                right.stream().map(DmarcAggregateEvidence::id).collect(Collectors.toSet()));
            assertEquals(2, aggregateService.findByDomainId(domain.id()).size());
        } finally {
            pool.shutdownNow();
        }
    }

    private int occupancyCount(String domainId, String reportId) {
        try (Connection con = DataSourceUtils.getConnection(dataSource);
             var ps = con.prepareStatement(
                 "SELECT COUNT(*) FROM dmarc_aggregate_report_occupancy WHERE domain_id = ? AND report_id = ?")) {
            ps.setString(1, domainId);
            ps.setString(2, reportId);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                return rs.getInt(1);
            }
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
