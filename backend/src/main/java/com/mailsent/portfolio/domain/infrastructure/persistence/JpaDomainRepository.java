package com.mailsent.portfolio.domain.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JpaDomainRepository — Spring Data repository over {@link DomainJpaEntity}. A domain is
 * identified by its stable identity, looked up by its canonical name, and scanned by its
 * owning organization. No reassignment or sender-resolution queries are defined (ORG-003
 * scope).
 */
public interface JpaDomainRepository extends JpaRepository<DomainJpaEntity, String> {

    Optional<DomainJpaEntity> findByName(String name);

    List<DomainJpaEntity> findByOrganizationId(String organizationId);

    boolean existsByName(String name);
}