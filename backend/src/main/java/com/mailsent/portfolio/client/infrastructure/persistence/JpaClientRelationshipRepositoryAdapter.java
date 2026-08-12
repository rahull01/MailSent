package com.mailsent.portfolio.client.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mailsent.portfolio.client.application.ClientRelationshipRepository;
import com.mailsent.portfolio.client.domain.ClientRelationship;

/**
 * JpaClientRelationshipRepositoryAdapter — production {@link ClientRelationshipRepository}
 * backed by PostgreSQL. Active in the non-test profile; the hermetic in-memory adapter backs
 * tests. The {@link Transactional} writer keeps relationship formation and ending atomic.
 */
@Repository
@Profile("!test")
public class JpaClientRelationshipRepositoryAdapter implements ClientRelationshipRepository {

    private final JpaClientRelationshipRepository jpa;

    public JpaClientRelationshipRepositoryAdapter(JpaClientRelationshipRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    @Transactional
    public ClientRelationship save(ClientRelationship relationship) {
        return toDomain(jpa.saveAndFlush(toEntity(relationship)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ClientRelationship> findById(String id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientRelationship> findByAgencyOrganizationId(String agencyOrganizationId) {
        return jpa.findByAgencyOrganizationId(agencyOrganizationId).stream().map(this::toDomain).toList();
    }

    private ClientRelationshipJpaEntity toEntity(ClientRelationship relationship) {
        return new ClientRelationshipJpaEntity(relationship.id(), relationship.agencyOrganizationId(),
            relationship.clientOrganizationId(), relationship.formedAt(), relationship.status(),
            relationship.endedAt());
    }

    private ClientRelationship toDomain(ClientRelationshipJpaEntity entity) {
        return ClientRelationship.restore(entity.id(), entity.agencyOrganizationId(),
            entity.clientOrganizationId(), entity.formedAt(), entity.status(), entity.endedAt());
    }
}