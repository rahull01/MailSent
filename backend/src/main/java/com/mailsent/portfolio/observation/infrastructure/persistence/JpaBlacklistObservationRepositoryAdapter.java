package com.mailsent.portfolio.observation.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mailsent.portfolio.observation.application.BlacklistObservationRepository;
import com.mailsent.portfolio.observation.domain.BlacklistObservation;
import com.mailsent.portfolio.observation.domain.BlacklistSource;

/**
 * JpaBlacklistObservationRepositoryAdapter — production
 * {@link BlacklistObservationRepository} backed by PostgreSQL. Active in the
 * non-test profile; the hermetic in-memory adapter backs tests.
 */
@Repository
@Profile("!test")
public class JpaBlacklistObservationRepositoryAdapter implements BlacklistObservationRepository {

    private final JpaBlacklistObservationRepository jpa;

    public JpaBlacklistObservationRepositoryAdapter(JpaBlacklistObservationRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    @Transactional
    public BlacklistObservation save(BlacklistObservation observation) {
        return toDomain(jpa.saveAndFlush(toEntity(observation)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BlacklistObservation> findById(String id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlacklistObservation> findByDomainId(String domainId) {
        return jpa.findByDomainId(domainId).stream().map(this::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlacklistObservation> findByOrganizationId(String organizationId) {
        return jpa.findByOrganizationId(organizationId).stream().map(this::toDomain).toList();
    }

    private BlacklistObservationJpaEntity toEntity(BlacklistObservation observation) {
        return new BlacklistObservationJpaEntity(
            observation.id(),
            observation.organizationId(),
            observation.domainId(),
            observation.domainName(),
            observation.source().name(),
            observation.observedAt(),
            observation.payload());
    }

    private BlacklistObservation toDomain(BlacklistObservationJpaEntity entity) {
        return BlacklistObservation.restore(
            entity.id(),
            entity.organizationId(),
            entity.domainId(),
            entity.domainName(),
            BlacklistSource.valueOf(entity.source()),
            entity.observedAt(),
            entity.payload());
    }
}
