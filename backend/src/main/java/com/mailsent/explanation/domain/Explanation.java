package com.mailsent.explanation.domain;

import java.util.UUID;

import com.mailsent.portfolio.observation.domain.HealthIndication;
import com.mailsent.portfolio.observation.domain.HealthSignal;

/**
 * Explanation - a business-language translation of a health state (EXPL-001).
 *
 * <p>Owned by the Business Explanation context. It translates technical
 * facts into plain language (what happened) and business impact (why it matters)
 * per FUN-E1 and FUN-E2.</p>
 *
 * <p><strong>Grounding rule (N2 / INV-04).</strong> Every sentence states only what the
 * canonical indication itself means — the approved semantics are: at-risk means problems
 * are putting delivery at risk; healthy means delivery health is in a healthy state;
 * unknown means the current state cannot be determined reliably. Causes (which record
 * failed or why), mechanisms, provider behaviour, and thresholds are never asserted here:
 * they live in the cited evidence ({@link Grounding}), not in prose.</p>
 */
public final class Explanation {

    private final String id;
    private final String organizationId;
    private final String domainId;
    private final String domainName;
    private final Grounding grounding;
    private final String whatHappened;
    private final String whyItMatters;
    private final String technicalDetail;

    private Explanation(String id, String organizationId, String domainId, String domainName,
                        Grounding grounding, String whatHappened, String whyItMatters, String technicalDetail) {
        this.id = id;
        this.organizationId = organizationId;
        this.domainId = domainId;
        this.domainName = domainName;
        this.grounding = grounding;
        this.whatHappened = whatHappened;
        this.whyItMatters = whyItMatters;
        this.technicalDetail = technicalDetail;
    }

    /**
     * Translates a HealthSignal into a business Explanation.
     */
    public static Explanation translate(HealthSignal signal) {
        if (signal == null) {
            throw new IllegalArgumentException("A health signal is required for explanation.");
        }

        String whatHappened;
        String whyItMatters;
        String technicalDetail;

        if (signal.indication() == HealthIndication.AT_RISK) {
            whatHappened = "Our latest check found problems that put email delivery for this domain at risk.";
            whyItMatters = "While this continues, messages sent from your domain may not reach your customers' inboxes.";
            // Technical detail stays inside the indication vocabulary: which specific
            // check failed and why is carried by the cited evidence ids, never asserted here.
            technicalDetail = "Recent reported checks show results consistent with delivery problems. "
                + "The exact failing checks are listed in the evidence referenced by this explanation.";
        } else if (signal.indication() == HealthIndication.HEALTHY) {
            whatHappened = "Email delivery health for this domain checked out as healthy.";
            whyItMatters = "A healthy state means there are no detected problems putting delivery at risk right now.";
            technicalDetail = "All checks behind this result passed on the most recent reported data "
                + "(see the evidence referenced by this explanation).";
        } else {
            whatHappened = "We could not reliably determine the delivery-health state of this domain from its latest reported data.";
            whyItMatters = "While the state is unclear, delivery problems could go unnoticed until the next successful check.";
            technicalDetail = "The latest reported data was inconclusive rather than clearly passing or failing. "
                + "This is an explicit unknown — no health claim is being made.";
        }

        return new Explanation(
            UUID.randomUUID().toString(),
            signal.organizationId(),
            signal.domainId(),
            signal.domainName(),
            Grounding.from(signal),
            whatHappened,
            whyItMatters,
            technicalDetail
        );
    }

    public ExplanationView renderView(ExplanationMode mode) {
        return ExplanationView.from(this, mode);
    }

    public String id() { return id; }
    public String organizationId() { return organizationId; }
    public String domainId() { return domainId; }
    public String domainName() { return domainName; }
    public Grounding grounding() { return grounding; }
    public String whatHappened() { return whatHappened; }
    public String whyItMatters() { return whyItMatters; }
    public String technicalDetail() { return technicalDetail; }
}
