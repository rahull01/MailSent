package com.mailsent.identity.team;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.test.context.ActiveProfiles;

import com.mailsent.identity.authentication.application.SignUpCommand;
import com.mailsent.identity.authentication.application.SignUpService;
import com.mailsent.identity.authentication.application.UserRepository;
import com.mailsent.identity.authentication.domain.Email;
import com.mailsent.identity.authentication.domain.Role;
import com.mailsent.identity.authentication.domain.User;
import com.mailsent.identity.team.application.InvitationIssued;
import com.mailsent.identity.team.application.InvitationRepository;
import com.mailsent.identity.team.application.InvitationService;
import com.mailsent.identity.team.application.MembershipRepository;
import com.mailsent.identity.team.domain.Invitation;
import com.mailsent.identity.team.domain.InvitationAlreadyAcceptedException;
import com.mailsent.identity.team.domain.InvitationTokenFactory;
import com.mailsent.identity.team.domain.InvalidInvitationTokenException;
import com.mailsent.identity.team.domain.InviteeNotFoundException;
import com.mailsent.identity.team.domain.Membership;
import com.mailsent.identity.team.domain.MembershipAlreadyExistsException;
import com.mailsent.portfolio.organization.application.OrganizationService;

/**
 * Real-PostgreSQL integration tests for TEAM-001 persistence and its invariants.
 *
 * <p>Runs against a live PostgreSQL on port 5433 (see {@code application-it.yml}). Flyway
 * applies V1–V6 at context startup and {@code ddl-auto: validate} verifies the schema
 * against the JPA entities. Exercises the true path:
 * <pre>
 * InvitationService → JPA invitation/membership adapters → Spring Data JPA → PostgreSQL
 * </pre>
 * plus the single-use, duplicate-membership, organization-ownership, home-org preservation,
 * and concurrency invariants. Idempotent (randomized emails/orgs). Gated on
 * {@code mailsent.it.postgres}.</p>
 */
@SpringBootTest
@ActiveProfiles("it")
@EnabledIfSystemProperty(named = "mailsent.it.postgres", matches = "true")
class TeamPersistenceIntegrationTest {

    private static final String PWD = "a-sufficiently-long-password-1";
    private static final String PREFIX = "it-" + java.util.UUID.randomUUID() + "-";

    @Autowired
    private InvitationService invitationService;
    @Autowired
    private InvitationRepository invitations;
    @Autowired
    private MembershipRepository memberships;
    @Autowired
    private InvitationTokenFactory tokenFactory;
    @Autowired
    private OrganizationService organizationService;
    @Autowired
    private SignUpService signUpService;
    @Autowired
    private UserRepository users;
    @Autowired
    private DataSource dataSource;

    private String targetOrganization() {
        return organizationService.create("Target " + java.util.UUID.randomUUID()).id();
    }

    private void createAccount(String email) {
        signUpService.signUp(new SignUpCommand(email, PWD));
    }

    private InvitationIssued invite(String orgId, String email, String role) {
        return invitationService.invite(orgId, email, role);
    }

    @Test
    void invitePersistReloadThroughRealPostgres() {
        String orgId = targetOrganization();
        String email = PREFIX + "invite@example.com";
        InvitationIssued issued = invite(orgId, email, "VIEWER");

        Invitation reloaded = invitations.findByTokenHash(tokenFactory.hash(issued.rawToken())).orElseThrow();
        assertEquals(Invitation.Status.PENDING, reloaded.status());
        assertEquals(orgId, reloaded.organizationId());
        assertEquals(email, reloaded.inviteeEmail().value());
        assertEquals(Role.VIEWER, reloaded.role());
        assertNotNull(reloaded.createdAt());
    }

    @Test
    void acceptCreatesMembershipAndMarksInvitationAcceptedThroughRealPostgres() {
        createAccount(PREFIX + "accept@example.com");
        String orgId = targetOrganization();
        InvitationIssued issued = invite(orgId, PREFIX + "accept@example.com", "ANALYST");

        Membership membership = invitationService.accept(issued.rawToken());

        assertEquals(orgId, membership.organizationId());
        assertEquals(Role.ANALYST, membership.role());
        assertTrue(memberships.existsByUserAndOrganization(membership.userId(), orgId),
            "membership is persisted");
        assertTrue(invitations.findByTokenHash(tokenFactory.hash(issued.rawToken()))
            .orElseThrow().isAccepted(), "invitation accepted (single-use)");
    }

    @Test
    void acceptIsSingleUseThroughRealPostgres() {
        createAccount(PREFIX + "single@example.com");
        String orgId = targetOrganization();
        InvitationIssued issued = invite(orgId, PREFIX + "single@example.com", "VIEWER");

        invitationService.accept(issued.rawToken());
        assertThrows(InvitationAlreadyAcceptedException.class,
            () -> invitationService.accept(issued.rawToken()));
    }

    @Test
    void acceptPreventsDuplicateActiveMembershipThroughRealPostgres() {
        createAccount(PREFIX + "dup@example.com");
        String orgId = targetOrganization();

        InvitationIssued first = invite(orgId, PREFIX + "dup@example.com", "VIEWER");
        invitationService.accept(first.rawToken());

        InvitationIssued second = invite(orgId, PREFIX + "dup@example.com", "ANALYST");
        assertThrows(MembershipAlreadyExistsException.class,
            () -> invitationService.accept(second.rawToken()));
    }

