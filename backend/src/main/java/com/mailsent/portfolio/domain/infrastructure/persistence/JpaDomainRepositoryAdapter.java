package com.mailsent.portfolio.domain.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mailsent.portfolio.domain.application.DomainRepository;
import com.mailsent.portfolio.domain.domain.Domain;
import com.mailsent.portfolio.domain.domain.DomainAlreadyExistsException;

/**
 * JpaDomainRepositoryAdapter — production {@link DomainRepository} backed by PostgreSQL.
 * Active in the non-test profile; the hermetic in-memory adapter backs tests. The
 * {@link Transactional} writer keeps domain registration atomic.
 */
@Repository
@Profile("!test")
public class JpaDomainRepositoryAdapter implements DomainRepository {

    private final JpaDomainRepository jpa;

    public JpaDomainRepositoryAdapter(JpaDomainRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    @Transactional
    public Domain save(Domain domain) {
        try {
            return toDomain(jpa.saveAndFlush(toEntity(domain)));
        } catch (DataIntegrityViolationException ex) {
            // The uk_domains_name constraint rejected a concurrent/duplicate insert.
            throw new DomainAlreadyExistsException(domain.name());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Domain> findById(String id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Domain> findByName(String name) {
        return jpa.findByName(name).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Domain> findByOrganizationId(String organizationId) {
        return jpa.findByOrganizationId(organizationId).stream().map(this::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Domain> findAll() {
        return jpa.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return jpa.existsByName(name);
    }

    private DomainJpaEntity toEntity(Domain domain) {
        return new DomainJpaEntity(domain.id(), domain.organizationId(), domain.name(), domain.createdAt());
    }

    private Domain toDomain(DomainJpaEntity entity) {
        return Domain.restore(entity.id(), entity.organizationId(), entity.name(), entity.createdAt());
    }
}