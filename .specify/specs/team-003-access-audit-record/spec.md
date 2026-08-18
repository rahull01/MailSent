# Feature Specification: TEAM-003 — Access Audit Record

<!--
  AUTHORITY & SUBORDINATION:
  This Feature Specification is a scoped operational slice subordinate to MailSent's authoritative documents:
  - Operational & Product Source of Truth: docs/00_Foundation/MAILSENT_MASTER_WORKFLOW.md
  - Engineering Truth: docs/00_Foundation/PROJECT_CONSTITUTION.md
  - Security Architecture: docs/03_Architecture/09_SECURITY_ARCHITECTURE.md
  - Non-Functional Requirements: docs/02_Product_Design/NON_FUNCTIONAL_REQUIREMENTS.md (NFR-AU)
  - Preceding Implementation: TEAM-002 (Role→Capability Map & AuthorizationService)
-->

**Feature Branch**: `team-003-access-audit-record`  
**Created**: 2026-08-18  
**Status**: Draft (Corrected Specification Phase)  
**Input**: TEAM-003 — Access audit record specification contract.  

---

## 1. Governance & Traceability Anchors *(mandatory)*

- **Master Workflow Module**: WF Feature Pipeline / Phase 04 Teams (Access Control & Auditing)
- **Parent Requirement**: `NFR-AU` (`AU1` "Every conclusion/decision must have an audit trail... so history is inspectable", `AU2` "Keep a record of what occurred and when with its context")
- **Security Principles**: `09_SECURITY_ARCHITECTURE.md` §2 (Least authority, Accountability, Separation of responsibility `INV-09`, No Silent Loss `N3`), `09_SECURITY_ARCHITECTURE.md` §8 (Failure philosophy)
- **Master Checklist Task ID**: `TEAM-003` (Phase 04 — Teams, P1)
- **The Golden Rule & Recurring Value**: Ensures all authorization decisions (especially authorization denials and multi-tenant boundary breaches) are deterministically auditable with MDC diagnostic context, satisfying enterprise security posture without introducing database overhead or runtime drag.
- **Bounded Context**: `Identity and Access` (owns User, Role, and Authorization auditing).

---

## 2. Minimal Audit Model Scope & Boundaries

Per CTO directive:
1. **NO Database Persistence**: TEAM-003 does NOT create a JPA entity, table, Flyway migration, or Spring Data repository. Audit records are immutable domain value objects / structured log events.
2. **Strict Authorization Scope**: TEAM-003 audits authorization and capability evaluations from `AuthorizationService` (TEAM-002).
3. **Explicitly Excluded**:
   - `INVITATION_SENT` and `INVITATION_ACCEPTED` (owned strictly by `TEAM-001` / `InvitationService`).
   - General-purpose SIEM, Kafka, event sourcing, or database audit ledgers.
   - Cross-context data mutation auditing (owned by individual domain contexts).

---

## 3. Audit Record Contract & Fields

An immutable domain Value Object `AccessAuditRecord` representing an evaluated authorization decision:

| Field | Type | Description | Justification |
|-------|------|-------------|---------------|
| `occurredAt` | `Instant` | Precise timestamp of the authorization attempt | `NFR-AU` (AU2 "what occurred and when") |
| `userId` | `String` | Acting actor user ID (or `"anonymous"` / `"unauthenticated"`) | `09_SECURITY_ARCHITECTURE.md` §2 (Accountability) |
| `userOrganizationId` | `String` | Home organization ID of the acting user (or `"none"`) | `INV-09` (Multi-tenant isolation tracking) |
| `targetOrganizationId` | `String` | Target organization ID where the action was attempted | `INV-09` (Tenant boundary verification) |
| `capability` | `Capability` | The requested capability being evaluated | `TEAM-002` (`Capability.java`) |
| `decision` | `AccessDecision` | Outcome: `GRANTED` or `DENIED` | `TEAM-002`, `N3` |
| `reason` | `String` | Explanation for denial (e.g. role mismatch, tenant boundary violation) | `09_SECURITY_ARCHITECTURE.md` §8, `N3` |

---

## 4. Authorization Decision Recording Rules

1. **Denials (`AUTH_DENIED` / `AccessDecision.DENIED`) — MANDATORY**:
   - Every denied authorization attempt (unauthorized role, cross-tenant attempt, null parameter, unauthenticated user) MUST trigger an audit event and be logged with structured MDC context (`userId`, `userOrg`, `targetOrg`, `capability`, `decision`, `reason`).
2. **Grants (`AUTH_GRANTED` / `AccessDecision.GRANTED`) — OPTIONAL/TRACE**:
   - Evaluated in-memory; emits structured debug/info audit event without adding I/O or database contention.
3. **Zero Performance & Logic Drag**:
   - Auditing MUST NOT alter, delay, or mutate the underlying authorization decision evaluated by `RoleCapabilityPolicy` and `AuthorizationService`.
   - Auditing is performed via an injectable, hermetic port (`AccessAuditSink` or `AccessAuditLogger`).

---

## 5. Architectural Invariants

- **`INV-09` (Multi-Tenant Isolation)**: Cross-organization attempts (where `userOrganizationId != targetOrganizationId`) are recorded explicitly as tenant boundary violations.
- **`N3` (No Silent Loss / Fail Loudly)**: Denials are never swallowed silently; they are captured in the audit record and logged.
- **`N4` (Zero Secrets)**: No passwords, hashes, or sensitive tokens may ever appear in audit records or logs.
