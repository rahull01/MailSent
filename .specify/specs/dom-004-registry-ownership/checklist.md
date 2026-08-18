# Quality Checklist: DOM-004 — Registry Ownership

<!--
  AUTHORITY & SUBORDINATION:
  Subordinate to .specify/specs/dom-004-registry-ownership/spec.md.
-->

## Quality & Governance Criteria

- [ ] **Authority**: Grounded strictly in `FUN-D4`, `INV-09`, `INV-10`, and `INV-14`.
- [ ] **No Fabrication (`N2`)**: Registry ownership relies strictly on persisted relations; zero synthetic metrics.
- [ ] **No Silent Loss (`N3`)**: Invalid ownership attempts and missing organizations fail loudly.
- [ ] **Zero Secrets in Code/Logs (`N4`)**: No credentials or sensitive data exposure.
- [ ] **Mandatory Automated Tests (`N5`)**: Unit and live PostgreSQL integration tests fully verify all ownership rules.
- [ ] **Multi-Tenant Isolation (`INV-09`)**: Every domain belongs to exactly one organization; no cross-agency leakage.
- [ ] **Global Uniqueness (`INV-10`)**: Enforced at application and database schema levels.
- [ ] **Referential Integrity (`INV-14`)**: Domain `organization_id` foreign key constraint prevents orphan domains.
- [ ] **Schema Immobility**: Flyway migrations `V1–V18` remain 100% frozen; zero new migrations created.
- [ ] **Definition of Done**: All 10 points in Constitution §19 satisfied.
