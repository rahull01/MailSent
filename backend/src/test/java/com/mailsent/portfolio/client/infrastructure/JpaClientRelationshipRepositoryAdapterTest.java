package com.mailsent.portfolio.client.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.mailsent.portfolio.client.domain.ClientRelationship;
import com.mailsent.portfolio.client.infrastructure.persistence.ClientRelationshipJpaEntity;
import com.mailsent.portfolio.client.infrastructure.persistence.JpaClientRelationshipRepository;
import com.mailsent.portfolio.client.infrastructure.persistence.JpaClientRelationshipRepositoryAdapter;

/**
 * Tests the production {@link JpaClientRelationshipRepositoryAdapter} in isolation with a
 * mocked Spring Data repository ({@code @Profile("!test")}).
 */
class JpaClientRelationshipRepositoryAdapterTest {

    private final JpaClientRelationshipRepository jpa = mock(JpaClientRelationshipRepository.class);
    private final JpaClientRelationshipRepositoryAdapter adapter = new JpaClientRelationshipRepositoryAdapter(jpa);
    private final Instant now = Instant.parse("2026-08-08T10:00:00Z");

    @Test
    void saveMapsAndFlushesRelationshipToEntity() {
        ClientRelationship relationship = ClientRelationship.form("org-agency", "org-client", now);
        ClientRelationshipJpaEntity entity = new ClientRelationshipJpaEntity(relationship.id(),
            relationship.agencyOrganizationId(), relationship.clientOrganizationId(), relationship.formedAt(),
            relationship.status(), relationship.endedAt());
        when(jpa.saveAndFlush(any())).thenReturn(entity);

        ClientRelationship saved = adapter.save(relationship);

        assertEquals(relationship.id(), saved.id());
        assertEquals("org-agency", saved.agencyOrganizationId());
        assertEquals("org-client", saved.clientOrganizationId());
        assertEquals(ClientRelationship.Status.ACTIVE, saved.status());
    }

    @Test
    void findByIdReturnsByStableIdentity() {
        ClientRelationshipJpaEntity entity = new ClientRelationshipJpaEntity("rel-1", "org-agency",
            "org-client", now, ClientRelationship.Status.ENDED, now.plusSeconds(10));
        when(jpa.findById("rel-1")).thenReturn(Optional.of(entity));

        assertTrue(adapter.findById("rel-1").isPresent());
        assertEquals("rel-1", adapter.findById("rel-1").get().id());
        assertEquals(ClientRelationship.Status.ENDED, adapter.findById("rel-1").get().status());
    }
}