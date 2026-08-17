package com.mailsent.portfolio.observation.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mailsent.portfolio.observation.application.HealthSignalRepository;
import com.mailsent.portfolio.observation.domain.HealthIndication;
import com.mailsent.portfolio.observation.domain.HealthSignal;

/**
 * JpaHealthSignalRepositoryAdapter — production {@link HealthSignalRepository}
 * backed by PostgreSQL. Active in the non-test profile; the hermetic in-memory
 * adapter backs tests. Lineage id-list JSON serialization lives only here.
 */
@Repository
@Profile("!test")
public class JpaHealthSignalRepositoryAdapter implements HealthSignalRepository {

    private static final TypeReference<List<String>> ID_LIST_TYPE = new TypeReference<>() { };

    private final JpaHealthSignalRepository jpa;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JpaHealthSignalRepositoryAdapter(JpaHealthSignalRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    @Transactional
    public HealthSignal save(HealthSignal signal) {
        return toDomain(jpa.saveAndFlush(toEntity(signal)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<HealthSignal> findById(String id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HealthSignal> findByDomainId(String domainId) {
        return jpa.findByDomainId(domainId).stream().map(this::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HealthSignal> findByOrganizationId(String organizationId) {
        return jpa.findByOrganizationId(organizationId).stream().map(this::toDomain).toList();
    }

    private HealthSignalJpaEntity toEntity(HealthSignal signal) {
        return new HealthSignalJpaEntity(
            signal.id(),
            signal.organizationId(),
            signal.domainId(),
            signal.domainName(),
            signal.indication().token(),
            signal.observationEvidenceId(),
            writeJson(signal.dmarcAggregateEvidenceIds()),
            writeJson(signal.reputationObservationIds()),
            signal.observedAt());
    }

    private HealthSignal toDomain(HealthSignalJpaEntity entity) {
        return HealthSignal.restore(
            entity.id(),
            entity.organizationId(),
            entity.domainId(),
            entity.domainName(),
            HealthIndication.fromToken(entity.indication()),
            entity.observationEvidenceId(),
            readJson(entity.dmarcAggregateEvidenceIds()),
            readJson(entity.reputationObservationIds()),
            entity.observedAt());
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to serialize Health Signal lineage ids.", ex);
        }
    }

    private List<String> readJson(String json) {
        try {
            return objectMapper.readValue(json, ID_LIST_TYPE);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to deserialize Health Signal lineage ids.", ex);
        }
    }
}
