package com.mailsent.portfolio.observation.application;

/**
 * Raised when a concurrent store loses the {@code domain_id} + {@code report_id}
 * occupancy race. The caller must treat the report as already stored.
 */
public class DuplicateDmarcAggregateOccupancyException extends RuntimeException {

    private final String domainId;
    private final String reportId;

    public DuplicateDmarcAggregateOccupancyException(String domainId, String reportId, Throwable cause) {
        super("DMARC aggregate occupancy already exists for this Domain and report_id.", cause);
        this.domainId = domainId;
        this.reportId = reportId;
    }

    public String domainId() {
        return domainId;
    }

    public String reportId() {
        return reportId;
    }
}
