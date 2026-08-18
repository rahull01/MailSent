---
description: "Task list for TEAM-002: Role→capability map (least-authority)"
---

# Tasks: TEAM-002 — Role→Capability Map (Least-Authority)

<!--
  AUTHORITY & SUBORDINATION:
  This Task List is subordinate to MailSent's Engineering Execution System:
  - Execution Master: implementation/00_IMPLEMENTATION_MASTER_CHECKLIST.md
  - Engineering Truth: docs/00_Foundation/PROJECT_CONSTITUTION.md
  - Architecture Baseline: docs/03_Architecture/
-->

**Input**: Design documents from `.specify/specs/team-002-role-capability-map/`  
**Prerequisites**: `spec.md`, `plan.md`  
**Parent Master Checklist Task**: `TEAM-002` (Phase 04 — Teams, P0)  
**Traceability Anchors**: `CP: CP-8 | Req: FUN-D5 | Arch: 09_SECURITY_ARCHITECTURE.md | WF: Phase 04`  

---

## Execution Standards

1. **Task Sizing**: Every task is sized for **30–60 minutes**.
2. **Testing is MANDATORY**: Full T-MAT coverage (happy path, edge cases, failure recovery, security isolation) required.
3. **Checklist Synchronization**: Update `TEAM-002` in `implementation/00_IMPLEMENTATION_MASTER_CHECKLIST.md` upon completion.

---

## Phase 1: Domain Models & Capability Matrix (Prerequisites)

- [ ] `T-TEAM-002-1` [P] Implement `Capability.java` enum in `backend/src/main/java/com/mailsent/identity/team/domain/Capability.java` containing all 13 authorized capability tokens.
- [ ] `T-TEAM-002-2` [P] Implement `AccessDeniedException.java` domain exception in `backend/src/main/java/com/mailsent/identity/team/domain/AccessDeniedException.java`.
- [ ] `T-TEAM-002-3` Implement immutable `RoleCapabilityPolicy.java` in `backend/src/main/java/com/mailsent/identity/team/domain/RoleCapabilityPolicy.java` defining the explicit mapping for `OWNER`, `ANALYST`, and `VIEWER` with default-deny behavior.

---

## Phase 2: Application Authorization Service & Guards

- [ ] `T-TEAM-002-4` Implement `AuthorizationService.java` in `backend/src/main/java/com/mailsent/identity/team/application/AuthorizationService.java` evaluating `(User, OrganizationId, Capability)` with tenant boundary validation (`INV-09`).
- [ ] `T-TEAM-002-5` Implement `PrivilegeGuard.java` helper utility for assert-style access checks with structured MDC logging on denial (`N3`).

---

## Phase 3: Mandatory Automated Testing (T-MAT & S-MAT)

- [ ] `T-TEAM-002-6` [P] Implement hermetic unit tests in `backend/src/test/java/com/mailsent/identity/team/domain/RoleCapabilityPolicyTest.java` verifying complete matrix mapping and least-authority isolation.
- [ ] `T-TEAM-002-7` Implement comprehensive authorization and failure tests in `backend/src/test/java/com/mailsent/identity/team/application/AuthorizationServiceTest.java` covering:
  - Allowed operations for Owner, Analyst, and Viewer
  - Denied operations for Analyst (e.g. `TEAM_INVITE`, `BILLING_MANAGE`)
  - Denied operations for Viewer (all mutation/operational triggers)
  - Multi-tenant mismatch rejection (`INV-09`)
  - Null parameters and unauthenticated user rejection (Default-Deny)
- [ ] `T-TEAM-002-8` Run `mvn test` to verify zero regression across existing backend suites.

---

## Phase 4: Definition of Done & Checklist Sync

- [ ] `T-TEAM-002-9` Verify all 10 Definition of Done criteria from Constitution §19.
- [ ] `T-TEAM-002-10` Update `TEAM-002` status to `✅ Done` in `implementation/00_IMPLEMENTATION_MASTER_CHECKLIST.md`.
