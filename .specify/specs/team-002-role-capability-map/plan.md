# Implementation Plan: TEAM-002 — Role→Capability Map (Least-Authority)

<!--
  AUTHORITY & SUBORDINATION:
  This Implementation Plan is subordinate to MailSent's frozen architecture and documentation:
  - Architecture Baseline: docs/03_Architecture/ (01_DOMAIN_MODEL to 10_DEPLOYMENT_ARCHITECTURE)
  - Engineering Truth: docs/00_Foundation/PROJECT_CONSTITUTION.md
  - Operational Truth: docs/00_Foundation/MAILSENT_MASTER_WORKFLOW.md
  - Execution Checklist: implementation/00_IMPLEMENTATION_MASTER_CHECKLIST.md
-->

**Branch**: `team-002-role-capability-map` | **Date**: 2026-08-18 | **Spec**: `.specify/specs/team-002-role-capability-map/spec.md`  
**Master Checklist Task ID**: `TEAM-002`  
**Bounded Context**: `Identity and Access`  

---

## 1. Executive Summary & Traceability

- **Parent Requirement**: `FUN-D5` ("Access control... roles that reflect who can see health and who can make changes")
- **Customer Problem & Value**: `CP-8` (Security & trust in team delegation), `VP-6` (Confidence in enterprise access boundaries)
- **Technical Approach**: 
  1. Define a strongly-typed domain value object / enum `Capability` in `com.mailsent.identity.team.domain`.
  2. Implement an immutable domain `RoleCapabilityPolicy` mapping each `Role` (`OWNER`, `ANALYST`, `VIEWER`) to its explicitly authorized `Set<Capability>`.
  3. Implement an application-level `AuthorizationService` (or `CapabilityEvaluator`) that evaluates `(User, OrganizationId, Capability)` with default-deny semantics and MDC logging on authorization rejection.
  4. Provide unit and failure-path tests verifying strict least-privilege, tenant isolation, and default-deny invariants.
- **Lineage & Invariants**: `INV-09` Multi-Tenant Isolation, `INV-11` Deterministic Lineage / Auditability, `N2` Truthfulness, `N3` No Silent Failure.

---

## 2. Locked Technical Stack & Runtime Environment

- **Backend**: Java 21 · Spring Boot 3.x · Maven · Spring Data JPA
- **Database**: PostgreSQL (No schema changes required for in-memory / policy matrix; Flyway remains unchanged)
- **Frontend**: Next.js 14+ (App Router) · TypeScript · Tailwind CSS
- **Build & CI**: Maven (`mvn test-compile`, `mvn test`)

---

## 3. Architecture Alignment & Frozen Decisions Check

1. **Bounded Context Boundaries**: Code resides within `com.mailsent.identity.team` and `com.mailsent.identity.authentication`.
2. **No DB Schema Mutation Needed**: Canonical roles (`OWNER`, `ANALYST`, `VIEWER`) already exist in `Role.java` and `UserJpaEntity`. Capability mappings are deterministic domain policies, not dynamic DB tables.
3. **Multi-Tenant Scoping**: All capability evaluations require an `organizationId` parameter to ensure tenant boundary enforcement (`INV-09`).
4. **Default-Deny**: Unknown roles, unmapped capabilities, or null parameters must evaluate to `false` (Deny).

---

## 4. Proposed Code Layout & File Locations

```text
backend/src/main/java/com/mailsent/identity/team/
├── domain/
│   ├── Capability.java                 # Strongly-typed enum of authorized capabilities
│   ├── RoleCapabilityPolicy.java       # Immutable domain policy mapping Role -> Set<Capability>
│   └── AccessDeniedException.java      # Domain exception for unauthorized actions
└── application/
    ├── AuthorizationService.java       # Evaluates (User, organizationId, Capability)
    └── PrivilegeGuard.java             # Guard utility to assert authorization before action

backend/src/test/java/com/mailsent/identity/team/
├── domain/
│   └── RoleCapabilityPolicyTest.java   # Matrix verification for OWNER, ANALYST, VIEWER
└── application/
    └── AuthorizationServiceTest.java   # Default-deny, tenant boundary, and logging tests
```

---

## 5. Verification Matrices (T-MAT, S-MAT, O-MAT)

- **T-MAT (Testing Plan)**:
  - Verify `OWNER` has all 13 defined capabilities.
  - Verify `ANALYST` has read and operational capabilities, but denied administrative capabilities (`TEAM_INVITE`, `BILLING_MANAGE`, `DOMAIN_MANAGE`, `CONSENT_GRANT`, `TEAM_MANAGE_ROLES`, `ORG_SETTINGS_MANAGE`).
  - Verify `VIEWER` has read capabilities only, and denied all mutation/operational capabilities (`DOMAIN_OBSERVE_TRIGGER`, `RESOLUTION_VERIFY`).
  - Verify negative cases: unknown capability / null user / wrong org evaluates to DENY.
- **S-MAT (Security Plan)**:
  - Default-deny evaluation.
  - Tenant matching: user belonging to Org A cannot assert capabilities for Org B (`INV-09`).
- **O-MAT (Observability Plan)**:
  - Rejection logging with MDC context `userId`, `orgId`, and `attemptedCapability`.

---

## 6. Constitution Check & Gating

- [x] **Golden Rule**: Validated team security & multi-tenant access control for 12+ month subscription value.
- [x] **DoD Compliance**: Complete unit test plan, zero secrets, hermetic verification.
- [x] **No Architecture Drift**: Uses only frozen canonical roles and domain entities.
