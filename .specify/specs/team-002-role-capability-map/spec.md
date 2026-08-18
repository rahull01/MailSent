# Feature Specification: TEAM-002 — Role→Capability Map (Least-Authority)

<!--
  AUTHORITY & SUBORDINATION:
  This Feature Specification is a scoped operational slice subordinate to MailSent's authoritative documents:
  - Operational & Product Source of Truth: docs/00_Foundation/MAILSENT_MASTER_WORKFLOW.md
  - Engineering Truth: docs/00_Foundation/PROJECT_CONSTITUTION.md
  - Requirements Baseline: docs/02_Product_Design/ (PRODUCT_REQUIREMENTS.md, FUNCTIONAL_REQUIREMENTS.md)
  - Security Architecture: docs/03_Architecture/09_SECURITY_ARCHITECTURE.md
  - Bounded Contexts: docs/03_Architecture/03_BOUNDED_CONTEXTS.md
  - Data Model: docs/03_Architecture/07_DATA_MODEL.md
-->

**Feature Branch**: `team-002-role-capability-map`  
**Created**: 2026-08-18  
**Status**: Draft (Specification Phase)  
**Input**: TEAM-002 — Role→capability map (least-authority) specification contract.  

---

## 1. Governance & Traceability Anchors *(mandatory)*

- **Master Workflow Module**: WF Feature Pipeline / Phase 04 Teams & Access Control
- **Parent Requirement**: `FUN-D5` ("Access control. The product must let an organization limit who on its team can view and act, with roles that reflect who can see health and who can make changes.")
- **Security Principles**: `09_SECURITY_ARCHITECTURE.md` §2 (Least authority, Trust before access, Separation of responsibility `INV-09`, Explicit Consent `INV-02`)
- **Customer Problem (CP)**: `CP-8` (Trust & Governance for Teams & Agencies)
- **Customer Journey (J) & Value Prop (VP)**: `J-3` / `J-6` (Secure operational delegation), `VP-6` (Confidence in enterprise access boundaries)
- **Master Checklist Task ID**: `TEAM-002` (Phase 04 — Teams, P0)
- **The Golden Rule & Recurring Value**: Prevents unauthorized modifications, data leakage across multi-tenant boundaries (`INV-09`), and unintended mutations of delivery infrastructure while enabling team collaboration across agencies and client portfolios for 12+ months.
- **Bounded Context**: `Identity and Access` (owns User, Role, Capability Authorization) collaborating with `Portfolio and Client Management` and `Delivery Observation`.

---

## 2. Supported Roles & Ubiquitous Language

Per `docs/00_Foundation/GLOSSARY.md`, `docs/03_Architecture/07_DATA_MODEL.md`, and `com.mailsent.identity.authentication.domain.Role`:

1. **`OWNER`**: Full organization and delivery authority. May manage organization membership, team invites, domain registrations, consent grants, settings, billing, and execute mutations/re-scans.
2. **`ANALYST`**: Delivery operational authority. May view delivery health, trigger domain re-scans, verify fixes, view evidence, and download reports within the organization scope. CANNOT invite/remove team members, alter billing/subscription plans, or close the organization.
3. **`VIEWER`**: Least-authority read-only access (default role for new signups per `AUTH-003`). May observe delivery health signals, dashboards, timelines, and explanations. CANNOT trigger active probes, alter configuration, modify team members, or change settings.

---

## 3. Capability Catalog & Justification Matrix

Capabilities are defined as explicit, strongly typed constants/enumerations belonging to the `Identity and Access` bounded context. Every capability is explicitly justified by frozen architecture and requirements:

| Capability Identifier | Bounded Context | Operations / Actions | Justification & Traceability |
|-----------------------|-----------------|----------------------|------------------------------|
| `HEALTH_VIEW` | Delivery Observation | Read Health Signal, Health Trend, raw evidence snapshots | `FUN-D5`, `FUN-H1`, `FUN-H4` ("who can see health") |
| `DIAGNOSIS_VIEW` | Diagnosis & Explanation | Read Issues, Root Causes, Plain-Language Explanations | `FUN-E1`, `FUN-E3`, `FUN-X3` |
| `REPORT_VIEW` | Reporting & Evidence | View & export executive summaries, evidence proofs | `FUN-R1`, `FUN-R2`, `FUN-R3` |
| `READINESS_VIEW` | Readiness & Compliance | View mailbox provider readiness checklists | `FUN-C1`, `FUN-C2` |
| `MAP_VIEW` | Infrastructure Map | View topology and node detail panels | `FUN-D4`, `MAP-001`, `MAP-003` |
| `DOMAIN_OBSERVE_TRIGGER` | Delivery Observation | Request immediate manual domain re-scan (`re-observe`) | `FUN-M1`, `INF-004`, `FUN-D5` ("who can make changes") |
| `RESOLUTION_VERIFY` | Guidance & Resolution | Trigger fix verification check | `FUN-G5`, `RES-004` |
| `DOMAIN_MANAGE` | Portfolio & Client | Add, pause, remove domains in registry | `FUN-D1`, `FUN-D2`, `SET-003` |
| `CONSENT_GRANT` | Identity & Access | Grant / revoke observation consent (`INV-02`) | `09_SECURITY_ARCHITECTURE.md` §5, `AUTH-005` |
| `TEAM_INVITE` | Identity / Team | Issue email invitation to join organization | `TEAM-001`, `FUN-D5` |
| `TEAM_MANAGE_ROLES` | Identity / Team | Update team member role or remove member | `SET-002`, `09_SECURITY_ARCHITECTURE.md` §4 |
| `ORG_SETTINGS_MANAGE` | Settings / Admin | Manage org name, notification channels, webhooks | `SET-001`, `NOTIF-001` |
| `BILLING_MANAGE` | Billing | Change subscription tier, update payment details | `BILL-001`, `BILL-002` |

