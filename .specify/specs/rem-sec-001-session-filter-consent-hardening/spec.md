# Feature Specification: REM-SEC-001 — SessionAuthenticationFilter & ConsentController Hardening

<!--
  AUTHORITY & SUBORDINATION:
  This Feature Specification is a scoped operational slice subordinate to MailSent's authoritative documents:
  - Level 1 Product Truth: docs/00_Foundation/MAILSENT_MASTER_WORKFLOW.md
  - Level 2 Engineering Truth: docs/00_Foundation/PROJECT_CONSTITUTION.md (Rules N3, N4, N5, N6; Invariants INV-09, INV-10)
  - Level 3 Execution Truth: implementation/00_IMPLEMENTATION_MASTER_CHECKLIST.md
  - Security Architecture: docs/03_Architecture/09_SECURITY_ARCHITECTURE.md
  - Bounded Contexts: docs/03_Architecture/03_BOUNDED_CONTEXTS.md
  - Preceding Tasks: AUTH-000, AUTH-001, AUTH-002, AUTH-003, ORG-001, TEAM-001
-->

**Feature Branch**: `rem-sec-001-session-filter-consent-hardening`  
**Created**: 2026-08-20  
**Status**: Specification Phase  
**Input**: REM-SEC-001 — Implement SessionAuthenticationFilter and Secure ConsentController.  

---

## 1. Governance & Traceability Anchors *(mandatory)*

- **Master Workflow Module**: Foundation Security, WF-1 Agency Control Center & Identity/Access Context.
- **Parent Requirements**: 
  - `09_SECURITY_ARCHITECTURE.md` §2 (Least authority, Tenant separation `INV-09`, `INV-10`).
  - `PROJECT_CONSTITUTION.md` §3 (Non-Negotiable Invariants: `N3` No Silent Loss, `N4` Zero Secrets in Logs, `N5` Mandatory Automated Tests, `N6` No Lying to User, `INV-09` Multi-Tenant Isolation).
- **Master Checklist Task ID**: `REM-SEC-001` (Security Remediation Baseline, P0).
- **The Golden Rule & Recurring Value**: Prevents unauthorized access, tenant data bleeding, and unauthorized DNS/domain mutations across organizations.
- **Bounded Context**: `Identity and Access` collaborating with `Common Kernel` (Security & Context).

---

## 2. Authentication Mechanism & Bearer Token Extraction

1. **HTTP Transport Header**:
   - The filter intercepts all incoming HTTP requests.
   - Extracts the `Authorization` header: `Authorization: Bearer <hexToken>`.
2. **Public vs. Protected Endpoints**:
   - **Public Endpoints (Filter Bypassed / Permit All)**:
     - `POST /api/sessions` (Sign In)
     - `POST /api/identities` (Sign Up)
     - `POST /api/invitations/accept` (Accept Team Invitation)
     - `POST /ingest/dmarc/rua/**` (DMARC RUA Ingestion Webhook)
     - `/actuator/**` (Spring Boot Health/Metrics management probes)
     - HTTP `OPTIONS` requests (CORS Preflight requests)
   - **Protected Endpoints (Authentication Mandatory)**:
     - `POST /api/v1/consents`
     - `DELETE /api/v1/consents/{consentId}`
     - All subsequent REST endpoints exposed across the platform.
3. **Missing & Malformed Token Failure Modes**:
   - If a request targets a protected endpoint without an `Authorization` header:
     - Immediate HTTP `401 Unauthorized`.
     - Error payload: `ApiError.simple("UNAUTHORIZED", "AUTH_REQUIRED", "Authentication is required to access this resource.")`.
   - If the `Authorization` header does not start with `"Bearer "` or is blank:
     - Immediate HTTP `401 Unauthorized`.
     - Error payload: `ApiError.simple("UNAUTHORIZED", "INVALID_TOKEN_FORMAT", "Authorization header must use Bearer scheme.")`.

---

## 3. Session Lookup, Token Validation & Expiry Rules

1. **Cryptographic One-Way Hashing**:
   - Raw bearer tokens are 64-character hexadecimal strings (32 bytes entropy from `SecureSessionTokenFactory`).
   - The filter computes the SHA-256 digest of the raw token bytes into a 64-character hexadecimal string (`tokenHash`).
   - Raw bearer tokens are **NEVER logged, stored, or echoed** (Rule N4).
2. **Session Storage Lookup**:
   - Queries `SessionRepository.findByTokenHash(tokenHash)`.
   - If no session matches the hash:
     - Immediate HTTP `401 Unauthorized`.
     - Error payload: `ApiError.simple("UNAUTHORIZED", "INVALID_TOKEN", "Session is invalid or does not exist.")`.
