package com.mailsent.identity.team.domain;

/**
 * AccessAuditSink — Outbound domain port interface for recording access audit records (TEAM-003).
 */
@FunctionalInterface
public interface AccessAuditSink {

    /**
     * Records an immutable access audit record.
     *
     * @param record the access audit record to record
     */
    void record(AccessAuditRecord record);
}
