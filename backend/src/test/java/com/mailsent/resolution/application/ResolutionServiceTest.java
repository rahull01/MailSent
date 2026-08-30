package com.mailsent.resolution.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.mailsent.diagnosis.domain.Issue;
import com.mailsent.diagnosis.domain.IssueRepository;
import com.mailsent.diagnosis.domain.RootCause;
import com.mailsent.diagnosis.domain.Sender;
import com.mailsent.portfolio.domain.application.DomainRepository;
import com.mailsent.portfolio.domain.domain.Domain;
import com.mailsent.portfolio.observation.domain.HealthIndication;
import com.mailsent.portfolio.observation.domain.HealthSignal;
import com.mailsent.resolution.domain.DnsProvider;
import com.mailsent.resolution.domain.ResolutionAction;

/**
 * RES-001/002/003 tests. N2/INV-04 grounding rules are enforced negatively: no invented
 * record values, selectors, keys, include strings, or placeholder domains may ever appear
 * in produced actions.
 */
class ResolutionServiceTest {

    private static final String[] FORBIDDEN_FABRICATIONS = {
        "reports@mailsent.com", "_spf.example.com", "include:", "p=..."
    };

    private Issue issue(RootCause cause, Sender sender) {
        return Issue.discover("org-1", "dom-1", "example.com", cause, sender,
            "hs-1", List.of("agg-1"), Instant.now());
    }

    /** Service wired with an org-1-owned example.com domain and an empty issue repo. */
    private ResolutionService wired(Issue issue) {
        DomainRepository domainRepository = mock(DomainRepository.class);
        IssueRepository issueRepository = mock(IssueRepository.class);
        Domain domain = Domain.restore("dom-1", "org-1", "example.com", Instant.now().minusSeconds(86400));
        when(domainRepository.findById("dom-1")).thenReturn(Optional.of(domain));
        when(issueRepository.findById("iss-1")).thenReturn(Optional.ofNullable(issue));
        return new ResolutionService(domainRepository, issueRepository);
    }

    private void assertNoFabricatedValues(List<ResolutionAction> actions) {
        for (ResolutionAction action : actions) {
            String haystack = String.join(" ", action.exactSteps())
                    + " " + action.title() + " " + action.description()
                    + " " + action.recordName() + " " + action.recordValue();
            for (String forbidden : FORBIDDEN_FABRICATIONS) {
                assertThat(haystack).doesNotContain(forbidden);
            }
            assertThat(haystack).doesNotContain("v=DKIM1");
            assertNull(action.recordValue(),
                "exact DNS values are not carried by evidence — absence must stay explicit (N2/INV-04)");
        }
    }

    @Test
    void generatesActionsForAtRisk() {
        ResolutionService service = new ResolutionService();
        HealthSignal signal = HealthSignal.restore("hs-1", "org-1", "dom-1", "example.com",
            HealthIndication.AT_RISK, "oe-1", List.of(), List.of(), Instant.now());

        List<ResolutionAction> actions = service.generateActions(signal);

        assertEquals(2, actions.size());
        assertEquals(1, actions.get(0).priority());
        assertTrue(actions.get(0).title().contains("Identify failing sender"));
        assertTrue(actions.get(0).exactSteps().get(0).contains("example.com"));
        assertEquals(2, actions.get(1).priority());
        assertTrue(actions.get(1).title().contains("authentication"));
    }

    @Test
    void generatesTailoredActionsForIssueWithCloudflare() {
        ResolutionService service = wired(issue(RootCause.DMARC_REJECT, null));

        List<ResolutionAction> actions = service.generateActionsForIssue("org-1", "iss-1", DnsProvider.CLOUDFLARE);

        assertThat(actions).hasSize(1);
        ResolutionAction action = actions.get(0);
        assertThat(action.dnsProvider()).isEqualTo(DnsProvider.CLOUDFLARE);
        // Record NAME is derived from the domain fact; the VALUE is never invented:
        assertThat(action.recordName()).isEqualTo("_dmarc.example.com");
        assertThat(action.exactSteps().stream().anyMatch(s -> s.contains("Grey Cloud")))
            .as("provider-specific proxy guidance remains for Cloudflare")
            .isTrue();
        assertNoFabricatedValues(actions);
    }

