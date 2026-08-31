package com.mailsent.portfolio.portfolio.domain;

import java.util.Objects;

import com.mailsent.portfolio.observation.domain.HealthIndication;

/**
 * DomainOverview — one registered Domain's row inside a {@link ClientOverview} (PORT-002,
 * FUN-D4): the domain's registry identity plus its real delivery health.
 *
 * <p>The health is the canonical HEALTH-001 indication derived from persisted snapshots —
 * it is passed through verbatim, never recalculated and never invented here. A
 * {@code null} {@code healthIndication} is an explicit, honest state meaning "no Health
 * Signal has been persisted for this domain yet" (HEALTH-001 persists nothing when its
 * required sources are missing); it is never rendered as healthy or at-risk by any
 * consumer of this projection.</p>
 */
public record DomainOverview(String domainId, String name, HealthIndication healthIndication) {

    public DomainOverview {
        Objects.requireNonNull(domainId, "A domain id is required.");
        if (domainId.isBlank()) {
            throw new IllegalArgumentException("A domain id is required.");
        }
        Objects.requireNonNull(name, "A domain name is required.");
        if (name.isBlank()) {
            throw new IllegalArgumentException("A domain name is required.");
        }
        // healthIndication is intentionally nullable: absence of a signal is a real state.
    }
}
