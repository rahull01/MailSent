package com.mailsent.portfolio.observation.application;

import java.util.ArrayList;
import java.util.List;

/**
 * Test-only DIAG-001 double. Counts invocations; never derives Alignment.
 */
final class FakeDiag001AlignmentDerivationPort implements Diag001AlignmentDerivationPort {

    private final List<DmarcIngestDiag001Lineage> invocations = new ArrayList<>();

    @Override
    public void trigger(DmarcIngestDiag001Lineage lineage) {
        invocations.add(lineage);
    }

    int invocationCount() {
        return invocations.size();
    }

    DmarcIngestDiag001Lineage last() {
        return invocations.get(invocations.size() - 1);
    }

    List<DmarcIngestDiag001Lineage> invocations() {
        return List.copyOf(invocations);
    }
}
