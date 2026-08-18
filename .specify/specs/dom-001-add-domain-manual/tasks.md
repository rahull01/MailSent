# Tasks: DOM-001 — Add Domain (Manual)

<!--
  AUTHORITY & SUBORDINATION:
  Subordinate to .specify/specs/dom-001-add-domain-manual/spec.md and plan.md.
-->

## Task Group 1: Domain Service Authorization & Validation Enhancement
- [ ] Task 1.1: Wire `AuthorizationService` into `DomainService` and `DomainConfig`.
- [ ] Task 1.2: Implement `DomainService.register(User caller, String organizationId, String domainName)` checking `Capability.DOMAIN_MANAGE` via `AuthorizationService`.
- [ ] Task 1.3: Validate domain syntax constraints (RFC 1035 / length / format) before creation.
- [ ] Task 1.4: Enforce `INV-09` (tenant match) and `INV-10` (global uniqueness via `DomainAlreadyExistsException`).

## Task Group 2: Unit Testing & Verification
- [ ] Task 2.1: Add unit tests in `DomainServiceTest` for `Role.OWNER` success path.
- [ ] Task 2.2: Add unit tests in `DomainServiceTest` for `Role.ANALYST` and `Role.VIEWER` denials.
- [ ] Task 2.3: Add unit tests in `DomainServiceTest` for cross-tenant registration denial (`INV-09`).
- [ ] Task 2.4: Add unit tests in `DomainServiceTest` for duplicate domain name conflict (`INV-10`).
- [ ] Task 2.5: Add unit tests in `DomainServiceTest` for invalid domain name syntax and nonexistent organization (`INV-14`).

## Task Group 3: Regression & Integration Verification
- [ ] Task 3.1: Execute full regression suite across identity, team, client, portfolio, and domain modules.
- [ ] Task 3.2: Verify zero Flyway `V1–V18` changes and zero new migrations.
