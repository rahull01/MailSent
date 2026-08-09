package com.mailsent.portfolio.organization.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mailsent.portfolio.organization.application.OrganizationRepository;
import com.mailsent.portfolio.organization.domain.Organization;

/**
 * JpaOrganizationRepositoryAdapter — production {@link OrganizationRepository} backed by
 * PostgreSQL. Active in the non-test profile; the hermetic in-memory adapter backs tests.
 * The {@link Transactional} writers keep organization creation and closure atomic.</p>
 */
@Repository
@Profile("!test")
public class JpaOrganizationRepositoryAdapter implements OrganizationRepository {

    private final JpaOrganizationRepository jpa;

    public JpaOrganizationRepositoryAdapter(JpaOrganizationRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    @Transactional
    public Organization save(Organization org) {
        return toDomain(jpa.saveAndFlush(toEntity(org)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Organization> findById(String id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Organization> findAll() {
        return jpa.findAll().stream().map(this::toDomain).toList();
    }

    private OrganizationJpaEntity toEntity(Organization org) {
        return new OrganizationJpaEntity(org.id(), org.name(), org.createdAt(), org.status(), org.closedAt());
    }

    private Organization toDomain(OrganizationJpaEntity e) {
        return Organization.restore(e.id(), e.name(), e.createdAt(), e.status(), e.closedAt());
    }
}