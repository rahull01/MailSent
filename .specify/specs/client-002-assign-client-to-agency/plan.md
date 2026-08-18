# Implementation Plan: CLIENT-002 — Assign Client to Agency (Revised)

<!--
  AUTHORITY & SUBORDINATION:
  This Implementation Plan is subordinate to MailSent's frozen architecture and documentation:
  - Architecture Baseline: docs/03_Architecture/ (09_SECURITY_ARCHITECTURE.md, 03_BOUNDED_CONTEXTS.md, 07_DATA_MODEL.md)
  - Engineering Truth: docs/00_Foundation/PROJECT_CONSTITUTION.md
  - Operational Truth: docs/00_Foundation/MAILSENT_MASTER_WORKFLOW.md
  - Execution Checklist: implementation/00_IMPLEMENTATION_MASTER_CHECKLIST.md
-->

**Branch**: `client-002-assign-client-to-agency` | **Date**: 2026-08-18 | **Spec**: `.specify/specs/client-002-assign-client-to-agency/spec.md`  
**Master Checklist Task ID**: `CLIENT-002` (P0)  
**Bounded Context**: `Portfolio and Client Management`  

---

## 1. Executive Summary & Traceability

- **Parent Requirement**: `FUN-D3`, `FUN-D4` (Agency / Client organization relationships).
- **Security Baseline**: `09_SECURITY_ARCHITECTURE.md` §4 (Owner control); `INV-09` Multi-Tenant Isolation; `TEAM-002` / `TEAM-003` Authorization & Auditing.
- **Technical Approach**:
  1. Add `findActiveByAgencyAndClient(String agencyOrgId, String clientOrgId)` to `ClientRelationshipRepository`.
  2. Implement in `InMemoryClientRelationshipRepository` and `JpaClientRelationshipRepositoryAdapter`.
  3. Add `assign(User caller, String agencyOrganizationId, String clientOrganizationId)` to `ClientRelationshipService`.
  4. Enforce caller authorization: caller must belong to `agencyOrganizationId` with `Role.OWNER`.
  5. Validate existence of both agency and client organizations via `OrganizationRepository` (`INV-14`).
  6. Idempotency: return existing active relationship if found; otherwise form and save new `ClientRelationship`.
  7. **NO SCHEMA CHANGES**: Reuses existing `client_relationships` table (Flyway V7).

---

## 2. Locked Technical Stack & Runtime Environment

- **Backend**: Java 21 · Spring Boot 3.x · Spring Data JPA · PostgreSQL · JUnit 5
- **Database**: Reuses existing schema (`client_relationships` table).
- **Build & CI**: Maven (`mvn test`).

---

## 3. Code Layout & File Locations

```text
backend/src/main/java/com/mailsent/portfolio/client/
├── application/
│   ├── ClientRelationshipRepository.java          # Add findActiveByAgencyAndClient port method
│   └── ClientRelationshipService.java             # Add assign(User, agencyId, clientId)
└── infrastructure/
    ├── InMemoryClientRelationshipRepository.java    # Implement findActiveByAgencyAndClient
    └── persistence/
        ├── JpaClientRelationshipRepository.java    # Add query method
        └── JpaClientRelationshipRepositoryAdapter.java # Implement port method

backend/src/test/java/com/mailsent/portfolio/client/
└── application/
    └── ClientRelationshipServiceTest.java         # Tests for assign, idempotency, and INV-09
```

---

## 4. Verification Matrices (T-MAT, S-MAT, O-MAT)

- **T-MAT**:
  - Verify Agency Owner successfully assigns a client organization.
  - Verify duplicate active assignment returns existing relationship without duplication.
  - Verify missing agency/client organization throws `OrganizationNotFoundException`.
  - Verify self-assignment (`agencyId == clientId`) throws `IllegalArgumentException`.
- **S-MAT**:
  - Verify `INV-09`: Caller belonging to Org X cannot assign clients to Agency Org Y (`AccessDeniedException`).
  - Verify least privilege: Analyst and Viewer callers cannot assign clients (`AccessDeniedException`).
- **O-MAT**:
  - Rejections logged with MDC context via `AccessAuditSink`.
