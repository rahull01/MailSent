# Quality & Verification Checklist: CLIENT-002 — Assign Client to Agency (Revised)

<!--
  AUTHORITY & SUBORDINATION:
  This Checklist is subordinate to MailSent's Engineering Standards:
  - Quality Review: docs/00_Foundation/PROJECT_CONSTITUTION.md (§19 DoD, §26 Review Checklist)
  - Testing & Verification: implementation/00_IMPLEMENTATION_MASTER_CHECKLIST.md (Standard Matrices)
-->

**Feature**: `CLIENT-002 — Assign Client to Agency` | **Branch**: `client-002-assign-client-to-agency`  
**Master Checklist Task ID**: `CLIENT-002` (P0)  
**Review Ownership**: Reviewer-owned quality and verification gate.  

---

## 1. Testing & Verification Matrix (T-MAT)

- [ ] `CHK-T01` **Happy Path**: Agency Owner successfully assigns an existing client organization.
- [ ] `CHK-T02` **Idempotency**: Attempting to assign an already-active client returns the existing relationship without duplication.
- [ ] `CHK-T03` **Multi-Tenant Isolation (`INV-09`)**: Cross-tenant or non-member caller throws `AccessDeniedException`.
- [ ] `CHK-T04` **Least Authority**: Non-owner roles (`ANALYST`, `VIEWER`) cannot execute client assignments.
- [ ] `CHK-T05` **Entity Validation**: Missing agency/client throws `OrganizationNotFoundException`; self-assignment throws `IllegalArgumentException`.
- [ ] `CHK-T06` **Regression Suite**: All existing tests pass cleanly (`mvn test`).

---

## 2. Security & Compliance Matrix (S-MAT)

- [ ] `CHK-S01` **Multi-Tenant Isolation (`INV-09`)**: Caller's home organization must match `agencyOrganizationId`.
- [ ] `CHK-S02` **Secrets Guard (`N4`)**: Zero credentials or sensitive data in logging or responses.
- [ ] `CHK-S03` **No Database Migrations**: Reuses existing `client_relationships` table (V7 migration).

---

## 3. Observability Matrix (O-MAT)

- [ ] `CHK-O01` **Structured Logging**: Authorization denials logged with MDC context via `AccessAuditSink`.
- [ ] `CHK-O02` **No Silent Failure (`N3`)**: Failures are surfaced with explicit domain exceptions.

---

## 4. Definition of Done (DoD) Verification (Constitution §19)

- [ ] `CHK-D01` **Validated**: Enables agency-client management for multi-tenant portfolios.
- [ ] `CHK-D02` **Works**: Implemented and functioning strictly per specification.
- [ ] `CHK-D03` **Tested**: Unit and security isolation tests automated.
- [ ] `CHK-D04` **Verified**: Clean compile and test verification.
- [ ] `CHK-D05` **Reviewed**: Meets all criteria in Constitution §26.
- [ ] `CHK-D06` **Documented**: Javadoc complete for assignment methods.
- [ ] `CHK-D07` **Observable**: MDC logging in place.
- [ ] `CHK-D08` **Secured**: Passed security matrix (S-MAT).
- [ ] `CHK-D09` **Honest**: Accurate relationship state.
- [ ] `CHK-D10` **Committed & Clean**: Zero dead code.
