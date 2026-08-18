# Quality Checklist: DOM-003 — Domain List/State

<!--
  AUTHORITY & SUBORDINATION:
  Subordinate to .specify/specs/dom-003-domain-list-state/spec.md.
-->

## Quality & Governance Criteria

- [ ] **Authority**: Grounded in `FUN-D3` and solves validated domain visibility need without speculative features.
- [ ] **No Fabrication (`N2`)**: Projections contain strictly grounded persisted metadata; zero fake health/risk indicators.
- [ ] **No Silent Loss (`N3`)**: Query boundaries and error cases fail loudly or return explicit typed `Optional.empty()`.
- [ ] **Zero Secrets in Code/Logs (`N4`)**: No credential leaks or sensitive data exposure.
- [ ] **Mandatory Automated Tests (`N5`)**: 100% unit and PostgreSQL integration test coverage.
- [ ] **Multi-Tenant Isolation (`INV-09`)**: Queries are strictly organization-scoped; cross-tenant discovery is impossible.
- [ ] **Global Uniqueness (`INV-10`)**: Supported by canonical normalization in lookups.
- [ ] **Referential Integrity (`INV-14`)**: Organization existence validated; domain foreign keys preserved.
- [ ] **Schema Immobility**: Zero changes to Flyway migrations `V1–V18`; zero new migrations added.
- [ ] **Definition of Done**: All 10 points in Constitution §19 satisfied.
