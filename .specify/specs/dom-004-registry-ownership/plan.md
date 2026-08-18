# Implementation Plan: DOM-004 — Registry Ownership

<!--
  AUTHORITY & SUBORDINATION:
  Subordinate to .specify/specs/dom-004-registry-ownership/spec.md,
  docs/00_Foundation/MAILSENT_MASTER_WORKFLOW.md, and docs/00_Foundation/PROJECT_CONSTITUTION.md.
-->

## 1. Technical Context & Scope

- **Bounded Context**: `Portfolio and Client Management` (Domain & Portfolio sub-contexts) collaborating with `Identity and Access`.
- **Primary Objective**: Formalize and verify domain registry ownership invariants across standalone domains and portfolio aggregations (`FUN-D4`, `INV-09`, `INV-10`, `INV-14`).
- **Persistence Target**: Existing PostgreSQL tables `domains` (`V5`), `organizations` (`V3`), and `client_relationships` (`V4`).

---

## 2. Architecture & Design Principles

1. **Ownership Enforcement at Entity & Application Layers**:
   - `Domain.java`: Guarantees `organizationId` is non-null and immutable on registration and restoration.
   - `DomainService.java` & `DomainImportService.java`: Reuses existing existence check (`INV-14`) and uniqueness check (`INV-10`).
   - `PortfolioService.java` & `PortfolioOverviewService.java`: Reuses client domain loading without modifying domain aggregate state.
2. **Database Integrity Backstop**:
   - `fk_domains_organization`: Ensures zero orphan domains can exist.
   - `uk_domains_name`: Ensures global domain uniqueness.
3. **No Code Sprawl / No Unneeded Migrations**:
   - The required domain aggregates, services, and repositories are fully implemented and verified in DOM-001–003 and PORT-001–003.
   - Zero schema migrations (`V1–V18` remain frozen).

---

## 3. Verification Strategy

1. **Unit / Application Tests (`DomainRegistryOwnershipTest`)**:
   - Verify `Domain` entity rejects null/blank `organizationId`.
   - Verify `DomainService` rejects orphan domain registration.
   - Verify portfolio projection retains client `organizationId`.
   - Verify zero mutable ownership setters exist on `Domain`.
2. **Real-PostgreSQL Integration Tests (`DomainRegistryOwnershipPersistenceIntegrationTest`)**:
   - Run scoped `@DataJpaTest` with PostgreSQL 16 on `localhost:5433`.
   - Verify database FK and UNIQUE constraint enforcement.
   - Verify agency portfolio aggregation preserves client domain ownership under real database relations.
   - Verify multi-tenant partition between agencies.
