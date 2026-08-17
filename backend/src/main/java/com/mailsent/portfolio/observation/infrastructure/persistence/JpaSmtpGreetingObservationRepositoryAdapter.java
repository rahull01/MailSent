package com.mailsent.portfolio.observation.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mailsent.portfolio.observation.application.SmtpGreetingObservationRepository;
import com.mailsent.portfolio.observation.domain.IpAddressFamily;
import com.mailsent.portfolio.observation.domain.SmtpGreetingObservation;
import com.mailsent.portfolio.observation.domain.SmtpProbeOutcome;

/**
 * JpaSmtpGreetingObservationRepositoryAdapter — production
 * {@link SmtpGreetingObservationRepository} backed by PostgreSQL. Active in the
 * non-test profile; the hermetic in-memory adapter backs tests. Greeting and
 * EHLO remain TEXT; probe outcome is stored as VARCHAR.
 */
@Repository
@Profile("!test")
public class JpaSmtpGreetingObservationRepositoryAdapter implements SmtpGreetingObservationRepository {

    private final JpaSmtpGreetingObservationRepository jpa;

    public JpaSmtpGreetingObservationRepositoryAdapter(JpaSmtpGreetingObservationRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    @Transactional
    public SmtpGreetingObservation save(SmtpGreetingObservation observation) {
        return toDomain(jpa.saveAndFlush(toEntity(observation)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SmtpGreetingObservation> findById(String id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SmtpGreetingObservation> findByDomainId(String domainId) {
        return jpa.findByDomainId(domainId).stream().map(this::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SmtpGreetingObservation> findByOrganizationId(String organizationId) {
        return jpa.findByOrganizationId(organizationId).stream().map(this::toDomain).toList();
    }

    private SmtpGreetingObservationJpaEntity toEntity(SmtpGreetingObservation observation) {
        return new SmtpGreetingObservationJpaEntity(
            observation.id(),
            observation.organizationId(),
            observation.domainId(),
            observation.domainName(),
            observation.sourceMxHostname(),
            observation.ip(),
            observation.addressFamily().name(),
            observation.probeOutcome().name(),
            observation.rawGreeting(),
            observation.rawEhlo(),
            observation.observedAt());
    }

    private SmtpGreetingObservation toDomain(SmtpGreetingObservationJpaEntity entity) {
        return SmtpGreetingObservation.restore(
            entity.id(),
            entity.organizationId(),
            entity.domainId(),
            entity.domainName(),
            entity.sourceMxHostname(),
            entity.ip(),
            IpAddressFamily.valueOf(entity.addressFamily()),
            SmtpProbeOutcome.valueOf(entity.probeOutcome()),
            entity.rawGreeting(),
            entity.rawEhlo(),
            entity.observedAt());
    }
}