    @Test
    void generatesTailoredActionsForIssueWithRoute53() {
        ResolutionService service = wired(issue(RootCause.SPF_ALIGNMENT_FAILURE, null));

        List<ResolutionAction> actions = service.generateActionsForIssue("org-1", "iss-1", DnsProvider.ROUTE53);

        assertThat(actions).hasSize(1);
        ResolutionAction action = actions.get(0);
        assertThat(action.dnsProvider()).isEqualTo(DnsProvider.ROUTE53);
        assertThat(action.exactSteps().get(0)).contains("AWS Route 53");
        assertNoFabricatedValues(actions);
    }

    @Test
    void spfActionWithDetectedSenderNamesProviderWithoutInventingMechanism() {
        ResolutionService service = wired(
            issue(RootCause.SPF_ALIGNMENT_FAILURE, new Sender("sendgrid", "SendGrid")));

        List<ResolutionAction> actions = service.generateActionsForIssue("org-1", "iss-1", DnsProvider.GENERIC_DNS);

        assertThat(actions).hasSize(1);
        assertThat(actions.get(0).exactSteps().stream().anyMatch(s -> s.contains("SendGrid")))
            .as("the detected provider name is a grounded fact and may be referenced")
            .isTrue();
        assertNoFabricatedValues(actions);
    }

    @Test
    void dkimActionNeverInventsSelectorOrKey() {
        ResolutionService service = wired(issue(RootCause.DKIM_ALIGNMENT_FAILURE, null));

        List<ResolutionAction> actions = service.generateActionsForIssue("org-1", "iss-1", DnsProvider.CLOUDFLARE);

        assertThat(actions).hasSize(1);
        ResolutionAction action = actions.get(0);
        assertNull(action.recordName(), "selector is provider-specific — must not be constructed");
        assertNull(action.recordValue(), "public key must come from the mail provider — never invented");
        assertThat(String.join(" ", action.exactSteps()))
            .contains("admin console")
            .contains("_domainkey");
        assertNoFabricatedValues(actions);
    }

    @Test
    void enforcestenantIsolationOnIssueActions() {
        ResolutionService service = wired(issue(RootCause.DMARC_REJECT, null));

        assertThatThrownBy(() -> service.generateActionsForIssue("org-other", "iss-1", DnsProvider.CLOUDFLARE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tenant boundary violation");
    }

    @Test
    void generatesNoActionsForUnknownIndication() {
        ResolutionService service = new ResolutionService();
        HealthSignal signal = HealthSignal.restore("hs-1", "org-1", "dom-1", "example.com",
            HealthIndication.UNKNOWN, "oe-1", List.of(), List.of(), Instant.now());

        List<ResolutionAction> actions = service.generateActions(signal);

        assertTrue(actions.isEmpty(),
            "UNKNOWN carries no grounded remediation — inventing a DMARC fix from absence violates N2");
    }

    @Test
    void generatesNoActionsForHealthy() {
        ResolutionService service = new ResolutionService();
        HealthSignal signal = HealthSignal.restore("hs-1", "org-1", "dom-1", "example.com",
            HealthIndication.HEALTHY, "oe-1", List.of(), List.of(), Instant.now());

        List<ResolutionAction> actions = service.generateActions(signal);

        assertTrue(actions.isEmpty());
    }

    @Test
    void requiresSignal() {
        ResolutionService service = new ResolutionService();
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class, () -> service.generateActions(null)
        );
        assertTrue(exception.getMessage().contains("Health signal is required"));
    }
}
