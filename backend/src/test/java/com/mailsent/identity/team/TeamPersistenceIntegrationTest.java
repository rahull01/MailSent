package com.mailsent.identity.team;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.mailsent.identity.authentication.application.SignUpCommand;
import com.mailsent.identity.authentication.application.SignUpService;
import com.mailsent.identity.authentication.application.UserRepository;
import com.mailsent.identity.authentication.domain.Email;
import com.mailsent.identity.team.application.InvitationCreationResult;
import com.mailsent.identity.team.application.InvitationRepository;
import com.mailsent.identity.team.application.InvitationService;
import com.mailsent.identity.team.domain.Invitation;
import com.mailsent.identity.team.domain.InvitationAlreadyAcceptedException;
import com.mailsent.identity.team.domain.InvitationTokenFactory;
import com.mailsent.identity.team.domain.InvalidInvitationTokenException;
import com.mailsent.identity.team.domain.InviteeNotFoundException;
import com.mailsent.identity.team.domain.OrganizationMismatchException;
import com.mailsent.identity.team.domain.PendingInvitationAlreadyExistsException;
import com.mailsent.portfolio.organization.application.OrganizationService;

/**
 * Real-PostgreSQL integration tests for TEAM-001 (frozen contract v8 §13, category B).
 *
 * <p>Runs against a live PostgreSQL on port 5433 (see {@code application-it.yml}). Flyway
 * applies V1–V6 at startup and {@code ddl-auto: validate} verifies the schema against the
 * JPA entities — so a schema/entity mismatch fails context startup (item: {@code ddl-auto:
 * validate}). Covers every category-B requirement exactly once, including the HTTP
 * contract exercised against the real stack via MockMvc, and the DB-backed concurrency
 * invariants. Idempotent (randomized emails/orgs). Gated on {@code mailsent.it.postgres}.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("it")
@EnabledIfSystemProperty(named = "mailsent.it.postgres", matches = "true")
class TeamPersistenceIntegrationTest {

    private static final String PWD = "a-sufficiently-long-password-1";
    private static final String PREFIX = "it-" + java.util.UUID.randomUUID() + "-";
    private static final String ACCEPT = "/api/invitations/accept";

    @Autowired private InvitationService invitationService;
    @Autowired private InvitationRepository invitations;
    @Autowired private InvitationTokenFactory tokenFactory;
    @Autowired private OrganizationService organizationService;
    @Autowired private SignUpService signUpService;
    @Autowired private UserRepository users;
    @Autowired private DataSource dataSource;
    @Autowired private MockMvc mockMvc;

    private String existingAccount(String email) {
        signUpService.signUp(new SignUpCommand(email, PWD));
        return users.findByEmail(new Email(email)).orElseThrow().organizationId();
    }

    private String targetOrganization() {
        return organizationService.create("Target " + java.util.UUID.randomUUID()).id();
    }

    private InvitationCreationResult invite(String orgId, String email) {
        return invitationService.invite(orgId, email, "VIEWER");
    }

    private Invitation reload(String rawToken) {
        return invitations.findByTokenHash(tokenFactory.hash(rawToken)).orElseThrow();
    }

    // ---------- schema / constraints ----------

    @Test
    void schemaHasExactInvitationsTable() throws Exception {
        try (Connection con = DataSourceUtils.getConnection(dataSource)) {
            Set<String> cols = new HashSet<>();
            try (ResultSet rs = con.getMetaData().getColumns(null, null, "invitations", null)) {
                while (rs.next()) {
                    cols.add(rs.getString("COLUMN_NAME"));
                }
            }
            assertEquals(Set.of("id", "organization_id", "invitee_email", "role", "token_hash",
                "status", "created_at", "accepted_at"), cols, "exact invitations schema (category B: exact schema)");
        }
    }

    @Test
    void schemaHasOrganizationForeignKey() throws Exception {
        try (Connection con = DataSourceUtils.getConnection(dataSource)) {
            DatabaseMetaData meta = con.getMetaData();
            boolean found = false;
            try (ResultSet fk = meta.getImportedKeys(null, null, "invitations")) {
                while (fk.next()) {
                    if ("fk_invitations_organization".equalsIgnoreCase(fk.getString("FK_NAME"))
                            && "organization_id".equalsIgnoreCase(fk.getString("FKCOLUMN_NAME"))
                            && "organizations".equalsIgnoreCase(fk.getString("PKTABLE_NAME"))) {
                        found = true;
                    }
                }
            }
            assertTrue(found, "fk_invitations_organization -> organizations(id)");
        }
    }

    @Test
    void schemaHasTokenHashUniqueAndPendingPartialUniqueIndex() throws Exception {
        try (Connection con = DataSourceUtils.getConnection(dataSource)) {
            Set<String> unique = new HashSet<>();
            try (ResultSet ri = con.getMetaData().getIndexInfo(null, null, "invitations", true, false)) {
                while (ri.next()) {
                    unique.add(ri.getString("INDEX_NAME"));
                }
            }
            assertTrue(unique.contains("uk_invitations_token_hash"), "token_hash UNIQUE (token identity)");
            assertTrue(unique.contains("uk_invitations_pending_org_email"), "PENDING partial unique index");
        }
    }

    @Test
    void membershipsTableDoesNotExist() throws Exception {
        // Category B: no membership row / no membership architecture.
        try (Connection con = DataSourceUtils.getConnection(dataSource);
             ResultSet rs = con.createStatement().executeQuery(
                 "SELECT table_name FROM information_schema.tables WHERE table_name = 'memberships'")) {
            assertTrue(!rs.next(), "no memberships table exists in the schema");
        }
    }

    // ---------- persistence round-trip / lifecycle / invariants ----------

    @Test
    void persistenceRoundTripPreservesPendingStateWithNullAcceptedAt() {
        String orgId = targetOrganization();
        String email = PREFIX + "roundtrip@example.com";
        InvitationCreationResult r = invite(orgId, email);

        Invitation reloaded = reload(r.rawToken());
        assertEquals(Invitation.Status.PENDING, reloaded.status());
        assertEquals(orgId, reloaded.organizationId());
        assertEquals(email, reloaded.inviteeEmail().value());
        assertEquals(email.toLowerCase(), reloaded.inviteeEmail().value(), "exact canonical email persisted");
        assertTrue(reloaded.acceptedAt() == null, "accepted_at is NULL while PENDING (DB invariant)");
    }

    @Test
    void duplicatePendingInvitationIsRejectedByPartialIndex() {
        String orgId = targetOrganization();
        String email = PREFIX + "dup@example.com";
        invite(orgId, email);
        assertThrows(PendingInvitationAlreadyExistsException.class, () -> invite(orgId, email));
    }

    @Test
    void acceptTransitionsPendingToAcceptedAndPopulatesAcceptedAt() {
        String email = PREFIX + "accept@example.com";
        String homeOrgId = existingAccount(email);
        InvitationCreationResult r = invite(homeOrgId, email);

        Invitation accepted = invitationService.accept(r.rawToken());
        assertTrue(accepted.isAccepted());

        Invitation reloaded = reload(r.rawToken());
        assertEquals(Invitation.Status.ACCEPTED, reloaded.status(), "PENDING -> ACCEPTED");
        assertNotNull(reloaded.acceptedAt(), "accepted_at populated on acceptance (DB invariant)");
    }

    @Test
    void acceptanceSingleUseThroughRealPostgres() {
        String homeOrgId = existingAccount(PREFIX + "single@example.com");
        InvitationCreationResult r = invite(homeOrgId, PREFIX + "single@example.com");
        invitationService.accept(r.rawToken());
        assertThrows(InvitationAlreadyAcceptedException.class,
            () -> invitationService.accept(r.rawToken()));
    }

    @Test
    void acceptanceDifferentHomeOrgIsRejected() {
        String homeOrgId = existingAccount(PREFIX + "orgmismatch@example.com");
        String otherOrgId = targetOrganization();
        InvitationCreationResult r = invite(otherOrgId, PREFIX + "orgmismatch@example.com");
        assertThrows(OrganizationMismatchException.class, () -> invitationService.accept(r.rawToken()));
    }

    @Test
    void acceptanceMissingAccountIsRejected() {
        String orgId = targetOrganization();
        InvitationCreationResult r = invite(orgId, PREFIX + "nobody@example.com");
        assertThrows(InviteeNotFoundException.class, () -> invitationService.accept(r.rawToken()));
    }

    @Test
    void acceptanceUnknownTokenIsRejected() {
        assertThrows(InvalidInvitationTokenException.class, () -> invitationService.accept(
            "0000000000000000000000000000000000000000000000000000000000000000"));
    }

    @Test
    void acceptancePreservesHomeOrganization() {
        String homeEmail = PREFIX + "home@example.com";
        signUpService.signUp(new SignUpCommand(homeEmail, PWD));
        String homeOrgId = users.findByEmail(new Email(homeEmail)).orElseThrow().organizationId();
        InvitationCreationResult r = invite(homeOrgId, homeEmail);

        invitationService.accept(r.rawToken());
        assertEquals(homeOrgId, users.findByEmail(new Email(homeEmail))
            .orElseThrow().organizationId(), "home org unchanged by acceptance (category B: home org unchanged)");
    }

    // ---------- concurrency (DB-backed) ----------

    @Test
    void concurrentAcceptanceProducesExactlyOneSuccessAndOneDomainError() throws Exception {
        String homeOrgId = existingAccount(PREFIX + "race@example.com");
        InvitationCreationResult r = invite(homeOrgId, PREFIX + "race@example.com");
        String rawToken = r.rawToken();

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

            long successes = results.stream().filter(Invitation.class::isInstance).count();
            long domainErrors = results.stream()
                .filter(InvitationAlreadyAcceptedException.class::isInstance).count();
            long leaked = results.stream()
                .filter(DataIntegrityViolationException.class::isInstance).count();

            assertEquals(1, successes, "exactly one concurrent acceptance succeeds (atomic conditional UPDATE)");
            assertEquals(1, domainErrors, "the losing acceptance surfaces a domain-level result");
            assertEquals(0, leaked, "no raw persistence exception leaks");
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void concurrentDuplicateInvitationCreationProducesOneSuccessAndOneDomainError() throws Exception {
        String orgId = targetOrganization();
        String email = PREFIX + "duprate@example.com";

        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Callable<Object> attempt = () -> {
                try {
                    return invite(orgId, email);
                } catch (Throwable t) {
                    return t;
                }
            };
            Future<Object> f1 = pool.submit(attempt);
            Future<Object> f2 = pool.submit(attempt);
            List<Object> results = List.of(f1.get(30, TimeUnit.SECONDS), f2.get(30, TimeUnit.SECONDS));

            long successes = results.stream().filter(InvitationCreationResult.class::isInstance).count();
            long domainErrors = results.stream()
                .filter(PendingInvitationAlreadyExistsException.class::isInstance).count();
            long leaked = results.stream()
                .filter(DataIntegrityViolationException.class::isInstance).count();

            assertEquals(1, successes, "exactly one concurrent duplicate-pending creation succeeds");
            assertEquals(1, domainErrors, "the losing creation surfaces the duplicate-pending domain error");
            assertEquals(0, leaked, "no raw persistence exception leaks");
        } finally {
            pool.shutdownNow();
        }
    }

    // ---------- HTTP contract (category B) ----------

    @Test
    void httpAcceptReturnsExactSuccessBody() throws Exception {
        String email = PREFIX + "http@example.com";
        String homeOrgId = existingAccount(email);
        InvitationCreationResult r = invite(homeOrgId, email);

        mockMvc.perform(post(ACCEPT).contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\":\"" + r.rawToken() + "\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accepted").value(true))
            .andExpect(jsonPath("$.invitationId").value(r.invitationId()))
            .andExpect(jsonPath("$.token").doesNotExist())
            .andExpect(jsonPath("$.email").doesNotExist())
            .andExpect(jsonPath("$.organizationId").doesNotExist());
    }

    @Test
    void httpAcceptMapsMalformedTokenToBadRequest() throws Exception {
        mockMvc.perform(post(ACCEPT).contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\":\"not-a-token\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_INVITATION_TOKEN"));
    }

    @Test
    void httpAcceptMapsUnknownTokenToNotFound() throws Exception {
        mockMvc.perform(post(ACCEPT).contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\":\"0000000000000000000000000000000000000000000000000000000000000000\"}"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("INVALID_INVITATION_TOKEN"));
    }

    @Test
    void httpAcceptMapsAlreadyAcceptedToConflict() throws Exception {
        String email = PREFIX + "httpconflict@example.com";
        String homeOrgId = existingAccount(email);
        InvitationCreationResult r = invite(homeOrgId, email);
        String body = "{\"token\":\"" + r.rawToken() + "\"}";
        mockMvc.perform(post(ACCEPT).contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isOk());
        mockMvc.perform(post(ACCEPT).contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("INVITATION_ALREADY_ACCEPTED"));
    }

    @Test
    void httpAcceptMapsOrgMismatchToConflictWithoutMetadataLeak() throws Exception {
        String email = PREFIX + "httpmismatch@example.com";
        existingAccount(email);
        String otherOrgId = targetOrganization();
        InvitationCreationResult r = invite(otherOrgId, email);

        mockMvc.perform(post(ACCEPT).contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\":\"" + r.rawToken() + "\"}"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("ORG_MISMATCH"))
            .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.not(
                org.hamcrest.Matchers.containsString(otherOrgId))))
            .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.not(
                org.hamcrest.Matchers.containsString(email))));
    }

    @Test
    void httpAcceptMapsMissingAccountToConflict() throws Exception {
        String orgId = targetOrganization();
        InvitationCreationResult r = invite(orgId, PREFIX + "httpnobody@example.com");
        mockMvc.perform(post(ACCEPT).contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\":\"" + r.rawToken() + "\"}"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("ACCOUNT_REQUIRED"));
    }
}