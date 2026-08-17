package com.mailsent.identity.consent.infrastructure.persistence;

import com.mailsent.identity.consent.application.ConsentRepository;
import com.mailsent.identity.consent.domain.ActionType;
import com.mailsent.identity.consent.domain.Consent;
import com.mailsent.identity.consent.domain.Status;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
@Profile("!test")
public class JpaConsentRepositoryAdapter implements ConsentRepository {

    private final JpaConsentRepository jpaRepository;

    public JpaConsentRepositoryAdapter(JpaConsentRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(Consent consent) {
        ConsentJpaEntity entity = new ConsentJpaEntity(
            consent.getId(),
            consent.getOrganizationId(),
            consent.getDomainId(),
            consent.getGrantedByUserId(),
            consent.getActionType().name(),
            consent.getStatus().name(),
            consent.getGrantedAt(),
            consent.getRevokedAt()
        );
        jpaRepository.save(entity);
    }

    @Override
    public Optional<Consent> findById(String id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Consent> findByDomainIdAndActionTypeAndStatus(String domainId, ActionType actionType, Status status) {
        return jpaRepository.findByDomainIdAndActionTypeAndStatus(domainId, actionType.name(), status.name())
            .map(this::toDomain);
    }

    private Consent toDomain(ConsentJpaEntity entity) {
        return Consent.restore(
            entity.id(),
            entity.organizationId(),
            entity.domainId(),
            entity.grantedByUserId(),
            ActionType.valueOf(entity.actionType()),
            Status.valueOf(entity.status()),
            entity.grantedAt(),
            entity.revokedAt()
        );
    }
}
