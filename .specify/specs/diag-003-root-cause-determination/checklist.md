# Checklist: DIAG-003 — Root Cause Determination

## Definition of Done (Constitution §19 & Task Criteria)

- [ ] **1. Taxonomy & Classification**
  - [x] `DMARC_REJECT`
  - [x] `DMARC_QUARANTINE`
  - [x] `SPF_ALIGNMENT_FAILURE`
  - [x] `DKIM_ALIGNMENT_FAILURE`
  - [x] `SPF_AUTHENTICATION_FAILURE`
  - [x] `DKIM_AUTHENTICATION_FAILURE`
  - [x] `UNKNOWN_ISSUE`

- [ ] **2. Invariant Conformance**
  - [x] **`N2` Never Fabricate Truth**: Returns `Optional.empty()` for non-at-risk signals (`HEALTHY`/`UNKNOWN`).
  - [x] **`INV-09` Multi-Tenant Isolation**: Operates exclusively over cited lineage evidence of the target domain.
  - [x] **`INV-11` Deterministic Lineage**: Derived strictly from cited `dmarcAggregateEvidenceIds`.
  - [x] **Deterministic Precedence**: Disposition $\rightarrow$ Alignment $\rightarrow$ Authentication $\rightarrow$ Unknown.

- [ ] **3. Architectural Boundaries**
  - [x] Pure in-memory analyzer service.
  - [x] No database table created.
  - [x] No Flyway migrations created (`V1–V18` 100% frozen).
  - [x] Clean bounded context separation from `HEALTH-001` and `EXPL-001`.
