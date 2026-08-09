package com.mailsent.identity.authentication.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import com.mailsent.identity.authentication.domain.DuplicateEmailException;
import com.mailsent.identity.authentication.domain.Email;
import com.mailsent.identity.authentication.domain.Role;
import com.mailsent.identity.authentication.domain.User;
import com.mailsent.identity.authentication.infrastructure.persistence.JpaUserRepository;
import com.mailsent.identity.authentication.infrastructure.persistence.JpaUserRepositoryAdapter;
import com.mailsent.identity.authentication.infrastructure.persistence.UserJpaEntity;

/**
 * Tests the production {@link JpaUserRepositoryAdapter} logic in isolation, using a
 * mocked Spring Data repository to simulate the PostgreSQL unique-constraint failure.
 *
 * <p>The adapter is {@code @Profile("!test")}, so it is never a test-profile bean; this
 * test constructs it directly with a mock to verify the race-safe duplicate mapping
 * independent of a live database.</p>
 */
class JpaUserRepositoryAdapterTest {

    private final JpaUserRepository jpa = mock(JpaUserRepository.class);
    private final JpaUserRepositoryAdapter adapter = new JpaUserRepositoryAdapter(jpa);

    @Test
    void saveMapsConstraintViolationToDuplicateEmailException() {
        when(jpa.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("uk_users_email"));

        User user = User.register(new Email("owner@example.com"), "pbkdf2$hash", Instant.now());

        assertThrows(DuplicateEmailException.class, () -> adapter.save(user));
    }

    @Test
    void existsByEmailDelegatesToSpringData() {
        when(jpa.existsByEmail("owner@example.com")).thenReturn(true);
        assertTrue(adapter.existsByEmail(new Email("owner@example.com")));
    }

    @Test
    void restorePreservesStableIdentityAndRole() {
        User user = User.register(new Email("owner@example.com"), "hash", Instant.now());
        User restored = User.restore(user.id(), user.email(), user.passwordHash(), Role.VIEWER, user.createdAt());
        assertEquals(user.id(), restored.id());
        assertEquals(Role.VIEWER, restored.role());
    }

    @Test
    void saveMapsAndFlushesDomainUserToEntity() {
        User user = User.register(new Email("save@example.com"), "pbkdf2$hash", Instant.now());
        UserJpaEntity saved = new UserJpaEntity(user.id(), user.email().value(), user.passwordHash(), user.role(), user.createdAt());
        when(jpa.saveAndFlush(any())).thenReturn(saved);

        User returned = adapter.save(user);

        assertEquals(user.id(), returned.id());
        assertEquals("save@example.com", returned.email().value());
        assertEquals(user.passwordHash(), returned.passwordHash());
        assertEquals(user.createdAt(), returned.createdAt());
    }

    @Test
    void entityExposesPersistenceFields() {
        Instant now = Instant.now();
        UserJpaEntity e = new UserJpaEntity("id-1", "owner@example.com", "h", Role.VIEWER, now);

        assertEquals("id-1", e.id());
        assertEquals("owner@example.com", e.email());
        assertEquals("h", e.passwordHash());
        assertEquals(Role.VIEWER, e.role());
        assertEquals(now, e.createdAt());
    }

    @Test
    void nonDefaultRoleRoundTripsThroughAdapter() {
        // AUTH-002 identity/role persistence: a user created with a non-default role
        // (e.g. OWNER) must be stored and hydrated back with that exact role preserved —
        // the role is part of the identity's persisted state (INV-10/INV-13).
        User user = User.restore("u-1", new Email("owner@example.com"), "h", Role.OWNER, Instant.now());
        UserJpaEntity saved = new UserJpaEntity(user.id(), user.email().value(),
            user.passwordHash(), user.role(), user.createdAt());
        when(jpa.saveAndFlush(any())).thenReturn(saved);
        when(jpa.findByEmail("owner@example.com")).thenReturn(java.util.Optional.of(saved));

        User returned = adapter.save(user);
        User loaded = adapter.findByEmail(new Email("owner@example.com")).orElseThrow();

        assertEquals(Role.OWNER, returned.role(), "saved user retains its role");
        assertEquals(Role.OWNER, loaded.role(), "loaded user retains its role");
        assertEquals(user.id(), loaded.id(), "stable identity preserved across persistence");
    }
}