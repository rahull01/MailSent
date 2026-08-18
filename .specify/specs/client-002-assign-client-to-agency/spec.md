# Feature Specification: CLIENT-002 — Assign Client to Agency (Revised)

<!--
  AUTHORITY & SUBORDINATION:
  This Feature Specification is a scoped operational slice subordinate to MailSent's authoritative documents:
  - Operational & Product Source of Truth: docs/00_Foundation/MAILSENT_MASTER_WORKFLOW.md
  - Engineering Truth: docs/00_Foundation/PROJECT_CONSTITUTION.md
  - Requirements Baseline: docs/02_Product_Design/ (FUNCTIONAL_REQUIREMENTS.md)
  - Security Architecture: docs/03_Architecture/09_SECURITY_ARCHITECTURE.md
  - Bounded Contexts: docs/03_Architecture/03_BOUNDED_CONTEXTS.md
  - Data Model: docs/03_Architecture/07_DATA_MODEL.md
  - Preceding Implementation: CLIENT-001 (ClientRelationship Aggregate, Service, & Repository)
-->

**Feature Branch**: `client-002-assign-client-to-agency`  
**Created**: 2026-08-18  
**Status**: Draft (CTO Revision)  
**Input**: CLIENT-002 — Assign client to agency contract specification.  

---

## 1. Governance & Traceability Anchors *(mandatory)*

- **Master Workflow Module**: WF-1 Agency Control Center & Feature Pipeline / Phase 05 Clients
- **Parent Requirement**: `FUN-D3` ("Multiple organizations... organization-level agency/client/portfolio relationships"), `FUN-D4` ("Portfolio overview... for a management organization across all its clients").
- **Security Baseline**: `09_SECURITY_ARCHITECTURE.md` §2 (Least authority, Separation of responsibility `INV-09`, Explicit Consent `INV-02`), `09_SECURITY_ARCHITECTURE.md` §3 (Trust boundaries: "Agency to its client is an explicit, consented relationship"), `09_SECURITY_ARCHITECTURE.md` §4 (Ownership principles: "Only the owner changes its membership or its consent").
- **Master Checklist Task ID**: `CLIENT-002` (Phase 05 — Clients, P0)
- **The Golden Rule & Recurring Value**: Enables agency owners to associate and manage client organizations under their portfolio container while strictly isolating tenant data and health (`INV-09`).
- **Bounded Context**: `Portfolio and Client Management` collaborating with `Identity and Access`.

---

## 2. Actor Authorization & Role Requirements

1. **Authorization Grounding (`09_SECURITY_ARCHITECTURE.md` §4, `INV-09`)**:
   - Assigning a client organization to an agency is an organizational ownership action governed by `09_SECURITY_ARCHITECTURE.md` §4: *"The customer owns their organization, its users, and its domains. Only the owner changes its membership or its consent."*
   - In accordance with `TEAM-002`'s canonical role model, only an authenticated `Role.OWNER` of the acting Agency Organization (`caller.organizationId().equals(agencyOrganizationId)` AND `caller.role() == Role.OWNER`) is authorized to form/assign an agency-client relationship.
   - Non-owner callers (`ANALYST`, `VIEWER`) or cross-tenant callers are DENIED assignment authority with `AccessDeniedException`.
2. **Audit Logging (`TEAM-003`)**:
   - Unauthorized attempts trigger structured `AccessDecision.DENIED` audit logs via `AccessAuditSink`.

---

## 3. Target Organization Validation (Authoritative Basis: `CLIENT-001` & `V7`)

1. **Explicit Authoritative Requirement**:
   - Both the Agency Organization and the Client Organization MUST already exist in `OrganizationRepository`.
   - Source: `V7__create_client_relationships.sql` (`fk_client_relationships_agency` and `fk_client_relationships_client`) and `ClientRelationshipService.form(...)`:
     ```java
     organizations.findById(agencyOrganizationId).orElseThrow(() -> new OrganizationNotFoundException(agencyOrganizationId));
     organizations.findById(clientOrganizationId).orElseThrow(() -> new OrganizationNotFoundException(clientOrganizationId));
     ```
   - Invariant `INV-14` (No orphans): A relationship is never formed against nonexistent organizations.
2. **Self-Assignment Guard**:
   - `ClientRelationship.form(...)` throws `IllegalArgumentException` if `agencyOrganizationId.equals(clientOrganizationId)`.

---

## 4. Existing Active Relationship & Duplicate Assignment Handling

1. **Authoritative Basis (`V7` & `ClientRelationship.java`)**:
   - `V7__create_client_relationships.sql` notes: *"The two FKs are the only integrity constraints beyond the PK: no speculative indexes or uniqueness are added (CLIENT-001 contract)."*
   - Lifecycle is `ACTIVE → ENDED`.
   - **Decision**: `ClientRelationshipService.assign(...)` queries existing relationships for `(agencyOrganizationId, clientOrganizationId)`. If an `ACTIVE` relationship is already present, it is returned idempotently without persisting duplicate rows. If no active relationship exists, a new `ClientRelationship` is formed and saved.

---

## 5. Architectural Invariants

- **`INV-09` (Multi-Tenant Isolation)**: The caller MUST be a registered user of the Agency Organization (`caller.organizationId() == agencyOrganizationId`).
- **`INV-10` (Stable Identity)**: Relationship ID is unique, immutable, and preserved.
- **`N3` (No Silent Loss)**: Nonexistent organizations or unauthorized access attempts fail loudly with explicit domain exceptions.
- **`N4` (Zero Secrets)**: No credentials or tokens are involved in client assignment.
- **No Schema Changes**: Reuses existing `client_relationships` table (Flyway V7).
