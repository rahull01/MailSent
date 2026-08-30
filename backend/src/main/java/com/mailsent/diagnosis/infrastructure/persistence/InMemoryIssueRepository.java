package com.mailsent.diagnosis.infrastructure.persistence;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.mailsent.diagnosis.domain.Issue;
import com.mailsent.diagnosis.domain.IssueRepository;
import com.mailsent.diagnosis.domain.IssueStatus;
import com.mailsent.diagnosis.domain.RootCause;

/**
 * InMemoryIssueRepository — a hermetic, thread-safe {@link IssueRepository} for
 * the test profile. Issues are keyed by their stable identity; all multi-field
 * lookups stream over the in-memory store.
 *
 * <p>Thread-safety is provided by {@link ConcurrentHashMap}. All read operations
 * stream a snapshot of the map's values, so no additional synchronization is required.
 *
 * <p>Tenant isolation (INV-09) is preserved: every lookup predicate begins with
 * {@code organizationId} and never derives organization ownership from {@code domainId}.
 *
 * <p>Configuration and profile binding are handled by {@code MonitoringConfig}
 * (T-P01-11); no Spring annotations are added here.
 */
public class InMemoryIssueRepository implements IssueRepository {

    private final Map<String, Issue> byId = new ConcurrentHashMap<>();

    @Override
    public Issue save(Issue issue) {
        Objects.requireNonNull(issue, "issue cannot be null");
        byId.put(issue.id(), issue);
        return issue;
    }

    @Override
    public Optional<Issue> findById(String id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public List<Issue> findByOrganizationId(String organizationId) {
        return byId.values().stream()
                .filter(issue -> organizationId.equals(issue.organizationId()))
                .toList();
    }

    @Override
    public List<Issue> findByOrganizationIdAndDomainId(String organizationId, String domainId) {
        return byId.values().stream()
                .filter(issue -> organizationId.equals(issue.organizationId())
                        && domainId.equals(issue.domainId()))
                .toList();
    }

    /**
     * Mirrors the {@code findActiveIssue} JPQL semantics in
     * {@link SpringDataIssueRepository}: matches on
     * {@code organizationId, domainId, rootCause}, null-safe {@code providerId},
     * and status in {@code DISCOVERED | DIAGNOSED | RESOLVED}.
     */
    @Override
    public Optional<Issue> findActiveIssue(String organizationId, String domainId,
                                           RootCause rootCause, String providerId) {
        return byId.values().stream()
                .filter(issue -> organizationId.equals(issue.organizationId()))
                .filter(issue -> domainId.equals(issue.domainId()))
                .filter(issue -> rootCause == issue.rootCause())
                .filter(issue -> Objects.equals(providerId,
                        issue.sender() != null ? issue.sender().providerId() : null))
                .filter(issue -> issue.status() == IssueStatus.DISCOVERED
                        || issue.status() == IssueStatus.DIAGNOSED
                        || issue.status() == IssueStatus.RESOLVED)
                .findFirst();
    }

    /** Clears all state; used by tests to reset the shared bean between cases. */
    public void clear() {
        byId.clear();
    }
}
