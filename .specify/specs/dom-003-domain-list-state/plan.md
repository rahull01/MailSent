# Implementation Plan: DOM-003 — Domain List/State

<!--
  AUTHORITY & SUBORDINATION:
  Subordinate to .specify/specs/dom-003-domain-list-state/spec.md,
  docs/00_Foundation/MAILSENT_MASTER_WORKFLOW.md, and docs/00_Foundation/PROJECT_CONSTITUTION.md.
-->

## 1. Technical Context & Scope

- **Bounded Context**: `Portfolio and Client Management` (Domain sub-context) collaborating with `Identity and Access`.
- **Target Operations**: Organization-scoped domain listing and single-domain lookup query service with `Capability.HEALTH_VIEW` authorization.
- **Persistence Target**: Existing PostgreSQL table `domains` (`V5__create_domains.sql`).
- **Dependencies**: `DomainRepository`, `OrganizationRepository`, `AuthorizationService`.

---

## 2. Architecture & Design Decisions

1. **DTO Design**:
   - Introduce immutable record `DomainSummary(String id, String organizationId, String name, Instant createdAt)`.
2. **Repository Enhancements**:
   - Ensure `DomainRepository` / adapters support:
     - `List<Domain> findByOrganizationId(String organizationId)`
     - `Optional<Domain> findByName(String name)`
3. **Application Query Service**:
   - Create `DomainQueryService` (or add query methods to `DomainService`):
     - `listDomains(User caller, String organizationId)`: Enforces `HEALTH_VIEW` and tenant matching, validates organization existence, maps to `List<DomainSummary>`.
     - `getDomain(User caller, String organizationId, String name)`: Enforces `HEALTH_VIEW` and tenant matching, normalizes name, queries domain, returns `Optional<DomainSummary>` only if `domain.organizationId().equals(organizationId)`.
4. **Audit Logging**:
   - Uses `AuthorizationService` to evaluate access and emit audit events to `AccessAuditSink`.

---

## 3. Hermetic & Integration Testing Strategy

1. **Unit Tests (`DomainQueryServiceTest` / `DomainServiceTest`)**:
   - `Role.OWNER`, `Role.ANALYST`, and `Role.VIEWER` successfully list own organization's domains.
   - Cross-tenant caller rejected with `AccessDeniedException` (`INV-09`).
   - Unauthenticated caller rejected with `AccessDeniedException`.
   - Single-domain lookup by canonical name returns domain summary for own organization.
   - Single-domain lookup returns `Optional.empty()` if domain belongs to another organization (cross-tenant discovery protection).
   - Nonexistent domain returns `Optional.empty()`.
   - Rejection of nonexistent organization with `DomainOrganizationNotFoundException`.
   - Verification that no synthetic health or risk scores are produced (`N2`).
2. **PostgreSQL Integration Tests (`DomainListPersistenceIntegrationTest`)**:
   - Live PostgreSQL verification under `@ActiveProfiles("it")` with `-Dmailsent.it.postgres=true`.
   - Organization A and B domains persist; queries for Org A return strictly Org A domains; Org A cannot lookup Org B domain by name.
