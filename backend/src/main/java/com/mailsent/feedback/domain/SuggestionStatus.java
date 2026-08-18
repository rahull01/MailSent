package com.mailsent.feedback.domain;

/**
 * SuggestionStatus — Lifecycle status for user-submitted contextual suggestions (ANAL-005 / FUN-F2).
 */
public enum SuggestionStatus {
    PENDING_REVIEW,
    ACCEPTED,
    REJECTED,
    INCORPORATED
}
