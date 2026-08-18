---
description: "Task list for CLIENT-002: Assign client to agency"
---

# Tasks: CLIENT-002 — Assign Client to Agency (Revised)

<!--
  AUTHORITY & SUBORDINATION:
  This Task List is subordinate to MailSent's Engineering Execution System:
  - Execution Master: implementation/00_IMPLEMENTATION_MASTER_CHECKLIST.md
  - Engineering Truth: docs/00_Foundation/PROJECT_CONSTITUTION.md
-->

**Input**: Design documents from `.specify/specs/client-002-assign-client-to-agency/`  
**Prerequisites**: `spec.md`, `plan.md`  
**Parent Master Checklist Task**: `CLIENT-002` (Phase 05 — Clients, P0)  
**Traceability Anchors**: `CP: CP-8 | Req: FUN-D3, FUN-D4 | Arch: 07_DATA_MODEL.md, 09_SECURITY_ARCHITECTURE.md | WF: Phase 05`  

---

## Phase 1: Repository Port & Adapter Enhancements

- [ ] `T-CLIENT-002-1` [P] Add `Optional<ClientRelationship> findActiveByAgencyAndClient(String agencyOrgId, String clientOrgId)` to `ClientRelationshipRepository.java`.
- [ ] `T-CLIENT-002-2` [P] Implement `findActiveByAgencyAndClient` in `InMemoryClientRelationshipRepository.java`.
- [ ] `T-CLIENT-002-3` [P] Add `findByAgencyOrganizationIdAndClientOrganizationIdAndStatus` to `JpaClientRelationshipRepository.java` and adapt in `JpaClientRelationshipRepositoryAdapter.java`.

---

## Phase 2: Application Service Implementation

- [ ] `T-CLIENT-002-4` Implement `assign(User caller, String agencyOrganizationId, String clientOrganizationId)` in `ClientRelationshipService.java`:
  - Enforce caller is authenticated, belongs to `agencyOrganizationId` (`INV-09`), and has `Role.OWNER`.
  - Validate agency and client organizations exist in `OrganizationRepository` (`INV-14`).
  - Validate `!agencyOrganizationId.equals(clientOrganizationId)`.
  - Check `findActiveByAgencyAndClient`: return existing if active, else form and save new `ClientRelationship`.

---

## Phase 3: Automated Testing & Verification (T-MAT)

- [ ] `T-CLIENT-002-5` Implement unit & security tests in `ClientRelationshipServiceTest.java`:
  - Happy path: Agency Owner assigns client organization.
  - Idempotency: Duplicate active assignment returns existing relationship without creating duplicate entities.
  - Multi-tenant isolation (`INV-09`): Non-agency user throws `AccessDeniedException`.
  - Least privilege: Analyst and Viewer callers throw `AccessDeniedException`.
  - Organization validation: Missing agency or client organization throws `OrganizationNotFoundException`.
  - Self-assignment rejection: Agency assigning itself throws `IllegalArgumentException`.
- [ ] `T-CLIENT-002-6` Run `mvn test` verifying full test suite passes with zero regressions.

---

## Phase 4: Definition of Done & Checklist Sync

- [ ] `T-CLIENT-002-7` Verify all 10 Definition of Done criteria (Constitution §19).
- [ ] `T-CLIENT-002-8` Update `CLIENT-002` status to `✅ Done` in `implementation/00_IMPLEMENTATION_MASTER_CHECKLIST.md`.
