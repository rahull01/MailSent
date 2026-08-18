# Tasks: DOM-003 — Domain List/State

<!--
  AUTHORITY & SUBORDINATION:
  Subordinate to .specify/specs/dom-003-domain-list-state/spec.md and plan.md.
-->

## Task Group 1: DTO and Application Service Implementation
- [ ] Task 1.1: Create `DomainSummary` immutable record in `com.mailsent.portfolio.domain.application`.
- [ ] Task 1.2: Implement `DomainQueryService` with `listDomains(User caller, String organizationId)` and `getDomain(User caller, String organizationId, String domainName)` enforcing `HEALTH_VIEW` and `INV-09`.
- [ ] Task 1.3: Wire `DomainQueryService` into `DomainConfig`.

## Task Group 2: Unit Testing & Verification
- [ ] Task 2.1: Add unit tests in `DomainQueryServiceTest` verifying `OWNER`, `ANALYST`, and `VIEWER` authorized list access.
- [ ] Task 2.2: Add unit tests verifying cross-tenant listing rejection (`INV-09`).
- [ ] Task 2.3: Add unit tests for single-domain lookup including canonical normalization and cross-tenant privacy protection (`Optional.empty()`).
- [ ] Task 2.4: Add unit tests verifying nonexistent organization and invalid inputs.
- [ ] Task 2.5: Add assertions confirming zero synthetic health/risk fields (`N2`).

## Task Group 3: Real PostgreSQL Integration & Regression
- [ ] Task 3.1: Create `DomainListPersistenceIntegrationTest` executing against PostgreSQL 16 on port 5433 with scoped `@DataJpaTest`.
- [ ] Task 3.2: Run full regression suite across all implemented phases and verify `V1–V18` immutability.
