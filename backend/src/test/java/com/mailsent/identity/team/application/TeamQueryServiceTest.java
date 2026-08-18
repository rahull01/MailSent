package com.mailsent.identity.team.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mailsent.identity.authentication.domain.Email;
import com.mailsent.identity.authentication.domain.Role;
import com.mailsent.identity.authentication.domain.User;
import com.mailsent.identity.authentication.infrastructure.InMemoryUserRepository;
import com.mailsent.identity.team.domain.AccessDeniedException;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TeamQueryService Unit & Multi-Tenant Isolation Tests (TEAM-004)")
class TeamQueryServiceTest {

    private InMemoryUserRepository userRepository;
    private TeamQueryService teamQueryService;
    private Instant now;

    @BeforeEach
    void setUp() {
        userRepository = new InMemoryUserRepository();
        teamQueryService = new TeamQueryService(userRepository);
        now = Instant.now();
    }

    private User createUser(String id, String email, Role role, String orgId, Instant createdAt) {
        return User.restore(id, new Email(email), "$2a$12$dummyhash", role, createdAt, orgId);
    }

    @Test
    @DisplayName("Authorized members (OWNER, ANALYST, VIEWER) can list their own organization's team")
    void authorizedRolesCanQueryOwnTeam() {
        User owner = createUser("u-1", "owner@agency.com", Role.OWNER, "org-1", now.minusSeconds(300));
        User analyst = createUser("u-2", "analyst@agency.com", Role.ANALYST, "org-1", now.minusSeconds(200));
        User viewer = createUser("u-3", "viewer@agency.com", Role.VIEWER, "org-1", now.minusSeconds(100));

        userRepository.save(owner);
        userRepository.save(analyst);
        userRepository.save(viewer);

        // Owner query
        List<TeamMemberSummary> ownerResult = teamQueryService.listTeamMembers(owner, "org-1");
        assertEquals(3, ownerResult.size());
        assertEquals("owner@agency.com", ownerResult.get(0).email());
        assertEquals(Role.OWNER, ownerResult.get(0).role());
        assertEquals("analyst@agency.com", ownerResult.get(1).email());
        assertEquals(Role.ANALYST, ownerResult.get(1).role());
        assertEquals("viewer@agency.com", ownerResult.get(2).email());
        assertEquals(Role.VIEWER, ownerResult.get(2).role());

        // Analyst query
        List<TeamMemberSummary> analystResult = teamQueryService.listTeamMembers(analyst, "org-1");
        assertEquals(3, analystResult.size());

        // Viewer query
        List<TeamMemberSummary> viewerResult = teamQueryService.listTeamMembers(viewer, "org-1");
        assertEquals(3, viewerResult.size());
    }

    @Test
    @DisplayName("Multi-Tenant Isolation (INV-09): User from Org A cannot list members of Org B")
    void multiTenantIsolationEnforced() {
        User userOrgA = createUser("u-1", "user@a.com", Role.OWNER, "org-a", now);
        User userOrgB = createUser("u-2", "user@b.com", Role.OWNER, "org-b", now);

        userRepository.save(userOrgA);
        userRepository.save(userOrgB);

        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () ->
                teamQueryService.listTeamMembers(userOrgA, "org-b"));

        assertTrue(ex.getMessage().contains("Multi-tenant violation"));
    }

    @Test
    @DisplayName("Deterministic ordering: results are sorted by createdAt ASC, id ASC")
    void deterministicOrderingPreserved() {
        User u3 = createUser("u-3", "c@agency.com", Role.VIEWER, "org-1", now.plusSeconds(30));
        User u1 = createUser("u-1", "a@agency.com", Role.OWNER, "org-1", now.minusSeconds(30));
        User u2 = createUser("u-2", "b@agency.com", Role.ANALYST, "org-1", now);

        userRepository.save(u3);
        userRepository.save(u1);
        userRepository.save(u2);

        List<TeamMemberSummary> result = teamQueryService.listTeamMembers(u1, "org-1");

        assertEquals(3, result.size());
        assertEquals("u-1", result.get(0).userId());
        assertEquals("u-2", result.get(1).userId());
        assertEquals("u-3", result.get(2).userId());
    }

    @Test
    @DisplayName("Zero Secrets (N4): TeamMemberSummary excludes passwordHash")
    void passwordHashExcluded() {
        User owner = createUser("u-1", "owner@agency.com", Role.OWNER, "org-1", now);
        userRepository.save(owner);

        List<TeamMemberSummary> result = teamQueryService.listTeamMembers(owner, "org-1");
        assertEquals(1, result.size());
        TeamMemberSummary summary = result.get(0);

        assertEquals("u-1", summary.userId());
        assertEquals("owner@agency.com", summary.email());
        assertEquals(Role.OWNER, summary.role());
        assertEquals(now, summary.createdAt());
    }

    @Test
    @DisplayName("Empty organization returns empty list")
    void emptyOrgReturnsEmptyList() {
        User caller = createUser("u-caller", "caller@neworg.com", Role.OWNER, "empty-org", now);
        // Note: caller not saved in repository
        List<TeamMemberSummary> result = teamQueryService.listTeamMembers(caller, "empty-org");
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Null or unauthenticated caller throws AccessDeniedException")
    void unauthenticatedCallerThrows() {
        assertThrows(AccessDeniedException.class, () -> teamQueryService.listTeamMembers(null, "org-1"));

        User caller = createUser("u-1", "caller@a.com", Role.OWNER, "org-1", now);
        assertThrows(AccessDeniedException.class, () -> teamQueryService.listTeamMembers(caller, null));
        assertThrows(AccessDeniedException.class, () -> teamQueryService.listTeamMembers(caller, "   "));
    }
}
