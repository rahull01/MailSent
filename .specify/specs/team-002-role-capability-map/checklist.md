# Quality & Verification Checklist: TEAM-002 — Role→Capability Map (Least-Authority)

<!--
  AUTHORITY & SUBORDINATION:
  This Checklist is subordinate to MailSent's Engineering Standards:
  - Quality Review: docs/00_Foundation/PROJECT_CONSTITUTION.md (§19 DoD, §26 Review Checklist)
  - Testing & Verification: implementation/00_IMPLEMENTATION_MASTER_CHECKLIST.md (Standard Matrices)
-->

**Feature**: `TEAM-002 — Role→Capability Map` | **Branch**: `team-002-role-capability-map`  
**Master Checklist Task ID**: `TEAM-002` (P0)  
**Review Ownership**: Reviewer-owned quality and verification gate.  

---

## 1. Testing & Verification Matrix (T-MAT)

- [ ] `CHK-T01` **Happy Path**: `OWNER` has full capabilities; `ANALYST` has read + operational mutation; `VIEWER` has read only.
- [ ] `CHK-T02` **Edge Cases**: Unmapped capability tokens, empty strings, null user/role, or missing organization IDs fail securely with Deny.
- [ ] `CHK-T03` **Negative & Failure Paths**: Unauthorized capability invocations raise `AccessDeniedException` and log with MDC without crashing.
- [ ] `CHK-T04` **Diagnostic Accuracy**: Truthful evaluation; no implicit role escalation.
- [ ] `CHK-T05` **Regression Suite**: All existing unit tests pass cleanly (`mvn test`).

---

## 2. Security & Compliance Matrix (S-MAT)

- [ ] `CHK-S01` **Multi-Tenant Isolation (`INV-09`)**: A user in Organization A cannot exercise permissions in Organization B, regardless of role.
- [ ] `CHK-S02` **Secrets Guard (`N4`)**: Zero credentials or sensitive data in logging or tests.
- [ ] `CHK-S03` **Default-Deny**: Every capability check defaults to false/denied unless an explicit role grant exists.
- [ ] `CHK-S04` **Least Privilege (`NFR-S1`)**: Newly registered users (`VIEWER`) cannot perform any mutations.

---

## 3. Observability Matrix (O-MAT)

- [ ] `CHK-O01` **Structured Logging**: MDC context (`requestId`, `userId`, `organizationId`) attached to authorization denial events.
- [ ] `CHK-O02` **No Silent Failure (`N3`)**: Authorization rejections are surfaced and logged explicitly.
- [ ] `CHK-O03` **Lineage (`INV-11`)**: Audit log contains target capability and user identity.

---

## 4. Definition of Done (DoD) Verification (Constitution §19)

- [ ] `CHK-D01` **Validated**: Solves team access control and multi-tenant security for 12+ month subscription value.
- [ ] `CHK-D02` **Works**: Implemented and functioning strictly per capability matrix.
- [ ] `CHK-D03` **Tested**: Unit tests and failure-path matrix completely automated.
- [ ] `CHK-D04` **Verified**: Clean compile and test verification.
- [ ] `CHK-D05` **Reviewed**: Meets all criteria in Constitution §26.
- [ ] `CHK-D06` **Documented**: Domain javadoc for `Capability`, `RoleCapabilityPolicy`, and `AuthorizationService`.
- [ ] `CHK-D07` **Observable**: Rejections logged with MDC context.
- [ ] `CHK-D08` **Secured**: Passed security matrix (S-MAT).
- [ ] `CHK-D09` **Honest**: Status reported truthfully.
- [ ] `CHK-D10` **Committed & Clean**: No orphaned/speculative code.

---

## 5. Master Checklist Synchronization

- [ ] `CHK-SYNC` `TEAM-002` updated in `implementation/00_IMPLEMENTATION_MASTER_CHECKLIST.md`.
