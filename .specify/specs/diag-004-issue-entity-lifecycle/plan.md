# Implementation Plan: DIAG-004 — Issue Entity Lifecycle (Hardened Persistence Model)

## 1. Architecture & Boundaries
- **Bounded Context**: Diagnosis Engine (`com.mailsent.diagnosis`).
- **Domain Layer**:
  - `Issue` — Aggregate Root entity with origin lineage and latest tracking.
  - `IssueStatus` — Enum (`DISCOVERED`, `DIAGNOSED`, `RESOLVED`, `CLOSED`).
  - `IssueRepository` — Domain port for querying and saving issues.
- **Application Layer**:
  - `IssueLifecycleService` — Handles discovery, deduplication, resolution, closure, and reopen.
- **Infrastructure Layer**:
  - `IssueJpaEntity` — JPA entity mapped to table `issues`.
  - `SpringDataIssueRepository` — Spring Data JPA repository.
  - `JpaIssueRepositoryAdapter` — Adapter implementing `IssueRepository`.
  - `V19__create_issues.sql` — Schema migration.

---

## 2. Domain Model Blueprint

```java
package com.mailsent.diagnosis.domain;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Issue {

    private final String id;
    private final String organizationId;
    private final String domainId;
    private final String domainName;
    private final RootCause rootCause;
    private final Sender sender; // nullable
    private final String originHealthSignalId;
    private final List<String> originDmarcAggregateEvidenceIds;
    private final Instant discoveredAt;

    private IssueStatus status;
    private String latestHealthSignalId;
    private List<String> latestDmarcAggregateEvidenceIds;
    private Instant resolvedAt;
    private Instant closedAt;
    private Instant updatedAt;

    // Factory methods
    public static Issue discover(String orgId, String domainId, String domainName,
                                 RootCause rootCause, Sender sender,
                                 String healthSignalId, List<String> evidenceIds);

    public static Issue restore(...);

    // Business transition methods
    public void diagnose();
    public void resolve();
    public void close();
    public void reopen();
    public void recordSubsequentObservation(String newHealthSignalId, List<String> newEvidenceIds);
}
```

---

## 3. Concurrency & De-duplication Strategy
- **Transaction Boundary**: All mutating operations inside `IssueLifecycleService` run with `@Transactional`.
- **Database Partial Unique Index**: `uk_issues_active_fingerprint` ensures at most one active issue per fingerprint.
- **Race Condition Handling**: Catches `DataIntegrityViolationException` on duplicate concurrent inserts and safely reloads existing active issue.

---

## 4. Verification Matrix
- **Unit Tests (`IssueTest`, `IssueLifecycleServiceTest`)**:
  - Valid transitions and invalid transition rejections.
  - Origin lineage immutability vs latest observation updates.
  - Verification failure (reopening `RESOLVED` issue) vs new incident (new issue on `CLOSED`).
- **PostgreSQL Integration Tests (`IssuePersistenceIntegrationTest`)**:
  - Real PostgreSQL schema validation against Flyway `V19`.
  - Foreign key validation against `organizations` and `domains`.
  - Verification of partial unique constraint `uk_issues_active_fingerprint`.
