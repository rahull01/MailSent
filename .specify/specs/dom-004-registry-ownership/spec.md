# Feature Specification: DOM-004 — Registry Ownership

<!--
  AUTHORITY & SUBORDINATION:
  This Feature Specification is a scoped operational slice subordinate to MailSent's authoritative documents:
  - Operational & Product Source of Truth: docs/00_Foundation/MAILSENT_MASTER_WORKFLOW.md
  - Engineering Truth: docs/00_Foundation/PROJECT_CONSTITUTION.md
  - Requirements Baseline: docs/02_Product_Design/FUNCTIONAL_REQUIREMENTS.md
  - Security Architecture: docs/03_Architecture/09_SECURITY_ARCHITECTURE.md
  - Bounded Contexts: docs/03_Architecture/03_BOUNDED_CONTEXTS.md
  - Data Model: docs/03_Architecture/07_DATA_MODEL.md
  - Preceding Implementation: DOM-001, DOM-002, DOM-003, PORT-001, PORT-002, PORT-003
-->

**Feature Branch**: `dom-004-registry-ownership`  
**Created**: 2026-08-18  
**Status**: Draft (Ready for CTO Review)  
**Input**: DOM-004 — Registry ownership specification.  

---

## 1. Governance & Traceability Anchors *(mandatory)*

- **Master Workflow Module**: WF-1 Agency Control Center & Phase 07 Domains
- **Parent Requirement**: `FUN-D4` ("Registry ownership: every sending domain belongs to exactly one Organization; agency portfolio views aggregate client domains without altering ownership").
- **Security Baseline**: `09_SECURITY_ARCHITECTURE.md` §4 ("The customer owns their organization, its users, and its domains"), `PROJECT_CONSTITUTION.md` (`INV-09` Multi-Tenant Isolation, `INV-10` Global Domain Uniqueness, `INV-14` Referential Integrity).
- **Master Checklist Task ID**: `DOM-004` (Phase 07 — Domains, P0)
- **The Golden Rule & Recurring Value**: Guarantees cryptographically and architecturally airtight domain ownership boundaries across standalone organizations and agency-client relationships.
- **Bounded Context**: `Portfolio and Client Management` (Domain & Portfolio sub-contexts) collaborating with `Identity and Access`.

---

## 2. Invariants & Ownership Principles

1. **Strict 1:N Organization Domain Ownership (`INV-09`, `01_DOMAIN_MODEL.md` §3)**:
   - Every `Domain` aggregate MUST belong to exactly one `Organization` (`organizationId` is mandatory and immutable).
   - No domain can exist without an owning organization (`INV-14`: backed by `organization_id NOT NULL REFERENCES organizations(id)` in PostgreSQL).
   - No orphan domains are ever permitted.
2. **Global Domain Uniqueness (`INV-10`)**:
   - A canonical sending domain hostname (case-folded) is globally unique across the entire platform.
   - It cannot be registered or claimed simultaneously by two different organizations.
3. **Ownership Immutability**:
   - Domain ownership is immutable throughout normal domain registration, querying, and import operations.
   - Domain reassignment between organizations is prohibited/deferred as no approved reassignment contract exists.
4. **Portfolio Aggregation vs. Ownership Boundary (`PORT-001` - `PORT-003`)**:
   - Agency portfolio views aggregate domains across assigned client organizations (`ClientRelationship`) purely as **read-only projections**.
   - Portfolio aggregation MUST NEVER transfer, duplicate, or mutate the underlying domain's `organizationId`.
   - Agency A cannot observe or query domains belonging to Agency B or Agency B's clients (`INV-09`).

---

## 3. Authorization Boundaries

- **`Capability.DOMAIN_MANAGE`**:
  - Required for domain registration, import, and registry mutation.
  - Role mapping: `Role.OWNER` only.
- **`Capability.HEALTH_VIEW`**:
  - Required for domain read/listing and portfolio observability.
  - Role mapping: `Role.OWNER`, `Role.ANALYST`, `Role.VIEWER`.
- **Tenant Scope Enforcement**:
  - All operations require `caller.organizationId().equals(targetOrganizationId)`. Cross-organization requests without an authorized relationship are rejected with `AccessDeniedException`.

---

## 4. N2 No Truth Fabrication

- Registry ownership rules evaluate and project strictly persisted relationships (`organizations`, `domains`, `client_relationships`).
- No synthetic health, risk, or compliance status values may be fabricated.

---

## 5. Persistence & Schema Integrity

- **Database Table**: Existing table `domains` created in frozen migration `V5__create_domains.sql`.
- **Constraints**: `uk_domains_name` (unique hostname) and `fk_domains_organization` (`FOREIGN KEY (organization_id) REFERENCES organizations(id)`).
- **Migration Policy**:
  - Zero changes to `V1–V18`.
  - Zero new migrations (`V19+`).

---

## 6. Testing Contract

1. **Hermetic Unit & Application Tests**:
   - Domain creation enforces non-null, non-blank `organizationId`.
   - Domain restoration preserves stored `organizationId` verbatim.
   - Domain registration fails if target organization does not exist (`INV-14`).
   - Duplicate domain registration across organizations is rejected (`INV-10`).
   - Portfolio aggregation reads client domains without modifying their `organizationId`.
2. **Real-PostgreSQL Integration Tests (`DomainRegistryOwnershipPersistenceIntegrationTest`)**:
   - Enforce database foreign key constraint: inserting a domain with a non-existent `organization_id` fails at the database level.
   - Enforce database unique constraint: inserting duplicate domain names across distinct organizations violates `uk_domains_name`.
   - Multi-tenant isolation: agency portfolio queries over live PostgreSQL return client domains while strictly preserving their client `organization_id`.
   - Agency A cannot observe Agency B's client domains in live PostgreSQL.
