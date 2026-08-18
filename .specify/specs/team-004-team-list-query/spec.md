# Feature Specification: TEAM-004 — Team List Query (Revised)

<!--
  AUTHORITY & SUBORDINATION:
  This Feature Specification is a scoped operational slice subordinate to MailSent's authoritative documents:
  - Operational & Product Source of Truth: docs/00_Foundation/MAILSENT_MASTER_WORKFLOW.md
  - Engineering Truth: docs/00_Foundation/PROJECT_CONSTITUTION.md
  - Requirements Baseline: docs/02_Product_Design/ (FUNCTIONAL_REQUIREMENTS.md)
  - Security Architecture: docs/03_Architecture/09_SECURITY_ARCHITECTURE.md
  - Bounded Contexts: docs/03_Architecture/03_BOUNDED_CONTEXTS.md
  - Data Model: docs/03_Architecture/07_DATA_MODEL.md
  - Preceding Authorization Contracts: TEAM-002 (RoleCapabilityPolicy), TEAM-003 (AccessAuditRecord)
-->

**Feature Branch**: `team-004-team-list-query`  
**Created**: 2026-08-18  
**Status**: Draft (CTO Revision)  
**Input**: TEAM-004 — Team list query contract specification.  

---

## 1. Governance & Traceability Anchors *(mandatory)*

- **Master Workflow Module**: WF Feature Pipeline / Phase 04 Teams
- **Parent Requirement**: `FUN-D5` ("Access control. The product must let an organization limit who on its team can view and act, with roles that reflect who can see health and who can make changes.")
- **Security Principles**: `09_SECURITY_ARCHITECTURE.md` §2 (Least authority, Privacy by design, Separation of responsibility `INV-09`, Zero Secrets `N4`), `09_SECURITY_ARCHITECTURE.md` §3 (Trust boundaries)
- **Master Checklist Task ID**: `TEAM-004` (Phase 04 — Teams, P1)
- **The Golden Rule & Recurring Value**: Allows authenticated team members within an organization to view active registered colleagues and their assigned roles, maintaining operational transparency without cross-tenant leakage or credential disclosure.
- **Bounded Context**: `Identity and Access`.

---

## 2. Authorization & Canonical Role Visibility

1. **Intra-Tenant Visibility Rule (Authoritative Basis: `FUN-D5`)**:
   - Any authenticated member belonging to the organization (`OWNER`, `ANALYST`, `VIEWER`) is authorized to view the active members of their own organization.
2. **Multi-Tenant Isolation Invariant (`INV-09`)**:
   - Cross-organization queries are strictly denied: An authenticated user from Organization A cannot query the team list of Organization B.
   - Unauthenticated or tenant-mismatch requests throw `AccessDeniedException` and emit `DENIED` audit events via `TEAM-003`.

---

## 3. Scope: Active Members vs. Pending Invitations

1. **Active Registered Members**:
   - Queries users currently registered and bound to the target organization (`User.organizationId() == targetOrganizationId`).
2. **Pending Invitations**:
   - Pending invitation tokens from `TEAM-001` are managed separately under the invitation lifecycle. `TEAM-004` queries active user accounts only.

---

## 4. DTO & Privacy Boundaries (`N4`, Privacy by Design)

The query returns `List<TeamMemberSummary>`. Each item contains only the minimum authoritative fields:

| Field | Type | Domain Origin | Justification & Privacy Constraint |
|-------|------|---------------|------------------------------------|
| `userId` | `String` | `User.id()` | `INV-10` Stable identity |
| `email` | `String` | `User.email().value()` | Authorized business identity |
| `role` | `Role` | `User.role()` | `FUN-D5` Role visibility (`OWNER`, `ANALYST`, `VIEWER`) |
| `createdAt` | `Instant` | `User.createdAt()` | Authoritative registration timestamp (projected directly from `User.createdAt`) |

**Strict Exclusion (`N4`)**:
- Password hash (`passwordHash`) MUST NEVER be included in DTOs, logs, or responses.

---

## 5. Behavior & Implementation Decisions

1. **Ordering (Implementation Decision)**:
   - Ordered chronologically by `createdAt ASC, id ASC` to ensure deterministic presentation.
2. **Empty Organization / Nonexistent Organization**:
   - Querying an organization with no matching users returns an empty list `[]`.
   - Cross-tenant mismatch fails loudly with `AccessDeniedException` (`INV-09`, `N3`).
3. **No Unapproved Capabilities / Dynamic Pagination**:
   - Returns unpaged `List<TeamMemberSummary>` for the tenant. Dynamic filtering and pagination are deferred to future API tasks.
