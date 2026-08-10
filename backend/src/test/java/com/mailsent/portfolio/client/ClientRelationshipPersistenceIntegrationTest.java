package com.mailsent.portfolio.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.test.context.ActiveProfiles;

import com.mailsent.portfolio.client.application.ClientRelationshipService;
import com.mailsent.portfolio.client.domain.ClientRelationship;
import com.mailsent.portfolio.organization.application.OrganizationService;
import com.mailsent.portfolio.organization.domain.Organization;

/**
 * Real-PostgreSQL integration tests for the CLIENT-001 persistence path and invariants.
 *
 * <p>Runs against a live PostgreSQL on port 5433 (see {@code application-it.yml}). Flyway
 * applies V1–V7 at context startup and {@code ddl-auto: validate} verifies the schema,
 * exercising:
 * <pre>
 * OrganizationService/ClientRelationshipService → JPA adapters → PostgreSQL → reload
 * </pre>
 * Covers the relationship round-trip, the ACTIVE → ENDED lifecycle, the organization foreign
 * keys, and that the relationship is organization-to-organization only (no membership row /
 * table, no User.homeOrganization mutation). Gated on {@code mailsent.it.postgres}.</p>
 */
@SpringBootTest
@ActiveProfiles("it")
@EnabledIfSystemProperty(named = "mailsent.it.postgres", matches = "true")
class ClientRelationshipPersistenceIntegrationTest {

    private static final String PREFIX = "it-" + java.util.UUID.randomUUID() + "-";

    @Autowired private OrganizationService organizationService;
    @Autowired private ClientRelationshipService clientRelationships;
    @Autowired private DataSource dataSource;

    private String createdOrganization(String label) {
        return organizationService.create(PREFIX + label).id();
    }

    @Test
    void formPersistReloadThroughRealPostgres() {
        String agency = createdOrganization("agency");
        String client = createdOrganization("client");

        ClientRelationship formed = clientRelationships.form(agency, client);
        ClientRelationship reloaded = clientRelationships.findById(formed.id()).orElseThrow();

        assertEquals(formed.id(), reloaded.id(), "stable identity unchanged");
        assertEquals(agency, reloaded.agencyOrganizationId());
        assertEquals(client, reloaded.clientOrganizationId());
        assertEquals(ClientRelationship.Status.ACTIVE, reloaded.status());
        assertNotNull(reloaded.formedAt(), "formation time persisted");
        assertTrue(reloaded.endedAt() == null, "endedAt null while ACTIVE (lifecycle invariant)");
    }

    @Test
    void endPersistsEndedWithEndedAtThroughRealPostgres() {
        String agency = createdOrganization("agency");
        String client = createdOrganization("client");
        ClientRelationship formed = clientRelationships.form(agency, client);

        clientRelationships.end(formed.id());
        ClientRelationship reloaded = clientRelationships.findById(formed.id()).orElseThrow();

        assertEquals(ClientRelationship.Status.ENDED, reloaded.status());
        assertNotNull(reloaded.endedAt(), "endedAt persisted on end");
        // Irreversible: ending again leaves the original endedAt unchanged.
        clientRelationships.end(formed.id());
        assertEquals(reloaded.endedAt(), clientRelationships.findById(formed.id()).orElseThrow().endedAt(),
            "a second end does not change endedAt (irreversible)");
    }

    @Test
    void relationshipReferencesOrganizationsOnly() throws Exception {
        try (Connection con = DataSourceUtils.getConnection(dataSource)) {
            Set<String> columns = new HashSet<>();
            try (ResultSet rs = con.getMetaData().getColumns(null, null, "client_relationships", null)) {
                while (rs.next()) {
                    columns.add(rs.getString("COLUMN_NAME"));
                }
            }
            assertEquals(Set.of("id", "agency_organization_id", "client_organization_id", "formed_at",
                "status", "ended_at"), columns,
                "client_relationships references organizations only — no user/membership column");
        }
    }

    @Test
    void organizationForeignKeysPresentAndNoMembershipTable() throws Exception {
        try (Connection con = DataSourceUtils.getConnection(dataSource)) {
            DatabaseMetaData meta = con.getMetaData();
            Set<String> fks = new HashSet<>();
            try (ResultSet fk = meta.getImportedKeys(null, null, "client_relationships")) {
                while (fk.next()) {
                    fks.add(fk.getString("FK_NAME") + ":" + fk.getString("FKCOLUMN_NAME"));
                }
            }
            assertTrue(fks.contains("fk_client_relationships_agency:agency_organization_id"),
                "agency FK -> organizations");
            assertTrue(fks.contains("fk_client_relationships_client:client_organization_id"),
                "client FK -> organizations");

            try (ResultSet rs = con.createStatement().executeQuery(
                "SELECT table_name FROM information_schema.tables WHERE table_name = 'memberships'")) {
                assertTrue(!rs.next(), "no memberships table exists (no membership architecture)");
            }
        }
    }

    @Test
    void lifecycleNeverAltersEitherOrganization() {
        String agency = createdOrganization("agency");
        String client = createdOrganization("client");
        ClientRelationship relationship = clientRelationships.form(agency, client);
        clientRelationships.end(relationship.id());

        Organization reloadedAgency = organizationService.findById(agency).orElseThrow();
        Organization reloadedClient = organizationService.findById(client).orElseThrow();
        assertEquals(Organization.Status.ACTIVE, reloadedAgency.status(),
            "agency identity/lifecycle unchanged by the relationship");
        assertEquals(Organization.Status.ACTIVE, reloadedClient.status(),
            "client identity/lifecycle unchanged by the relationship");
    }
}