package com.mailsent.diagnosis.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mailsent.diagnosis.domain.Issue;
import com.mailsent.diagnosis.domain.IssueRepository;
import com.mailsent.diagnosis.domain.IssueStatus;
import com.mailsent.diagnosis.domain.RootCause;
import com.mailsent.diagnosis.domain.Sender;
import com.mailsent.portfolio.domain.application.DomainRepository;
import com.mailsent.portfolio.domain.domain.Domain;
import com.mailsent.portfolio.observation.application.DmarcAggregateEvidenceRepository;
import com.mailsent.portfolio.observation.domain.DmarcAggregateEvidence;
import com.mailsent.portfolio.observation.domain.HealthIndication;
import com.mailsent.portfolio.observation.domain.HealthSignal;

@ExtendWith(MockitoExtension.class)
class IssueLifecycleServiceTest {

    @Mock
    private IssueRepository issueRepository;
    @Mock
    private DomainRepository domainRepository;
    @Mock
    private RootCauseDeterminationService rootCauseDeterminationService;
    @Mock
    private SenderDetectionService senderDetectionService;
    @Mock
    private DmarcAggregateEvidenceRepository dmarcEvidenceRepository;

    private IssueLifecycleService service;

    @BeforeEach
    void setUp() {
        service = new IssueLifecycleService(
                issueRepository,
                domainRepository,
                rootCauseDeterminationService,
                senderDetectionService,
                dmarcEvidenceRepository
        );
    }

    private Domain buildDomain(String orgId, String domId, String name) {
        return Domain.restore(domId, orgId, name, Instant.now());
    }

    private HealthSignal buildSignal(String orgId, String domId, HealthIndication indication, List<String> evIds) {
        return HealthSignal.restore(
                "hs-1", orgId, domId, "example.com",
                indication, "obs-1", evIds, List.of("rep-1"), Instant.now()
        );
    }

    @Test
    void processHealthSignal_HealthySignal_ClosesActiveIssues() {
        Domain domain = buildDomain("org-1", "dom-1", "example.com");
        when(domainRepository.findById("dom-1")).thenReturn(Optional.of(domain));

        Issue activeIssue = Issue.discover("org-1", "dom-1", "example.com", RootCause.DMARC_REJECT, null, "hs-old", List.of("ev-old"), Instant.now());
        when(issueRepository.findByOrganizationIdAndDomainId("org-1", "dom-1")).thenReturn(List.of(activeIssue));

        HealthSignal healthy = buildSignal("org-1", "dom-1", HealthIndication.HEALTHY, List.of("ev-1"));
        Optional<Issue> result = service.processHealthSignal(healthy, Instant.now());

        assertThat(result).isEmpty();
        assertThat(activeIssue.status()).isEqualTo(IssueStatus.CLOSED);
        verify(issueRepository).save(activeIssue);
    }

    @Test
    void processHealthSignal_UnknownSignal_CreatesNoIssue() {
        Domain domain = buildDomain("org-1", "dom-1", "example.com");
        when(domainRepository.findById("dom-1")).thenReturn(Optional.of(domain));

        HealthSignal unknown = buildSignal("org-1", "dom-1", HealthIndication.UNKNOWN, List.of("ev-1"));
        Optional<Issue> result = service.processHealthSignal(unknown, Instant.now());

        assertThat(result).isEmpty();
        verify(issueRepository, never()).save(any());
    }

    @Test
    void processHealthSignal_AtRiskSignal_DiscoversNewIssueWhenNoneActive() {
        Domain domain = buildDomain("org-1", "dom-1", "example.com");
        when(domainRepository.findById("dom-1")).thenReturn(Optional.of(domain));

        HealthSignal atRisk = buildSignal("org-1", "dom-1", HealthIndication.AT_RISK, List.of("ev-1"));
        when(rootCauseDeterminationService.determineRootCause(atRisk)).thenReturn(Optional.of(RootCause.DMARC_REJECT));
        when(issueRepository.findActiveIssue("org-1", "dom-1", RootCause.DMARC_REJECT, null)).thenReturn(Optional.empty());
        when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<Issue> result = service.processHealthSignal(atRisk, Instant.now());

        assertThat(result).isPresent();
        Issue created = result.get();
        assertThat(created.status()).isEqualTo(IssueStatus.DISCOVERED);
        assertThat(created.rootCause()).isEqualTo(RootCause.DMARC_REJECT);
        assertThat(created.originHealthSignalId()).isEqualTo("hs-1");
    }

