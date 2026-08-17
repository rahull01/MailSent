package com.mailsent.portfolio.observation.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mailsent.portfolio.observation.application.DmarcAggregateEvidenceRepository;
import com.mailsent.portfolio.observation.application.DmarcAggregateStoreResult;
import com.mailsent.portfolio.observation.application.DuplicateDmarcAggregateOccupancyException;
import com.mailsent.portfolio.observation.domain.DmarcAggregateEvidence;

/**
 * JpaDmarcAggregateEvidenceRepositoryAdapter — production
 * {@link DmarcAggregateEvidenceRepository} backed by PostgreSQL. Active in the
 * non-test profile; the hermetic in-memory adapter backs tests.
 * {@code dkim_domains} JSON serialization lives only here.
 */
@Repository
@Profile("!test")
public class JpaDmarcAggregateEvidenceRepositoryAdapter implements DmarcAggregateEvidenceRepository {

    private static final TypeReference<List<String>> DKIM_DOMAINS_TYPE = new TypeReference<>() { };

    private final JpaDmarcAggregateEvidenceRepository jpa;
    private final JpaDmarcAggregateReportOccupancyRepository occupancy;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JpaDmarcAggregateEvidenceRepositoryAdapter(JpaDmarcAggregateEvidenceRepository jpa,
                                                      JpaDmarcAggregateReportOccupancyRepository occupancy) {
        this.jpa = jpa;
        this.occupancy = occupancy;
    }

    @Override
    @Transactional
    public DmarcAggregateEvidence save(DmarcAggregateEvidence evidence) {
        return toDomain(jpa.saveAndFlush(toEntity(evidence)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DmarcAggregateEvidence> findById(String id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DmarcAggregateEvidence> findByDomainId(String domainId) {
        return jpa.findByDomainId(domainId).stream().map(this::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DmarcAggregateEvidence> findByOrganizationId(String organizationId) {
        return jpa.findByOrganizationId(organizationId).stream().map(this::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DmarcAggregateEvidence> findByDomainIdAndReportId(String domainId, String reportId) {
        return jpa.findByDomainIdAndReportId(domainId, reportId).stream().map(this::toDomain).toList();
    }

    @Override
    @Transactional
    public DmarcAggregateStoreResult persistNewSnapshotSet(List<DmarcAggregateEvidence> snapshots) {
        if (snapshots == null || snapshots.isEmpty()) {
            return new DmarcAggregateStoreResult(List.of(), false);
        }
        DmarcAggregateEvidence first = snapshots.get(0);
        List<DmarcAggregateEvidence> existing =
            findByDomainIdAndReportId(first.domainId(), first.reportId());
        if (!existing.isEmpty()) {
            return new DmarcAggregateStoreResult(existing, false);
        }
        try {
            occupancy.saveAndFlush(new DmarcAggregateReportOccupancyJpaEntity(
                first.domainId(), first.reportId(), first.organizationId()));
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateDmarcAggregateOccupancyException(first.domainId(), first.reportId(), ex);
        }
        List<DmarcAggregateEvidence> stored = new java.util.ArrayList<>();
        for (DmarcAggregateEvidence snapshot : snapshots) {
            stored.add(toDomain(jpa.save(toEntity(snapshot))));
        }
        jpa.flush();
        return new DmarcAggregateStoreResult(List.copyOf(stored), true);
    }

    private DmarcAggregateEvidenceJpaEntity toEntity(DmarcAggregateEvidence evidence) {
        return new DmarcAggregateEvidenceJpaEntity(
            evidence.id(),
            evidence.organizationId(),
            evidence.domainId(),
            evidence.domainName(),
            evidence.receiptId(),
            evidence.reportId(),
            evidence.sourceIp(),
            evidence.utcDay(),
            evidence.count(),
            evidence.spfResult(),
            evidence.dkimResult(),
            evidence.spfAligned(),
            evidence.dkimAligned(),
            evidence.envelopeFrom(),
            evidence.headerFrom(),
            writeJson(evidence.dkimDomains()),
            evidence.disposition());
    }

    private DmarcAggregateEvidence toDomain(DmarcAggregateEvidenceJpaEntity entity) {
        return DmarcAggregateEvidence.restore(
            entity.id(),
            entity.organizationId(),
            entity.domainId(),
            entity.domainName(),
            entity.receiptId(),
            entity.reportId(),
            entity.sourceIp(),
            entity.utcDay(),
            entity.messageCount(),
            entity.spfResult(),
            entity.dkimResult(),
            entity.spfAligned(),
            entity.dkimAligned(),
            entity.envelopeFrom(),
            entity.headerFrom(),
            readJson(entity.dkimDomains()),
            entity.disposition());
    }

    private String writeJson(List<String> value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to serialize dkim_domains.", ex);
        }
    }

    private List<String> readJson(String json) {
        try {
            return objectMapper.readValue(json, DKIM_DOMAINS_TYPE);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to deserialize dkim_domains.", ex);
        }
    }
}
