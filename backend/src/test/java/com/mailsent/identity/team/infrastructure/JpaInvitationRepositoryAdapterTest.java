package com.mailsent.identity.team.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.mailsent.identity.authentication.domain.Email;
import com.mailsent.identity.authentication.domain.Role;
import com.mailsent.identity.team.domain.Invitation;
import com.mailsent.identity.team.infrastructure.persistence.InvitationJpaEntity;
import com.mailsent.identity.team.infrastructure.persistence.JpaInvitationRepository;
import com.mailsent.identity.team.infrastructure.persistence.JpaInvitationRepositoryAdapter;

/**
 * Tests the production {@link JpaInvitationRepositoryAdapter} in isolation with a mocked
 * Spring Data repository ({@code @Profile("!test")}).
 */
class JpaInvitationRepositoryAdapterTest {

    private final JpaInvitationRepository jpa = mock(JpaInvitationRepository.class);
    private final JpaInvitationRepositoryAdapter adapter = new JpaInvitationRepositoryAdapter(jpa);
    private final Instant now = Instant.parse("2026-08-08T10:00:00Z");

    @Test
    void saveMapsAndFlushesInvitationToEntity() {
        Invitation invitation = Invitation.register("org-1", new Email("member@example.com"),
            Role.VIEWER, "abc123", now);
        InvitationJpaEntity entity = new InvitationJpaEntity(invitation.id(), invitation.organizationId(),
            invitation.inviteeEmail().value(), invitation.role(), invitation.tokenHash(),
            invitation.status(), invitation.createdAt());
        when(jpa.saveAndFlush(any())).thenReturn(entity);

        Invitation saved = adapter.save(invitation);

        assertEquals(invitation.id(), saved.id());
        assertEquals("org-1", saved.organizationId());
        assertEquals("member@example.com", saved.inviteeEmail().value());
        assertEquals(Role.VIEWER, saved.role());
        assertEquals("abc123", saved.tokenHash());
    }

    @Test
    void findByTokenHashReturnsByStoredHash() {
        InvitationJpaEntity entity = new InvitationJpaEntity("inv-1", "org-1", "member@example.com",
            Role.VIEWER, "abc123", Invitation.Status.PENDING, now);
        when(jpa.findByTokenHash("abc123")).thenReturn(Optional.of(entity));

        Optional<Invitation> found = adapter.findByTokenHash("abc123");

        assertTrue(found.isPresent());
        assertEquals("inv-1", found.get().id());
        assertEquals(Invitation.Status.PENDING, found.get().status());
    }
}