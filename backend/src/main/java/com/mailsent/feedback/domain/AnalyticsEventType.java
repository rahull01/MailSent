package com.mailsent.feedback.domain;

/**
 * AnalyticsEventType — Product events tracked for usage and lifecycle analytics (ANAL-001).
 */
public enum AnalyticsEventType {
    SCAN_INITIATED,
    SCAN_COMPLETED,
    FIX_APPLIED,
    ALERT_TRIGGERED,
    ALERT_ACKNOWLEDGED,
    REPORT_GENERATED,
    GUIDANCE_VIEWED
}
