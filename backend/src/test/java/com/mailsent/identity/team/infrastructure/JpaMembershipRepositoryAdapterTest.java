package com.mailsent.identity.team.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import com.mailsent.identity.authentication.domain.Role;
import com.mailsent.identity.team.domain.Membership;
import com.mailsent.identity.team.domain.MembershipAlreadyExistsException;
import com.mailsent.identity.team.infrastructure.persistence.JpaMembershipRepository;
import com.mailsent.identity.team.infrastructure.persistence.JpaMembershipRepositoryAdapter;
import com.mailsent.identity.team.infrastructure.persistence.MembershipJpaEntity;

/**
 * Tests the production {@link JpaMembershipRepositoryAdapter} in isolation with a mocked
 * Spring Data repository ({@code @Profile("!test")}).
 */
class JpaMembershipRepositoryAdapterTest {

    private final JpaMembershipRepository jpa = mock(JpaMembershipRepository.class);
    private final JpaMembershipRepositoryAdapter adapter = new JpaMembershipRepositoryAdapter(jpa);
    private final Instant now = Instant.parse("2026-08-08T10:00:00Z");

    @Test
    void saveMapsAndFlushesMembershipToEntity() {
        Membership membership = Membership.create("user-1", "org-1", Role.ANALYST, now);
        MembershipJpaEntity entity = new MembershipJpaEntity(membership.id(), membership.userId(),
            membership.organizationId(), membership.role(), membership.createdAt());
        when(jpa.saveAndFlush(any())).thenReturn(entity);

        Membership saved = adapter.save(membership);

        assertEquals(membership.id(), saved.id());
        assertEquals("user-1", saved.userId());
        assertEquals("org-1", saved.organizationId());
        assertEquals(Role.ANALYST, saved.role());
    }

    @Test
    void existsByUserAndOrganizationDelegatesToSpringData() {
        when(jpa.existsByUserIdAndOrganizationId("user-1", "org-1")).thenReturn(true);
        assertTrue(adapter.existsByUserAndOrganization("user-1", "org-1"));
    }

    @Test
    void saveMapsUniqueViolationToMembershipAlreadyExistsException() {
        Membership membership = Membership.create("user-1", "org-1", Role.VIEWER, now);
        // The uk_memberships_user_organization constraint rejected a concurrent/duplicate insert.
        when(jpa.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("uk_memberships_user_organization"));

        assertThrows(MembershipAlreadyExistsException.class, () -> adapter.save(membership));
    }
}