# Quality Checklist: DOM-001 — Add Domain (Manual)

<!--
  AUTHORITY & SUBORDINATION:
  Subordinate to .specify/specs/dom-001-add-domain-manual/spec.md.
-->

## Quality & Governance Criteria

- [ ] **Authority**: Solves validated customer problem with 12+ month subscription value (The Golden Rule).
- [ ] **No Fabrication (`N2`)**: No placeholder health/status indicators generated during domain registration.
- [ ] **No Silent Loss (`N3`)**: Invalid syntax, nonexistent orgs, and duplicate registrations fail loudly with explicit exceptions.
- [ ] **Zero Secrets in Code/Logs (`N4`)**: No credentials or sensitive data logged or stored.
- [ ] **Mandatory Automated Tests (`N5`)**: 100% test coverage over success and failure/security paths in `DomainServiceTest`.
- [ ] **Multi-Tenant Isolation (`INV-09`)**: Caller cannot register a domain into an organization they do not own or belong to.
- [ ] **Global Uniqueness (`INV-10`)**: No duplicate domain registration allowed across the entire system.
- [ ] **Referential Integrity (`INV-14`)**: Domain always references an existing organization; verified by schema FK `fk_domains_organization`.
- [ ] **Schema Immobility**: Zero changes to Flyway migrations `V1–V18`; zero new migrations added.
- [ ] **Definition of Done**: All 10 points in Constitution §19 satisfied.
