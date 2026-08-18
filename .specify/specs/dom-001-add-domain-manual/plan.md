# Implementation Plan: DOM-001 — Add Domain (Manual)

<!--
  AUTHORITY & SUBORDINATION:
  Subordinate to .specify/specs/dom-001-add-domain-manual/spec.md,
  docs/00_Foundation/MAILSENT_MASTER_WORKFLOW.md, and docs/00_Foundation/PROJECT_CONSTITUTION.md.
-->

## 1. Technical Context & Scope

- **Bounded Context**: `Portfolio and Client Management` (Domain sub-context) collaborating with `Identity and Access`.
- **Target Operations**: Domain registration application service with `Capability.DOMAIN_MANAGE` caller authorization.
- **Persistence Target**: Existing PostgreSQL table `domains` (`V5__create_domains.sql`).
- **Dependencies**: `DomainRepository`, `OrganizationRepository`, `AuthorizationService`, `Clock`.

---

## 2. Architecture & Design Decisions

1. **Service Boundary**:
   - Enhance `DomainService` (or add authorized overload `register(User caller, String organizationId, String domainName)`) that:
     a. Checks caller authorization via `AuthorizationService.isAuthorized(caller, organizationId, Capability.DOMAIN_MANAGE)`.
     b. If false, throws `AccessDeniedException`.
     c. Verifies organization exists in `OrganizationRepository`.
     d. Validates domain name syntax (length <= 253, non-blank, valid hostname structure).
     e. Normalizes domain name (trimmed, lowercased).
     f. Checks `DomainRepository.existsByName(normalizedName)` and throws `DomainAlreadyExistsException` if duplicate.
     g. Persists domain to `DomainRepository.save(...)`.
2. **Persistence Boundary**:
   - Uses existing `JpaDomainRepositoryAdapter` and `InMemoryDomainRepository`.
   - Relies on database unique constraint `uk_domains_name` (`INV-10`) and foreign key `fk_domains_organization` (`INV-14`).
3. **Audit Logging**:
   - `AuthorizationService` logs access decisions (`GRANTED` / `DENIED`) to `AccessAuditSink`.

---

## 3. Hermetic Test Strategy (T-MAT)

1. **Unit Tests (`DomainServiceTest`)**:
   - Successful domain registration by `Role.OWNER` in same organization.
   - Rejection of non-authorized roles (`Role.ANALYST`, `Role.VIEWER`) with `AccessDeniedException`.
   - Rejection of cross-tenant user (`caller.organizationId() != targetOrgId`) with `AccessDeniedException`.
   - Rejection of invalid/blank/oversized domain names with `IllegalArgumentException`.
   - Rejection of duplicate domain name (case-insensitive) across same or different organizations with `DomainAlreadyExistsException`.
   - Rejection of nonexistent target organization with `DomainOrganizationNotFoundException`.
2. **Integration Verification (`DomainPersistenceIntegrationTest`)**:
   - Real PostgreSQL test running under `@ActiveProfiles("it")` with `-Dmailsent.it.postgres=true` asserting `uk_domains_name` and `fk_domains_organization`.
