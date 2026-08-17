package com.mailsent.portfolio.observation;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
import com.mailsent.portfolio.observation.application.DmarcReportReceiveService;
import com.mailsent.portfolio.observation.application.ObservationEvidenceRepository;
import com.mailsent.portfolio.observation.domain.DmarcReportReceipt;
import com.mailsent.portfolio.organization.application.OrganizationService;

/**
 * Real-PostgreSQL integration tests for the INT-003 DMARC Report Receipt
 * persistence path.
 *
 * <p>Runs against a live PostgreSQL on port 5433 (see {@code application-it.yml}).
 * Flyway applies V1–V14 at context startup, and {@code ddl-auto: validate}
 * verifies the schema against the JPA entity. Gated on
 * {@code mailsent.it.postgres}.</p>
 */
@SpringBootTest
@ActiveProfiles("it")
@EnabledIfSystemProperty(named = "mailsent.it.postgres", matches = "true")
class DmarcReportReceiptPersistenceIntegrationTest {

    private static final byte[] PAYLOAD = "opaque-aggregate-bytes".getBytes();

    @Autowired private OrganizationService organizationService;
    @Autowired private DomainService domainService;
    @Autowired private DmarcReportReceiveService receiveService;
    @Autowired private ObservationEvidenceRepository evidence;
    @Autowired private DataSource dataSource;

    private Domain registeredDomain() {
        String orgId = organizationService.create("DMARC " + UUID.randomUUID()).id();
        return domainService.register(orgId, "it-" + UUID.randomUUID() + ".com");
    }

    @Test
    void receivePersistReloadRoundTripsThroughRealPostgres() {
        Domain domain = registeredDomain();
        DmarcReportReceipt created = receiveService.receive(domain.id(), PAYLOAD).orElseThrow();

        DmarcReportReceipt reloaded = receiveService.findById(created.id()).orElseThrow();
        assertEquals(created.id(), reloaded.id());
        assertEquals(domain.organizationId(), reloaded.organizationId());
        assertEquals(domain.id(), reloaded.domainId());
        assertEquals(domain.name(), reloaded.domainName());
        assertNull(reloaded.reportId());
        assertArrayEquals(PAYLOAD, reloaded.payload());
        assertEquals(created.receivedAt(), reloaded.receivedAt());
        assertEquals(1, receiveService.findByDomainId(domain.id()).size());
        assertTrue(evidence.findByDomainId(domain.id()).isEmpty());
    }

    @Test
    void unknownUuidPersistsNothing() {
        assertTrue(receiveService.receive(UUID.randomUUID().toString(), PAYLOAD).isEmpty());
    }

    @Test
    void secondReceiveCreatesASecondRowLeavingTheFirstUnchanged() {
        Domain domain = registeredDomain();
        DmarcReportReceipt first = receiveService.receive(domain.id(), PAYLOAD).orElseThrow();
        DmarcReportReceipt second = receiveService.receive(domain.id(), "second".getBytes()).orElseThrow();

        assertEquals(2, receiveService.findByDomainId(domain.id()).size());
        DmarcReportReceipt reloadedFirst = receiveService.findById(first.id()).orElseThrow();
        assertArrayEquals(PAYLOAD, reloadedFirst.payload());
        assertEquals(first.receivedAt(), reloadedFirst.receivedAt());
        assertArrayEquals("second".getBytes(), second.payload());
    }

    @Test
    void differentOrganizationsRemainIsolated() {
        String orgA = organizationService.create("DmarcA " + UUID.randomUUID()).id();
        String orgB = organizationService.create("DmarcB " + UUID.randomUUID()).id();
        Domain domainA = domainService.register(orgA, "it-" + UUID.randomUUID() + "a.com");
        Domain domainB = domainService.register(orgB, "it-" + UUID.randomUUID() + "b.com");

        DmarcReportReceipt forA = receiveService.receive(domainA.id(), PAYLOAD).orElseThrow();
        DmarcReportReceipt forB = receiveService.receive(domainB.id(), PAYLOAD).orElseThrow();

        List<DmarcReportReceipt> ownedByA = receiveService.findByOrganizationId(orgA);
        List<DmarcReportReceipt> ownedByB = receiveService.findByOrganizationId(orgB);
        assertTrue(ownedByA.stream().anyMatch(e -> e.id().equals(forA.id())));
        assertTrue(ownedByB.stream().anyMatch(e -> e.id().equals(forB.id())));
        assertTrue(ownedByA.stream().allMatch(e -> e.organizationId().equals(orgA)));
        assertFalse(ownedByA.stream().anyMatch(e -> e.id().equals(forB.id())));
    }

