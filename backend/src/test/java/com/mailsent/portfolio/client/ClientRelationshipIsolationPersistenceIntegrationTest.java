package com.mailsent.portfolio.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.mailsent.portfolio.client.application.ClientRelationshipService;
import com.mailsent.portfolio.client.domain.ClientRelationship;
import com.mailsent.portfolio.organization.application.OrganizationService;

/**
 * Real-PostgreSQL integration tests for the CLIENT-003 INV-09 isolation invariant over the
 * frozen CLIENT-001 Client Relationship model (test-only; no production change).
 *
 * <p>Proves that <strong>multiple ClientRelationships remain isolated</strong> — mirroring
 * the approved ORG-004 isolation pattern, but over the org-to-org relationship reads. It
 * adds only the multi-relationship partition coverage that CLIENT-001 does not already
 * assert (CLIENT-001 covers a single relationship's round-trip, lifecycle, org-only columns,
 * FKs, and per-relationship org immutability):</p>
 *
 * <ul>
 *   <li>distinct agency/client organization pairs coexist;</li>
 *   <li>{@code findById(A)} returns only relationship A's organization references and never
 *       relationship B's (no-overlap partition — INV-09);</li>
 *   <li>ending relationship A does not alter relationship B;</li>
 *   <li>forming/ending one relationship mutates neither the other relationship nor any
 *       referenced Organization;</li>
 *   <li>every reference resolves to an existing Organization (foreign-key / referential
 *       integrity, INV-14).</li>
 * </ul>
 *
 * <p>Runs against a live PostgreSQL on port 5433 (see {@code application-it.yml}); Flyway
 * applies V1–V7 at startup. Gated on {@code mailsent.it.postgres}; idempotent
 * (randomized org names).</p>
 */
@SpringBootTest
@ActiveProfiles("it")
@EnabledIfSystemProperty(named = "mailsent.it.postgres", matches = "true")
class ClientRelationshipIsolationPersistenceIntegrationTest {

    private static final String PREFIX = "it-" + UUID.randomUUID() + "-";

    @Autowired private OrganizationService organizationService;
    @Autowired private ClientRelationshipService clientRelationships;

    private String createdOrganization(String label) {
        return organizationService.create(PREFIX + label).id();
    }

    @Test
    void multipleDistinctRelationshipPairsCoexist() {
        String agencyA = createdOrganization("agencyA");
        String clientA = createdOrganization("clientA");
        String agencyB = createdOrganization("agencyB");
        String clientB = createdOrganization("clientB");

        ClientRelationship relA = clientRelationships.form(agencyA, clientA);
        ClientRelationship relB = clientRelationships.form(agencyB, clientB);

        assertTrue(!relA.id().equals(relB.id()), "relationships have distinct stable identities");
        assertEquals(ClientRelationship.Status.ACTIVE, relA.status());
        assertEquals(ClientRelationship.Status.ACTIVE, relB.status());
        assertTrue(clientRelationships.findById(relA.id()).isPresent());
        assertTrue(clientRelationships.findById(relB.id()).isPresent());
    }

    @Test
    void findByIdReturnsOnlyItsOwnOrganizationReferences() {
        String agencyA = createdOrganization("agencyA");
        String clientA = createdOrganization("clientA");
        String agencyB = createdOrganization("agencyB");
        String clientB = createdOrganization("clientB");

        ClientRelationship relA = clientRelationships.form(agencyA, clientA);
        ClientRelationship relB = clientRelationships.form(agencyB, clientB);

        ClientRelationship loadedA = clientRelationships.findById(relA.id()).orElseThrow();
        ClientRelationship loadedB = clientRelationships.findById(relB.id()).orElseThrow();

        // No-overlap partition (INV-09): A sees only its own pair, B's pair never appears in A.
        assertEquals(Set.of(agencyA, clientA), orgPairOf(loadedA));
        assertEquals(Set.of(agencyB, clientB), orgPairOf(loadedB));

        Set<String> seen = new HashSet<>();
        for (String id : List.of(loadedA.agencyOrganizationId(), loadedA.clientOrganizationId(),
                                 loadedB.agencyOrganizationId(), loadedB.clientOrganizationId())) {
            assertTrue(seen.add(id), "no organization appears in two different relationships (INV-09)");
        }
    }

    @Test
    void endingOneRelationshipDoesNotAlterTheOther() {
        String agencyA = createdOrganization("agencyA");
        String clientA = createdOrganization("clientA");
        String agencyB = createdOrganization("agencyB");
        String clientB = createdOrganization("clientB");

        ClientRelationship relA = clientRelationships.form(agencyA, clientA);
        ClientRelationship relB = clientRelationships.form(agencyB, clientB);

        clientRelationships.end(relA.id());

        ClientRelationship loadedB = clientRelationships.findById(relB.id()).orElseThrow();
        assertEquals(ClientRelationship.Status.ACTIVE, loadedB.status(),
            "ending relationship A does not alter relationship B");
        assertTrue(loadedB.endedAt() == null, "relationship B's endedAt stays null");
        assertEquals(Set.of(agencyB, clientB), orgPairOf(loadedB));

        ClientRelationship loadedA = clientRelationships.findById(relA.id()).orElseThrow();
        assertEquals(ClientRelationship.Status.ENDED, loadedA.status());
    }

    @Test
    void formingAndEndingOneRelationshipDoesNotMutateOthersOrganizations() {
        String agencyA = createdOrganization("agencyA");
        String clientA = createdOrganization("clientA");
        String agencyB = createdOrganization("agencyB");
        String clientB = createdOrganization("clientB");

        ClientRelationship relA = clientRelationships.form(agencyA, clientA);
        ClientRelationship relB = clientRelationships.form(agencyB, clientB);
        clientRelationships.end(relA.id());

        // The other relationship is untouched, and none of the four referenced organizations
        // (including relationship A's) has its identity or lifecycle mutated.
        assertEquals(ClientRelationship.Status.ACTIVE,
            clientRelationships.findById(relB.id()).orElseThrow().status());
        for (String orgId : List.of(agencyA, clientA, agencyB, clientB)) {
            assertEquals(com.mailsent.portfolio.organization.domain.Organization.Status.ACTIVE,
                organizationService.findById(orgId).orElseThrow().status(),
                "organization " + orgId + " unchanged by relationship forming/ending");
        }
    }

    @Test
    void referencesResolveToExistingOrganizations() {
        String agencyA = createdOrganization("agencyA");
        String clientA = createdOrganization("clientA");
        String agencyB = createdOrganization("agencyB");
        String clientB = createdOrganization("clientB");
        ClientRelationship relA = clientRelationships.form(agencyA, clientA);
        ClientRelationship relB = clientRelationships.form(agencyB, clientB);

        for (ClientRelationship relationship : List.of(relA, relB)) {
            ClientRelationship loaded = clientRelationships.findById(relationship.id()).orElseThrow();
            assertTrue(organizationService.findById(loaded.agencyOrganizationId()).isPresent(),
                "agency reference resolves to an existing organization (FK integrity)");
            assertTrue(organizationService.findById(loaded.clientOrganizationId()).isPresent(),
                "client reference resolves to an existing organization (FK integrity)");
        }
    }

    private static Set<String> orgPairOf(ClientRelationship relationship) {
        return Set.of(relationship.agencyOrganizationId(), relationship.clientOrganizationId());
    }
}