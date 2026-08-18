# Quality & Verification Checklist: TEAM-004 — Team List Query (Revised)

<!--
  AUTHORITY & SUBORDINATION:
  This Checklist is subordinate to MailSent's Engineering Standards:
  - Quality Review: docs/00_Foundation/PROJECT_CONSTITUTION.md (§19 DoD, §26 Review Checklist)
  - Testing & Verification: implementation/00_IMPLEMENTATION_MASTER_CHECKLIST.md (Standard Matrices)
-->

**Feature**: `TEAM-004 — Team List Query` | **Branch**: `team-004-team-list-query`  
**Master Checklist Task ID**: `TEAM-004` (P1)  
**Review Ownership**: Reviewer-owned quality and verification gate.  

---

## 1. Testing & Verification Matrix (T-MAT)

- [ ] `CHK-T01` **Happy Path**: Any authenticated member (`OWNER`, `ANALYST`, `VIEWER`) can query their own organization's team member list.
- [ ] `CHK-T02` **Multi-Tenant Isolation (`INV-09`)**: A user in Organization A querying Organization B throws `AccessDeniedException`.
- [ ] `CHK-T03` **Edge Cases**: Empty organization returns empty list; unauthenticated user throws `AccessDeniedException`.
- [ ] `CHK-T04` **Deterministic Ordering**: Team member list is ordered chronologically by `createdAt ASC, id ASC`.
- [ ] `CHK-T05` **Regression Suite**: All existing tests pass cleanly (`mvn test`).

---

## 2. Security & Compliance Matrix (S-MAT)

- [ ] `CHK-S01` **Multi-Tenant Isolation (`INV-09`)**: Queries strictly partitioned by `organization_id`.
- [ ] `CHK-S02` **Secrets Guard (`N4`)**: Password hashes and sensitive tokens are strictly excluded from DTOs.
- [ ] `CHK-S03` **No Flyway Migrations**: Reuses existing `organization_id` column on `users` table; no schema changes.

---

## 3. Observability Matrix (O-MAT)

- [ ] `CHK-O01` **Structured Logging**: Authorization denials logged via `AuthorizationService` with MDC context.
- [ ] `CHK-O02` **No Silent Failure (`N3`)**: Failures are surfaced with meaningful domain exceptions.

---

## 4. Definition of Done (DoD) Verification (Constitution §19)

- [ ] `CHK-D01` **Validated**: Enables team visibility within tenant boundaries per `FUN-D5`.
- [ ] `CHK-D02` **Works**: Implemented and functioning strictly per revised specification.
- [ ] `CHK-D03` **Tested**: Unit and security isolation tests automated.
- [ ] `CHK-D04` **Verified**: Clean compile and test verification.
- [ ] `CHK-D05` **Reviewed**: Meets all criteria in Constitution §26.
- [ ] `CHK-D06` **Documented**: Javadoc complete for `TeamQueryService` and `TeamMemberSummary`.
- [ ] `CHK-D07` **Observable**: MDC logging in place.
- [ ] `CHK-D08` **Secured**: Passed security matrix (S-MAT).
- [ ] `CHK-D09` **Honest**: Accurate team list representation.
- [ ] `CHK-D10` **Committed & Clean**: Zero dead code.
