package com.mailsent.portfolio.observation.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mailsent.portfolio.observation.application.ReputationObservationRepository;
import com.mailsent.portfolio.observation.domain.ReputationObservation;
import com.mailsent.portfolio.observation.domain.ReputationSource;

/**
 * JpaReputationObservationRepositoryAdapter — production
 * {@link ReputationObservationRepository} backed by PostgreSQL. Active in the
 * non-test profile; the hermetic in-memory adapter backs tests.
 */
@Repository
@Profile("!test")
public class JpaReputationObservationRepositoryAdapter implements ReputationObservationRepository {

    private final JpaReputationObservationRepository jpa;

    public JpaReputationObservationRepositoryAdapter(JpaReputationObservationRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    @Transactional
    public ReputationObservation save(ReputationObservation observation) {
        return toDomain(jpa.saveAndFlush(toEntity(observation)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReputationObservation> findById(String id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReputationObservation> findByDomainId(String domainId) {
        return jpa.findByDomainId(domainId).stream().map(this::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReputationObservation> findByOrganizationId(String organizationId) {
        return jpa.findByOrganizationId(organizationId).stream().map(this::toDomain).toList();
    }

    private ReputationObservationJpaEntity toEntity(ReputationObservation observation) {
        return new ReputationObservationJpaEntity(
            observation.id(),
            observation.organizationId(),
            observation.domainId(),
            observation.domainName(),
            observation.source().name(),
            observation.observedAt(),
            observation.payload());
    }

    private ReputationObservation toDomain(ReputationObservationJpaEntity entity) {
        return ReputationObservation.restore(
            entity.id(),
            entity.organizationId(),
            entity.domainId(),
            entity.domainName(),
            ReputationSource.valueOf(entity.source()),
            entity.observedAt(),
            entity.payload());
    }
}
