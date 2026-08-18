# Implementation Plan: TEAM-004 — Team List Query (Revised)

<!--
  AUTHORITY & SUBORDINATION:
  This Implementation Plan is subordinate to MailSent's frozen architecture and documentation:
  - Architecture Baseline: docs/03_Architecture/ (09_SECURITY_ARCHITECTURE.md, 03_BOUNDED_CONTEXTS.md)
  - Engineering Truth: docs/00_Foundation/PROJECT_CONSTITUTION.md
  - Operational Truth: docs/00_Foundation/MAILSENT_MASTER_WORKFLOW.md
  - Execution Checklist: implementation/00_IMPLEMENTATION_MASTER_CHECKLIST.md
-->

**Branch**: `team-004-team-list-query` | **Date**: 2026-08-18 | **Spec**: `.specify/specs/team-004-team-list-query/spec.md`  
**Master Checklist Task ID**: `TEAM-004` (P1)  
**Bounded Context**: `Identity and Access`  

---

## 1. Executive Summary & Traceability

- **Parent Requirement**: `FUN-D5` ("Access control... roles that reflect who can see health and who can make changes")
- **Security Baseline**: `09_SECURITY_ARCHITECTURE.md` §2, `INV-09` Multi-Tenant Isolation, `N4` Zero Secrets.
- **Technical Approach**:
  1. Add `findAllByOrganizationId(String organizationId)` to application port `UserRepository`.
  2. Implement `findAllByOrganizationId` in `InMemoryUserRepository` and `JpaUserRepositoryAdapter` (ordered by `createdAt ASC, id ASC`).
  3. Implement `TeamQueryService` in `com.mailsent.identity.team.application` mapping active users to `TeamMemberSummary` (`userId`, `email`, `role`, `createdAt`).
  4. Enforce tenant match (`caller.organizationId().equals(targetOrganizationId)`) and default-deny via `AuthorizationService`.
  5. **NO DATABASE MIGRATION**: Reuses existing `organization_id` on `users` table.

---

## 2. Locked Technical Stack & Runtime Environment

- **Backend**: Java 21 · Spring Boot 3.x · Spring Data JPA · PostgreSQL · JUnit 5
- **Database**: Reuses existing schema (`users` table).
- **Build & CI**: Maven (`mvn test`).

---

## 3. Code Layout & File Locations

```text
backend/src/main/java/com/mailsent/identity/
├── authentication/
│   ├── application/
│   │   └── UserRepository.java                # Add findAllByOrganizationId
│   └── infrastructure/
│       ├── InMemoryUserRepository.java        # Impl findAllByOrganizationId
│       └── persistence/
│           ├── JpaUserRepository.java         # Add findByOrganizationIdOrderByCreatedAtAscIdAsc
│           └── JpaUserRepositoryAdapter.java  # Impl findAllByOrganizationId
└── team/
    ├── application/
    │   ├── TeamQueryService.java              # Query service enforcing tenant boundary
    │   └── TeamMemberSummary.java             # Safe DTO (userId, email, role, createdAt)
    └── config/
        └── TeamConfig.java                    # Bean definition

backend/src/test/java/com/mailsent/identity/team/
└── application/
    └── TeamQueryServiceTest.java              # Verification of query, DTO, and INV-09
```

---

## 4. Verification Matrices (T-MAT, S-MAT, O-MAT)

- **T-MAT**:
  - Verify Owner, Analyst, and Viewer receive accurate team member lists for their home organization.
  - Verify deterministic ordering by `createdAt ASC, id ASC`.
  - Verify empty list for organization without additional members.
- **S-MAT**:
  - Verify `INV-09` cross-tenant denial (Org A caller querying Org B throws `AccessDeniedException`).
  - Verify `N4`: `passwordHash` is excluded from `TeamMemberSummary`.
- **O-MAT**:
  - Denials logged via `AuthorizationService` with structured MDC context (`TEAM-003`).
