package com.mailsent.diagnosis.domain;

/**
 * Lifecycle states of an Issue aggregate.
 */
public enum IssueStatus {
    DISCOVERED,
    DIAGNOSED,
    RESOLVED,
    CLOSED
}
