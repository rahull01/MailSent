package com.mailsent.portfolio.observation.application;

/**
 * Outbound port from INT-006 to DIAG-001.
 *
 * <p>Delivery Observation does not derive Alignment. Diagnosis implements this
 * port. INT-006 does not own Alignment persistence or algorithm.</p>
 */
public interface Diag001AlignmentDerivationPort {

    /**
     * Requests DIAG-001 for one newly stored {@code report_id} + Domain.
     * Implementations must not invent Alignment facts.
     */
    void trigger(DmarcIngestDiag001Lineage lineage);
}
