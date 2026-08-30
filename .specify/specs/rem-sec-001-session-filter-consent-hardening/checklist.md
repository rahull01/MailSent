# Pre-Implementation Checklist: REM-SEC-001 — SessionAuthenticationFilter & ConsentController Hardening

<!--
  AUTHORITY & SUBORDINATION:
  Subordinate to PROJECT_CONSTITUTION.md §19 (Definition of Done) & §26 (Review Checklist)
-->

**Feature**: `REM-SEC-001`  
**Status**: Pre-Implementation Review Complete  

---

## 1. Constitutional Compliance Checklist
- [x] **Rule N1 (The Golden Rule)**: Solves real multi-tenant security vulnerability protecting enterprise clients.
- [x] **Rule N2 (Never Fabricate Truth)**: Sessions and tenants are validated against durable database records; no synthetic tokens.
- [x] **Rule N3 (No Silent Loss)**: All authentication/authorization failures produce structured `ApiError` responses with MDC correlation IDs.
- [x] **Rule N4 (Zero Secrets in Logs/Code)**: Raw bearer tokens are never logged or stored; only SHA-256 hashes are processed.
- [x] **Rule N5 (Mandatory Automated Tests)**: Complete suite of unit and MockMvc integration tests planned.
- [x] **Rule N6 (No Lying to the User)**: Zero mock state.
- [x] **Invariant INV-09 (Multi-Tenant Isolation)**: Eliminates BOLA/IDOR by deriving tenant context exclusively from the verified database session.
- [x] **Invariant INV-10 (Deterministic Stable Identity)**: Preserves immutable User and Session identities.
- [x] **NFR-S1 (Least Authority)**: Enforces `Role.canChange()` so `Role.VIEWER` cannot perform mutations.

---

## 2. Architecture & Design Alignment
- [x] **No Parallel Auth System**: Reuses existing `Session`, `SessionToken`, `SecureSessionTokenFactory` SHA-256 token hashing, and `User` aggregates.
- [x] **No Unnecessary Dependencies**: Uses standard Spring Web MVC `OncePerRequestFilter` and `WebMvcConfigurer` without introducing heavy Spring Security baggage.
- [x] **Zero Migration Impact**: Reuses existing `sessions` table and `uk_sessions_token_hash` unique index created in `V2__create_sessions.sql`.
- [x] **DTO / Domain Model Separation**: Request and Response DTOs completely decouple HTTP transport from domain entities.
- [x] **Thread-Safety & Cleanup**: Guaranteed `try-finally` cleanup of `TenantContext` ThreadLocal and SLF4J MDC.
