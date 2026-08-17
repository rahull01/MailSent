package com.mailsent.portfolio.observation.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mailsent.portfolio.observation.application.DnsHealthObservationRepository;
import com.mailsent.portfolio.observation.domain.DnsHealthObservation;
import com.mailsent.portfolio.observation.domain.DnssecObservation;
import com.mailsent.portfolio.observation.domain.PropagationObservation;
import com.mailsent.portfolio.observation.domain.TtlObservation;

/**
 * JpaDnsHealthObservationRepositoryAdapter — production
 * {@link DnsHealthObservationRepository} backed by PostgreSQL. Active in the
 * non-test profile; the hermetic in-memory adapter backs tests. TTL, DNSSEC,
 * and propagation JSON serialization lives only here.
 */
@Repository
@Profile("!test")
public class JpaDnsHealthObservationRepositoryAdapter implements DnsHealthObservationRepository {

    private static final TypeReference<List<TtlObservation>> TTL_TYPE = new TypeReference<>() { };
    private static final TypeReference<List<DnssecObservation>> DNSSEC_TYPE = new TypeReference<>() { };
    private static final TypeReference<List<PropagationObservation>> PROPAGATION_TYPE = new TypeReference<>() { };

    private final JpaDnsHealthObservationRepository jpa;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JpaDnsHealthObservationRepositoryAdapter(JpaDnsHealthObservationRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    @Transactional
    public DnsHealthObservation save(DnsHealthObservation observation) {
        return toDomain(jpa.saveAndFlush(toEntity(observation)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DnsHealthObservation> findById(String id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DnsHealthObservation> findByDomainId(String domainId) {
        return jpa.findByDomainId(domainId).stream().map(this::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DnsHealthObservation> findByOrganizationId(String organizationId) {
        return jpa.findByOrganizationId(organizationId).stream().map(this::toDomain).toList();
    }

    private DnsHealthObservationJpaEntity toEntity(DnsHealthObservation observation) {
        return new DnsHealthObservationJpaEntity(
            observation.id(),
            observation.organizationId(),
            observation.domainId(),
            observation.domainName(),
            observation.observedAt(),
            writeJson(observation.ttlObservations()),
            writeJson(observation.dnssecObservations()),
            writeJson(observation.propagationObservations()));
    }

    private DnsHealthObservation toDomain(DnsHealthObservationJpaEntity entity) {
        return DnsHealthObservation.restore(
            entity.id(),
            entity.organizationId(),
            entity.domainId(),
            entity.domainName(),
            entity.observedAt(),
            readJson(entity.ttlObservations(), TTL_TYPE),
            readJson(entity.dnssecObservations(), DNSSEC_TYPE),
            readJson(entity.propagationObservations(), PROPAGATION_TYPE));
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to serialize DNS health observation records.", ex);
        }
    }

    private <T> T readJson(String json, TypeReference<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to deserialize DNS health observation records.", ex);
        }
    }
}
