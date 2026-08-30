# Tasks: RES-003 — Match by DNS Provider

- [ ] **Task 1: Spec Kit Artifact Creation & Freezing** (Status: Complete)
  - [x] Create `spec.md` with DNS provider detection, tailoring matrix, and invariants.
  - [x] Create `plan.md` with architecture layout and verification strategy.
  - [x] Create `tasks.md` with dependency-ordered tasks.
  - [x] Create `checklist.md` with DoD acceptance criteria.

- [ ] **Task 2: Domain Modeling & Provider Detection (Pending CTO Authorization)**
  - [ ] Create `DnsProvider.java` enum with nameserver detection logic.
  - [ ] Update `ResolutionAction.java` to support provider context, exact DNS values, and `issueId` lineage.
  - [ ] Create `DnsProviderTest.java` and update `ResolutionActionTest.java`.

- [ ] **Task 3: Application Service & Tenant Isolation (Pending CTO Authorization)**
  - [ ] Update `ResolutionService.java` with `generateActionsForIssue` supporting `INV-09` tenant boundaries.
  - [ ] Update `ResolutionServiceTest.java` with provider-tailored test cases and tenant negative tests.

- [ ] **Task 4: Platform Regression & Master Checklist Closeout (Pending CTO Authorization)**
  - [ ] Run complete 242+ platform regression test suite with live PostgreSQL.
  - [ ] Run `git diff --check`.
  - [ ] Update Master Checklist upon CTO approval.
