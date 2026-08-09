package com.mailsent.portfolio.organization.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mailsent.portfolio.organization.domain.Organization;
import com.mailsent.portfolio.organization.infrastructure.InMemoryOrganizationRepository;

/**
 * Unit tests for {@link OrganizationService} — the ORG-001 application boundary
 * (create/read/list/close) against the hermetic in-memory repository.
 */
class OrganizationServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-08-08T10:00:00Z"), ZoneOffset.UTC);

    private InMemoryOrganizationRepository repository;
    private OrganizationService service;

    @BeforeEach
    void setUp() {
        repository = new InMemoryOrganizationRepository();
        service = new OrganizationService(repository, CLOCK);
    }

    @Test
    void createPersistsAnActiveOrganization() {
        Organization org = service.create("Acme Agency");
        assertEquals(Organization.Status.ACTIVE, org.status());
        assertTrue(repository.findById(org.id()).isPresent());
    }

    @Test
    void findByIdReturnsExistingOrganization() {
        Organization org = service.create("Acme");
        assertEquals(org.id(), service.findById(org.id()).orElseThrow().id());
        assertFalse(service.findById("nope").isPresent());
    }

    @Test
    void findAllReturnsAllPersistedOrganizations() {
        service.create("Acme");
        service.create("Beta");
        List<Organization> all = service.findAll();
        assertEquals(2, all.size());
    }

    @Test
    void closeEndsOrganizationAndPersistsLifecycleState() {
        Organization org = service.create("Acme");
        Organization closed = service.close(org.id()).orElseThrow();
        assertEquals(Organization.Status.CLOSED, closed.status());

        Organization reloaded = service.findById(org.id()).orElseThrow();
        assertEquals(Organization.Status.CLOSED, reloaded.status());
        assertTrue(reloaded.id().equals(org.id()));
    }

    @Test
    void closeOnUnknownIdReturnsEmpty() {
        assertFalse(service.close("unknown").isPresent());
    }
}