    @Test
    void processHealthSignal_AtRiskSignal_DeduplicatesExistingActiveIssue() {
        Domain domain = buildDomain("org-1", "dom-1", "example.com");
        when(domainRepository.findById("dom-1")).thenReturn(Optional.of(domain));

        HealthSignal atRisk = buildSignal("org-1", "dom-1", HealthIndication.AT_RISK, List.of("ev-2"));
        when(rootCauseDeterminationService.determineRootCause(atRisk)).thenReturn(Optional.of(RootCause.DMARC_REJECT));

        Issue existing = Issue.discover("org-1", "dom-1", "example.com", RootCause.DMARC_REJECT, null, "hs-1", List.of("ev-1"), Instant.now());
        when(issueRepository.findActiveIssue("org-1", "dom-1", RootCause.DMARC_REJECT, null)).thenReturn(Optional.of(existing));
        when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<Issue> result = service.processHealthSignal(atRisk, Instant.now());

        assertThat(result).isPresent();
        Issue updated = result.get();
        assertThat(updated.id()).isEqualTo(existing.id());
        assertThat(updated.originHealthSignalId()).isEqualTo("hs-1");
        assertThat(updated.latestHealthSignalId()).isEqualTo("hs-1");
        assertThat(updated.latestDmarcAggregateEvidenceIds()).containsExactly("ev-2");
    }

    @Test
    void processHealthSignal_AtRiskSignal_ReopensResolvedIssueOnVerificationFailure() {
        Domain domain = buildDomain("org-1", "dom-1", "example.com");
        when(domainRepository.findById("dom-1")).thenReturn(Optional.of(domain));

        HealthSignal atRisk = buildSignal("org-1", "dom-1", HealthIndication.AT_RISK, List.of("ev-2"));
        when(rootCauseDeterminationService.determineRootCause(atRisk)).thenReturn(Optional.of(RootCause.DMARC_REJECT));

        Issue resolved = Issue.discover("org-1", "dom-1", "example.com", RootCause.DMARC_REJECT, null, "hs-1", List.of("ev-1"), Instant.now());
        resolved.resolve(Instant.now());
        assertThat(resolved.status()).isEqualTo(IssueStatus.RESOLVED);

        when(issueRepository.findActiveIssue("org-1", "dom-1", RootCause.DMARC_REJECT, null)).thenReturn(Optional.of(resolved));
        when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<Issue> result = service.processHealthSignal(atRisk, Instant.now());

        assertThat(result).isPresent();
        Issue reopened = result.get();
        assertThat(reopened.status()).isEqualTo(IssueStatus.DISCOVERED);
        assertThat(reopened.resolvedAt()).isNull();
    }

    @Test
    void processHealthSignal_TenantMismatch_ThrowsException() {
        Domain domain = buildDomain("org-1", "dom-1", "example.com");
        when(domainRepository.findById("dom-1")).thenReturn(Optional.of(domain));

        // Signal claims org-2 for a domain owned by org-1
        HealthSignal crossTenant = buildSignal("org-2", "dom-1", HealthIndication.AT_RISK, List.of("ev-1"));

        assertThatThrownBy(() -> service.processHealthSignal(crossTenant, Instant.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tenant boundary violation");
    }

    @Test
    void diagnoseIssue_TenantMismatch_ThrowsException() {
        Issue issue = Issue.discover("org-1", "dom-1", "example.com", RootCause.DMARC_REJECT, null, "hs-1", List.of("ev-1"), Instant.now());
        when(issueRepository.findById("iss-1")).thenReturn(Optional.of(issue));

        assertThatThrownBy(() -> service.diagnoseIssue("org-other", "iss-1", Instant.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tenant boundary violation");
    }

    @Test
    void resolveIssue_TenantMismatch_ThrowsException() {
        Issue issue = Issue.discover("org-1", "dom-1", "example.com", RootCause.DMARC_REJECT, null, "hs-1", List.of("ev-1"), Instant.now());
        when(issueRepository.findById("iss-1")).thenReturn(Optional.of(issue));

        assertThatThrownBy(() -> service.resolveIssue("org-other", "iss-1", Instant.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tenant boundary violation");
    }

    @Test
    void findById_CrossTenantIsolation_ReturnsEmpty() {
        Issue issue = Issue.discover("org-1", "dom-1", "example.com", RootCause.DMARC_REJECT, null, "hs-1", List.of("ev-1"), Instant.now());
        when(issueRepository.findById("iss-1")).thenReturn(Optional.of(issue));

        Optional<Issue> query = service.findById("org-other", "iss-1");
        assertThat(query).isEmpty();
    }
}
