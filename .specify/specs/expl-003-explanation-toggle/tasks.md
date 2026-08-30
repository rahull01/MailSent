# Tasks: EXPL-003 — Simpler Toggle

- [ ] **Task 1: Spec Kit Artifact Creation & Freezing** (Status: Complete)
  - [x] Create `spec.md` with mode definitions, rendering matrix, and invariants.
  - [x] Create `plan.md` with package layout and verification strategy.
  - [x] Create `tasks.md` with dependency-ordered tasks.
  - [x] Create `checklist.md` with DoD acceptance criteria.

- [ ] **Task 2: Domain Modeling & Mode Projections (Pending CTO Authorization)**
  - [ ] Create `ExplanationMode.java` enum.
  - [ ] Create `ExplanationView.java` domain projection.
  - [ ] Enhance `Explanation.java` with mode projection methods.
  - [ ] Create `ExplanationViewTest.java`.

- [ ] **Task 3: Application Service Enhancement & Tenant Boundary (Pending CTO Authorization)**
  - [ ] Update `ExplanationService.java` to support `ExplanationMode` and `INV-09` domain ownership.
  - [ ] Update `ExplanationServiceTest.java` with negative tenant isolation tests and mode rendering tests.

- [ ] **Task 4: Platform Regression & Master Checklist Closeout (Pending CTO Authorization)**
  - [ ] Run complete 236+ platform regression test suite.
  - [ ] Run `git diff --check`.
  - [ ] Update Master Checklist upon CTO approval.
