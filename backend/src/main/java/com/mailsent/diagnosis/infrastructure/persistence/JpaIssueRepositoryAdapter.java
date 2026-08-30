package com.mailsent.diagnosis.infrastructure.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mailsent.diagnosis.domain.Issue;
import com.mailsent.diagnosis.domain.IssueRepository;
import com.mailsent.diagnosis.domain.IssueStatus;
import com.mailsent.diagnosis.domain.RootCause;
import com.mailsent.diagnosis.domain.Sender;

@Repository
@Profile("!test")
public class JpaIssueRepositoryAdapter implements IssueRepository {


    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() {};
    private final SpringDataIssueRepository repository;
    private final ObjectMapper objectMapper;

    public JpaIssueRepositoryAdapter(SpringDataIssueRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    public Issue save(Issue issue) {
        IssueJpaEntity entity = toEntity(issue);
        IssueJpaEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Issue> findById(String id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Issue> findByOrganizationId(String organizationId) {
        return repository.findByOrganizationId(organizationId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Issue> findByOrganizationIdAndDomainId(String organizationId, String domainId) {
        return repository.findByOrganizationIdAndDomainId(organizationId, domainId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<Issue> findActiveIssue(String organizationId, String domainId, RootCause rootCause, String providerId) {
        return repository.findActiveIssue(organizationId, domainId, rootCause.name(), providerId)
                .map(this::toDomain);
    }

    private IssueJpaEntity toEntity(Issue issue) {
        String originEvidenceJson = serializeList(issue.originDmarcAggregateEvidenceIds());
        String latestEvidenceJson = serializeList(issue.latestDmarcAggregateEvidenceIds());

        String providerId = issue.sender() != null ? issue.sender().providerId() : null;
        String providerName = issue.sender() != null ? issue.sender().providerName() : null;

        return new IssueJpaEntity(
                issue.id(),
                issue.organizationId(),
                issue.domainId(),
                issue.domainName(),
                issue.rootCause().name(),
                providerId,
                providerName,
                issue.status().name(),
                issue.originHealthSignalId(),
                originEvidenceJson,
                issue.latestHealthSignalId(),
                latestEvidenceJson,
                issue.discoveredAt(),
                issue.resolvedAt(),
                issue.closedAt(),
                issue.updatedAt()
        );
    }

    private Issue toDomain(IssueJpaEntity entity) {
        List<String> originEvidenceIds = deserializeList(entity.getOriginDmarcAggregateEvidenceIds());
        List<String> latestEvidenceIds = deserializeList(entity.getLatestDmarcAggregateEvidenceIds());

        Sender sender = null;
        if (entity.getProviderId() != null && entity.getProviderName() != null) {
            sender = new Sender(entity.getProviderId(), entity.getProviderName());
        }

        return Issue.restore(
                entity.getId(),
                entity.getOrganizationId(),
                entity.getDomainId(),
                entity.getDomainName(),
                RootCause.valueOf(entity.getRootCause()),
                sender,
                entity.getOriginHealthSignalId(),
                originEvidenceIds,
                entity.getLatestHealthSignalId(),
                latestEvidenceIds,
                IssueStatus.valueOf(entity.getStatus()),
                entity.getDiscoveredAt(),
                entity.getResolvedAt(),
                entity.getClosedAt(),
                entity.getUpdatedAt()
        );
    }

    private String serializeList(List<String> list) {
        try {
            return objectMapper.writeValueAsString(list != null ? list : List.of());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize evidence ids list", e);
        }
    }

    private List<String> deserializeList(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, STRING_LIST_TYPE);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize evidence ids list", e);
        }
    }
}
