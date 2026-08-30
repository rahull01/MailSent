# Quality & Verification Checklist: [FEATURE NAME]

<!--
  AUTHORITY & SUBORDINATION:
  This Checklist is subordinate to MailSent's Engineering Standards:
  - Quality Review: docs/00_Foundation/PROJECT_CONSTITUTION.md (§19 DoD, §26 Review Checklist)
  - Testing & Verification: implementation/00_IMPLEMENTATION_MASTER_CHECKLIST.md (Standard Matrices)
-->

**Feature**: `[FEATURE NAME]` | **Branch**: `[###-feature-name]`  
**Master Checklist Task ID**: `[e.g., MAP-002, DIAG-002, COMP-001]`  
**Review Ownership**: Reviewer-owned quality and verification gate.  

---

## 1. Testing & Verification Matrix (T-MAT)

- [ ] `CHK-T01` **Happy Path**: Core user scenarios function strictly per acceptance criteria.
- [ ] `CHK-T02` **Edge Cases**: Boundary inputs, nulls, and unusual formats handled predictably.
- [ ] `CHK-T03` **Negative & Failure Paths**: System degrades gracefully on DB/adapter failures without crash (N3).
- [ ] `CHK-T04` **Diagnostic Accuracy**: Truthful reporting without guessed or fabricated states (N2).
- [ ] `CHK-T05` **Regression Suite**: All existing unit and integration tests pass (`mvn test`).

---

## 2. Security & Compliance Matrix (S-MAT)

- [ ] `CHK-S01` **Multi-Tenant Isolation (`INV-09`)**: Queries and cache keys strictly partitioned by `organization_id`.
- [ ] `CHK-S02` **Secrets Guard (`N4`)**: Zero credentials, tokens, or PII in source, logs, or test fixtures.
- [ ] `CHK-S03` **Input Validation**: All incoming REST payloads validated at boundary (`@Valid`, sanitized).
- [ ] `CHK-S04` **Least Privilege**: Role-based permissions enforced across all actions.

---

## 3. Observability Matrix (O-MAT)

- [ ] `CHK-O01` **Structured Logging**: MDC context (`requestId`, `organizationId`) attached to all logs.
- [ ] `CHK-O02` **No Silent Failure (`N3`)**: Caught exceptions logged with actionable context before handling.
- [ ] `CHK-O03` **Lineage (`INV-11`)**: Output entities cite source evidence IDs.

---

## 4. Definition of Done (DoD) Verification (Constitution §19)

- [ ] `CHK-D01` **Validated**: Solves real customer problem with 12+ month recurring value (Golden Rule).
- [ ] `CHK-D02` **Works**: Implemented and functioning per specification.
- [ ] `CHK-D03` **Tested**: Full automated test suite in place.
- [ ] `CHK-D04` **Verified**: Build clean, checks pass with evidence.
- [ ] `CHK-D05` **Reviewed**: Meets all criteria in Constitution §26.
- [ ] `CHK-D06` **Documented**: Necessary code comments and API docs complete.
- [ ] `CHK-D07` **Observable**: Logs, metrics, and tracing in place.
- [ ] `CHK-D08` **Secured**: Passed security checks without shortcuts.
- [ ] `CHK-D09` **Honest**: Status reported truthfully; UI copy clear and calm.
- [ ] `CHK-D10` **Committed & Clean**: No dead code, committed to appropriate feature branch.

---

## 5. Master Checklist Synchronization

- [ ] `CHK-SYNC` Parent task in `implementation/00_IMPLEMENTATION_MASTER_CHECKLIST.md` updated with accurate live status.
