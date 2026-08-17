package com.mailsent.portfolio.domain.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.mailsent.portfolio.domain.domain.Domain;

/**
 * INF-004: {@link InMemoryDomainRepository#findAll()} returns a copy of every
 * stored domain.
 */
class InMemoryDomainRepositoryFindAllTest {

    private static final Instant NOW = Instant.parse("2026-08-08T10:00:00Z");

    @Test
    void findAllReturnsCopyOfEveryStoredDomain() {
        InMemoryDomainRepository repository = new InMemoryDomainRepository();
        Domain first = Domain.register("org-1", "example.com", NOW);
        Domain second = Domain.register("org-2", "other.com", NOW);
        repository.save(first);
        repository.save(second);

        List<Domain> all = repository.findAll();

        assertEquals(2, all.size());
        assertTrue(all.stream().anyMatch(d -> d.id().equals(first.id())));
        assertTrue(all.stream().anyMatch(d -> d.id().equals(second.id())));

        repository.save(Domain.register("org-3", "third.com", NOW));
        assertEquals(2, all.size(), "findAll returns a copy, not a live view");
        assertThrows(UnsupportedOperationException.class, () -> all.add(first));
    }

    @Test
    void findAllIsEmptyWhenNothingIsRegistered() {
        assertTrue(new InMemoryDomainRepository().findAll().isEmpty());
    }
}
