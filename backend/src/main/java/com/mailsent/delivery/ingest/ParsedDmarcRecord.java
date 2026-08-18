package com.mailsent.delivery.ingest;

import java.util.Objects;

/**
 * ParsedDmarcRecord — Individual record extracted from an aggregate RUA XML report (INT-004).
 */
public class ParsedDmarcRecord {

    private final String sourceIp;
    private final int count;
    private final String disposition;
    private final String dkimResult;
    private final String spfResult;
    private final boolean dkimAligned;
    private final boolean spfAligned;
    private final String headerFrom;

    public ParsedDmarcRecord(String sourceIp, int count, String disposition,
                             String dkimResult, String spfResult,
                             boolean dkimAligned, boolean spfAligned, String headerFrom) {
        this.sourceIp = Objects.requireNonNull(sourceIp);
        this.count = count;
        this.disposition = Objects.requireNonNull(disposition);
        this.dkimResult = Objects.requireNonNull(dkimResult);
        this.spfResult = Objects.requireNonNull(spfResult);
        this.dkimAligned = dkimAligned;
        this.spfAligned = spfAligned;
        this.headerFrom = headerFrom;
    }

    public String sourceIp() { return sourceIp; }
    public int count() { return count; }
    public String disposition() { return disposition; }
    public String dkimResult() { return dkimResult; }
    public String spfResult() { return spfResult; }
    public boolean isDkimAligned() { return dkimAligned; }
    public boolean isSpfAligned() { return spfAligned; }
    public String headerFrom() { return headerFrom; }
}
