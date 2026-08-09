package com.mailsent.identity.authentication.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.mailsent.identity.authentication.domain.Session;
import com.mailsent.identity.authentication.domain.SessionToken;
import com.mailsent.identity.authentication.infrastructure.persistence.JpaSessionRepository;
import com.mailsent.identity.authentication.infrastructure.persistence.JpaSessionRepositoryAdapter;
import com.mailsent.identity.authentication.infrastructure.persistence.SessionJpaEntity;

/**
 * Tests the production {@link JpaSessionRepositoryAdapter} in isolation with a mocked
 * Spring Data repository (the adapter is {@code @Profile("!test")}). Verifies that only
 * the token hash is persisted and that lookup is by hash, not the raw value.</p>
 */
class JpaSessionRepositoryAdapterTest {

    private final JpaSessionRepository jpa = mock(JpaSessionRepository.class);
    private final JpaSessionRepositoryAdapter adapter = new JpaSessionRepositoryAdapter(jpa);

    private final Instant now = Instant.parse("2026-08-08T10:00:00Z");
    private final Session session = Session.issue("user-1", new SessionToken("raw", "hash"), now, Duration.ofMinutes(60));

    @Test
    void savePersistsOnlyHashNotRawToken() {
        SessionJpaEntity entity = new SessionJpaEntity(session.id(), session.userId(),
            session.tokenHash(), session.createdAt(), session.expiresAt());
        when(jpa.saveAndFlush(any())).thenReturn(entity);

        Session returned = adapter.save(session);

        assertEquals("hash", returned.tokenHash());
        assertTrue(!returned.tokenHash().equals("raw"));
        assertEquals("user-1", returned.userId());
    }

    }