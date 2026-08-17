package com.mailsent.ai.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * GroundedAiExplanation — AI-001 Explanation with grounding.
 *
 * <p>Represents an AI-generated business explanation that is strictly anchored
 * to a verifiable evidence trace. This fulfills the AI-001 requirement that
 * AI explains with grounding and never fabricates decisions without a trace
 * to raw observation snapshots.</p>
 */
public class GroundedAiExplanation {

    private final String id;
    private final String sourceExplanationId;
    private final String groundingEvidenceTraceId;
    private final String aiGeneratedContent;
    private final Instant generatedAt;

    private GroundedAiExplanation(String id, String sourceExplanationId, 
                                  String groundingEvidenceTraceId, String aiGeneratedContent, 
                                  Instant generatedAt) {
        this.id = Objects.requireNonNull(id);
        this.sourceExplanationId = Objects.requireNonNull(sourceExplanationId);
        this.groundingEvidenceTraceId = Objects.requireNonNull(groundingEvidenceTraceId);
        this.aiGeneratedContent = Objects.requireNonNull(aiGeneratedContent);
        this.generatedAt = Objects.requireNonNull(generatedAt);
    }

    /**
     * Generates a grounded AI explanation.
     *
     * @param sourceExplanationId      the canonical explanation context
     * @param groundingEvidenceTraceId the evidence trace anchoring this AI generation
     * @param aiGeneratedContent       the AI-generated business translation
     * @param generatedAt              the timestamp of generation
     * @return a GroundedAiExplanation instance
     */
    public static GroundedAiExplanation generate(String sourceExplanationId, 
                                                 String groundingEvidenceTraceId, 
                                                 String aiGeneratedContent, 
                                                 Instant generatedAt) {
        if (aiGeneratedContent.isBlank()) {
            throw new IllegalArgumentException("AI generated content must not be blank.");
        }
        if (groundingEvidenceTraceId.isBlank()) {
            throw new IllegalArgumentException("AI explanations MUST be grounded to an evidence trace (AI-001).");
        }
        return new GroundedAiExplanation(UUID.randomUUID().toString(), sourceExplanationId, 
                                         groundingEvidenceTraceId, aiGeneratedContent, generatedAt);
    }

    public String id() { return id; }
    public String sourceExplanationId() { return sourceExplanationId; }
    public String groundingEvidenceTraceId() { return groundingEvidenceTraceId; }
    public String aiGeneratedContent() { return aiGeneratedContent; }
    public Instant generatedAt() { return generatedAt; }
}
