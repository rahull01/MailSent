# Tasks: DIAG-002 — Sender Detection (Recognized Providers)

- [ ] **Task 1: Spec Kit Artifact Creation** (Status: Complete)
  - [x] Create `spec.md` with frozen recognized provider catalog.
  - [x] Create `plan.md` with in-memory detection architecture.
  - [x] Create `tasks.md` with dependency-ordered verification tasks.
  - [x] Create `checklist.md` with DoD acceptance criteria.

- [ ] **Task 2: Test Suite Audit & Gap Expansion**
  - [ ] Inspect existing `SenderDetectionServiceTest`.
  - [ ] Add explicit tests for case-insensitivity across all providers.
  - [ ] Add explicit tests for DKIM vs Envelope From precedence.
  - [ ] Add explicit tests for multiple DKIM domains.
  - [ ] Run `SenderDetectionServiceTest` and verify 100% pass rate.

- [ ] **Task 3: Regression Verification**
  - [ ] Run full regression test suite with real PostgreSQL.
  - [ ] Verify `V1–V18` migrations are untouched and no `V19+` migration exists.
  - [ ] Run `git diff --check` and verify repository cleanliness.

- [ ] **Task 4: CTO Review & Close Authorization**
  - [ ] Submit verification report to CTO.
  - [ ] Update Master Checklist upon approval.
