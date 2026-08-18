# Feature Specification: DOM-003 — Domain List/State

<!--
  AUTHORITY & SUBORDINATION:
  This Feature Specification is a scoped operational slice subordinate to MailSent's authoritative documents:
  - Operational & Product Source of Truth: docs/00_Foundation/MAILSENT_MASTER_WORKFLOW.md
  - Engineering Truth: docs/00_Foundation/PROJECT_CONSTITUTION.md
  - Requirements Baseline: docs/02_Product_Design/FUNCTIONAL_REQUIREMENTS.md
  - Security Architecture: docs/03_Architecture/09_SECURITY_ARCHITECTURE.md
  - Bounded Contexts: docs/03_Architecture/03_BOUNDED_CONTEXTS.md
  - Data Model: docs/03_Architecture/07_DATA_MODEL.md
  - Preceding Implementation: DOM-001, DOM-002, TEAM-002, TEAM-003, ORG-003
-->

**Feature Branch**: `dom-003-domain-list-state`  
**Created**: 2026-08-18  
**Status**: Draft (Ready for CTO Review)  
**Input**: DOM-003 — Domain list/state contract specification.  

---

## 1. Governance & Traceability Anchors *(mandatory)*

- **Master Workflow Module**: WF-1 Agency Control Center & WF-2 Onboarding / Phase 07 Domains
- **Parent Requirement**: `FUN-D3` ("Domain list: view all sending domains for an organization, with status (active/paused/error) and key properties").
- **Security Baseline**: `09_SECURITY_ARCHITECTURE.md` §2 (Least authority, Separation of responsibility `INV-09`, `INV-10`), `09_SECURITY_ARCHITECTURE.md` §4 (Ownership principles: "The customer owns their organization, its users, and its domains").
- **Master Checklist Task ID**: `DOM-003` (Phase 07 — Domains, P0)
- **The Golden Rule & Recurring Value**: Provides authenticated organization members with visibility into all registered sending domains, their registration metadata, and verified properties under strict multi-tenant isolation.
- **Bounded Context**: `Portfolio and Client Management` (Domain sub-context) collaborating with `Identity and Access`.

---

## 2. Actor Authorization & Capability Requirements

1. **Authorization Grounding (`TEAM-002`, `09_SECURITY_ARCHITECTURE.md` §4, `INV-09`)**:
   - Domain listing and read access is governed by `Capability.HEALTH_VIEW` (*"Read Health Signal, Health Trend, and raw evidence snapshots / domain observability"*).
   - In accordance with `RoleCapabilityPolicy` (TEAM-002):
     - `Role.OWNER`: Authorized for `Capability.HEALTH_VIEW` (true).
     - `Role.ANALYST`: Authorized for `Capability.HEALTH_VIEW` (true).
     - `Role.VIEWER`: Authorized for `Capability.HEALTH_VIEW` (true).
   - **Cross-Organization Denial (`INV-09`)**: Caller must belong to the target organization (`caller.organizationId().equals(targetOrganizationId)`). If caller's home organization does not match the target organization or caller is unauthenticated, access is denied with `AccessDeniedException`.
2. **Audit Logging (`TEAM-003`)**:
   - Authorization evaluations pass through `AuthorizationService`, recording structured audit events to `AccessAuditSink`.

---

## 3. Grounded Data & N2 No Truth Fabrication

1. **Persisted Properties Only**:
   - The returned `DomainSummary` projection exposes ONLY real, observed, and persisted domain properties from the PostgreSQL `domains` table:
     - `id`: Stable UUID string.
     - `organizationId`: Stable UUID string of the owning organization.
     - `name`: Canonical (lowercase) hostname string.
     - `createdAt`: Instant of domain registration.
2. **Strict Prohibition of Synthetic State (`N2`)**:
   - The term "state" in `FUN-D3` refers to persisted lifecycle/metadata; it MUST NOT invent or fabricate:
     - Synthetic health percentages or trust scores (e.g., "98% healthy").
     - Guessed risk statuses (e.g., "at-risk" before Phase 10 Health Signal exists).
     - Mock reputation or compliance indicators.
   - Downstream health and diagnostic state are supplied exclusively by Phase 08 Observation Evidence and Phase 10 Health Signals.

---

## 4. Multi-Tenant Isolation (`INV-09`) & Query Scoping

1. **Organization-Scoped List Query**:
   - Queries must be executed against the database filtered strictly by `organization_id`:
     `List<Domain> findByOrganizationId(String organizationId)`
   - Never loads the full database table into memory to filter by tenant in code.
2. **Organization-Scoped Single-Domain Lookup**:
   - Lookup by domain name must enforce organization ownership:
     `Optional<Domain> findByNameAndOrganizationId(String name, String organizationId)` (or verifying `domain.organizationId().equals(callerOrgId)`).
   - Organization A CANNOT discover or observe a domain registered by Organization B, even if Organization A queries the exact canonical hostname.
   - Cross-organization queries return empty (`Optional.empty()`), preserving privacy and isolation.

---

## 5. Application Boundary & DTO Projection Design

1. **Application Service Interface**:
   - Following established project architecture, `DomainQueryService` (or enhanced `DomainService`) exposes:
     - `List<DomainSummary> listDomains(User caller, String organizationId)`
     - `Optional<DomainSummary> getDomain(User caller, String organizationId, String domainName)`
2. **Projection DTO Record**:
   ```java
   public record DomainSummary(
       String id,
       String organizationId,
       String name,
       Instant createdAt
   ) {}
   ```
   - Encapsulates domain properties cleanly without leaking JPA entity mappings.

---

## 6. Persistence & Schema Integrity

1. **Database Schema**:
   - Uses existing table `domains` created in frozen migration `V5__create_domains.sql`.
   - Index `uk_domains_name` and foreign key `fk_domains_organization`.
2. **No Schema Migrations**:
   - Zero changes to `V1–V18`.
   - Zero new migrations (`V19+`) required.

---

## 7. Error Semantics

- **Unauthenticated caller (`caller == null`)**: Throws `AccessDeniedException`.
- **Cross-tenant caller (`caller.organizationId() != targetOrganizationId`)**: Throws `AccessDeniedException` (`INV-09`).
- **Missing / Blank organizationId**: Throws `AccessDeniedException` or `IllegalArgumentException`.
- **Missing / Nonexistent organization**: Throws `DomainOrganizationNotFoundException` (`INV-14`).
- **Domain not found**: Returns `Optional.empty()` for single-domain lookup; empty list `List.of()` for list query.
