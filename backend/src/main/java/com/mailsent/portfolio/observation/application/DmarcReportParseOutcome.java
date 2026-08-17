package com.mailsent.portfolio.observation.application;

import java.util.Optional;

import com.mailsent.portfolio.observation.domain.ParsedDmarcReport;

/**
 * INT-004 parse result: either a valid {@link ParsedDmarcReport} or an
 * unusable input. Invalid reports are never converted into DMARC Aggregate
 * Evidence.
 */
public final class DmarcReportParseOutcome {

    private final ParsedDmarcReport parsed;
    private final DmarcReportParseFailure failure;

    private DmarcReportParseOutcome(ParsedDmarcReport parsed, DmarcReportParseFailure failure) {
        this.parsed = parsed;
        this.failure = failure;
    }

    public static DmarcReportParseOutcome parsed(ParsedDmarcReport report) {
        if (report == null) {
            throw new IllegalArgumentException("A parsed report is required for a success outcome.");
        }
        return new DmarcReportParseOutcome(report, null);
    }

    public static DmarcReportParseOutcome unusable(DmarcReportParseFailure failure) {
        if (failure == null) {
            throw new IllegalArgumentException("An unusable parse requires a failure reason.");
        }
        return new DmarcReportParseOutcome(null, failure);
    }

    public boolean usable() {
        return parsed != null;
    }

    public Optional<ParsedDmarcReport> parsed() {
        return Optional.ofNullable(parsed);
    }

    public Optional<DmarcReportParseFailure> failure() {
        return Optional.ofNullable(failure);
    }
}
