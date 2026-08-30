# Implementation Tasks: REM-SEC-001 — SessionAuthenticationFilter & ConsentController Hardening

<!--
  AUTHORITY & SUBORDINATION:
  Subordinate to .specify/specs/rem-sec-001-session-filter-consent-hardening/plan.md
-->

**Feature Branch**: `rem-sec-001-session-filter-consent-hardening`  
**Execution Order**: Dependency Order (Sequential)  

---

## Task Breakdown

### Phase 1: Repository Foundation & Session Lookup
- [ ] **T1.1**: Update `SessionRepository.java` to declare `Optional<Session> findByTokenHash(String tokenHash)`.
- [ ] **T1.2**: Update `JpaSessionRepository.java` to declare `Optional<SessionJpaEntity> findByTokenHash(String tokenHash)`.
- [ ] **T1.3**: Implement `findByTokenHash` in `JpaSessionRepositoryAdapter.java` mapping `SessionJpaEntity` to `Session`.
- [ ] **T1.4**: Implement `findByTokenHash` in `InMemorySessionRepository.java`.

### Phase 2: Tenant Context & Security Infrastructure
- [ ] **T2.1**: Create `AuthenticatedIdentity.java` (immutable record: `userId`, `email`, `organizationId`, `role`).
- [ ] **T2.2**: Create `TenantContext.java` (ThreadLocal storage with `set`, `get`, `getRequired`, `clear`).
- [ ] **T2.3**: Create `UnauthenticatedException.java`.
- [ ] **T2.4**: Implement `SessionAuthenticationFilter.java` (`OncePerRequestFilter` handling Bearer token extraction, SHA-256 hashing, session lookup, expiry check, user lookup, `TenantContext` & MDC binding, and `try-finally` cleanup).
- [ ] **T2.5**: Create `WebMvcSecurityConfig.java` registering `SessionAuthenticationFilter` and configuring WebMvc CORS for `http://localhost:3000`.

### Phase 3: ConsentController Security Hardening & DTO Separation
- [ ] **T3.1**: Create `GrantConsentRequest.java` DTO (`domainId`, `actionType`).
- [ ] **T3.2**: Create `ConsentResponse.java` DTO mapping domain `Consent` to API response.
- [ ] **T3.3**: Refactor `ConsentController.java` to read `organizationId` and `userId` strictly from `TenantContext.getRequired()`, removing all request body IDOR parameters.
- [ ] **T3.4**: Update `GlobalExceptionHandler.java` with handlers for `SecurityException` (403), `UnauthenticatedException` (401), and `ConsentAlreadyExistsException` (409).

### Phase 4: Verification & Automated Tests
- [ ] **T4.1**: Create `SessionAuthenticationFilterTest.java` (Unit tests verifying 8 scenarios).
- [ ] **T4.2**: Create `ConsentControllerSecurityTest.java` (MockMvc integration tests verifying 8 scenarios).
- [ ] **T4.3**: Run full test verification suite.
