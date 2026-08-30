package com.mailsent.explanation.domain;

import java.util.Objects;

/**
 * ExplanationView — rendered presentation view of an explanation tailored by ExplanationMode (EXPL-003).
 */
public final class ExplanationView {

    private final String explanationId;
    private final String organizationId;
    private final String domainId;
    private final String domainName;
    private final ExplanationMode mode;
    private final String headline;
    private final String summary;
    private final String details;
    private final Grounding grounding;

    public ExplanationView(String explanationId, String organizationId, String domainId, String domainName,
                           ExplanationMode mode, String headline, String summary, String details, Grounding grounding) {
        this.explanationId = Objects.requireNonNull(explanationId, "explanationId cannot be null");
        this.organizationId = Objects.requireNonNull(organizationId, "organizationId cannot be null");
        this.domainId = Objects.requireNonNull(domainId, "domainId cannot be null");
        this.domainName = Objects.requireNonNull(domainName, "domainName cannot be null");
        this.mode = Objects.requireNonNull(mode, "mode cannot be null");
        this.headline = Objects.requireNonNull(headline, "headline cannot be null");
        this.summary = Objects.requireNonNull(summary, "summary cannot be null");
        this.details = Objects.requireNonNull(details, "details cannot be null");
        this.grounding = Objects.requireNonNull(grounding, "grounding cannot be null");
    }

    public static ExplanationView from(Explanation explanation, ExplanationMode mode) {
        Objects.requireNonNull(explanation, "explanation cannot be null");
        Objects.requireNonNull(mode, "mode cannot be null");

        String headline;
        String summary;
        String details;

        if (mode == ExplanationMode.BUSINESS_SUMMARY) {
            headline = explanation.whatHappened();
            summary = explanation.whyItMatters();
            details = "";
        } else {
            headline = "Technical Infrastructure Breakdown";
            summary = explanation.whatHappened() + " " + explanation.whyItMatters();
            details = explanation.technicalDetail();
        }

        return new ExplanationView(
                explanation.id(),
                explanation.organizationId(),
                explanation.domainId(),
                explanation.domainName(),
                mode,
                headline,
                summary,
                details,
                explanation.grounding()
        );
    }

    public String explanationId() { return explanationId; }
    public String organizationId() { return organizationId; }
    public String domainId() { return domainId; }
    public String domainName() { return domainName; }
    public ExplanationMode mode() { return mode; }
    public String headline() { return headline; }
    public String summary() { return summary; }
    public String details() { return details; }
    public Grounding grounding() { return grounding; }
}
