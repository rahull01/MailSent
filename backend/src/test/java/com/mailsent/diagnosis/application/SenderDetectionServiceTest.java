package com.mailsent.diagnosis.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mailsent.diagnosis.domain.Sender;
import com.mailsent.portfolio.observation.domain.DmarcAggregateEvidence;

/**
 * Comprehensive unit tests for DIAG-002 (Sender detection / recognized providers).
 */
class SenderDetectionServiceTest {

    private SenderDetectionService senderDetectionService;

    @BeforeEach
    void setUp() {
        senderDetectionService = new SenderDetectionService();
    }

    private DmarcAggregateEvidence createEvidence(List<String> dkimDomains, String envelopeFrom) {
        return DmarcAggregateEvidence.restore(
                "ev-1", "org-1", "dom-1", "example.com",
                "rec-1", "rep-1", "192.0.2.1", LocalDate.now(),
                1L, "pass", "pass", "pass", "pass",
                envelopeFrom, "example.com",
                dkimDomains, "none"
        );
    }

    @Test
    void shouldDetectGoogleWorkspaceFromDkim() {
        DmarcAggregateEvidence evidence = createEvidence(List.of("google.com"), "unknown.net");
        Optional<Sender> sender = senderDetectionService.detectFromEvidence(evidence);
        assertThat(sender).isPresent();
        assertThat(sender.get()).isEqualTo(SenderDetectionService.GOOGLE_WORKSPACE);
    }

    @Test
    void shouldDetectMicrosoft365FromDkim() {
        DmarcAggregateEvidence evidence = createEvidence(List.of("outlook.com"), "unknown.net");
        Optional<Sender> sender = senderDetectionService.detectFromEvidence(evidence);
        assertThat(sender).isPresent();
        assertThat(sender.get()).isEqualTo(SenderDetectionService.MICROSOFT_365);

        DmarcAggregateEvidence onMicrosoft = createEvidence(List.of("company.onmicrosoft.com"), "unknown.net");
        assertThat(senderDetectionService.detectFromEvidence(onMicrosoft)).contains(SenderDetectionService.MICROSOFT_365);

        DmarcAggregateEvidence msft = createEvidence(List.of("mail.microsoft.com"), "unknown.net");
        assertThat(senderDetectionService.detectFromEvidence(msft)).contains(SenderDetectionService.MICROSOFT_365);
    }

    @Test
    void shouldDetectSalesforceFromDkim() {
        DmarcAggregateEvidence evidence = createEvidence(List.of("salesforce.com"), "unknown.net");
        Optional<Sender> sender = senderDetectionService.detectFromEvidence(evidence);
        assertThat(sender).isPresent();
        assertThat(sender.get()).isEqualTo(SenderDetectionService.SALESFORCE);

        DmarcAggregateEvidence sfdc = createEvidence(List.of("relay.sfdc.net"), "unknown.net");
        assertThat(senderDetectionService.detectFromEvidence(sfdc)).contains(SenderDetectionService.SALESFORCE);
    }

    @Test
    void shouldDetectMailchimpFromDkim() {
        DmarcAggregateEvidence evidence = createEvidence(List.of("mcsv.net"), "unknown.net");
        Optional<Sender> sender = senderDetectionService.detectFromEvidence(evidence);
        assertThat(sender).isPresent();
        assertThat(sender.get()).isEqualTo(SenderDetectionService.MAILCHIMP);

        DmarcAggregateEvidence rsgsv = createEvidence(List.of("mail.rsgsv.net"), "unknown.net");
        assertThat(senderDetectionService.detectFromEvidence(rsgsv)).contains(SenderDetectionService.MAILCHIMP);

        DmarcAggregateEvidence mc = createEvidence(List.of("mailchimp.com"), "unknown.net");
        assertThat(senderDetectionService.detectFromEvidence(mc)).contains(SenderDetectionService.MAILCHIMP);
    }

