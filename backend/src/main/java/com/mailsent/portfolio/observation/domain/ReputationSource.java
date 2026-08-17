package com.mailsent.portfolio.observation.domain;

/**
 * Identity of the external reputation provider that produced a
 * {@link ReputationObservation}. These names are source labels, not a health
 * conclusion.
 */
public enum ReputationSource {
    POSTMASTER,
    SNDS
}
