# Tasks: DOM-004 — Registry Ownership

<!--
  AUTHORITY & SUBORDINATION:
  Subordinate to .specify/specs/dom-004-registry-ownership/spec.md and plan.md.
-->

## Task Group 1: Registry Ownership Specification & Verification
- [ ] Task 1.1: Verify `Domain` entity and services enforce 1:1 organization ownership and immutability.
- [ ] Task 1.2: Add dedicated unit test suite `DomainRegistryOwnershipTest` in `com.mailsent.portfolio.domain.application` validating entity, service, and portfolio ownership boundaries.

## Task Group 2: Real PostgreSQL Persistence Verification
- [ ] Task 2.1: Implement `DomainRegistryOwnershipPersistenceIntegrationTest` in `com.mailsent.portfolio.domain` testing real PostgreSQL FK constraint, unique constraint, and agency-client portfolio domain ownership retention.
- [ ] Task 2.2: Execute `DomainRegistryOwnershipPersistenceIntegrationTest` with `-Dmailsent.it.postgres=true`.

## Task Group 3: Full Platform Regression & Verification Gate
- [ ] Task 3.1: Execute all Phase 01–07 test suites and confirm 100% PASS with live PostgreSQL.
- [ ] Task 3.2: Verify zero modifications to Flyway migrations `V1–V18` and zero `V19+` migrations created.
