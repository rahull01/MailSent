package com.mailsent.portfolio.organization.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JpaOrganizationRepository — Spring Data repository over {@link OrganizationJpaEntity}.
 * The Organization is identified by its stable identity; no cross-entity ownership or
 * membership queries are defined (ORG-001 scope).</p>
 */
public interface JpaOrganizationRepository extends JpaRepository<OrganizationJpaEntity, String> {
}