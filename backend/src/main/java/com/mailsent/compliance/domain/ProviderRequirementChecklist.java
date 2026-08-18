package com.mailsent.compliance.domain;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * ProviderRequirementChecklist — Evaluated checklist of provider requirements for a domain (COMP-001 / FUN-C1 / FUN-C2).
 */
public class ProviderRequirementChecklist {

    private final String id;
    private final String organizationId;
    private final String domainId;
    private final MailboxProvider provider;
    private final ReadinessLevel overallReadiness;
    private final List<ProviderRequirement> requirements;
    private final Instant assessedAt;

    private ProviderRequirementChecklist(String id, String organizationId, String domainId,
                                         MailboxProvider provider, ReadinessLevel overallReadiness,
                                         List<ProviderRequirement> requirements, Instant assessedAt) {
        this.id = Objects.requireNonNull(id);
        this.organizationId = Objects.requireNonNull(organizationId);
        this.domainId = Objects.requireNonNull(domainId);
        this.provider = Objects.requireNonNull(provider);
        this.overallReadiness = Objects.requireNonNull(overallReadiness);
        this.requirements = Collections.unmodifiableList(Objects.requireNonNull(requirements));
        this.assessedAt = Objects.requireNonNull(assessedAt);
    }

    public static ProviderRequirementChecklist create(String organizationId, String domainId,
                                                      MailboxProvider provider,
                                                      List<ProviderRequirement> requirements,
                                                      Instant assessedAt) {
        if (organizationId.isBlank()) {
            throw new IllegalArgumentException("Organization ID must not be blank.");
        }
        if (domainId.isBlank()) {
            throw new IllegalArgumentException("Domain ID must not be blank.");
        }
        if (requirements == null || requirements.isEmpty()) {
            throw new IllegalArgumentException("Requirements list must not be empty.");
        }

        // Determine overall readiness
        boolean hasUnsatisfied = requirements.stream()
                .anyMatch(r -> r.status() == RequirementStatus.NOT_SATISFIED);
        boolean hasWarning = requirements.stream()
                .anyMatch(r -> r.status() == RequirementStatus.WARNING || r.status() == RequirementStatus.UNKNOWN);

        ReadinessLevel readiness = ReadinessLevel.READY;
        if (hasUnsatisfied) {
            readiness = ReadinessLevel.NOT_READY;
        } else if (hasWarning) {
            readiness = ReadinessLevel.AT_RISK;
        }

        return new ProviderRequirementChecklist(
                UUID.randomUUID().toString(),
                organizationId,
                domainId,
                provider,
                readiness,
                requirements,
                assessedAt
        );
    }

    public String id() { return id; }
    public String organizationId() { return organizationId; }
    public String domainId() { return domainId; }
    public MailboxProvider provider() { return provider; }
    public ReadinessLevel overallReadiness() { return overallReadiness; }
    public List<ProviderRequirement> requirements() { return requirements; }
    public Instant assessedAt() { return assessedAt; }
}
