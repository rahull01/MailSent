# Checklist: DIAG-004 — Issue Entity Lifecycle (Hardened Persistence Model)

## Definition of Done (Constitution §19 & Task Criteria)

- [ ] **1. Issue Domain Model & State Machine**
  - [x] State transitions formally frozen: `DISCOVERED` $\rightarrow$ `DIAGNOSED` $\rightarrow$ `RESOLVED` $\rightarrow$ `CLOSED` (with `REOPENED`).
  - [x] Clear semantics: `RESOLVED` (awaiting verification, active index) vs `CLOSED` (verified terminal, leaves active index).
  - [x] Reopen vs New: `RESOLVED` reopens on same failure; `CLOSED` spawns a new `Issue` for a fresh historical incident.

- [ ] **2. Invariant Conformance**
  - [x] **`N2` Never Fabricate Truth**: Issues created exclusively on `AT_RISK` signals with concrete `RootCause`.
  - [x] **`INV-09` Multi-Tenant Isolation**: Strict organization partition on all operations.
  - [x] **`INV-11` Deterministic Lineage**: Clear separation of immutable origin snapshot (`originHealthSignalId`, `originDmarcAggregateEvidenceIds`) from mutable ongoing state (`latestHealthSignalId`, `latestDmarcAggregateEvidenceIds`).
  - [x] **`INV-14` Referential Integrity**: Foreign keys enforce valid `organizations(id)` and `domains(id)`.

- [ ] **3. Persistence & Concurrency Strategy**
  - [x] Flyway `V19__create_issues.sql` schema defined.
  - [x] Partial unique index `uk_issues_active_fingerprint` prevents duplicate active issues on repeated observations.
  - [x] Evidence stored as standard serialized JSON arrays in `TEXT` columns.

- [ ] **4. Architectural & Bounded Context Integrity**
  - [x] Sits strictly within `Diagnosis` Bounded Context (`com.mailsent.diagnosis`).
  - [x] No score fabrication (delegated to `AI-002`).
  - [x] No remediation guide fabrication (delegated to `Guidance and Resolution`).
