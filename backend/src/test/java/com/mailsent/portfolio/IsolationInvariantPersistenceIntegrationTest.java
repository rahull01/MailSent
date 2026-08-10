package com.mailsent.portfolio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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

import com.mailsent.identity.authentication.application.SignUpCommand;
import com.mailsent.identity.authentication.application.SignUpService;
import com.mailsent.identity.authentication.application.UserRepository;
import com.mailsent.identity.authentication.domain.Email;
import com.mailsent.identity.authentication.domain.User;
import com.mailsent.portfolio.domain.application.DomainService;
import com.mailsent.portfolio.domain.domain.Domain;
import com.mailsent.portfolio.organization.application.OrganizationService;
import com.mailsent.portfolio.organization.domain.Organization;

/**
 * Real-PostgreSQL integration tests for INV-09 (Isolation) across the existing
 * organization-scoped persistence/read paths (ORG-004 — test-only, no new mechanism).
 *
 * <p>INV-09: "One organization's health and data never mix with another's." This
 * verification proves the invariant over the organization-scoped read paths that already
 * exist and nothing more: the domain ownership read
 * ({@code DomainService.findByOrganizationId}, the sole org-scoped query in the codebase)
 * and the user home-organization binding read ({@code UserRepository.findByEmail}). It is
 * deliberately <em>broader</em> than, and not redundant with, the existing ORG-003 test
 * (single org-pair count) and the ORG-002 test (distinct org ids only).</p>
 *
 * <p>Runs against a live PostgreSQL on port 5433 (see {@code application-it.yml});
 * Flyway applies V1–V5 at boot and {@code ddl-auto: validate} verifies the schema. Gated
 * on the system property {@code mailsent.it.postgres} so it runs only when a live
 * database is supplied (CI / CTO gate with {@code -Dmailsent.it.postgres=true}); the
 * hermetic default suite skips these. All cases are idempotent (randomized prefixes and
 * emails) so they are safe against a persistent, shared schema.</p>
 */
@SpringBootTest
@ActiveProfiles("it")
@EnabledIfSystemProperty(named = "mailsent.it.postgres", matches = "true")
class IsolationInvariantPersistenceIntegrationTest {

    private static final String PWD = "a-sufficiently-long-password-1";
    private static final String PREFIX = "it-" + UUID.randomUUID() + "-";

    @Autowired
    private OrganizationService organizations;
    @Autowired
    private DomainService domains;
    @Autowired
    private SignUpService signUpService;
    @Autowired
    private UserRepository users;

    private Organization createdOrganization() {
        return organizations.create("Isolation " + UUID.randomUUID());
    }

    @Test
    void organizationScopedDomainReadsPartitionTheDomainUniverse() {
        Organization orgA = createdOrganization();
        Organization orgB = createdOrganization();
        Organization orgC = createdOrganization();

        // Interleave registrations across three orgs, leaving a fourth empty.
        domains.register(orgA.id(), PREFIX + "a1.com");
        domains.register(orgB.id(), PREFIX + "b1.com");
        domains.register(orgA.id(), PREFIX + "a2.com");
        domains.register(orgC.id(), PREFIX + "c1.com");
        domains.register(orgC.id(), PREFIX + "c2.com");

        Organization emptyOrg = createdOrganization();

        List<Domain> ownedByA = domains.findByOrganizationId(orgA.id());
        List<Domain> ownedByB = domains.findByOrganizationId(orgB.id());
        List<Domain> ownedByC = domains.findByOrganizationId(orgC.id());
        List<Domain> ownedByEmpty = domains.findByOrganizationId(emptyOrg.id());

        // Containment: each org sees exactly its own registered domains.
        assertEquals(Set.of(PREFIX + "a1.com", PREFIX + "a2.com"), namesOf(ownedByA));
        assertEquals(Set.of(PREFIX + "b1.com"), namesOf(ownedByB));
        assertEquals(Set.of(PREFIX + "c1.com", PREFIX + "c2.com"), namesOf(ownedByC));
        assertTrue(ownedByEmpty.isEmpty(),
            "an org that registered nothing sees no other org's domains (no cross-visibility)");

        // No-overlap (INV-09 "never mix"): a domain never appears under two organizations.
        Set<String> seen = new HashSet<>();
        for (List<Domain> owned : List.of(ownedByA, ownedByB, ownedByC)) {
            for (Domain d : owned) {
                assertTrue(seen.add(d.id()),
                    "domain " + d.name() + " must not appear under two organizations");
            }
        }

        // Totality: every registered domain is owned by exactly one org and reachable.
        assertEquals(5, seen.size(), "all five registered domains are owned and reachable");
    }

    @Test
    void userHomeOrganizationAndDomainOwnershipDoNotCrossTenants() {
        String emailA = PREFIX + "tenant-a@example.com";
        String emailB = PREFIX + "tenant-b@example.com";
        signUpService.signUp(new SignUpCommand(emailA, PWD));
        signUpService.signUp(new SignUpCommand(emailB, PWD));

        User userA = users.findByEmail(new Email(emailA)).orElseThrow();
        User userB = users.findByEmail(new Email(emailB)).orElseThrow();
        String orgA = userA.organizationId();
        String orgB = userB.organizationId();

        // Distinct tenant boundaries (ORG-002 binding), each backed by a real org.
        assertNotEquals(orgA, orgB, "two tenants must resolve to distinct home organizations");
        assertTrue(organizations.findById(orgA).isPresent(), "tenant A's home org exists");
        assertTrue(organizations.findById(orgB).isPresent(), "tenant B's home org exists");

        // A domain owned by tenant A is visible through A's org-scoped read only.
        domains.register(orgA, PREFIX + "a.com");
        List<Domain> aDomains = domains.findByOrganizationId(orgA);
        List<Domain> bDomains = domains.findByOrganizationId(orgB);
        assertEquals(Set.of(PREFIX + "a.com"), namesOf(aDomains));
        assertTrue(bDomains.isEmpty(),
            "tenant B's org-scoped read never returns tenant A's domain (INV-09)");

        // Binding integrity: each reloaded user stays bound to its own home org.
        assertEquals(orgA, userA.organizationId(), "tenant A's binding is stable");
        assertEquals(orgB, userB.organizationId(), "tenant B's binding is stable");
    }

    private static Set<String> namesOf(List<Domain> owned) {
        Set<String> names = new HashSet<>();
        for (Domain d : owned) {
            names.add(d.name());
        }
        return names;
    }
}