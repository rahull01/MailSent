package com.mailsent.portfolio.observation.domain;

import java.util.List;

/**
 * DnssecObservation — raw DNSSEC evidence for one record type (INF-007).
 *
 * <p>Presence is {@link DnssecPresence#PRESENT}, {@link DnssecPresence#ABSENT}, or
 * {@link DnssecPresence#UNAVAILABLE}. Records are empty when absent or
 * unavailable. This is not "DNSSEC valid" and is not a score.</p>
 */
public record DnssecObservation(String recordType, DnssecPresence presence, List<DnssecRecordFact> records) {

    public DnssecObservation {
        if (recordType == null || recordType.isBlank()) {
            throw new IllegalArgumentException("A DNSSEC record type is required.");
        }
        if (presence == null) {
            throw new IllegalArgumentException("A DNSSEC presence is required.");
        }
        records = records == null ? List.of() : List.copyOf(records);
        if (presence != DnssecPresence.PRESENT && !records.isEmpty()) {
            throw new IllegalArgumentException("Non-present DNSSEC evidence must not carry fabricated records.");
        }
    }
}
