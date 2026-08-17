package com.mailsent.portfolio.observation.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mailsent.portfolio.observation.application.ReverseDnsObservationRepository;
import com.mailsent.portfolio.observation.domain.IpAddressFamily;
import com.mailsent.portfolio.observation.domain.ReverseDnsObservation;

/**
 * JpaReverseDnsObservationRepositoryAdapter — production
 * {@link ReverseDnsObservationRepository} backed by PostgreSQL. Active in the
 * non-test profile; the hermetic in-memory adapter backs tests. PTR-list JSON
 * serialization lives only here.
 */
@Repository
@Profile("!test")
public class JpaReverseDnsObservationRepositoryAdapter implements ReverseDnsObservationRepository {

    private static final TypeReference<List<String>> PTR_TYPE = new TypeReference<>() { };

    private final JpaReverseDnsObservationRepository jpa;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JpaReverseDnsObservationRepositoryAdapter(JpaReverseDnsObservationRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    @Transactional
    public ReverseDnsObservation save(ReverseDnsObservation observation) {
        return toDomain(jpa.saveAndFlush(toEntity(observation)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReverseDnsObservation> findById(String id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReverseDnsObservation> findByDomainId(String domainId) {
        return jpa.findByDomainId(domainId).stream().map(this::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReverseDnsObservation> findByOrganizationId(String organizationId) {
        return jpa.findByOrganizationId(organizationId).stream().map(this::toDomain).toList();
    }

    private ReverseDnsObservationJpaEntity toEntity(ReverseDnsObservation observation) {
        return new ReverseDnsObservationJpaEntity(
            observation.id(),
            observation.organizationId(),
            observation.domainId(),
            observation.domainName(),
            observation.sourceMxHostname(),
            observation.ip(),
            observation.addressFamily().name(),
            writeJson(observation.ptrValues()),
            observation.observedAt());
    }

    private ReverseDnsObservation toDomain(ReverseDnsObservationJpaEntity entity) {
        return ReverseDnsObservation.restore(
            entity.id(),
            entity.organizationId(),
            entity.domainId(),
            entity.domainName(),
            entity.sourceMxHostname(),
            entity.ip(),
            IpAddressFamily.valueOf(entity.addressFamily()),
            readJson(entity.ptrValues()),
            entity.observedAt());
    }

    private String writeJson(List<String> value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to serialize reverse DNS PTR values.", ex);
        }
    }

    private List<String> readJson(String json) {
        try {
            return objectMapper.readValue(json, PTR_TYPE);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to deserialize reverse DNS PTR values.", ex);
        }
    }
}