    @Test
    void schemaEnforcesPrimaryKeyForeignKeysIndexesAndAllowsRepeatDomainReceipts() throws Exception {
        try (Connection con = DataSourceUtils.getConnection(dataSource)) {
            DatabaseMetaData meta = con.getMetaData();

            boolean pkFound = false;
            try (ResultSet pk = meta.getPrimaryKeys(null, null, "dmarc_report_receipt")) {
                while (pk.next()) {
                    if ("pk_dmarc_report_receipt".equalsIgnoreCase(pk.getString("PK_NAME"))
                            && "id".equalsIgnoreCase(pk.getString("COLUMN_NAME"))) {
                        pkFound = true;
                    }
                }
            }
            assertTrue(pkFound, "PostgreSQL enforces pk_dmarc_report_receipt PRIMARY KEY (id)");

            boolean orgFk = false;
            boolean domainFk = false;
            try (ResultSet fk = meta.getImportedKeys(null, null, "dmarc_report_receipt")) {
                while (fk.next()) {
                    String name = fk.getString("FK_NAME");
                    String column = fk.getString("FKCOLUMN_NAME");
                    String pkTable = fk.getString("PKTABLE_NAME");
                    if ("fk_dmarc_report_receipt_organization".equalsIgnoreCase(name)
                            && "organization_id".equalsIgnoreCase(column)
                            && "organizations".equalsIgnoreCase(pkTable)) {
                        orgFk = true;
                    }
                    if ("fk_dmarc_report_receipt_domain".equalsIgnoreCase(name)
                            && "domain_id".equalsIgnoreCase(column)
                            && "domains".equalsIgnoreCase(pkTable)) {
                        domainFk = true;
                    }
                }
            }
            assertTrue(orgFk, "PostgreSQL enforces fk_dmarc_report_receipt_organization -> organizations(id)");
            assertTrue(domainFk, "PostgreSQL enforces fk_dmarc_report_receipt_domain -> domains(id)");

            boolean orgIdx = false;
            boolean domainIdx = false;
            boolean uniqueOnDomainId = false;
            try (ResultSet ri = meta.getIndexInfo(null, null, "dmarc_report_receipt", false, false)) {
                while (ri.next()) {
                    String indexName = ri.getString("INDEX_NAME");
                    String column = ri.getString("COLUMN_NAME");
                    boolean unique = !ri.getBoolean("NON_UNIQUE");
                    if ("idx_dmarc_report_receipt_organization_id".equalsIgnoreCase(indexName)) {
                        orgIdx = true;
                    }
                    if ("idx_dmarc_report_receipt_domain_id".equalsIgnoreCase(indexName)) {
                        domainIdx = true;
                    }
                    if (unique && "domain_id".equalsIgnoreCase(column)
                            && !"pk_dmarc_report_receipt".equalsIgnoreCase(indexName)) {
                        uniqueOnDomainId = true;
                    }
                }
            }
            assertTrue(orgIdx, "idx_dmarc_report_receipt_organization_id is present");
            assertTrue(domainIdx, "idx_dmarc_report_receipt_domain_id is present");
            assertFalse(uniqueOnDomainId, "domain_id must not be UNIQUE (append-only receipts)");

            Set<String> columns = new HashSet<>();
            try (ResultSet cols = meta.getColumns(null, null, "dmarc_report_receipt", null)) {
                while (cols.next()) {
                    columns.add(cols.getString("COLUMN_NAME").toLowerCase());
                }
            }
            assertEquals(Set.of("id", "organization_id", "domain_id", "domain_name", "report_id", "payload",
                    "received_at"), columns, "table stores only receipt columns");
        }
    }
}
