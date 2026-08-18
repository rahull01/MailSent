package com.mailsent.portfolio.client.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mailsent.identity.authentication.domain.Email;
import com.mailsent.identity.authentication.domain.Role;
import com.mailsent.identity.authentication.domain.User;
import com.mailsent.identity.team.domain.AccessDeniedException;
import com.mailsent.portfolio.client.domain.ClientRelationship;
import com.mailsent.portfolio.client.domain.OrganizationNotFoundException;
import com.mailsent.portfolio.client.infrastructure.InMemoryClientRelationshipRepository;
import com.mailsent.portfolio.organization.domain.Organization;
import com.mailsent.portfolio.organization.infrastructure.InMemoryOrganizationRepository;

/**
 * Unit tests for {@link ClientRelationshipService} (CLIENT-001 / CLIENT-002) against the hermetic
 * in-memory repositories: forming a relationship, client assignment with authorization and INV-09,
 * reading, and the irreversible ACTIVE → ENDED lifecycle.
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

    private User createUser(String id, String email, Role role, String orgId) {
        return User.restore(id, new Email(email), "$2a$12$dummyhash", role, CLOCK.instant(), orgId);
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

    // ==========================================
    // CLIENT-002: Assign Client to Agency Tests
    // ==========================================

    @Test
    @DisplayName("Agency OWNER successfully assigns an existing client organization")
    void agencyOwnerCanAssignClient() {
        String agencyOrgId = createdOrganization("Acme Agency");
        String clientOrgId = createdOrganization("Beta Client");
        User agencyOwner = createUser("usr-1", "owner@acme.com", Role.OWNER, agencyOrgId);

        ClientRelationship relationship = service.assign(agencyOwner, agencyOrgId, clientOrgId);

        assertEquals(ClientRelationship.Status.ACTIVE, relationship.status());
        assertEquals(agencyOrgId, relationship.agencyOrganizationId());
        assertEquals(clientOrgId, relationship.clientOrganizationId());
        assertEquals(CLOCK.instant(), relationship.formedAt());
    }

    @Test
    @DisplayName("Duplicate active assignment is idempotent and returns existing relationship")
    void duplicateActiveAssignmentReturnsExisting() {
        String agencyOrgId = createdOrganization("Acme Agency");
        String clientOrgId = createdOrganization("Beta Client");
        User agencyOwner = createUser("usr-1", "owner@acme.com", Role.OWNER, agencyOrgId);

        ClientRelationship first = service.assign(agencyOwner, agencyOrgId, clientOrgId);
        ClientRelationship second = service.assign(agencyOwner, agencyOrgId, clientOrgId);

        assertEquals(first.id(), second.id());
        assertEquals(1, relationships.findByAgencyOrganizationId(agencyOrgId).size());
    }

    @Test
    @DisplayName("Assigning a previously ended relationship creates a new active relationship")
    void assigningAfterEndedFormsNewRelationship() {
        String agencyOrgId = createdOrganization("Acme Agency");
        String clientOrgId = createdOrganization("Beta Client");
        User agencyOwner = createUser("usr-1", "owner@acme.com", Role.OWNER, agencyOrgId);

        ClientRelationship first = service.assign(agencyOwner, agencyOrgId, clientOrgId);
        service.end(first.id());

        ClientRelationship second = service.assign(agencyOwner, agencyOrgId, clientOrgId);

        assertFalse(first.id().equals(second.id()));
        assertEquals(ClientRelationship.Status.ACTIVE, second.status());
    }

    @Test
    @DisplayName("Non-owner roles (ANALYST, VIEWER) are denied assignment")
    void nonOwnerRolesAreDeniedAssignment() {
        String agencyOrgId = createdOrganization("Acme Agency");
        String clientOrgId = createdOrganization("Beta Client");
        User analyst = createUser("usr-analyst", "analyst@acme.com", Role.ANALYST, agencyOrgId);
        User viewer = createUser("usr-viewer", "viewer@acme.com", Role.VIEWER, agencyOrgId);

        assertThrows(AccessDeniedException.class, () -> service.assign(analyst, agencyOrgId, clientOrgId));
        assertThrows(AccessDeniedException.class, () -> service.assign(viewer, agencyOrgId, clientOrgId));
    }

    @Test
    @DisplayName("Multi-Tenant Isolation (INV-09): Caller from Org X cannot assign client to Agency Org Y")
    void crossTenantCallerIsDeniedAssignment() {
        String agencyOrgId = createdOrganization("Acme Agency");
        String clientOrgId = createdOrganization("Beta Client");
        String otherOrgId = createdOrganization("Other Agency");
        User foreignOwner = createUser("usr-foreign", "owner@other.com", Role.OWNER, otherOrgId);

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> service.assign(foreignOwner, agencyOrgId, clientOrgId));
        assertTrue(ex.getMessage().contains("Multi-tenant violation"));
    }

    @Test
    @DisplayName("Assigning nonexistent agency or client throws OrganizationNotFoundException")
    void assigningNonexistentOrganizationsThrows() {
        String agencyOrgId = createdOrganization("Acme Agency");
        String clientOrgId = createdOrganization("Beta Client");
        User agencyOwner = createUser("usr-1", "owner@acme.com", Role.OWNER, agencyOrgId);

        assertThrows(OrganizationNotFoundException.class,
                () -> service.assign(agencyOwner, agencyOrgId, "no-such-client"));

        User foreignOwner = createUser("usr-fake", "fake@none.com", Role.OWNER, "no-such-agency");
        assertThrows(OrganizationNotFoundException.class,
                () -> service.assign(foreignOwner, "no-such-agency", clientOrgId));
    }

    @Test
    @DisplayName("Assigning self as client is rejected with IllegalArgumentException")
    void selfAssignmentIsRejected() {
        String agencyOrgId = createdOrganization("Acme Agency");
        User agencyOwner = createUser("usr-1", "owner@acme.com", Role.OWNER, agencyOrgId);

        assertThrows(IllegalArgumentException.class,
                () -> service.assign(agencyOwner, agencyOrgId, agencyOrgId));
    }

    @Test
    @DisplayName("Null or blank parameters are cleanly rejected")
    void invalidParametersAreRejected() {
        String agencyOrgId = createdOrganization("Acme Agency");
        String clientOrgId = createdOrganization("Beta Client");
        User agencyOwner = createUser("usr-1", "owner@acme.com", Role.OWNER, agencyOrgId);

        assertThrows(AccessDeniedException.class, () -> service.assign(null, agencyOrgId, clientOrgId));
        assertThrows(AccessDeniedException.class, () -> service.assign(agencyOwner, null, clientOrgId));
        assertThrows(AccessDeniedException.class, () -> service.assign(agencyOwner, "  ", clientOrgId));
        assertThrows(IllegalArgumentException.class, () -> service.assign(agencyOwner, agencyOrgId, null));
        assertThrows(IllegalArgumentException.class, () -> service.assign(agencyOwner, agencyOrgId, "  "));
    }
}