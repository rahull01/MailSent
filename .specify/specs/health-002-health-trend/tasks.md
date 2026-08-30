# Tasks: HEALTH-002 — Health Trend Series

- [ ] **Task 1: Spec Kit Artifact Creation & Freeze** (Status: Complete)
  - [x] Create `spec.md` with frozen domain model, calculation algorithm, and invariants.
  - [x] Create `plan.md` with package layout and verification matrix.
  - [x] Create `tasks.md` with dependency-ordered tasks.
  - [x] Create `checklist.md` with DoD acceptance criteria.

- [ ] **Task 2: Domain Modeling & Unit Tests (Pending CTO Authorization)**
  - [ ] Create `TrendDirection.java` enum in `com.mailsent.portfolio.observation.domain`.
  - [ ] Create `HealthTrend.java` domain projection in `com.mailsent.portfolio.observation.domain`.
  - [ ] Create `HealthTrendTest.java` verifying all transition permutations, minimum data gates, and lineage.

- [ ] **Task 3: Application Service & Integration Tests (Pending CTO Authorization)**
  - [ ] Create `HealthTrendService.java` in `com.mailsent.portfolio.observation.application`.
  - [ ] Create `HealthTrendServiceTest.java` verifying tenant isolation, time-window filtering, and lineage citing.

- [ ] **Task 4: Platform Regression & Master Checklist Closeout (Pending CTO Authorization)**
  - [ ] Run full 220+ regression test suite across all verified modules.
  - [ ] Execute tests against live PostgreSQL 16 on port 5433.
  - [ ] Run `git diff --check`.
  - [ ] Update Master Checklist upon CTO approval.