    @Test
    void acceptPreservesHomeOrganizationThroughRealPostgres() {
        String homeEmail = PREFIX + "home@example.com";
        signUpService.signUp(new SignUpCommand(homeEmail, PWD));
        User user = users.findByEmail(new Email(homeEmail)).orElseThrow();
        String homeOrgId = user.organizationId();

        String invitedOrgId = targetOrganization();
        InvitationIssued issued = invite(invitedOrgId, homeEmail, "VIEWER");
        Membership membership = invitationService.accept(issued.rawToken());

        assertEquals(invitedOrgId, membership.organizationId(),
            "membership references the invited organization, not the home org");
        assertEquals(homeOrgId, users.findByEmail(new Email(homeEmail))
            .orElseThrow().organizationId(), "home organization unchanged by acceptance (D6)");
    }

    @Test
    void acceptRejectsInviteeWithoutAccountThroughRealPostgres() {
        String orgId = targetOrganization();
        InvitationIssued issued = invite(orgId, PREFIX + "nobody@example.com", "VIEWER");
        assertThrows(InviteeNotFoundException.class, () -> invitationService.accept(issued.rawToken()));
    }

    @Test
    void acceptRejectsUnknownTokenThroughRealPostgres() {
        assertThrows(InvalidInvitationTokenException.class,
            () -> invitationService.accept("deadbeefdeadbeefdeadbeef"));
    }

    @Test
    void concurrentAcceptsProduceExactlyOneSuccessAndOneDomainError() throws Exception {
        createAccount(PREFIX + "race@example.com");
        String orgId = targetOrganization();
        InvitationIssued issued = invite(orgId, PREFIX + "race@example.com", "VIEWER");
        String rawToken = issued.rawToken();

        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Callable<Object> attempt = () -> {
                try {
                    return invitationService.accept(rawToken);
                } catch (Throwable t) {
                    return t;
                }
            };
            Future<Object> f1 = pool.submit(attempt);
            Future<Object> f2 = pool.submit(attempt);
            List<Object> results = List.of(f1.get(30, TimeUnit.SECONDS), f2.get(30, TimeUnit.SECONDS));

            long successes = results.stream().filter(Membership.class::isInstance).count();
            long singleUseErrors = results.stream()
                .filter(InvitationAlreadyAcceptedException.class::isInstance).count();
            long duplicateErrors = results.stream()
                .filter(MembershipAlreadyExistsException.class::isInstance).count();
            long leakedPersistence = results.stream()
                .filter(DataIntegrityViolationException.class::isInstance).count();

            assertEquals(1, successes, "exactly one concurrent acceptance succeeds");
            assertEquals(1, singleUseErrors + duplicateErrors,
                "the losing acceptance surfaces as a domain error (single-use or duplicate)");
            assertEquals(0, leakedPersistence, "no raw DataIntegrityViolationException leaks");
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void schemaEnforcesInvitationAndMembershipConstraints() throws Exception {
        // ddl-auto: validate already failed startup if columns mismatched; prove the
        // constraints validate does NOT check: token-hash uniqueness, membership
        // (user, org) uniqueness, and the ownership FKs (INV-14).
        try (Connection con = DataSourceUtils.getConnection(dataSource)) {
            DatabaseMetaData meta = con.getMetaData();

            boolean tokenHashUnique = hasUniqueIndex(meta, "invitations", "uk_invitations_token_hash");
            boolean membershipUnique = hasUniqueIndex(meta, "memberships", "uk_memberships_user_organization");
            boolean inviteFk = hasForeignKey(meta, "invitations", "fk_invitations_organization", "organization_id");
            boolean memberUserFk = hasForeignKey(meta, "memberships", "fk_memberships_user", "user_id");
            boolean memberOrgFk = hasForeignKey(meta, "memberships", "fk_memberships_organization", "organization_id");

            assertTrue(tokenHashUnique, "uk_invitations_token_hash unique index present");
            assertTrue(membershipUnique, "uk_memberships_user_organization unique index present");
            assertTrue(inviteFk, "fk_invitations_organization -> organizations present");
            assertTrue(memberUserFk, "fk_memberships_user -> users present");
            assertTrue(memberOrgFk, "fk_memberships_organization -> organizations present");
        }
    }

    private boolean hasUniqueIndex(DatabaseMetaData meta, String table, String indexName) throws Exception {
        try (ResultSet ri = meta.getIndexInfo(null, null, table, true, false)) {
            while (ri.next()) {
                if (indexName.equalsIgnoreCase(ri.getString("INDEX_NAME"))) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasForeignKey(DatabaseMetaData meta, String table, String fkName, String fkColumn)
            throws Exception {
        try (ResultSet fk = meta.getImportedKeys(null, null, table)) {
            while (fk.next()) {
                if (fkName.equalsIgnoreCase(fk.getString("FK_NAME"))
                        && fkColumn.equalsIgnoreCase(fk.getString("FKCOLUMN_NAME"))) {
                    return true;
                }
            }
        }
        return false;
    }
}