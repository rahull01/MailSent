package com.mailsent.portfolio.observation.application;

/**
 * Deterministic INT-004 parse outcomes that are not a valid Parsed DMARC
 * Report. This is not a Health Signal enum.
 */
public enum DmarcReportParseFailure {
    MISSING_RECEIPT,
    UNSUPPORTED_FORMAT,
    MALFORMED_XML,
    MISSING_REPORT_ID,
    INVALID_AGGREGATE
}
