package com.mailsent.compliance.application;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

import com.mailsent.compliance.domain.BimiEvaluator;
import com.mailsent.compliance.domain.MailboxProvider;
import com.mailsent.compliance.domain.MtaStsEvaluator;
import com.mailsent.compliance.domain.ProviderRequirement;
import com.mailsent.compliance.domain.ProviderRequirementChecklist;
import com.mailsent.compliance.domain.TlsRptEvaluator;
import com.mailsent.portfolio.observation.domain.ObservationEvidence;

/**
 * AdvancedProtocolReadinessService — COMP-004 MTA-STS / BIMI / TLS-RPT Readiness.
 *
 * <p>Evaluates DNS-level readiness for advanced email security and branding protocols
 * (RFC 8461 MTA-STS, RFC 8460 TLS-RPT, and BIMI) against authoritative DNS observation
 * evidence while strictly maintaining multi-tenant isolation (INV-09) and deterministic
 * evidence lineage (INV-11).</p>
 */
public final class AdvancedProtocolReadinessService {

    private AdvancedProtocolReadinessService() {}

    /**
     * Evaluates MTA-STS, TLS-RPT, and BIMI readiness from authoritative DNS observation evidence.
     *
     * @param organizationId the requesting organization ID (required)
     * @param domainId       the target domain ID (required)
     * @param evidence       the authoritative DNS observation evidence (may be null if unavailable)
     * @param assessedAt     the evaluation timestamp (required)
     * @return evaluated ProviderRequirementChecklist under generic provider classification
     * @throws IllegalArgumentException on blank IDs, null timestamps, or tenant isolation violations (INV-09)
     */
    public static ProviderRequirementChecklist evaluate(String organizationId, String domainId,
                                                         ObservationEvidence evidence, Instant assessedAt) {
        Objects.requireNonNull(organizationId, "organizationId is required");
        Objects.requireNonNull(domainId, "domainId is required");
        Objects.requireNonNull(assessedAt, "assessedAt is required");

        if (organizationId.isBlank() || domainId.isBlank()) {
            throw new IllegalArgumentException("Organization ID and Domain ID must not be blank.");
        }

        List<String> txtRecords = null;
        String evidenceRef = null;

        if (evidence != null) {
            // Strict tenant and domain boundary enforcement (INV-09)
            if (!organizationId.equals(evidence.organizationId()) || !domainId.equals(evidence.domainId())) {
                throw new IllegalArgumentException("ObservationEvidence does not belong to the requested organization/domain (INV-09).");
            }
            txtRecords = evidence.txtRecords();
            evidenceRef = evidence.id();
        }

        ProviderRequirement mtaSts = MtaStsEvaluator.evaluate(txtRecords, evidenceRef);
        ProviderRequirement tlsRpt = TlsRptEvaluator.evaluate(txtRecords, evidenceRef);
        ProviderRequirement bimi = BimiEvaluator.evaluate(txtRecords, evidenceRef);

        return ProviderRequirementChecklist.create(
            organizationId,
            domainId,
            MailboxProvider.GENERIC,
            List.of(mtaSts, tlsRpt, bimi),
            assessedAt
        );
    }
}