---

## 4. Role to Capability Mapping (The Matrix)

All authorization evaluates to **DEFAULT DENY**. A role has ONLY the capabilities explicitly checked below:

| Capability Identifier | `OWNER` | `ANALYST` | `VIEWER` |
|-----------------------|:-------:|:---------:|:--------:|
| `HEALTH_VIEW` | ✅ ALLOW | ✅ ALLOW | ✅ ALLOW |
| `DIAGNOSIS_VIEW` | ✅ ALLOW | ✅ ALLOW | ✅ ALLOW |
| `REPORT_VIEW` | ✅ ALLOW | ✅ ALLOW | ✅ ALLOW |
| `READINESS_VIEW` | ✅ ALLOW | ✅ ALLOW | ✅ ALLOW |
| `MAP_VIEW` | ✅ ALLOW | ✅ ALLOW | ✅ ALLOW |
| `DOMAIN_OBSERVE_TRIGGER` | ✅ ALLOW | ✅ ALLOW | ❌ DENY |
| `RESOLUTION_VERIFY` | ✅ ALLOW | ✅ ALLOW | ❌ DENY |
| `DOMAIN_MANAGE` | ✅ ALLOW | ❌ DENY | ❌ DENY |
| `CONSENT_GRANT` | ✅ ALLOW | ❌ DENY | ❌ DENY |
| `TEAM_INVITE` | ✅ ALLOW | ❌ DENY | ❌ DENY |
| `TEAM_MANAGE_ROLES` | ✅ ALLOW | ❌ DENY | ❌ DENY |
| `ORG_SETTINGS_MANAGE` | ✅ ALLOW | ❌ DENY | ❌ DENY |
| `BILLING_MANAGE` | ✅ ALLOW | ❌ DENY | ❌ DENY |

---

## 5. Architectural Invariants & Security Rules

1. **Strict Default-Deny (`N3`, `S-MAT`)**:
   - If a capability is not explicitly associated with a Role in the capability map, evaluation MUST yield `false` (Deny).
   - If a user, role, or organization is null or unauthenticated, evaluation MUST immediately fail loudly and deny.
2. **Multi-Tenant Isolation Invariant (`INV-09`)**:
   - A user holding `OWNER` role in Organization A has **NO** authority in Organization B.
   - Capability checks MUST always require both `(UserContext, OrganizationId, Capability)` to evaluate authorization.
3. **No Implicit Role Escalation (`09_SECURITY_ARCHITECTURE.md` §7)**:
   - A user cannot grant capabilities beyond their own role.
   - An Analyst or Viewer cannot modify their own or other members' roles.
4. **Deterministic Auditing (`N3`, `INV-11`)**:
   - Denied attempts on privileged operations (e.g. `DOMAIN_MANAGE`, `TEAM_INVITE`, `BILLING_MANAGE`) MUST be logged with MDC context (`userId`, `organizationId`, `targetCapability`, `attemptedAction`).

---

## 6. Edge Cases & Clarifications

1. **Unresolved / Out of Scope for TEAM-002**:
   - Custom dynamic RBAC permission creation (MailSent uses frozen canonical roles `OWNER`, `ANALYST`, `VIEWER`).
   - Cross-organization delegation / Agency Switcher authorization (governed separately by `PORTAL-008` / Portfolio Context).
2. **Read vs Privileged Distinction**:
   - Read operations (`*_VIEW`) are granted across all roles (`VIEWER`, `ANALYST`, `OWNER`).
   - Operational mutation operations (`*_TRIGGER`, `*_VERIFY`) are limited to `ANALYST` and `OWNER`.
   - Administrative / Governance operations (`*_MANAGE`, `*_INVITE`, `*_GRANT`) are restricted strictly to `OWNER`.
