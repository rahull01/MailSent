# Checklist: RES-003 — Match by DNS Provider

## Definition of Done (Constitution §19 & Task Criteria)

- [ ] **1. DNS Provider Matching & Invariants**
  - [x] Supported DNS providers modeled: `CLOUDFLARE`, `ROUTE53`, `GODADDY`, `GOOGLE_CLOUD_DNS`, `NAMECHEAP`, `GENERIC_DNS`.
  - [x] Automatic fallback to `GENERIC_DNS` on unrecognized nameservers.
  - [x] `N2` Never Fabricate Truth: Guidance steps and record values derived deterministically from diagnosed issue facts.
  - [x] `INV-09` Multi-Tenant Isolation: Action generation queries strictly scoped to `organization_id`.
  - [x] `INV-11` Deterministic Lineage: Output cites the originating `issueId` and `rootCause`.

- [ ] **2. Architectural & Bounded Context Integrity**
  - [x] Sits strictly within `Guidance and Resolution` Bounded Context (`com.mailsent.resolution`).
  - [x] Zero new database tables, zero Flyway migrations (`V1–V19` remain frozen).
  - [x] Backward compatible with existing `ResolutionService` consumers.
  - [x] Unit test coverage for all provider permutations, fallback rules, and tenant boundaries.
