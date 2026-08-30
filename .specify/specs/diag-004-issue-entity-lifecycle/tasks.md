# Tasks: DIAG-004 — Issue Entity Lifecycle (Hardened Persistence Model)

- [ ] **Task 1: Spec Kit Hardening & Schema Freeze** (Status: Complete)
  - [x] Create `spec.md` with hardened Issue aggregate model, state machine, origin/latest lineage, and Flyway V19 DDL.
  - [x] Create `plan.md` with domain, application, and persistence layout.
  - [x] Create `tasks.md` with dependency-ordered tasks.
  - [x] Create `checklist.md` with DoD acceptance criteria.

- [ ] **Task 2: Flyway V19 Migration Creation (Pending CTO Authorization)**
  - [ ] Create `backend/src/main/resources/db/migration/V19__create_issues.sql`.
  - [ ] Verify DDL with primary key, foreign keys to `organizations` and `domains`, indexes, and partial unique constraint.

- [ ] **Task 3: Domain Model & Unit Tests (Pending CTO Authorization)**
  - [ ] Create `IssueStatus.java` and `Issue.java` in `com.mailsent.diagnosis.domain`.
  - [ ] Create `IssueRepository.java` domain interface.
  - [ ] Create `IssueTest.java` verifying all state transitions, lineage immutability, and timestamp semantics.

- [ ] **Task 4: Application Service & Tests (Pending CTO Authorization)**
  - [ ] Create `IssueLifecycleService.java` in `com.mailsent.diagnosis.application`.
  - [ ] Create `IssueLifecycleServiceTest.java` verifying discovery, deduplication, repeated observations, verification failure reopen, and closure.

- [ ] **Task 5: JPA Persistence Adapter & Integration Tests (Pending CTO Authorization)**
  - [ ] Create `IssueJpaEntity.java`, `SpringDataIssueRepository.java`, and `JpaIssueRepositoryAdapter.java`.
  - [ ] Create `IssuePersistenceIntegrationTest.java` verifying real PostgreSQL persistence and constraint enforcement.

- [ ] **Task 6: Platform Regression & Master Checklist Closeout (Pending CTO Authorization)**
  - [ ] Run full regression test suite across all verified modules.
  - [ ] Perform `git diff --check` and verify repository cleanliness.
  - [ ] Update `implementation/00_IMPLEMENTATION_MASTER_CHECKLIST.md` upon CTO close authorization.
