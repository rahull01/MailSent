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
 */
public final class Explanation {

    private final String id;
    private final String organizationId;
    private final String domainId;
    private final String domainName;
    private final Grounding grounding;
    private final String whatHappened;
    private final String whyItMatters;

    private Explanation(String id, String organizationId, String domainId, String domainName,
                        Grounding grounding, String whatHappened, String whyItMatters) {
        this.id = id;
        this.organizationId = organizationId;
        this.domainId = domainId;
        this.domainName = domainName;
        this.grounding = grounding;
        this.whatHappened = whatHappened;
        this.whyItMatters = whyItMatters;
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

        if (signal.indication() == HealthIndication.AT_RISK) {
            whatHappened = "Your email delivery infrastructure is currently at risk due to critical authentication or alignment failures.";
            whyItMatters = "Emails sent from this domain may be quarantined or rejected by receiving mail servers, directly impacting your business communication and marketing reach.";
        } else if (signal.indication() == HealthIndication.HEALTHY) {
            whatHappened = "Your email delivery infrastructure is fully authenticated and correctly aligned.";
            whyItMatters = "Receiving mail servers can trust emails from this domain, maximizing the likelihood that your messages reach the inbox.";
        } else {
            whatHappened = "We do not have sufficient recent data to determine the current state of your email delivery infrastructure.";
            whyItMatters = "Without sufficient data, unseen delivery issues could be occurring, leaving your business communication vulnerable to unexpected blocking.";
        }

        return new Explanation(
            UUID.randomUUID().toString(),
            signal.organizationId(),
            signal.domainId(),
            signal.domainName(),
            Grounding.from(signal),
            whatHappened,
            whyItMatters
        );
    }

    public String id() { return id; }
    public String organizationId() { return organizationId; }
    public String domainId() { return domainId; }
    public String domainName() { return domainName; }
    public Grounding grounding() { return grounding; }
    public String whatHappened() { return whatHappened; }
    public String whyItMatters() { return whyItMatters; }
}
