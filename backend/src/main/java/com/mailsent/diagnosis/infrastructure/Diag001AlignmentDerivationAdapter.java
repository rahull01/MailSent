package com.mailsent.diagnosis.infrastructure;

import com.mailsent.diagnosis.application.AlignmentDerivationService;
import com.mailsent.portfolio.observation.application.Diag001AlignmentDerivationPort;
import com.mailsent.portfolio.observation.application.DmarcIngestDiag001Lineage;

/**
 * Production INT-006 → DIAG-001 adapter. Derives in-memory Alignment from
 * DMARC Aggregate Evidence; it does not persist Alignment or Health Signal.
 */
public class Diag001AlignmentDerivationAdapter implements Diag001AlignmentDerivationPort {

    private final AlignmentDerivationService derivation;

    public Diag001AlignmentDerivationAdapter(AlignmentDerivationService derivation) {
        this.derivation = derivation;
    }

    @Override
    public void trigger(DmarcIngestDiag001Lineage lineage) {
        derivation.derive(lineage);
    }
}
