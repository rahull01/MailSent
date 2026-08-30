package com.mailsent.explanation.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.mailsent.portfolio.observation.domain.HealthIndication;
import com.mailsent.portfolio.observation.domain.HealthSignal;

class ExplanationViewTest {

    private HealthSignal signal(HealthIndication indication) {
        return HealthSignal.restore("hs-1", "org-1", "dom-1", "example.com",
                indication, "oe-1", List.of("agg-1"), List.of("rep-1"), Instant.now());
    }

    @Test
    void shouldRenderBusinessSummaryForAtRiskSignal() {
        Explanation explanation = Explanation.translate(signal(HealthIndication.AT_RISK));

        ExplanationView view = explanation.renderView(ExplanationMode.BUSINESS_SUMMARY);

        assertThat(view.mode()).isEqualTo(ExplanationMode.BUSINESS_SUMMARY);
        assertThat(view.headline()).contains("at risk");
        assertThat(view.summary()).contains("may not reach");
        assertThat(view.details()).isEmpty();
        assertThat(view.grounding().sourceSignalId()).isEqualTo("hs-1");
    }

    @Test
    void shouldRenderTechnicalDeepDiveForAtRiskSignal() {
        Explanation explanation = Explanation.translate(signal(HealthIndication.AT_RISK));

        ExplanationView view = explanation.renderView(ExplanationMode.TECHNICAL_DEEP_DIVE);

        assertThat(view.mode()).isEqualTo(ExplanationMode.TECHNICAL_DEEP_DIVE);
        assertThat(view.headline()).isEqualTo("Technical Infrastructure Breakdown");
        assertThat(view.summary()).contains("at risk");
        // Grounding rule: technical view points at the cited evidence; it must not assert
        // specific mechanisms (SPF/DKIM/DMARC) the indication alone cannot prove.
        assertThat(view.details()).contains("evidence referenced by this explanation")
            .doesNotContain("SPF/DKIM/DMARC", "misconfigured", "missing");
        assertThat(view.grounding().sourceSignalId()).isEqualTo("hs-1");
    }

    @Test
    void shouldPreserveIdenticalGroundingBetweenModes() {
        Explanation explanation = Explanation.translate(signal(HealthIndication.HEALTHY));

        ExplanationView businessView = explanation.renderView(ExplanationMode.BUSINESS_SUMMARY);
        ExplanationView techView = explanation.renderView(ExplanationMode.TECHNICAL_DEEP_DIVE);

        assertThat(businessView.grounding().sourceSignalId()).isEqualTo(techView.grounding().sourceSignalId());
        assertThat(businessView.grounding().observationEvidenceId()).isEqualTo(techView.grounding().observationEvidenceId());
        assertThat(businessView.grounding().dmarcAggregateEvidenceIds()).isEqualTo(techView.grounding().dmarcAggregateEvidenceIds());
        assertThat(businessView.grounding().reputationObservationIds()).isEqualTo(techView.grounding().reputationObservationIds());
    }
}
