# Implementation Plan: TEAM-003 — Access Audit Record

<!--
  AUTHORITY & SUBORDINATION:
  This Implementation Plan is subordinate to MailSent's frozen architecture and documentation:
  - Architecture Baseline: docs/03_Architecture/ (09_SECURITY_ARCHITECTURE.md, 03_BOUNDED_CONTEXTS.md)
  - Engineering Truth: docs/00_Foundation/PROJECT_CONSTITUTION.md
  - Operational Truth: docs/00_Foundation/MAILSENT_MASTER_WORKFLOW.md
  - Execution Checklist: implementation/00_IMPLEMENTATION_MASTER_CHECKLIST.md
-->

**Branch**: `team-003-access-audit-record` | **Date**: 2026-08-18 | **Spec**: `.specify/specs/team-003-access-audit-record/spec.md`  
**Master Checklist Task ID**: `TEAM-003` (P1)  
**Bounded Context**: `Identity and Access`  

---

## 1. Executive Summary & Traceability

- **Parent Requirement**: `NFR-AU` (`AU1`, `AU2` Auditability)
- **Security Baseline**: `09_SECURITY_ARCHITECTURE.md` §2, §8; `INV-09` Multi-Tenant Isolation; `N3` No Silent Failure.
- **Technical Approach**:
  1. Define an immutable domain Value Object `AccessAuditRecord` in `com.mailsent.identity.team.domain`.
  2. Define an enum `AccessDecision` (`GRANTED`, `DENIED`).
  3. Define a lightweight outbound port `AccessAuditSink` interface in `com.mailsent.identity.team.domain`.
  4. Implement a default logging adapter `Slf4jAccessAuditLogger` in `com.mailsent.identity.team.infrastructure` emitting structured log events with MDC context.
  5. Integrate `AccessAuditSink` cleanly into `AuthorizationService` without modifying its default-deny or tenant-isolation logic.
  6. **NO DATABASE CHANGES**: Zero JPA entities, zero Flyway migrations, zero SQL tables.

---

## 2. Locked Technical Stack & Runtime Environment

- **Backend**: Java 21 · Spring Boot 3.x · SLF4J / MDC · JUnit 5
- **Database**: PostgreSQL (NO SCHEMA CHANGES REQUIRED).
- **Build & CI**: Maven (`mvn test-compile`, `mvn test`).

---

## 3. Architecture Alignment & Frozen Decisions Check

1. **Bounded Context**: Resides entirely within `com.mailsent.identity.team`.
2. **Hermetic & Decoupled**: `AuthorizationService` delegates record emission to `AccessAuditSink` without direct coupling to logging frameworks or external storage.
3. **Multi-Tenant Scoping (`INV-09`)**: `AccessAuditRecord` always captures `userOrganizationId` vs `targetOrganizationId`.
4. **Zero Performance Overhead**: In-memory value creation and logging without synchronous DB blocking.

---

## 4. Proposed Code Layout & File Locations

```text
backend/src/main/java/com/mailsent/identity/team/
├── domain/
│   ├── AccessDecision.java             # GRANTED / DENIED enum
│   ├── AccessAuditRecord.java          # Immutable domain value object
│   └── AccessAuditSink.java            # Outbound port interface for audit records
└── infrastructure/
    └── Slf4jAccessAuditLogger.java     # Default SLF4J / MDC logging adapter

backend/src/test/java/com/mailsent/identity/team/
├── domain/
│   └── AccessAuditRecordTest.java      # Immutable value object tests
└── application/
    └── AuthorizationServiceAuditTest.java # Verification of audit emission on grants/denials
```

---

## 5. Verification Matrices (T-MAT, S-MAT, O-MAT)

- **T-MAT**:
  - Verify `AccessAuditRecord` creation, immutability, and field validation.
  - Verify `AuthorizationService` emits `DENIED` record on unauthorized role, tenant mismatch, or invalid inputs.
  - Verify `AuthorizationService` emits `GRANTED` record on authorized capability.
- **S-MAT**:
  - Verify zero sensitive tokens/credentials in audit records (`N4`).
  - Verify tenant mismatch attempts are distinctly flagged.
- **O-MAT**:
  - Verify structured log messages and MDC context format.