    @Test
    void shouldDetectAllFourProvidersFromEnvelopeFrom() {
        DmarcAggregateEvidence googleEnv = createEvidence(List.of("unknown.net"), "bounces@google.com");
        assertThat(senderDetectionService.detectFromEvidence(googleEnv)).contains(SenderDetectionService.GOOGLE_WORKSPACE);

        DmarcAggregateEvidence msEnv = createEvidence(List.of("unknown.net"), "mailer@outlook.com");
        assertThat(senderDetectionService.detectFromEvidence(msEnv)).contains(SenderDetectionService.MICROSOFT_365);

        DmarcAggregateEvidence onMsEnv = createEvidence(List.of("unknown.net"), "mailer@tenant.onmicrosoft.com");
        assertThat(senderDetectionService.detectFromEvidence(onMsEnv)).contains(SenderDetectionService.MICROSOFT_365);

        DmarcAggregateEvidence sfEnv = createEvidence(List.of("unknown.net"), "bounces.salesforce.com");
        assertThat(senderDetectionService.detectFromEvidence(sfEnv)).contains(SenderDetectionService.SALESFORCE);

        DmarcAggregateEvidence mcEnv = createEvidence(List.of("unknown.net"), "bounce@mailchimp.com");
        assertThat(senderDetectionService.detectFromEvidence(mcEnv)).contains(SenderDetectionService.MAILCHIMP);
    }

    @Test
    void shouldHandleCaseInsensitivityForDkimAndEnvelope() {
        DmarcAggregateEvidence upperDkim = createEvidence(List.of("MAIL.GOOGLE.COM"), "unknown.net");
        assertThat(senderDetectionService.detectFromEvidence(upperDkim)).contains(SenderDetectionService.GOOGLE_WORKSPACE);

        DmarcAggregateEvidence mixedEnv = createEvidence(List.of("unknown.net"), "BoUnCeS@SaLeSfOrCe.CoM");
        assertThat(senderDetectionService.detectFromEvidence(mixedEnv)).contains(SenderDetectionService.SALESFORCE);
    }

    @Test
    void dkimPrecedenceOverConflictingEnvelopeFrom() {
        // DKIM is Google Workspace, but envelopeFrom says Salesforce -> DKIM wins per precedence
        DmarcAggregateEvidence conflict = createEvidence(List.of("google.com"), "bounces@salesforce.com");
        Optional<Sender> sender = senderDetectionService.detectFromEvidence(conflict);
        assertThat(sender).isPresent();
        assertThat(sender.get()).isEqualTo(SenderDetectionService.GOOGLE_WORKSPACE);
    }

    @Test
    void multipleDkimDomainsPicksFirstMatchingInIterationOrder() {
        // First is unknown, second is Mailchimp, third is Microsoft 365 -> Mailchimp wins
        DmarcAggregateEvidence multi = createEvidence(List.of("custom-domain.org", "mcsv.net", "outlook.com"), "bounces@unknown.net");
        Optional<Sender> sender = senderDetectionService.detectFromEvidence(multi);
        assertThat(sender).isPresent();
        assertThat(sender.get()).isEqualTo(SenderDetectionService.MAILCHIMP);
    }

    @Test
    void shouldReturnEmptyForUnknownSender() {
        DmarcAggregateEvidence evidence = createEvidence(List.of("unknown.net", "another-custom.com"), "bounces.unknown.net");
        Optional<Sender> sender = senderDetectionService.detectFromEvidence(evidence);
        assertThat(sender).isEmpty();
    }

    @Test
    void shouldReturnEmptyForNullEvidence() {
        Optional<Sender> sender = senderDetectionService.detectFromEvidence(null);
        assertThat(sender).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenFieldsAreEmpty() {
        DmarcAggregateEvidence empty = createEvidence(List.of(), "");
        assertThat(senderDetectionService.detectFromEvidence(empty)).isEmpty();

        DmarcAggregateEvidence nullFields = createEvidence(List.of(), null);
        assertThat(senderDetectionService.detectFromEvidence(nullFields)).isEmpty();
    }
}
