package com.mailsent.portfolio.observation.domain;

import java.util.List;

/**
 * PropagationObservation — raw answers from one named DNS resolver (INF-007).
 *
 * <p>When {@code available} is false, {@code answers} is empty — a failed resolver
 * is recorded as unavailable and never given a fabricated answer. Persistence of
 * the parent snapshot still proceeds if any other real observation exists.</p>
 */
public record PropagationObservation(String resolverIdentity, String queryType, String owner,
                                    boolean available, List<PropagationAnswer> answers) {

    public PropagationObservation {
        if (resolverIdentity == null || resolverIdentity.isBlank()) {
            throw new IllegalArgumentException("A resolver identity is required.");
        }
        if (queryType == null || queryType.isBlank()) {
            throw new IllegalArgumentException("A propagation query type is required.");
        }
        if (owner == null || owner.isBlank()) {
            throw new IllegalArgumentException("A propagation owner name is required.");
        }
        answers = answers == null ? List.of() : List.copyOf(answers);
        if (!available && !answers.isEmpty()) {
            throw new IllegalArgumentException("An unavailable resolver must not carry fabricated answers.");
        }
    }

    /** Records a named resolver that produced no usable answer. */
    public static PropagationObservation unavailable(String resolverIdentity, String queryType, String owner) {
        return new PropagationObservation(resolverIdentity, queryType, owner, false, List.of());
    }
}
