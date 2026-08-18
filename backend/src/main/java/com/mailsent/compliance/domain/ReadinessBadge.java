package com.mailsent.compliance.domain;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * ReadinessBadge — Aggregated compliance badge and view summary (COMP-002 / FUN-C2).
 */
public class ReadinessBadge {

    public enum BadgeTier {
        COMPLIANT,
        NEEDS_ATTENTION,
        NON_COMPLIANT
    }

    private final String id;
    private final String organizationId;
    private final String domainId;
    private final BadgeTier tier;
    private final int satisfiedCount;
    private final int totalCount;
    private final Map<MailboxProvider, ReadinessLevel> providerStatus;
    private final Instant computedAt;

    private ReadinessBadge(String id, String organizationId, String domainId,
                           BadgeTier tier, int satisfiedCount, int totalCount,
                           Map<MailboxProvider, ReadinessLevel> providerStatus,
                           Instant computedAt) {
        this.id = Objects.requireNonNull(id);
        this.organizationId = Objects.requireNonNull(organizationId);
        this.domainId = Objects.requireNonNull(domainId);
        this.tier = Objects.requireNonNull(tier);
        this.satisfiedCount = satisfiedCount;
        this.totalCount = totalCount;
        this.providerStatus = Collections.unmodifiableMap(Objects.requireNonNull(providerStatus));
        this.computedAt = Objects.requireNonNull(computedAt);
    }

    public static ReadinessBadge fromChecklists(String organizationId, String domainId,
                                                List<ProviderRequirementChecklist> checklists,
                                                Instant computedAt) {
        if (organizationId.isBlank()) {
            throw new IllegalArgumentException("Organization ID must not be blank.");
        }
        if (domainId.isBlank()) {
            throw new IllegalArgumentException("Domain ID must not be blank.");
        }
        if (checklists == null || checklists.isEmpty()) {
            throw new IllegalArgumentException("Checklists must not be empty.");
        }

        Map<MailboxProvider, ReadinessLevel> providerMap = checklists.stream()
                .collect(Collectors.toMap(
                        ProviderRequirementChecklist::provider,
                        ProviderRequirementChecklist::overallReadiness,
                        (existing, replace) -> replace
                ));

        int satisfied = 0;
        int total = 0;
        for (ProviderRequirementChecklist cl : checklists) {
            for (ProviderRequirement req : cl.requirements()) {
                total++;
                if (req.status() == RequirementStatus.SATISFIED) {
                    satisfied++;
                }
            }
        }

        BadgeTier tier = BadgeTier.COMPLIANT;
        if (providerMap.containsValue(ReadinessLevel.NOT_READY)) {
            tier = BadgeTier.NON_COMPLIANT;
        } else if (providerMap.containsValue(ReadinessLevel.AT_RISK)) {
            tier = BadgeTier.NEEDS_ATTENTION;
        }

        return new ReadinessBadge(
                UUID.randomUUID().toString(),
                organizationId,
                domainId,
                tier,
                satisfied,
                total,
                providerMap,
                computedAt
        );
    }

    public String id() { return id; }
    public String organizationId() { return organizationId; }
    public String domainId() { return domainId; }
    public BadgeTier tier() { return tier; }
    public int satisfiedCount() { return satisfiedCount; }
    public int totalCount() { return totalCount; }
    public Map<MailboxProvider, ReadinessLevel> providerStatus() { return providerStatus; }
    public Instant computedAt() { return computedAt; }
}
