package com.mailsent.feedback.domain;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * FeatureAdoptionScore — Quantifies how effectively an organization utilizes MailSent features (ANAL-002 / FUN-F3).
 */
public class FeatureAdoptionScore {

    private final String id;
    private final String organizationId;
    private final int score; // 0 to 100
    private final int activeFeaturesCount;
    private final int totalFeaturesCount;
    private final Map<String, Integer> featureUsageCounts;
    private final Instant calculatedAt;

    private FeatureAdoptionScore(String id, String organizationId, int score,
                                 int activeFeaturesCount, int totalFeaturesCount,
                                 Map<String, Integer> featureUsageCounts, Instant calculatedAt) {
        this.id = Objects.requireNonNull(id);
        this.organizationId = Objects.requireNonNull(organizationId);
        this.score = score;
        this.activeFeaturesCount = activeFeaturesCount;
        this.totalFeaturesCount = totalFeaturesCount;
        this.featureUsageCounts = Collections.unmodifiableMap(Objects.requireNonNull(featureUsageCounts));
        this.calculatedAt = Objects.requireNonNull(calculatedAt);
    }

    public static FeatureAdoptionScore calculate(String organizationId,
                                                 Map<String, Integer> featureUsageCounts,
                                                 int totalAvailableFeatures,
                                                 Instant calculatedAt) {
        if (organizationId.isBlank()) {
            throw new IllegalArgumentException("Organization ID must not be blank.");
        }
        if (totalAvailableFeatures <= 0) {
            throw new IllegalArgumentException("Total available features must be positive.");
        }

        Map<String, Integer> safeUsage = featureUsageCounts != null ? featureUsageCounts : Collections.emptyMap();
        int activeCount = (int) safeUsage.values().stream().filter(count -> count > 0).count();
        int rawScore = (int) Math.round(((double) activeCount / totalAvailableFeatures) * 100.0);
        int normalizedScore = Math.min(100, Math.max(0, rawScore));

        return new FeatureAdoptionScore(
                UUID.randomUUID().toString(),
                organizationId,
                normalizedScore,
                activeCount,
                totalAvailableFeatures,
                safeUsage,
                calculatedAt != null ? calculatedAt : Instant.now()
        );
    }

    public String id() { return id; }
    public String organizationId() { return organizationId; }
    public int score() { return score; }
    public int activeFeaturesCount() { return activeFeaturesCount; }
    public int totalFeaturesCount() { return totalFeaturesCount; }
    public Map<String, Integer> featureUsageCounts() { return featureUsageCounts; }
    public Instant calculatedAt() { return calculatedAt; }
}
