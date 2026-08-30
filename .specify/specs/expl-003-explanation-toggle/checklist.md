# Checklist: EXPL-003 — Simpler Toggle

## Definition of Done (Constitution §19 & Task Criteria)

- [ ] **1. Mode Definitions & Invariants**
  - [x] Canonical modes defined: `BUSINESS_SUMMARY`, `TECHNICAL_DEEP_DIVE`.
  - [x] Identical underlying truth and evidence across both modes (`N2`).
  - [x] `INV-09` Multi-Tenant Isolation: Queries strictly partitioned by `organizationId` and `domainId`.
  - [x] `INV-11` Deterministic Lineage: Output retains immutable `Grounding` citing source snapshots.

- [ ] **2. Architectural & Bounded Context Integrity**
  - [x] Sits strictly within `Business Explanation` Bounded Context (`com.mailsent.explanation`).
  - [x] Zero new database tables, zero Flyway migrations.
  - [x] Backward compatible with existing `ExplanationService` consumers.
  - [x] Unit test coverage for all modes, indications, and tenant isolation paths.
