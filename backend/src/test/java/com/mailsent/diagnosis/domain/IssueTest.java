package com.mailsent.diagnosis.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

class IssueTest {

    @Test
    void shouldDiscoverIssueWithOriginLineage() {
        Instant now = Instant.now();
        Issue issue = Issue.discover(
                "org-1", "dom-1", "example.com",
                RootCause.DMARC_REJECT,
                new Sender("google_workspace", "Google Workspace"),
                "hs-1", List.of("ev-1", "ev-2"),
                now
        );

        assertThat(issue.id()).isNotBlank();
        assertThat(issue.organizationId()).isEqualTo("org-1");
        assertThat(issue.domainId()).isEqualTo("dom-1");
        assertThat(issue.domainName()).isEqualTo("example.com");
        assertThat(issue.rootCause()).isEqualTo(RootCause.DMARC_REJECT);
        assertThat(issue.sender()).isEqualTo(new Sender("google_workspace", "Google Workspace"));
        assertThat(issue.status()).isEqualTo(IssueStatus.DISCOVERED);
        assertThat(issue.originHealthSignalId()).isEqualTo("hs-1");
        assertThat(issue.originDmarcAggregateEvidenceIds()).containsExactly("ev-1", "ev-2");
        assertThat(issue.latestHealthSignalId()).isEqualTo("hs-1");
        assertThat(issue.latestDmarcAggregateEvidenceIds()).containsExactly("ev-1", "ev-2");
        assertThat(issue.discoveredAt()).isEqualTo(now);
        assertThat(issue.updatedAt()).isEqualTo(now);
        assertThat(issue.resolvedAt()).isNull();
        assertThat(issue.closedAt()).isNull();
        assertThat(issue.isActive()).isTrue();
    }

    @Test
    void shouldTransitionThroughFullLifecycle() {
        Instant t0 = Instant.parse("2026-08-19T10:00:00Z");
        Instant t1 = Instant.parse("2026-08-19T10:15:00Z");
        Instant t2 = Instant.parse("2026-08-19T10:30:00Z");
        Instant t3 = Instant.parse("2026-08-19T11:00:00Z");

        Issue issue = Issue.discover(
                "org-1", "dom-1", "example.com",
                RootCause.SPF_ALIGNMENT_FAILURE,
                null, "hs-1", List.of("ev-1"), t0
        );

        // DISCOVERED -> DIAGNOSED
        issue.diagnose(t1);
        assertThat(issue.status()).isEqualTo(IssueStatus.DIAGNOSED);
        assertThat(issue.updatedAt()).isEqualTo(t1);
        assertThat(issue.isActive()).isTrue();

        // DIAGNOSED -> RESOLVED
        issue.resolve(t2);
        assertThat(issue.status()).isEqualTo(IssueStatus.RESOLVED);
        assertThat(issue.resolvedAt()).isEqualTo(t2);
        assertThat(issue.updatedAt()).isEqualTo(t2);
        assertThat(issue.isActive()).isTrue();

        // RESOLVED -> CLOSED
        issue.close(t3);
        assertThat(issue.status()).isEqualTo(IssueStatus.CLOSED);
        assertThat(issue.closedAt()).isEqualTo(t3);
        assertThat(issue.updatedAt()).isEqualTo(t3);
        assertThat(issue.isActive()).isFalse();
    }

    @Test
    void shouldReopenResolvedIssueWhenVerificationFails() {
        Instant t0 = Instant.parse("2026-08-19T10:00:00Z");
        Instant t1 = Instant.parse("2026-08-19T10:30:00Z");
        Instant t2 = Instant.parse("2026-08-19T11:00:00Z");

        Issue issue = Issue.discover("org-1", "dom-1", "example.com", RootCause.DMARC_QUARANTINE, null, "hs-1", List.of("ev-1"), t0);
        issue.resolve(t1);
        assertThat(issue.status()).isEqualTo(IssueStatus.RESOLVED);
        assertThat(issue.resolvedAt()).isEqualTo(t1);

        // Reopen (Verification failed)
        issue.reopen(t2);
        assertThat(issue.status()).isEqualTo(IssueStatus.DISCOVERED);
        assertThat(issue.resolvedAt()).isNull();
        assertThat(issue.closedAt()).isNull();
        assertThat(issue.updatedAt()).isEqualTo(t2);
        assertThat(issue.isActive()).isTrue();
    }

    @Test
    void shouldUpdateLatestObservationWhilePreservingOriginLineage() {
        Instant t0 = Instant.parse("2026-08-19T10:00:00Z");
        Instant t1 = Instant.parse("2026-08-19T11:00:00Z");

        Issue issue = Issue.discover("org-1", "dom-1", "example.com", RootCause.DMARC_REJECT, null, "hs-1", List.of("ev-1"), t0);
        issue.recordSubsequentObservation("hs-2", List.of("ev-2", "ev-3"), t1);

        assertThat(issue.originHealthSignalId()).isEqualTo("hs-1");
        assertThat(issue.originDmarcAggregateEvidenceIds()).containsExactly("ev-1");
        assertThat(issue.latestHealthSignalId()).isEqualTo("hs-2");
        assertThat(issue.latestDmarcAggregateEvidenceIds()).containsExactly("ev-2", "ev-3");
        assertThat(issue.updatedAt()).isEqualTo(t1);
    }

    @Test
    void shouldRejectInvalidTransitions() {
        Instant now = Instant.now();
        Issue issue = Issue.discover("org-1", "dom-1", "example.com", RootCause.DMARC_REJECT, null, "hs-1", List.of("ev-1"), now);

        issue.close(now);
        assertThat(issue.status()).isEqualTo(IssueStatus.CLOSED);

        // Cannot resolve a CLOSED issue
        assertThatThrownBy(() -> issue.resolve(now))
                .isInstanceOf(IllegalStateException.class);
    }
}
