---
description: "Task list for TEAM-004: Team list query"
---

# Tasks: TEAM-004 — Team List Query (Revised)

<!--
  AUTHORITY & SUBORDINATION:
  This Task List is subordinate to MailSent's Engineering Execution System:
  - Execution Master: implementation/00_IMPLEMENTATION_MASTER_CHECKLIST.md
  - Engineering Truth: docs/00_Foundation/PROJECT_CONSTITUTION.md
-->

**Input**: Design documents from `.specify/specs/team-004-team-list-query/`  
**Prerequisites**: `spec.md`, `plan.md`  
**Parent Master Checklist Task**: `TEAM-004` (Phase 04 — Teams, P1)  
**Traceability Anchors**: `CP: CP-8 | Req: FUN-D5 | Arch: 09_SECURITY_ARCHITECTURE.md | WF: Phase 04`  

---

## Phase 1: Persistence Port & Adapter Enhancements

- [ ] `T-TEAM-004-1` [P] Add `List<User> findAllByOrganizationId(String organizationId)` to `UserRepository.java`.
- [ ] `T-TEAM-004-2` [P] Implement `findAllByOrganizationId` in `InMemoryUserRepository.java`.
- [ ] `T-TEAM-004-3` [P] Add `findByOrganizationIdOrderByCreatedAtAscIdAsc` to `JpaUserRepository.java` and implement in `JpaUserRepositoryAdapter.java`.

---

## Phase 2: DTO & Application Service

- [ ] `T-TEAM-004-4` [P] Implement immutable record `TeamMemberSummary(String userId, String email, Role role, Instant createdAt)` in `backend/src/main/java/com/mailsent/identity/team/application/TeamMemberSummary.java`.
- [ ] `T-TEAM-004-5` Implement `TeamQueryService.java` in `backend/src/main/java/com/mailsent/identity/team/application/TeamQueryService.java` enforcing `INV-09` tenant boundaries and projecting `User` to `TeamMemberSummary`.
- [ ] `T-TEAM-004-6` Register `TeamQueryService` bean in `TeamConfig.java`.

---

## Phase 3: Automated Testing & Verification (T-MAT)

- [ ] `T-TEAM-004-7` Implement unit & security isolation tests in `backend/src/test/java/com/mailsent/identity/team/application/TeamQueryServiceTest.java`:
  - Happy path: Owner, Analyst, and Viewer query own organization team members.
  - Multi-tenant isolation (`INV-09`): User from Org A querying Org B throws `AccessDeniedException`.
  - Null/unauthenticated caller throws `AccessDeniedException`.
  - Verifies zero password hashes in returned `TeamMemberSummary` (`N4`).
  - Verifies deterministic ordering by `createdAt ASC, id ASC`.
- [ ] `T-TEAM-004-8` Run `mvn test` verifying full test suite passes with zero regressions.

---

## Phase 4: Definition of Done & Checklist Sync

- [ ] `T-TEAM-004-9` Verify all 10 Definition of Done criteria (Constitution §19).
- [ ] `T-TEAM-004-10` Update `TEAM-004` status to `✅ Done` in `implementation/00_IMPLEMENTATION_MASTER_CHECKLIST.md`.
