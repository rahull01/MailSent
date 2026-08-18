package com.mailsent.feedback.domain;

/**
 * WorkflowStage — Lifecycle stages tracked for time-to-value and drop-off analysis (ANAL-003).
 */
public enum WorkflowStage {
    ONBOARDED,
    FIRST_SCAN,
    ISSUES_IDENTIFIED,
    FIX_APPLIED,
    VERIFIED_HEALTHY
}
