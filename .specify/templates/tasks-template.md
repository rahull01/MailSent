---
description: "Task list template for MailSent feature implementation"
---

# Tasks: [FEATURE NAME]

<!--
  AUTHORITY & SUBORDINATION:
  This Task List is subordinate to MailSent's Engineering Execution System:
  - Execution Master: implementation/00_IMPLEMENTATION_MASTER_CHECKLIST.md
  - Engineering Truth: docs/00_Foundation/PROJECT_CONSTITUTION.md
  - Architecture Baseline: docs/03_Architecture/
-->

**Input**: Design documents from `/specs/[###-feature-name]/`  
**Prerequisites**: `spec.md` (required), `plan.md` (required)  
**Parent Master Checklist Task**: `[e.g., MAP-002, DIAG-002, COMP-001]`  
**Traceability Anchors**: `CP: [CP-x] | Req: [FUN-xxx] | Arch: [03_Architecture] | WF: [WF-x]`  

---

## Execution Standards

1. **Task Sizing**: Every task must be sized for **30–90 minutes**.
2. **Testing is MANDATORY**: Per Constitution Rule **N5** and Definition of Done, automated tests (unit, edge, failure-path) are required for all production paths.
3. **Checklist Synchronization**: Completing tasks in this file requires updating the parent task status in `implementation/00_IMPLEMENTATION_MASTER_CHECKLIST.md` (using 🟦 Code Complete, 🟪 Tests Complete, 🟩 Review Complete, ✅ Done).

## Format: `[ID] [P?] Description`
- **[ID]**: Sub-task ID mapped to parent checklist (e.g., `T-MAP-002-1`, `T-MAP-002-2`)
- **[P]**: Parallelizable (independent files, no blockers)
- Include exact file paths and verification commands in descriptions.

---

## Phase 1: Database & Domain Layer (Prerequisites)

**Purpose**: Establish domain entities, value objects, invariants, and database schema.

- [ ] `T-001` [P] Create/verify Flyway database migration `backend/src/main/resources/db/migration/V1x__feature_schema.sql` (if schema changes needed).
- [ ] `T-002` [P] Implement domain entity/value object with invariant validation in `backend/src/main/java/com/mailsent/[context]/domain/`.
- [ ] `T-003` Implement domain repository interface in `backend/src/main/java/com/mailsent/[context]/domain/`.

---

## Phase 2: Application Services & Core Business Logic

**Purpose**: Implement use cases, application services, and business rules.

- [ ] `T-004` Implement application service in `backend/src/main/java/com/mailsent/[context]/application/`.
- [ ] `T-005` Enforce tenant isolation (`INV-09`) and deterministic lineage (`INV-11`) in application logic.
- [ ] `T-006` [P] Implement infrastructure repository / external adapter in `backend/src/main/java/com/mailsent/[context]/infrastructure/`.

---

## Phase 3: REST Interfaces & DTO Contracts

**Purpose**: Expose REST endpoints adhering to `08_API_PHILOSOPHY.md`.

- [ ] `T-007` [P] Implement request/response DTOs with validation annotations (`@Valid`, `@NotNull`) in `backend/src/main/java/com/mailsent/[context]/interfaces/dto/`.
- [ ] `T-008` Implement Spring REST controller with structured error handling in `backend/src/main/java/com/mailsent/[context]/interfaces/rest/`.

---

## Phase 4: Frontend Implementation (if UI-facing)

**Purpose**: Implement Next.js components, typed API clients, and hooks.

- [ ] `T-009` [P] Create TypeScript types and API service client in `frontend/features/[feature]/services/`.
- [ ] `T-010` Implement React components in `frontend/features/[feature]/components/` answering the 5 UX questions.
- [ ] `T-011` Wire feature into dashboard view in `frontend/app/(dashboard)/...`.

---

## Phase 5: Mandatory Testing & Verification (T-MAT)

**Purpose**: Full automated test coverage including edge cases and failure paths.

- [ ] `T-012` [P] Unit tests covering happy path and edge cases in `backend/src/test/java/com/mailsent/[context]/...Test.java`.
- [ ] `T-013` Failure-path tests (timeouts, missing evidence, invalid inputs) verifying graceful degradation without data loss (N2, N3).
- [ ] `T-014` Integration test verifying multi-tenant isolation and end-to-end API response.
- [ ] `T-015` Run full verification suite (`mvn clean test` and `npm run build && npm run lint`).

---

## Phase 6: Definition of Done & Master Checklist Synchronization

**Purpose**: Finalize verification, update documentation, and sync repository status.

- [ ] `T-016` Verify all 10 Definition of Done (DoD) criteria from Constitution §19.
- [ ] `T-017` Update parent task status in `implementation/00_IMPLEMENTATION_MASTER_CHECKLIST.md`.