3. **Session Expiry & Lifecycle**:
   - Checks `session.isActive(Instant.now(clock))`.
   - If `session.expiresAt().isBefore(now)`:
     - Immediate HTTP `401 Unauthorized`.
     - Error payload: `ApiError.simple("UNAUTHORIZED", "SESSION_EXPIRED", "Your session has expired. Please sign in again.")`.
4. **User Entity Verification**:
   - Queries `UserRepository.findById(session.userId())`.
   - If user account does not exist or has been deleted:
     - Immediate HTTP `401 Unauthorized`.
     - Error payload: `ApiError.simple("UNAUTHORIZED", "USER_NOT_FOUND", "Authenticated user account no longer exists.")`.

---

## 4. TenantContext & MDC Lifecycle

1. **`AuthenticatedIdentity` Aggregate**:
   - Immutable record holding:
     - `String userId`
     - `String email`
     - `String organizationId`
     - `Role role` (`OWNER`, `ANALYST`, `VIEWER`)
2. **`TenantContext` ThreadLocal**:
   - `TenantContext.set(authenticatedIdentity)` is called immediately upon successful authentication.
   - `TenantContext.getRequired()` provides trusted tenant identity to downstream controllers and services.
3. **MDC Correlation**:
   - Filter binds `MDC.put("organizationId", identity.organizationId())` and `MDC.put("userId", identity.userId())`.
4. **Guaranteed Cleanup (Rule N3)**:
   - Filter executes `filterChain.doFilter(request, response)` inside a `try ... finally` block.
   - The `finally` block **strictly executes**:
     ```java
     TenantContext.clear();
     MDC.remove("organizationId");
     MDC.remove("userId");
     ```
   - Prevents memory leaks and cross-request state pollution across pooled Servlet threads.

---

## 5. ConsentController IDOR / BOLA Prevention & DTO Separation

1. **Request DTO Hardening**:
   - `GrantConsentRequest` contains **ONLY**:
     - `@NotBlank String domainId`
     - `@NotNull ActionType actionType`
   - `organizationId` and `grantedByUserId` are **permanently removed** from the request body.
2. **Controller Derivation**:
   - `ConsentController.grantConsent` retrieves `AuthenticatedIdentity auth = TenantContext.getRequired()`.
   - Passes `auth.organizationId()` and `auth.userId()` into `ConsentService.grantConsent(...)`.
3. **Revocation Hardening**:
   - `DELETE /api/v1/consents/{consentId}` accepts **NO request body**.
   - `ConsentController.revokeConsent` passes `consentId`, `auth.organizationId()`, and `auth.userId()` into `ConsentService.revokeConsent(...)`.
4. **Response DTO**:
   - `ConsentResponse` record: `(id, organizationId, domainId, grantedByUserId, actionType, status, grantedAt, revokedAt)`.
   - Raw domain entity `Consent` is never returned directly over HTTP.

---

## 6. Authorization & Error Status Mapping

1. **Role-Based Mutation Gate**:
   - In accordance with `Role.canChange()`:
     - `Role.OWNER`: Allowed (true).
     - `Role.ANALYST`: Allowed (true).
     - `Role.VIEWER`: Denied (false).
   - If a Viewer attempts to grant or revoke consent, `ConsentService` raises `SecurityException("User role does not have permission to grant consent")`.
2. **Global Exception Mapping**:
   - `SecurityException` -> HTTP `403 Forbidden` (`ApiError.simple("FORBIDDEN", "INSUFFICIENT_PERMISSIONS", ex.getMessage())`).
   - `UnauthenticatedException` -> HTTP `401 Unauthorized` (`ApiError.simple("UNAUTHORIZED", "AUTH_REQUIRED", ex.getMessage())`).
   - `ConsentAlreadyExistsException` -> HTTP `409 Conflict` (`ApiError.simple("CONFLICT", "CONSENT_ALREADY_EXISTS", ex.getMessage())`).
   - `IllegalArgumentException` (e.g. domain not found or not belonging to org) -> HTTP `400 Bad Request` or `404 Not Found`.

---

## 7. CORS, CSRF & Concurrency

1. **CORS Configuration**:
   - `WebMvcSecurityConfig` adds CORS mapping for `/api/**`:
     - Allowed Origins: `http://localhost:3000` (Next.js frontend development).
     - Allowed Methods: `GET`, `POST`, `PUT`, `DELETE`, `OPTIONS`, `PATCH`.
     - Allowed Headers: `Authorization`, `Content-Type`, `X-Request-Id`.
2. **CSRF Immunity**:
   - The platform uses stateless Bearer token authentication in the `Authorization` header (no ambient browser cookies used for API state mutations), preventing Cross-Site Request Forgery.
3. **Concurrency Safety**:
   - `TenantContext` uses `ThreadLocal` storage isolated per request thread.
   - Database uniqueness on `token_hash` prevents session collision races.
