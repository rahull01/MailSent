package com.mailsent.diagnosis.application;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

/**
 * IssueLifecycleService — Orchestrates Issue discovery, transitions, and deduplication (DIAG-004).
 */
@Service
public class IssueLifecycleService {

    private final IssueRepository issueRepository;
    private final DomainRepository domainRepository;
    private final RootCauseDeterminationService rootCauseDeterminationService;
    private final SenderDetectionService senderDetectionService;
    private final DmarcAggregateEvidenceRepository dmarcEvidenceRepository;

    public IssueLifecycleService(IssueRepository issueRepository,
                                 DomainRepository domainRepository,
                                 RootCauseDeterminationService rootCauseDeterminationService,
                                 SenderDetectionService senderDetectionService,
                                 DmarcAggregateEvidenceRepository dmarcEvidenceRepository) {
        this.issueRepository = issueRepository;
        this.domainRepository = domainRepository;
        this.rootCauseDeterminationService = rootCauseDeterminationService;
        this.senderDetectionService = senderDetectionService;
        this.dmarcEvidenceRepository = dmarcEvidenceRepository;
    }

    @Transactional
    public Optional<Issue> processHealthSignal(HealthSignal healthSignal, Instant now) {
        Objects.requireNonNull(healthSignal, "healthSignal cannot be null");
        Instant evaluationTime = now != null ? now : Instant.now();

        // 1. Verify registered domain and multi-tenant isolation
        Domain domain = domainRepository.findById(healthSignal.domainId())
                .orElseThrow(() -> new IllegalArgumentException("Domain not registered: " + healthSignal.domainId()));

        if (!domain.organizationId().equals(healthSignal.organizationId())) {
            throw new IllegalArgumentException("Tenant boundary violation: HealthSignal org does not match Domain org");
        }

        // 2. HEALTHY signal -> close active issues for this domain
        if (healthSignal.indication() == HealthIndication.HEALTHY) {
            List<Issue> activeIssues = issueRepository.findByOrganizationIdAndDomainId(domain.organizationId(), domain.id())
                    .stream()
                    .filter(Issue::isActive)
                    .toList();
            for (Issue active : activeIssues) {
                active.close(evaluationTime);
                issueRepository.save(active);
            }
            return Optional.empty();
        }

        // 3. UNKNOWN signal -> do not fabricate or alter issues (N2)
        if (healthSignal.indication() != HealthIndication.AT_RISK) {
            return Optional.empty();
        }

        // 4. AT_RISK signal -> determine RootCause
        Optional<RootCause> rootCauseOpt = rootCauseDeterminationService.determineRootCause(healthSignal);
        if (rootCauseOpt.isEmpty()) {
            return Optional.empty();
        }
        RootCause rootCause = rootCauseOpt.get();

        // 5. Detect Sender (if present in aggregate evidence)
        Sender sender = null;
        for (String evidenceId : healthSignal.dmarcAggregateEvidenceIds()) {
            Optional<DmarcAggregateEvidence> evOpt = dmarcEvidenceRepository.findById(evidenceId);
            if (evOpt.isPresent()) {
                Optional<Sender> detected = senderDetectionService.detectFromEvidence(evOpt.get());
                if (detected.isPresent()) {
                    sender = detected.get();
                    break;
                }
            }
        }

        String providerId = sender != null ? sender.providerId() : null;

        // 6. Check for existing active issue (idempotency / deduplication)
        Optional<Issue> activeIssueOpt = issueRepository.findActiveIssue(
                domain.organizationId(), domain.id(), rootCause, providerId
        );

        if (activeIssueOpt.isPresent()) {
            Issue existing = activeIssueOpt.get();
            if (existing.status() == IssueStatus.RESOLVED) {
                // Verification failed -> reopen issue
                existing.reopen(evaluationTime);
            }
            existing.recordSubsequentObservation(healthSignal.id(), healthSignal.dmarcAggregateEvidenceIds(), evaluationTime);
            return Optional.of(issueRepository.save(existing));
        }

        // 7. Create new Issue
        Issue newIssue = Issue.discover(
                domain.organizationId(),
                domain.id(),
                domain.name(),
                rootCause,
                sender,
                healthSignal.id(),
                healthSignal.dmarcAggregateEvidenceIds(),
                evaluationTime
        );

        try {
            return Optional.of(issueRepository.save(newIssue));
        } catch (DataIntegrityViolationException e) {
            // Concurrent insert race condition safety
            return issueRepository.findActiveIssue(domain.organizationId(), domain.id(), rootCause, providerId);
        }
    }

    @Transactional
    public Issue diagnoseIssue(String organizationId, String issueId, Instant now) {
        Issue issue = getAuthorizedIssue(organizationId, issueId);
        issue.diagnose(now);
        return issueRepository.save(issue);
    }

    @Transactional
    public Issue resolveIssue(String organizationId, String issueId, Instant now) {
        Issue issue = getAuthorizedIssue(organizationId, issueId);
        issue.resolve(now);
        return issueRepository.save(issue);
    }

    @Transactional(readOnly = true)
    public Optional<Issue> findById(String organizationId, String issueId) {
        return issueRepository.findById(issueId)
                .filter(issue -> issue.organizationId().equals(organizationId));
    }

    @Transactional(readOnly = true)
    public List<Issue> findByOrganization(String organizationId) {
        return issueRepository.findByOrganizationId(organizationId);
    }

    @Transactional(readOnly = true)
    public List<Issue> findByDomain(String organizationId, String domainId) {
        return issueRepository.findByOrganizationIdAndDomainId(organizationId, domainId);
    }

    private Issue getAuthorizedIssue(String organizationId, String issueId) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new IllegalArgumentException("Issue not found: " + issueId));
        if (!issue.organizationId().equals(organizationId)) {
            throw new IllegalArgumentException("Tenant boundary violation: issue does not belong to organization");
        }
        return issue;
    }
}
