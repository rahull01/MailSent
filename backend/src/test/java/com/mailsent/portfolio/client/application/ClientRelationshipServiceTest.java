package com.mailsent.portfolio.client.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mailsent.portfolio.client.domain.ClientRelationship;
import com.mailsent.portfolio.client.domain.OrganizationNotFoundException;
import com.mailsent.portfolio.client.infrastructure.InMemoryClientRelationshipRepository;
import com.mailsent.portfolio.organization.domain.Organization;
import com.mailsent.portfolio.organization.infrastructure.InMemoryOrganizationRepository;

/**
 * Unit tests for {@link ClientRelationshipService} (CLIENT-001) against the hermetic
 * in-memory repositories: forming a relationship between existing organizations, reading it,
 * and the irreversible ACTIVE → ENDED lifecycle — with neither organization altered.
 */
class ClientRelationshipServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-08-08T10:00:00Z"), ZoneOffset.UTC);

    private InMemoryOrganizationRepository organizations;
    private InMemoryClientRelationshipRepository relationships;
    private ClientRelationshipService service;

    @BeforeEach
    void setUp() {
        organizations = new InMemoryOrganizationRepository();
        relationships = new InMemoryClientRelationshipRepository();
        service = new ClientRelationshipService(relationships, organizations, CLOCK);
    }

    private String createdOrganization(String name) {
        return organizations.save(Organization.create(name, CLOCK.instant())).id();
    }

    @Test
    void formCreatesActiveRelationshipForExistingOrganizations() {
        String agency = createdOrganization("Agency");
        String client = createdOrganization("Client");

        ClientRelationship relationship = service.form(agency, client);

        assertEquals(ClientRelationship.Status.ACTIVE, relationship.status());
        assertEquals(agency, relationship.agencyOrganizationId());
        assertEquals(client, relationship.clientOrganizationId());
        assertEquals(CLOCK.instant(), relationship.formedAt());
        assertTrue(relationship.endedAt() == null, "endedAt is null while ACTIVE");
        assertTrue(relationships.findById(relationship.id()).isPresent(), "persisted");
    }

    @Test
    void formRejectsUnknownAgencyOrganization() {
        String client = createdOrganization("Client");
        assertThrows(OrganizationNotFoundException.class, () -> service.form("no-such-agency", client));
    }

    @Test
    void formRejectsUnknownClientOrganization() {
        String agency = createdOrganization("Agency");
        assertThrows(OrganizationNotFoundException.class, () -> service.form(agency, "no-such-client"));
    }

    @Test
    void formRejectsSameOrganization() {
        String org = createdOrganization("Solo");
        assertThrows(IllegalArgumentException.class, () -> service.form(org, org));
    }

    @Test
    void findByIdReturnsExistingAndEmptyForUnknown() {
        String agency = createdOrganization("Agency");
        String client = createdOrganization("Client");
        ClientRelationship relationship = service.form(agency, client);

        assertEquals(relationship.id(), service.findById(relationship.id()).orElseThrow().id());
        assertTrue(service.findById("no-such-id").isEmpty());
    }

    @Test
    void endTransitionsActiveToEndedAndIsNotReversible() {
        String agency = createdOrganization("Agency");
        String client = createdOrganization("Client");
        ClientRelationship relationship = service.form(agency, client);

        ClientRelationship ended = service.end(relationship.id()).orElseThrow();
        assertEquals(ClientRelationship.Status.ENDED, ended.status());
        assertEquals(CLOCK.instant().plusSeconds(0), ended.endedAt());

        ClientRelationship reloaded = service.findById(relationship.id()).orElseThrow();
        assertEquals(ClientRelationship.Status.ENDED, reloaded.status());
    }

    @Test
    void endOnUnknownIdReturnsEmpty() {
        assertFalse(service.end("no-such-id").isPresent());
    }

    @Test
    void endOnAlreadyEndedIsANoOp() {
        String agency = createdOrganization("Agency");
        String client = createdOrganization("Client");
        ClientRelationship relationship = service.form(agency, client);
        ClientRelationship first = service.end(relationship.id()).orElseThrow();

        ClientRelationship second = service.end(relationship.id()).orElseThrow();
        assertEquals(first.endedAt(), second.endedAt(), "endedAt is unchanged by a second end");
    }

    @Test
    void formingAndEndingNeverAltersEitherOrganization() {
        String agency = createdOrganization("Agency");
        String client = createdOrganization("Client");
        ClientRelationship relationship = service.form(agency, client);
        service.end(relationship.id());

        // Neither organization's identity or lifecycle is altered (INV-13/09).
        assertTrue(organizations.findById(agency).isPresent());
        assertTrue(organizations.findById(client).isPresent());
        assertEquals(Organization.Status.ACTIVE, organizations.findById(agency).orElseThrow().status());
        assertEquals(Organization.Status.ACTIVE, organizations.findById(client).orElseThrow().status());
    }
}