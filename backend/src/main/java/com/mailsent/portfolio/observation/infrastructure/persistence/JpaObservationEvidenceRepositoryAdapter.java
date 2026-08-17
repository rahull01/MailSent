package com.mailsent.portfolio.observation.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mailsent.portfolio.observation.application.ObservationEvidenceRepository;
import com.mailsent.portfolio.observation.domain.MxRecord;
import com.mailsent.portfolio.observation.domain.ObservationEvidence;

/**
 * JpaObservationEvidenceRepositoryAdapter — production {@link ObservationEvidenceRepository}
 * backed by PostgreSQL. Active in the non-test profile; the hermetic in-memory adapter
 * backs tests. MX and TXT JSON serialization lives only here.
 */
@Repository
@Profile("!test")
public class JpaObservationEvidenceRepositoryAdapter implements ObservationEvidenceRepository {

    private static final TypeReference<List<MxRecord>> MX_TYPE = new TypeReference<>() { };
    private static final TypeReference<List<String>> TXT_TYPE = new TypeReference<>() { };

    private final JpaObservationEvidenceRepository jpa;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JpaObservationEvidenceRepositoryAdapter(JpaObservationEvidenceRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    @Transactional
    public ObservationEvidence save(ObservationEvidence evidence) {
        return toDomain(jpa.saveAndFlush(toEntity(evidence)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ObservationEvidence> findById(String id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ObservationEvidence> findByDomainId(String domainId) {
        return jpa.findByDomainId(domainId).stream().map(this::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ObservationEvidence> findByOrganizationId(String organizationId) {
        return jpa.findByOrganizationId(organizationId).stream().map(this::toDomain).toList();
    }

    private ObservationEvidenceJpaEntity toEntity(ObservationEvidence evidence) {
        return new ObservationEvidenceJpaEntity(
            evidence.id(),
            evidence.organizationId(),
            evidence.domainId(),
            evidence.domainName(),
            evidence.observedAt(),
            writeJson(evidence.mxRecords()),
            writeJson(evidence.txtRecords()),
            evidence.spfRecord().orElse(null));
    }

    private ObservationEvidence toDomain(ObservationEvidenceJpaEntity entity) {
        return ObservationEvidence.restore(
            entity.id(),
            entity.organizationId(),
            entity.domainId(),
            entity.domainName(),
            entity.observedAt(),
            readJson(entity.mxRecords(), MX_TYPE),
            readJson(entity.txtRecords(), TXT_TYPE),
            entity.spfRecord());
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to serialize observation records.", ex);
        }
    }

    private <T> T readJson(String json, TypeReference<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to deserialize observation records.", ex);
        }
    }
}
