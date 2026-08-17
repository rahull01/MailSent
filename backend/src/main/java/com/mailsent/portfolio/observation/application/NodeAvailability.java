package com.mailsent.portfolio.observation.application;

/**
 * Whether a topology node is backed by a real fact (MAP-001). Not a health,
 * score, color, or severity.
 */
public enum NodeAvailability {
    OBSERVED,
    NOT_OBSERVED,
    UNAVAILABLE
}
