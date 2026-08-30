# Implementation Plan: [FEATURE NAME]

<!--
  AUTHORITY & SUBORDINATION:
  This Implementation Plan is subordinate to MailSent's frozen architecture and documentation:
  - Architecture Baseline: docs/03_Architecture/ (01_DOMAIN_MODEL to 10_DEPLOYMENT_ARCHITECTURE)
  - Engineering Truth: docs/00_Foundation/PROJECT_CONSTITUTION.md
  - Operational Truth: docs/00_Foundation/MAILSENT_MASTER_WORKFLOW.md
  - Execution Checklist: implementation/00_IMPLEMENTATION_MASTER_CHECKLIST.md
-->

**Branch**: `[###-feature-name]` | **Date**: [DATE] | **Spec**: `/specs/[###-feature-name]/spec.md`  
**Master Checklist Task ID**: `[e.g., MAP-002, DIAG-002, COMP-001]`  
**Bounded Context**: `[e.g., Delivery Observation, Diagnosis, Reporting from docs/03_Architecture/03_BOUNDED_CONTEXTS.md]`  

---

## 1. Executive Summary & Traceability

- **Parent Requirement**: `[e.g., FUN-H2, FUN-X1, AIR-AI1]`
- **Customer Problem & Value**: `[e.g., CP-1, VP-1]`
- **Technical Approach**: [Concise summary of backend service/repository changes and/or frontend UI components]
- **Lineage & Invariants**: [Citations of applicable invariants: `INV-01`, `INV-02`, `INV-09` Multi-Tenant Isolation, `INV-11` Deterministic Lineage]

---

## 2. Locked Technical Stack & Runtime Environment

The MailSent technical stack is **LOCKED** by the CTO:
- **Backend**: Java 21 · Spring Boot 3.x · Maven · Spring Data JPA / JDBC
- **Database & Cache**: PostgreSQL (with Flyway migrations) · Redis (caching/sessions)
- **Frontend**: Next.js 14+ (App Router) · TypeScript · Tailwind CSS · React
- **Build & CI**: Maven (`mvn clean verify`) · npm (`npm run build && npm run lint`) · Docker Compose

---

## 3. Architecture Alignment & Frozen Decisions Check

Verify compliance with frozen architectural decisions before implementation:
1. **Bounded Context Boundaries**: Code must reside within designated packages under `com.mailsent.<bounded_context>` or `frontend/features/<feature>`.
2. **Delivery Observation vs. Diagnosis Separation**: Delivery Observation owns raw facts and Health Signal (`HEALTH-001`). Diagnosis consumes observations; it must NOT mutate Observation Evidence or claim ownership of observation persistence.
3. **Health Signal Persisted Snapshots**: Any health/status calculation must read from persisted database snapshots only—never unpersisted in-memory objects.
4. **Database Migrations**: If schema changes are required, use next incremental Flyway migration (check `backend/src/main/resources/db/migration/` for latest version, e.g. `V19__...sql`). Never modify existing migrations (`V1` to `V18`).
5. **No Unsupported Integrations**: Respect V1 exclusions (no auto-DNS write APIs, no warmup mechanisms, no simulated ranks).

---

## 4. Proposed Code Layout & File Locations

```text
# Backend (Spring Boot DDD structure)
backend/src/main/java/com/mailsent/[bounded_context]/
├── domain/            # Domain entities, Value Objects, Domain events, Invariants
├── application/       # Use cases, application services, orchestration
├── infrastructure/    # JPA repositories, external adapters, DB entities, Flyway
└── interfaces/        # REST controllers, request/response DTOs, mappers

backend/src/test/java/com/mailsent/[bounded_context]/
├── [Service]Test.java          # Unit tests (hermetic, mock-free where possible)
└── [Context]IntegrationTest.java # SpringBootTest / WebMvcTest / DB tests

# Frontend (Next.js Feature structure)
frontend/
├── features/[feature_name]/
│   ├── components/    # Feature-specific React components
│   ├── hooks/         # Custom React hooks & state
│   ├── services/      # Typed API client services
│   └── types/         # TypeScript interfaces & types
└── app/(dashboard)/[route]/page.tsx
```

---

## 5. Verification Matrices (T-MAT, S-MAT, O-MAT, AI-MAT)

- **T-MAT (Testing Plan)**:
  - Unit tests: Happy path, boundary conditions, edge cases.
  - Failure paths: DB connection drop, external adapter unavailable, invalid input payload.
  - Regression: Full `mvn test` pass without breaking existing modules.
- **S-MAT (Security Plan)**:
  - Multi-tenant isolation verified on every query (`WHERE organization_id = :orgId`).
  - Input validation via `@Valid` / `@NotBlank` on all DTOs.
  - Zero secrets in logs or code.
- **O-MAT (Observability Plan)**:
  - Structured logging with MDC `requestId` / `organizationId`.
  - Spring Actuator health/metrics integration.
- **AI-MAT (AI Quality - if applicable)**:
  - Prompt grounding against raw database evidence only.
  - Fallback mechanisms when LLM provider times out.

---

## 6. Constitution Check & Gating

- [ ] **Golden Rule**: Confirmed 12+ month recurring subscription value.
- [ ] **DoD Compliance**: Plan satisfies all 10 Definition of Done requirements.
- [ ] **No Architecture Drift**: Uses only approved dependencies, entities, and protocols.
- [ ] **Review Checklist Ready**: Meets all criteria in Constitution §26.
