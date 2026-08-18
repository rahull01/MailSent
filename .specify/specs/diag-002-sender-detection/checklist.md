# Checklist: DIAG-002 — Sender Detection (Recognized Providers)

## Definition of Done (Constitution §19 & Task Criteria)

- [ ] **1. Frozen Provider Catalog Supported**
  - [x] Google Workspace (`google_workspace`)
  - [x] Microsoft 365 (`microsoft_365`)
  - [x] Salesforce (`salesforce`)
  - [x] Mailchimp (`mailchimp`)

- [ ] **2. Invariant Conformance**
  - [x] **`N2` Never Fabricate Truth**: Unrecognized senders return `Optional.empty()`; no guessing.
  - [x] **`INV-09` Multi-Tenant Isolation**: Scoped strictly to provided domain evidence snapshot.
  - [x] **`INV-11` Deterministic Lineage**: Derived strictly from cited DKIM/envelope facts.
  - [x] **Zero Health Scoring**: No health states or trust scores generated.

- [ ] **3. Matching Semantics**
  - [x] Primary evaluation on `dkimDomains`.
  - [x] Secondary evaluation on `envelopeFrom`.
  - [x] Case-insensitive domain and envelope matching.
  - [x] Handling of multiple DKIM domains.
  - [x] Null / missing evidence handling.

- [ ] **4. Architectural & Migration Boundaries**
  - [x] Pure in-memory analyzer service.
  - [x] No database table created.
  - [x] No Flyway migrations created (`V1–V18` 100% frozen).
  - [x] Unrelated contexts (`HEALTH-001`, `DIAG-003`, `MAP-002`) untouched.
