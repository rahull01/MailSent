package com.mailsent.portfolio.observation.infrastructure.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JpaSmtpGreetingObservationRepository — Spring Data repository over
 * {@link SmtpGreetingObservationJpaEntity}. Snapshots are identified by stable
 * identity and listed by domain or owning organization. No update or delete
 * queries are defined.
 */
public interface JpaSmtpGreetingObservationRepository
        extends JpaRepository<SmtpGreetingObservationJpaEntity, String> {

    List<SmtpGreetingObservationJpaEntity> findByDomainId(String domainId);

    List<SmtpGreetingObservationJpaEntity> findByOrganizationId(String organizationId);
}
