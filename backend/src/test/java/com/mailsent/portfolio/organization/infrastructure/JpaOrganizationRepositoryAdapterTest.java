package com.mailsent.portfolio.organization.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.mailsent.portfolio.organization.domain.Organization;
import com.mailsent.portfolio.organization.infrastructure.persistence.JpaOrganizationRepository;
import com.mailsent.portfolio.organization.infrastructure.persistence.JpaOrganizationRepositoryAdapter;
import com.mailsent.portfolio.organization.infrastructure.persistence.OrganizationJpaEntity;

/**
 * Tests the production {@link JpaOrganizationRepositoryAdapter} in isolation with a
 * mocked Spring Data repository ({@code @Profile("!test")}).
 */
class JpaOrganizationRepositoryAdapterTest {

    private final JpaOrganizationRepository jpa = mock(JpaOrganizationRepository.class);
    private final JpaOrganizationRepositoryAdapter adapter = new JpaOrganizationRepositoryAdapter(jpa);
    private final Instant now = Instant.parse("2026-08-08T10:00:00Z");

    @Test
    void saveMapsAndFlushesDomainToEntity() {
        Organization org = Organization.create("Acme", now);
        OrganizationJpaEntity entity = new OrganizationJpaEntity(
            org.id(), org.name(), org.createdAt(), org.status(), org.closedAt());
        when(jpa.saveAndFlush(any())).thenReturn(entity);

        Organization saved = adapter.save(org);

        assertEquals(org.id(), saved.id());
        assertEquals("Acme", saved.name());
        assertEquals(Organization.Status.ACTIVE, saved.status());
    }

    @Test
    void findByIdReturnsByStableIdentity() {
        OrganizationJpaEntity entity = new OrganizationJpaEntity(
            "org-1", "Acme", now, Organization.Status.CLOSED, now.plusSeconds(10));
        when(jpa.findById("org-1")).thenReturn(Optional.of(entity));

        Optional<Organization> found = adapter.findById("org-1");

        assertTrue(found.isPresent());
        assertEquals("org-1", found.get().id());
        assertEquals(Organization.Status.CLOSED, found.get().status());
    }

    @Test
    void findAllListsPersistedOrganizations() {
        OrganizationJpaEntity e = new OrganizationJpaEntity(
            "org-1", "Acme", now, Organization.Status.ACTIVE, null);
        when(jpa.findAll()).thenReturn(List.of(e));

        assertEquals(1, adapter.findAll().size());
        assertEquals("Acme", adapter.findAll().get(0).name());
    }
}