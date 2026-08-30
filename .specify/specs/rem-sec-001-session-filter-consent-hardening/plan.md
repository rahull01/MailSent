# Implementation Plan: REM-SEC-001 — SessionAuthenticationFilter & ConsentController Hardening

<!--
  AUTHORITY & SUBORDINATION:
  Subordinate to docs/00_Foundation/MAILSENT_MASTER_WORKFLOW.md and PROJECT_CONSTITUTION.md
-->

**Feature Branch**: `rem-sec-001-session-filter-consent-hardening`  
**Created**: 2026-08-20  
**Phase**: Security Hardening Baseline  

---

## 1. Plan Overview & Architectural Alignment

This plan defines the step-by-step implementation of `SessionAuthenticationFilter`, `TenantContext`, and the security hardening of `ConsentController`.

```
[ Step 1: Session Repository Extension ]
  ├── Add findByTokenHash() to SessionRepository interface
  ├── Implement in JpaSessionRepository (Spring Data query)
  ├── Implement in JpaSessionRepositoryAdapter (entity mapping)
  └── Implement in InMemorySessionRepository (test map lookup)
          │
          ▼
[ Step 2: Tenant Context & Security Foundation ]
  ├── Create AuthenticatedIdentity (Immutable record: userId, email, orgId, role)
  ├── Create TenantContext (ThreadLocal with strict set, get, getRequired, clear)
  └── Create UnauthenticatedException (Business exception)
          │
          ▼
[ Step 3: SessionAuthenticationFilter Implementation ]
  ├── Implement OncePerRequestFilter extracting "Authorization: Bearer <token>"
  ├── Compute SHA-256 hash of token bytes
  ├── Query sessionRepository.findByTokenHash() & validate expiry (now)
  ├── Query userRepository.findById(session.userId())
  ├── Populate TenantContext and SLF4J MDC (organizationId, userId)
  ├── Enforce try-finally TenantContext.clear() and MDC.remove()
  └── Register filter in WebMvcSecurityConfig with CORS configuration
          │
          ▼
[ Step 4: ConsentController Hardening & DTO Separation ]
  ├── Create GrantConsentRequest DTO (domainId, actionType only)
  ├── Create ConsentResponse DTO (domain entity mapping)
  ├── Refactor ConsentController.grantConsent to use TenantContext credentials
  ├── Refactor ConsentController.revokeConsent to use TenantContext credentials
  └── Delete legacy RevokeConsentRequest (eliminate IDOR vector)
          │
          ▼
[ Step 5: Global Exception Handler Mapping ]
  ├── Map SecurityException -> 403 FORBIDDEN
  ├── Map UnauthenticatedException -> 401 UNAUTHORIZED
  └── Map ConsentAlreadyExistsException -> 409 CONFLICT
          │
          ▼
[ Step 6: Automated Test Suite Verification ]
  ├── SessionAuthenticationFilterTest (Unit tests: 8 scenarios)
  ├── ConsentControllerSecurityTest (MockMvc integration tests: 8 scenarios)
  └── Regression tests on SignInController / SignUpController
```

---

## 2. File Change Manifest

### A. Proposed Files for Implementation (13 files):
1. `backend/src/main/java/com/mailsent/common/security/AuthenticatedIdentity.java` *(NEW)*
2. `backend/src/main/java/com/mailsent/common/security/TenantContext.java` *(NEW)*
3. `backend/src/main/java/com/mailsent/common/security/UnauthenticatedException.java` *(NEW)*
4. `backend/src/main/java/com/mailsent/common/security/SessionAuthenticationFilter.java` *(NEW)*
5. `backend/src/main/java/com/mailsent/common/config/WebMvcSecurityConfig.java` *(NEW)*
6. `backend/src/main/java/com/mailsent/identity/consent/interfaces/GrantConsentRequest.java` *(NEW)*
7. `backend/src/main/java/com/mailsent/identity/consent/interfaces/ConsentResponse.java` *(NEW)*
8. `backend/src/main/java/com/mailsent/identity/authentication/application/SessionRepository.java` *(MODIFY)*
9. `backend/src/main/java/com/mailsent/identity/authentication/infrastructure/persistence/JpaSessionRepository.java` *(MODIFY)*
10. `backend/src/main/java/com/mailsent/identity/authentication/infrastructure/persistence/JpaSessionRepositoryAdapter.java` *(MODIFY)*
11. `backend/src/main/java/com/mailsent/identity/authentication/infrastructure/InMemorySessionRepository.java` *(MODIFY)*
12. `backend/src/main/java/com/mailsent/identity/consent/interfaces/ConsentController.java` *(MODIFY)*
13. `backend/src/main/java/com/mailsent/common/error/GlobalExceptionHandler.java` *(MODIFY)*

### B. Out-of-Scope Files (Must NOT Be Modified):
- Flyway migrations `V1` to `V20`.
- Domain aggregates (`Session.java`, `User.java`, `Consent.java`, `Domain.java`, `Organization.java`).
- Core domain logic in `ConsentService.java` (business rules are already correct).
- Frontend files (`frontend/`).

---

## 3. Test Strategy & Verification Matrix

```
┌──────────────────────────────────────┬────────────────────────────────────────────────────────────────────────┐
│ Test Class                           │ Verified Scenarios & Assertions                                        │
├──────────────────────────────────────┼────────────────────────────────────────────────────────────────────────┤
│ SessionAuthenticationFilterTest      │ 1. Public endpoints (e.g. /api/sessions) bypass auth filter.           │
│ (Unit Tests)                         │ 2. Protected endpoint with missing header returns 401 AUTH_REQUIRED.   │
│                                      │ 3. Protected endpoint with malformed header returns 401.               │
│                                      │ 4. Non-existent token hash returns 401 INVALID_TOKEN.                  │
│                                      │ 5. Expired session returns 401 SESSION_EXPIRED.                        │
│                                      │ 6. Deleted user returns 401 USER_NOT_FOUND.                            │
│                                      │ 7. Valid bearer token populates TenantContext & SLF4J MDC.             │
│                                      │ 8. TenantContext & MDC are 100% cleared in finally block.              │
├──────────────────────────────────────┼────────────────────────────────────────────────────────────────────────┤
│ ConsentControllerSecurityTest        │ 1. Unauthenticated POST /api/v1/consents returns 401.                  │
│ (MockMvc Tests)                      │ 2. Malicious user from Org B cannot grant consent for Org A (IDOR).    │
│                                      │ 3. User with VIEWER role receives 403 FORBIDDEN.                       │
│                                      │ 4. User with OWNER/ANALYST role grants consent successfully (200 OK).  │
│                                      │ 5. Malicious user from Org B cannot revoke Org A consent (IDOR).       │
│                                      │ 6. Duplicate active consent returns 409 CONFLICT.                      │
│                                      │ 7. CORS preflight OPTIONS request returns 200 OK without token.        │
│                                      │ 8. Missing domainId or actionType returns 400 Bad Request.             │
└──────────────────────────────────────┴────────────────────────────────────────────────────────────────────────┘
```
