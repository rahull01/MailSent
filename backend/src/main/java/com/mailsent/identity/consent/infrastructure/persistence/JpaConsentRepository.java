package com.mailsent.identity.consent.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface JpaConsentRepository extends JpaRepository<ConsentJpaEntity, String> {
    Optional<ConsentJpaEntity> findByDomainIdAndActionTypeAndStatus(String domainId, String actionType, String status);
}
