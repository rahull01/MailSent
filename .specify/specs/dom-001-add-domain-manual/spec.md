# Feature Specification: DOM-001 — Add Domain (Manual)

<!--
  AUTHORITY & SUBORDINATION:
  This Feature Specification is a scoped operational slice subordinate to MailSent's authoritative documents:
  - Operational & Product Source of Truth: docs/00_Foundation/MAILSENT_MASTER_WORKFLOW.md
  - Engineering Truth: docs/00_Foundation/PROJECT_CONSTITUTION.md
  - Requirements Baseline: docs/02_Product_Design/FUNCTIONAL_REQUIREMENTS.md
  - Security Architecture: docs/03_Architecture/09_SECURITY_ARCHITECTURE.md
  - Bounded Contexts: docs/03_Architecture/03_BOUNDED_CONTEXTS.md
  - Data Model: docs/03_Architecture/07_DATA_MODEL.md
  - Preceding Implementation: ORG-003, TEAM-002, TEAM-003 (Domain Aggregate, Service, Repo, & Capability Model)
-->

**Feature Branch**: `dom-001-add-domain-manual`  
**Created**: 2026-08-18  
**Status**: Draft (Ready for CTO Review)  
**Input**: DOM-001 — Add domain (manual) contract specification.  

---

## 1. Governance & Traceability Anchors *(mandatory)*

- **Master Workflow Module**: WF-1 Agency Control Center & WF-2 Onboarding / Phase 07 Domains
- **Parent Requirement**: `FUN-D1` ("Add a sending domain manually (single domain input). Validates domain format; associates with authenticated user's organization; enforces uniqueness within the platform").
- **Security Baseline**: `09_SECURITY_ARCHITECTURE.md` §2 (Least authority, Separation of responsibility `INV-09`, `INV-10`), `09_SECURITY_ARCHITECTURE.md` §4 (Ownership principles: "The customer owns their organization, its users, and its domains").
- **Master Checklist Task ID**: `DOM-001` (Phase 07 — Domains, P0)
- **The Golden Rule & Recurring Value**: Establishes the authoritative primary domain registry per organization so sending infrastructure, DNS records, and email deliverability can be tracked and monitored.
- **Bounded Context**: `Portfolio and Client Management` (Domain sub-context) collaborating with `Identity and Access`.

---

## 2. Actor Authorization & Capability Requirements

1. **Authorization Grounding (`TEAM-002`, `09_SECURITY_ARCHITECTURE.md` §4, `INV-09`)**:
   - Adding a sending domain is an organization management mutation governed by `Capability.DOMAIN_MANAGE` (*"Add, pause, or remove domains in the portfolio registry"*).
   - In accordance with `RoleCapabilityPolicy` (TEAM-002):
     - `Role.OWNER`: Authorized for `Capability.DOMAIN_MANAGE` (true).
     - `Role.ANALYST`: Not authorized for `Capability.DOMAIN_MANAGE` (false, default-deny).
     - `Role.VIEWER`: Not authorized for `Capability.DOMAIN_MANAGE` (false, default-deny).
   - **Cross-Organization Denial (`INV-09`)**: Caller must belong to the target organization (`caller.organizationId().equals(targetOrganizationId)`). If caller's organization does not match or caller lacks `Capability.DOMAIN_MANAGE`, `AuthorizationService.isAuthorized(...)` returns false, resulting in `AccessDeniedException`.
2. **Audit Logging (`TEAM-003`)**:
   - Authorization evaluations are passed through `AuthorizationService`, recording structured audit events to `AccessAuditSink`.

---

## 3. Organization Validation & Referential Integrity (`INV-14`)

1. **Owning Organization Existence**:
   - The target organization MUST exist in `OrganizationRepository`.
   - If the organization does not exist, throw `DomainOrganizationNotFoundException(organizationId)` with standard HTTP 404 / domain error mapping.
2. **Referential Integrity (`INV-14`)**:
   - Enforced at both domain/service boundary and database level via `fk_domains_organization` foreign key in `V5__create_domains.sql`.

---

## 4. Domain Name Format Validation & Normalization

1. **Validation Rules**:
   - Name must not be null or blank.
   - Name must not exceed 253 characters (RFC 1035 / schema limit in `V5__create_domains.sql`).
   - Must conform to canonical hostname syntax: alphanumeric labels separated by dots, no leading/trailing dots or spaces, valid TLD.
2. **Canonical Normalization**:
   - Stripped of leading/trailing whitespace (`trim()`).
   - Case-folded to lowercase (`toLowerCase(Locale.ROOT)`) via `Domain.normalize(name)` ensuring case-insensitive global uniqueness and consistent querying.

---

## 5. Global Domain Uniqueness (`INV-10`) & Duplicate Handling

1. **Invariant `INV-10` (Global Uniqueness)**:
   - A real-world domain name can be registered by exactly ONE organization across the entire platform.
   - No two organizations may register the same normalized domain name.
2. **Duplicate Conflict Behavior**:
   - If `DomainRepository.existsByName(normalizedName)` returns true, `DomainService.register(...)` throws `DomainAlreadyExistsException(normalizedName)`.
   - Schema enforcement: Backstopped by `uk_domains_name` unique constraint on `domains(name)` in `V5__create_domains.sql`.

---

## 6. Persistence & Schema Alignment

1. **Database Schema**:
   - Backed strictly by table `domains` created in frozen Flyway migration `V5__create_domains.sql`.
   - Columns: `id VARCHAR(36) PRIMARY KEY`, `organization_id VARCHAR(36) NOT NULL REFERENCES organizations(id)`, `name VARCHAR(253) NOT NULL`, `created_at TIMESTAMP WITH TIME ZONE NOT NULL`.
2. **No Schema Migrations**:
   - Zero modifications to `V1–V18`.
   - Zero new migrations (`V19+`) required.

---

## 7. Delivery & Boundary Semantics

1. **Scope Limits**:
   - `DOM-001` handles registration and validation only.
   - Does NOT trigger automated DNS scanning, SPF/DKIM verification, or reputation lookups (those belong to Phase 08 `INF-001`/`INF-002`).
   - Does NOT produce fake health scores (`N2`).
