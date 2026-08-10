package com.mailsent.identity.team.infrastructure.persistence;

import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mailsent.identity.team.application.MembershipRepository;
import com.mailsent.identity.team.domain.Membership;
import com.mailsent.identity.team.domain.MembershipAlreadyExistsException;

/**
 * JpaMembershipRepositoryAdapter — production {@link MembershipRepository} backed by
 * PostgreSQL. Active in the non-test profile; the hermetic in-memory adapter backs tests.
 * The {@link Transactional} writer keeps membership establishment atomic. A concurrent
 * duplicate insert surfaces as a {@link DataIntegrityViolationException} from the
 * {@code uk_memberships_user_organization} constraint and is mapped here to
 * {@link MembershipAlreadyExistsException} so the use case stays unaware of storage.
 */
@Repository
@Profile("!test")
public class JpaMembershipRepositoryAdapter implements MembershipRepository {

    private final JpaMembershipRepository jpa;

    public JpaMembershipRepositoryAdapter(JpaMembershipRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    @Transactional
    public Membership save(Membership membership) {
        try {
            return toDomain(jpa.saveAndFlush(toEntity(membership)));
        } catch (DataIntegrityViolationException ex) {
            // The uk_memberships_user_organization constraint rejected a concurrent/duplicate insert.
            throw new MembershipAlreadyExistsException(membership.userId(), membership.organizationId());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUserAndOrganization(String userId, String organizationId) {
        return jpa.existsByUserIdAndOrganizationId(userId, organizationId);
    }

    private MembershipJpaEntity toEntity(Membership membership) {
        return new MembershipJpaEntity(membership.id(), membership.userId(), membership.organizationId(),
            membership.role(), membership.createdAt());
    }

    private Membership toDomain(MembershipJpaEntity entity) {
        return Membership.restore(entity.id(), entity.userId(), entity.organizationId(), entity.role(),
            entity.createdAt());
    }
}