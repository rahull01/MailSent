package com.mailsent.portfolio.domain.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import com.mailsent.portfolio.domain.domain.Domain;
import com.mailsent.portfolio.domain.domain.DomainAlreadyExistsException;
import com.mailsent.portfolio.domain.infrastructure.persistence.DomainJpaEntity;
import com.mailsent.portfolio.domain.infrastructure.persistence.JpaDomainRepository;
import com.mailsent.portfolio.domain.infrastructure.persistence.JpaDomainRepositoryAdapter;

/**
 * Tests the production {@link JpaDomainRepositoryAdapter} in isolation with a mocked
 * Spring Data repository ({@code @Profile("!test")}).
 */
class JpaDomainRepositoryAdapterTest {

    private final JpaDomainRepository jpa = mock(JpaDomainRepository.class);
    private final JpaDomainRepositoryAdapter adapter = new JpaDomainRepositoryAdapter(jpa);
    private final Instant now = Instant.parse("2026-08-08T10:00:00Z");

    @Test
    void saveMapsAndFlushesDomainToEntity() {
        Domain domain = Domain.register("org-1", "example.com", now);
        DomainJpaEntity entity = new DomainJpaEntity(
            domain.id(), domain.organizationId(), domain.name(), domain.createdAt());
        when(jpa.saveAndFlush(any())).thenReturn(entity);

        Domain saved = adapter.save(domain);

        assertEquals(domain.id(), saved.id());
        assertEquals("org-1", saved.organizationId());
        assertEquals("example.com", saved.name());
    }

    @Test
    void findByIdReturnsByStableIdentity() {
        DomainJpaEntity entity = new DomainJpaEntity(
            "dom-1", "org-1", "example.com", now);
        when(jpa.findById("dom-1")).thenReturn(Optional.of(entity));

        Optional<Domain> found = adapter.findById("dom-1");

        assertTrue(found.isPresent());
        assertEquals("dom-1", found.get().id());
        assertEquals("org-1", found.get().organizationId());
    }

    @Test
    void findByNameReturnsByCanonicalName() {
        DomainJpaEntity entity = new DomainJpaEntity(
            "dom-1", "org-1", "example.com", now);
        when(jpa.findByName("example.com")).thenReturn(Optional.of(entity));

        assertTrue(adapter.findByName("example.com").isPresent());
        assertEquals("example.com", adapter.findByName("example.com").get().name());
    }

    @Test
    void findByOrganizationIdListsOwnedDomains() {
        DomainJpaEntity e = new DomainJpaEntity("dom-1", "org-1", "example.com", now);
        when(jpa.findByOrganizationId("org-1")).thenReturn(List.of(e));

        assertEquals(1, adapter.findByOrganizationId("org-1").size());
        assertEquals("example.com", adapter.findByOrganizationId("org-1").get(0).name());
    }

    @Test
    void findAllMapsSpringDataFindAllToDomain() {
        DomainJpaEntity a = new DomainJpaEntity("dom-1", "org-1", "example.com", now);
        DomainJpaEntity b = new DomainJpaEntity("dom-2", "org-2", "other.com", now);
        when(jpa.findAll()).thenReturn(List.of(a, b));

        List<Domain> all = adapter.findAll();

        assertEquals(2, all.size());
        assertEquals("example.com", all.get(0).name());
        assertEquals("org-2", all.get(1).organizationId());
        assertEquals("dom-2", all.get(1).id());
    }

    @Test
    void existsByNameDelegatesToSpringData() {
        when(jpa.existsByName("example.com")).thenReturn(true);
        assertTrue(adapter.existsByName("example.com"));
    }

    @Test
    void saveMapsUniqueViolationToDomainAlreadyExistsException() {
        Domain domain = Domain.register("org-1", "example.com", now);
        // The uk_domains_name constraint rejected a concurrent/duplicate insert.
        when(jpa.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("uk_domains_name"));

        assertThrows(DomainAlreadyExistsException.class, () -> adapter.save(domain));
    }
}
