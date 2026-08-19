package com.mailsent.diagnosis.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mailsent.diagnosis.domain.RootCause;
import com.mailsent.portfolio.observation.application.DmarcAggregateEvidenceRepository;
import com.mailsent.portfolio.observation.domain.DmarcAggregateEvidence;
import com.mailsent.portfolio.observation.domain.HealthIndication;
import com.mailsent.portfolio.observation.domain.HealthSignal;

/**
 * Unit tests for DIAG-003 Root Cause Determination service.
 */
@ExtendWith(MockitoExtension.class)
class RootCauseDeterminationServiceTest {

    @Mock
    private DmarcAggregateEvidenceRepository repository;

    private RootCauseDeterminationService service;

    @BeforeEach
    void setUp() {
        service = new RootCauseDeterminationService(repository);
    }

    @Test
    void determineRootCause_NullHealthSignal_ThrowsException() {
        assertThatThrownBy(() -> service.determineRootCause(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void determineRootCause_HealthySignal_ReturnsEmpty() {
        HealthSignal signal = buildSignal("signal-healthy", HealthIndication.HEALTHY, List.of("dmarc-1"));
        Optional<RootCause> result = service.determineRootCause(signal);
        assertThat(result).isEmpty();
    }

    @Test
    void determineRootCause_UnknownSignal_ReturnsEmpty() {
        HealthSignal signal = buildSignal("signal-unknown", HealthIndication.UNKNOWN, List.of("dmarc-1"));
        Optional<RootCause> result = service.determineRootCause(signal);
        assertThat(result).isEmpty();
    }

    @Test
    void determineRootCause_AtRisk_DmarcReject_ReturnsDmarcReject() {
        HealthSignal signal = buildSignal("signal-reject", HealthIndication.AT_RISK, List.of("dmarc-1"));
        DmarcAggregateEvidence evidence = buildEvidence("dmarc-1", "reject", "pass", "pass", "pass", "pass");
        when(repository.findById("dmarc-1")).thenReturn(Optional.of(evidence));

        Optional<RootCause> result = service.determineRootCause(signal);
        assertThat(result).contains(RootCause.DMARC_REJECT);
    }

    @Test
    void determineRootCause_AtRisk_DmarcQuarantine_ReturnsDmarcQuarantine() {
        HealthSignal signal = buildSignal("signal-quarantine", HealthIndication.AT_RISK, List.of("dmarc-1"));
        DmarcAggregateEvidence evidence = buildEvidence("dmarc-1", "quarantine", "pass", "pass", "pass", "pass");
        when(repository.findById("dmarc-1")).thenReturn(Optional.of(evidence));

        Optional<RootCause> result = service.determineRootCause(signal);
        assertThat(result).contains(RootCause.DMARC_QUARANTINE);
    }

    @Test
    void determineRootCause_AtRisk_SpfAlignmentFail_ReturnsSpfAlignmentFailure() {
        HealthSignal signal = buildSignal("signal-spf-align", HealthIndication.AT_RISK, List.of("dmarc-1"));
        DmarcAggregateEvidence evidence = buildEvidence("dmarc-1", "none", "pass", "pass", "fail", "pass");
        when(repository.findById("dmarc-1")).thenReturn(Optional.of(evidence));

        Optional<RootCause> result = service.determineRootCause(signal);
        assertThat(result).contains(RootCause.SPF_ALIGNMENT_FAILURE);
    }

    @Test
    void determineRootCause_AtRisk_DkimAlignmentFail_ReturnsDkimAlignmentFailure() {
        HealthSignal signal = buildSignal("signal-dkim-align", HealthIndication.AT_RISK, List.of("dmarc-1"));
        DmarcAggregateEvidence evidence = buildEvidence("dmarc-1", "none", "pass", "pass", "pass", "fail");
        when(repository.findById("dmarc-1")).thenReturn(Optional.of(evidence));

        Optional<RootCause> result = service.determineRootCause(signal);
        assertThat(result).contains(RootCause.DKIM_ALIGNMENT_FAILURE);
    }

    @Test
    void determineRootCause_AtRisk_SpfAuthFail_ReturnsSpfAuthenticationFailure() {
        HealthSignal signal = buildSignal("signal-spf-auth", HealthIndication.AT_RISK, List.of("dmarc-1"));
        DmarcAggregateEvidence evidence = buildEvidence("dmarc-1", "none", "fail", "pass", "pass", "pass");
        when(repository.findById("dmarc-1")).thenReturn(Optional.of(evidence));

        Optional<RootCause> result = service.determineRootCause(signal);
        assertThat(result).contains(RootCause.SPF_AUTHENTICATION_FAILURE);
    }

    @Test
    void determineRootCause_AtRisk_DkimAuthFail_ReturnsDkimAuthenticationFailure() {
        HealthSignal signal = buildSignal("signal-dkim-auth", HealthIndication.AT_RISK, List.of("dmarc-1"));
        DmarcAggregateEvidence evidence = buildEvidence("dmarc-1", "none", "pass", "fail", "pass", "pass");
        when(repository.findById("dmarc-1")).thenReturn(Optional.of(evidence));

        Optional<RootCause> result = service.determineRootCause(signal);
        assertThat(result).contains(RootCause.DKIM_AUTHENTICATION_FAILURE);
    }

    @Test
    void determineRootCause_AtRisk_MissingOrUnrecognizedTokens_ReturnsUnknownIssue() {
        HealthSignal signal = buildSignal("signal-unknown-issue", HealthIndication.AT_RISK, List.of("dmarc-1"));
        // Signal was flagged at-risk, but cited evidence has non-fail/unrecognized tokens
        DmarcAggregateEvidence evidence = buildEvidence("dmarc-1", "none", "pass", "pass", "pass", "pass");
        when(repository.findById("dmarc-1")).thenReturn(Optional.of(evidence));

        Optional<RootCause> result = service.determineRootCause(signal);
        assertThat(result).contains(RootCause.UNKNOWN_ISSUE);
    }

    @Test
    void determineRootCause_AtRisk_EvidenceNotFound_ReturnsUnknownIssue() {
        HealthSignal signal = buildSignal("signal-missing-ev", HealthIndication.AT_RISK, List.of("dmarc-not-found"));
        when(repository.findById("dmarc-not-found")).thenReturn(Optional.empty());

        Optional<RootCause> result = service.determineRootCause(signal);
        assertThat(result).contains(RootCause.UNKNOWN_ISSUE);
    }

    @Test
    void precedence_DispositionOverAlignmentAndAuthentication() {
        HealthSignal signal = buildSignal("signal-prec-1", HealthIndication.AT_RISK, List.of("dmarc-1"));
        // All failures present on the evidence: reject disposition must win over alignment and auth failures
        DmarcAggregateEvidence evidence = buildEvidence("dmarc-1", "reject", "fail", "fail", "fail", "fail");
        when(repository.findById("dmarc-1")).thenReturn(Optional.of(evidence));

        Optional<RootCause> result = service.determineRootCause(signal);
        assertThat(result).contains(RootCause.DMARC_REJECT);
    }

    @Test
    void precedence_SpfAlignmentOverDkimAlignmentAndAuthentication() {
        HealthSignal signal = buildSignal("signal-prec-2", HealthIndication.AT_RISK, List.of("dmarc-1"));
        // Disposition is none; both SPF and DKIM alignment fail, SPF and DKIM auth fail -> SPF alignment wins
        DmarcAggregateEvidence evidence = buildEvidence("dmarc-1", "none", "fail", "fail", "fail", "fail");
        when(repository.findById("dmarc-1")).thenReturn(Optional.of(evidence));

        Optional<RootCause> result = service.determineRootCause(signal);
        assertThat(result).contains(RootCause.SPF_ALIGNMENT_FAILURE);
    }

    @Test
    void precedence_DkimAlignmentOverAuthentication() {
        HealthSignal signal = buildSignal("signal-prec-3", HealthIndication.AT_RISK, List.of("dmarc-1"));
        // Disposition is none, SPF aligned pass; DKIM aligned fail, SPF & DKIM auth fail -> DKIM alignment wins
        DmarcAggregateEvidence evidence = buildEvidence("dmarc-1", "none", "fail", "fail", "pass", "fail");
        when(repository.findById("dmarc-1")).thenReturn(Optional.of(evidence));

        Optional<RootCause> result = service.determineRootCause(signal);
        assertThat(result).contains(RootCause.DKIM_ALIGNMENT_FAILURE);
    }

    @Test
    void precedence_SpfAuthOverDkimAuth() {
        HealthSignal signal = buildSignal("signal-prec-4", HealthIndication.AT_RISK, List.of("dmarc-1"));
        // Alignments pass; both SPF and DKIM auth fail -> SPF authentication wins
        DmarcAggregateEvidence evidence = buildEvidence("dmarc-1", "none", "fail", "fail", "pass", "pass");
        when(repository.findById("dmarc-1")).thenReturn(Optional.of(evidence));

        Optional<RootCause> result = service.determineRootCause(signal);
        assertThat(result).contains(RootCause.SPF_AUTHENTICATION_FAILURE);
    }

    @Test
    void lineagedMultipleEvidence_EvaluatesInOrder() {
        HealthSignal signal = buildSignal("signal-multi", HealthIndication.AT_RISK, List.of("dmarc-1", "dmarc-2"));
        // First evidence is clean, second has reject -> reject is identified
        DmarcAggregateEvidence ev1 = buildEvidence("dmarc-1", "none", "pass", "pass", "pass", "pass");
        DmarcAggregateEvidence ev2 = buildEvidence("dmarc-2", "reject", "pass", "pass", "pass", "pass");
        when(repository.findById("dmarc-1")).thenReturn(Optional.of(ev1));
        when(repository.findById("dmarc-2")).thenReturn(Optional.of(ev2));

        Optional<RootCause> result = service.determineRootCause(signal);
        assertThat(result).contains(RootCause.DMARC_REJECT);
    }

    private HealthSignal buildSignal(String id, HealthIndication indication, List<String> dmarcIds) {
        return HealthSignal.restore(
                id, "org-1", "domain-1", "example.com",
                indication,
                "obs-1",
                dmarcIds,
                List.of("rep-1"),
                Instant.now()
        );
    }

    private DmarcAggregateEvidence buildEvidence(String id, String disposition, String spfResult, String dkimResult,
                                                 String spfAligned, String dkimAligned) {
        return DmarcAggregateEvidence.restore(
                id, "org-1", "domain-1", "example.com",
                "receipt-1", "report-1", "192.0.2.1", LocalDate.now(), 1L,
                spfResult, dkimResult, spfAligned, dkimAligned,
                "envelope@example.com", "header@example.com", List.of(), disposition
        );
    }
}
