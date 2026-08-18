---
description: "Task list for TEAM-003: Access audit record"
---

# Tasks: TEAM-003 — Access Audit Record

<!--
  AUTHORITY & SUBORDINATION:
  This Task List is subordinate to MailSent's Engineering Execution System:
  - Execution Master: implementation/00_IMPLEMENTATION_MASTER_CHECKLIST.md
  - Engineering Truth: docs/00_Foundation/PROJECT_CONSTITUTION.md
-->

**Input**: Design documents from `.specify/specs/team-003-access-audit-record/`  
**Prerequisites**: `spec.md`, `plan.md`  
**Parent Master Checklist Task**: `TEAM-003` (Phase 04 — Teams, P1)  
**Traceability Anchors**: `CP: CP-8 | Req: NFR-AU | Arch: 09_SECURITY_ARCHITECTURE.md | WF: Phase 04`  

---

## Phase 1: Domain Models & Port Interface

- [ ] `T-TEAM-003-1` [P] Implement `AccessDecision.java` enum (`GRANTED`, `DENIED`) in `backend/src/main/java/com/mailsent/identity/team/domain/AccessDecision.java`.
- [ ] `T-TEAM-003-2` [P] Implement immutable `AccessAuditRecord.java` value object in `backend/src/main/java/com/mailsent/identity/team/domain/AccessAuditRecord.java`.
- [ ] `T-TEAM-003-3` [P] Implement `AccessAuditSink.java` port interface in `backend/src/main/java/com/mailsent/identity/team/domain/AccessAuditSink.java`.

---

## Phase 2: Infrastructure Adapter & Service Integration

- [ ] `T-TEAM-003-4` Implement `Slf4jAccessAuditLogger.java` adapter in `backend/src/main/java/com/mailsent/identity/team/infrastructure/Slf4jAccessAuditLogger.java` with structured MDC logging.
- [ ] `T-TEAM-003-5` Wire `AccessAuditSink` into `AuthorizationService.java` in `backend/src/main/java/com/mailsent/identity/team/application/AuthorizationService.java` to record all decisions without altering default-deny semantics.

---

## Phase 3: Automated Testing & Verification (T-MAT)

- [ ] `T-TEAM-003-6` [P] Implement unit tests in `backend/src/test/java/com/mailsent/identity/team/domain/AccessAuditRecordTest.java` verifying immutability, field validation, and null safety.
- [ ] `T-TEAM-003-7` Implement integration/audit tests in `backend/src/test/java/com/mailsent/identity/team/application/AuthorizationServiceAuditTest.java` verifying:
  - Emission of `DENIED` record on unauthorized role attempt
  - Emission of `DENIED` record on cross-tenant mismatch attempt (`INV-09`)
  - Emission of `DENIED` record on null or unauthenticated user attempt
  - Emission of `GRANTED` record on authorized capability evaluation
- [ ] `T-TEAM-003-8` Run `mvn test` verifying full test suite passes with zero regressions.

---

## Phase 4: Definition of Done & Checklist Sync

- [ ] `T-TEAM-003-9` Verify all 10 Definition of Done criteria (Constitution §19).
- [ ] `T-TEAM-003-10` Update `TEAM-003` status to `✅ Done` in `implementation/00_IMPLEMENTATION_MASTER_CHECKLIST.md`.
