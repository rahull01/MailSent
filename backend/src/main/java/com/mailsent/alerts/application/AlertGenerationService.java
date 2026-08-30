package com.mailsent.alerts.application;

import java.util.Objects;
import java.util.Optional;

import com.mailsent.alerts.domain.Alert;
import com.mailsent.alerts.domain.AlertSeverity;
import com.mailsent.monitoring.domain.HealthChangeEvent;
import com.mailsent.portfolio.observation.domain.HealthIndication;

/**
 * AlertGenerationService — ALERT-002 (FUN-M4): turns an authoritative monitoring
 * {@link HealthChangeEvent} into a business-language {@link Alert}.
 *
 * <p><strong>Severity policy (CTO decision D1 — frozen for this milestone).</strong>
 * Only meaningful customer-facing changes generate alerts; the first observation of a
 * healthy or unknown domain is not a change and generates nothing.</p>
 *
 * <pre>
 * HEALTHY → AT_RISK   = CRITICAL      UNKNOWN  → AT_RISK   = CRITICAL
 * HEALTHY → UNKNOWN   = WARNING       AT_RISK  → UNKNOWN   = WARNING
 * AT_RISK  → HEALTHY  = INFO          UNKNOWN  → HEALTHY   = INFO
 * first observation (previous = null):
 *   → AT_RISK = CRITICAL;  → HEALTHY = no alert;  → UNKNOWN = no alert
 * </pre>
 *
 * <p><strong>Business language (FUN-M4 / WF-8).</strong> Every alert states what
 * happened, why it matters, and whether to act, using only facts carried by the event.
 * The permitted semantics are exactly: at-risk means problems are putting delivery at
 * risk; unknown means the state cannot be determined reliably; healthy means delivery
 * health has returned to a healthy state. Root causes, technical mechanisms, evidence
 * specifics, and remediation steps are never invented here.</p>
 *
 * <p><strong>Boundaries.</strong> Deterministic and pure: no database, no HTTP, no
 * notification sending, no channel selection, no digest, no user lookup, no persistence.
 * The tenant scope is the event's {@code organizationId}, propagated verbatim — never
 * substituted. {@code incidentId} is optional context supplied by the caller; when absent
 * the alert is health-change-originated (D2). No-alert outcomes return
 * {@link Optional#empty()} explicitly.</p>
 */
public class AlertGenerationService {

    /**
     * Generates the alert for a health change without incident context.
     */
    public Optional<Alert> generate(HealthChangeEvent event) {
        return generate(event, null);
    }

    /**
     * Generates the alert for a health change, linking it to an incident when the caller
     * holds one. A null/blank incident id yields a health-change-originated alert.
     */
    public Optional<Alert> generate(HealthChangeEvent event, String incidentId) {
        Objects.requireNonNull(event, "A health change event is required.");
        String organizationId = event.organizationId();
        String domainId = event.domainId();
        HealthIndication previous = event.previousIndication();
        HealthIndication current = event.currentIndication();

        Severity severity = severityFor(previous, current);
        if (severity == null) {
            // Not a meaningful customer-facing change (e.g. first observation of a
            // healthy/unknown domain): explicitly no alert — never a fabricated one.
            return Optional.empty();
        }

        return Optional.of(Alert.generate(
            organizationId,
            incidentId,
            severity.asAlertSeverity(),
            whatHappened(domainId, previous, current),
            whyItMatters(current),
            actionRequired(severity),
            event.detectedAt()));
    }

    private Severity severityFor(HealthIndication previous, HealthIndication current) {
        if (current == HealthIndication.AT_RISK) {
            // Entering the problem state is urgent regardless of where it came from
            // (including the very first observation of the domain).
            return Severity.CRITICAL;
        }
        if (current == HealthIndication.UNKNOWN) {
            // Losing certainty deserves attention only if we previously knew the state;
            // an unknown first reading establishes a baseline rather than reporting a change.
            return previous == null ? null : Severity.WARNING;
        }
        if (current == HealthIndication.HEALTHY) {
            // Recovery is good news worth recording only when there was something to recover from.
            return previous == null ? null : Severity.INFO;
        }
        throw new IllegalStateException("Unhandled health indication: " + current);
    }

    private String whatHappened(String domainId, HealthIndication previous, HealthIndication current) {
        if (current == HealthIndication.AT_RISK) {
            if (previous == null) {
                return "Our first check of domain '" + domainId + "' found problems that put its email delivery at risk.";
            }
            return "The delivery health of domain '" + domainId + "' has moved into an at-risk state.";
        }
        if (current == HealthIndication.UNKNOWN) {
            return "We could not determine the delivery-health state of domain '" + domainId + "' in its latest check.";
        }
        return "The delivery health of domain '" + domainId + "' has returned to a healthy state.";
    }

    private String whyItMatters(HealthIndication current) {
        if (current == HealthIndication.AT_RISK) {
            return "Problems are putting this domain's email delivery at risk, so messages may not reach your customers.";
        }
        if (current == HealthIndication.UNKNOWN) {
            return "While the state is unclear, delivery problems could go unnoticed until the next successful check.";
        }
        return "Email for this domain is being delivered from a healthy state again, so your customers can rely on it.";
    }

    private String actionRequired(Severity severity) {
        if (severity == Severity.CRITICAL) {
            return "Open this domain's delivery-health details to review what changed and what to fix first.";
        }
        if (severity == Severity.WARNING) {
            return "No action is needed right now; we are continuing to watch this domain and will update you when its state becomes clear.";
        }
        return null; // purely informational recovery notice
    }

    /** Bridge between the approved transition policy and the frozen ALERT-001 vocabulary. */
    private enum Severity {
        CRITICAL, WARNING, INFO;

        AlertSeverity asAlertSeverity() {
            return AlertSeverity.valueOf(name());
        }
    }
}
