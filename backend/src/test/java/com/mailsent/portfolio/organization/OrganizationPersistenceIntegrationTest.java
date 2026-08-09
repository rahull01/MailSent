package com.mailsent.portfolio.organization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.mailsent.portfolio.organization.application.OrganizationService;
import com.mailsent.portfolio.organization.domain.Organization;

/**
 * Real-PostgreSQL integration test for the ORG-001 persistence path.
 *
 * <p>Runs against a live PostgreSQL on port 5433 (see {@code application-it.yml}).
 * Flyway applies V1/V2/V3 at context startup, and {@code ddl-auto: validate} verifies
 * the schema against the JPA entities — so this test exercises the true path:</p>
 *
 * <pre>
 * OrganizationService → OrganizationRepository → JpaOrganizationRepositoryAdapter →
 * Spring Data JPA → PostgreSQL → reload
 * </pre>
 *
 * <p>This test is <strong>not</strong> part of the hermetic default suite (the
 * {@code test} profile excludes the DataSource). It is gated on the system property
 * {@code mailsent.it.postgres} so it runs only when a live database is explicitly
 * supplied (CI and ordinary {@code mvn verify} skip it; the CTO gates run it with
 * {@code -Dmailsent.it.postgres=true}).</p>
 */
@SpringBootTest
@ActiveProfiles("it")
@EnabledIfSystemProperty(named = "mailsent.it.postgres", matches = "true")
class OrganizationPersistenceIntegrationTest {

    @Autowired
    private OrganizationService service;

    @Test
    void createPersistReloadLifecycleThroughRealPostgres() {
        // 1. create
        Organization created = service.create("Acme Agency");

        // 5-6. reload by stable id and verify persisted state.
        // PostgreSQL timestamptz stores microsecond precision and rounds, so the exact
        // Instant is not preserved bit-for-bit. The documented invariant is that a
        // timestamp persists for the formation instant — assert it is present and within
        // a small tolerance, not nanosecond-identical.
        Organization reloaded = service.findById(created.id()).orElseThrow();
        assertEquals(created.id(), reloaded.id(), "stable identity unchanged");
        assertEquals("Acme Agency", reloaded.name());
        assertTrue(reloaded.createdAt() != null, "creation timestamp persisted");
        // PostgreSQL timestamptz stores microseconds; an Instant reloaded from it is
        // rounded to the nearest microsecond (<= 1000 ns drift). Assert at that
        // precision rather than the loose 1-second bound.
        long driftNanos = Math.abs(java.time.Duration.between(created.createdAt(), reloaded.createdAt()).toNanos());
        assertTrue(driftNanos <= 1000L, "creation timestamp persisted at microsecond precision, actual drift(ns)=" + driftNanos);
        assertEquals(Organization.Status.ACTIVE, reloaded.status());

        // 7-9. close and persist
        service.close(created.id());
        Organization closed = service.findById(created.id()).orElseThrow();
        assertEquals(Organization.Status.CLOSED, closed.status());
        assertTrue(closed.closedAt() != null, "closure timestamp persisted");
        assertEquals(created.id(), closed.id(), "identity unchanged after close");

        // 10-11. reload after close preserves stable identity and name
        assertEquals(created.id(), closed.id());
        assertEquals("Acme Agency", closed.name());
    }

}