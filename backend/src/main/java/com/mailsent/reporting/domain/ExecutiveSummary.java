package com.mailsent.reporting.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * ExecutiveSummary — REP-001 Weekly summary (3-bullet) (FUN-R1).
 *
 * <p>Represents a recurring, high-level business summary of health. FUN-R1 dictates
 * that this must be delivered in plain business terms. The standard implementation
 * enforces a strict 3-bullet format to guarantee conciseness for an executive audience.</p>
 */
public class ExecutiveSummary {

    private final String id;
    private final String targetId;
    private final String primaryFinding;
    private final String secondaryFinding;
    private final String recommendation;
    private final Instant generatedAt;

    private ExecutiveSummary(String id, String targetId, String primaryFinding, 
                             String secondaryFinding, String recommendation, Instant generatedAt) {
        this.id = Objects.requireNonNull(id);
        this.targetId = Objects.requireNonNull(targetId);
        this.primaryFinding = Objects.requireNonNull(primaryFinding);
        this.secondaryFinding = Objects.requireNonNull(secondaryFinding);
        this.recommendation = Objects.requireNonNull(recommendation);
        this.generatedAt = Objects.requireNonNull(generatedAt);
    }

    /**
     * Generates a strict 3-bullet executive summary.
     *
     * @param targetId         the identity of the domain or portfolio
     * @param primaryFinding   Bullet 1: The most critical business-level finding
     * @param secondaryFinding Bullet 2: The next most important context or risk
     * @param recommendation   Bullet 3: The recommended executive action
     * @param generatedAt      the deterministic timestamp of generation
     * @return a new ExecutiveSummary instance
     */
    public static ExecutiveSummary generate(String targetId, String primaryFinding, 
                                            String secondaryFinding, String recommendation, 
                                            Instant generatedAt) {
        if (primaryFinding.isBlank() || secondaryFinding.isBlank() || recommendation.isBlank()) {
            throw new IllegalArgumentException("An executive summary must contain exactly 3 populated bullets.");
        }
        return new ExecutiveSummary(UUID.randomUUID().toString(), targetId, primaryFinding, 
                                    secondaryFinding, recommendation, generatedAt);
    }

    public String id() { return id; }
    public String targetId() { return targetId; }
    public String primaryFinding() { return primaryFinding; }
    public String secondaryFinding() { return secondaryFinding; }
    public String recommendation() { return recommendation; }
    public Instant generatedAt() { return generatedAt; }
}
