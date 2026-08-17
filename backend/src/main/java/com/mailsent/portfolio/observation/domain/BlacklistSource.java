package com.mailsent.portfolio.observation.domain;

/**
 * Identity of the external blacklist provider that produced a
 * {@link BlacklistObservation}. These names are source labels, not a health
 * conclusion.
 */
public enum BlacklistSource {
    SPAMHAUS,
    BARRACUDA,
    SURBL,
    URIBL
}
