# MailSent — Implementation Master Checklist (Engineering Execution System)

> The definitive single executable source for the MailSent implementation team. It converts the frozen, approved documentation into explicit, small, testable tasks (30–90 min). Nothing is implemented outside this checklist; nothing on it is skipped, merged, removed, or invented.
>
> **Source of truth (frozen).** The approved documentation is immutable. On any conflict: **STOP and report**; never modify the documentation yourself.
>
> **Stack scope.** The frozen architecture intentionally leaves transport, database, framework, and file/class/package naming "decided elsewhere." Therefore every task specifies its work at the **contract level** (what must exist, its interface, its owner), never inventing stack-specific names.

---

## How to Use

Work unblocked phases in order; a task must not start while blocked. Mark **Status** with the legend; keep every phase **Progress / Completed / Remaining / Blocked** current. A task is **Done** only when the full **MPL** checklist completes.

## Task Status Legend

⬜ Not Started · 🟨 In Progress · 🟦 Code Complete · 🟪 Tests Complete · 🟩 Review Complete · ✅ Done · 🔒 Frozen

## Mandatory Checklist (MPL)

Planning → Implementation → Testing → Documentation → Review → Deployment → Release → Post-Release. Full contents: Planning(CP/Req/Arch/RTM) · Implementation(Backend/Frontend/DB/API/AI/Flag) · Testing(T-MAT) · Docs(API/README/Changelog) · Review(Code/Arch/Founder) · Deployment(flag/migration/rollback) · Release(note/smoke) · Post-Release(SLO/no-bug/acceptance).

## Standard Matrices

- **T-MAT** Testing (12): happy, edge, negative, failure-recovery, regression, performance, security, integration, E2E, AI-eval, accessibility, compatibility.
- **S-MAT** Security (14): auth, authorization, enc-at-rest, enc-in-transit, secrets, audit, rate-limit, input-validation, output-validation, dependency-scan, pen-test, OWASP, monitoring, incident-response.
- **O-MAT** Observability: logs, metrics, traces, alerts, dashboards, health-checks, jobs, retry, DLQ, error-budget, SLO, SLI.
- **D-MAT** Deployment: flags, blue-green, canary, rollback, DB-migration, backup, restore, DR, smoke, prod-verify.
- **AI-MAT** AI (15): prompt-registry, prompt-version, grounding, thresholds, hallucination-prevention, fallback, routing, token-budget, latency-target, caching, eval-dataset, observability, cost, safety, human-review.

## Traceability Key

- **CP** = CUSTOMER_PROBLEMS · **J** = CUSTOMER_JOURNEY · **VP** = VALUE_PROPOSITION · **PRD** = PRODUCT_REQUIREMENTS · **FUN** = FUNCTIONAL_REQUIREMENTS (D/H/X/E/P/G/M/R/C/F) · **AIR** = AI_REQUIREMENTS · **NFR** = NON_FUNCTIONAL · **REL** = RELEASE_STRATEGY · **RTM** = traceability matrix.
- **Architecture** — **DOM** 01 · **CTX** 03 · **INFO** 04 · **SYST** 05 · **AIAD** 06 · **DATA** 07 · **API** 08 · **SEC** 09 · **DEP** 10.
- **WF** = MASTER_WORKFLOW module (1 Agency Control Center · 2 Infrastructure Intelligence · 3 Alignment Intelligence · 4 Health/Trust Score+Timeline · 5 Infrastructure Map · 6 Guided Fix + AI Action Queue · 7 Continuous Monitoring · 8 Alerts · 9 AI Layer · 10 Compliance · 11 Analytics · 12 Reporting; + DMARC Ingestion, Feature Pipeline).
- **CONST** = Constitution (Golden Rule, invariants INV-nn) · **GLOS** = Glossary.

## Phase Dependencies

00→01→(all). 02→03→07. 04,05,06→portfolio. 07→08. 08→09→10→11→12. 08→13→14. 10,11→15→16. 08,09,11→17. 08,09→18. 19→20→21. 02–21→22. 23 toward 20–21. 24,25,26 cross-cutting. 27,28,29→30→31.

---

# PHASE 00 — Repository Foundation
**Purpose:** immutable, secure repository base. **Deliverables:** layout, entry docs, secrets guard, health check, validation report, repository standards, license status, status CI. **Dependencies:** none. **Exit:** a new engineer clones and runs baseline with no secrets. **Progress:** 8 Code Complete (each Waiting for Runtime Verification) · 0 Ready For Sign-off · 1 Deferred Decision (REPO-003).

- **REPO-000 — Initialize repository layout** — **Status: 🟦**
  - **Goal:** codebase base · **Value:** enables all downstream value · **CP:** internal · **WF:** Feature Pipeline step 3 · **Req:** REL · **Arch:** DEP 10
  - **Desc:** created layout + entry docs; default branch `main`, branch protection.
  - **Completed (Phase 00):** root `README.md`, `.gitignore`, `CONTRIBUTING.md`, `CHANGELOG.md`, `scripts/` created.
  - **Deferred decisions:** `app`/`domain`/`infra` scaffolding requires the stack decision (frozen docs leave stack open); branch protection requires a remote host.
  - **Accept:** clone builds a stub, branch protected — pending stack decision + host. · **Validate:** pending. · **DoD:** MPL
- **REPO-001 — Secrets guard** — **Status: 🟦** — `scripts/guard-secrets.sh` written; Waiting for Runtime Verification. **Finding:** `.jetro/daemon/credentials.json` present in the tree — excluded via `.gitignore`, must never be committed.
- **REPO-002 — Entry docs + changelog discipline** — **Status: 🟦** — `README.md`, `CONTRIBUTING.md`, `CHANGELOG.md` created per CONST §25.
- **REPO-004 — Repository health check** — **Status: 🟦** — Depends REPO-000/001/002 · `scripts/repo-health.sh` written: folder structure, required files, LF line endings, executable + shebang, duplicate content, empty dirs, broken links, invalid filenames. Waiting for Runtime Verification.
- **REPO-005 — Repository security hardening** — **Status: 🟦** — Depends REPO-001 · extended `scripts/guard-secrets.sh`: credentials.json, `*.secret`/`*.token`/`*.p12`/`*.pfx`/`*.jks`/`*.keystore`, service-account keys, SSH/private keys, GitHub/OpenAI/Anthropic/Google/AWS/Azure credentials. No credential contents read.
- **REPO-006 — Repository validation report** — **Status: 🟦** — Depends REPO-000‑005 · `implementation/05_REPO_006_VALIDATION_REPORT.md` created; Waiting for Runtime Verification.
- **REPO-007 — Repository standards** — **Status: 🟦** — Depends REPO-000 · `.editorconfig` created: UTF-8, LF, final newline, indentation, trim trailing whitespace; no language-specific formatting.
- **REPO-008 — License status** — **Status: 🟦** — Depends REPO-000 · `LICENSE_PENDING.md` created: records no OSS license selected, founders own copyright, no contributor may assume licensing; no MIT/Apache/GPL chosen.
- **REPO-003 — Phase progress status** — **Status: ⬜ Deferred Decision** — CI provider selection and the remote host are intentionally left open by the architecture; to be decided later, not a build blocker.

---

# PHASE 01 — Engineering Foundation
**Purpose:** build/test/observe base. **Deliverables:** backend shell (Spring Boot/Java21), frontend shell (Next.js/TS/Tailwind), Docker Compose, CI, error/log/validation/observability foundations, tests. **Deps:** 00 (approved). **Exit:** baseline application starts; a trivial change flows green.
**Stack (LOCKED by CTO):** Java 21 · Spring Boot · Next.js · TypeScript · Tailwind · PostgreSQL · Redis · Docker Compose · **Maven** (the locked engineering build standard for MailSent).
**Status:** 🔒 Frozen (approved to freeze by CTO) · **Progress:** 6 Code Complete · 2 Not Started (ENG-003/004, carried forward) · 0 Ready For Sign-off.

- **ENG-000 — App shell (backend + frontend)** — **🟦** — Spring Boot shell (`backend/`) + Next.js shell (`frontend/`) created; baseline app starts.
- **ENG-001 — CI pipeline (lint, build, unit)** — **🟦** — `.github/workflows/ci.yml`: secrets guard, health check, `mvn verify`, `npm lint` + `npm build`.
- **ENG-002 — Unit test foundation** — **🟦** — test profile (hermetic), context-load test, structured-error test (`backend/src/test`).
- **ENG-003 — Integration harness (fake external provider)** — **⬜ Not Started** — requires a provider-stub harness.
- **ENG-004 — E2E harness (stubs)** — **⬜ Not Started**.
- **ENG-005 — Continuous-test descriptors** — **🟦** — Maven Surefire + Next lint/build wired as the test contract.
- **ENG-006 — Error + logging foundation (no silent failure, INV)** — **🟦** — `GlobalExceptionHandler`, `RequestIdFilter` (MDC), logging pattern.
- **ENG-007 — Observability foundation (metrics/traces/SLO)** — **🟦** — Spring Actuator (health, metrics, Prometheus) exposed.

**Note (freeze):** Phase 01 is 🔒 **Frozen**. The 🟦 tasks are Code Complete in scope. A full CI run (`mvn verify`, `npm build`, compose smoke) remains a verification follow-up, not a Phase 01 blocker. ENG-003/004 (integration/E2E harnesses) are **carried forward** to a future hardening. Phase 01 is not modified unless a **verified bug** is found.

---

# PHASE 02 — Authentication & Identity
**Status:** ✅ Done · **Purpose:** authenticate users; Identity (User, Role), Consent. **Deps:** 01. **Exit:** sign-up + role; consent recorded before action. **Execution Progress:** AUTH-000 ✅ · AUTH-001 ✅ · AUTH-002 ✅ · AUTH-003 ✅ · AUTH-007 ✅ · AUTH-004 ⏸️ Deferred/No-contract · AUTH-005 ✅ · AUTH-006 ⏸️ Deferred/Unspecified.
**Fully closed:** Phase 02 is now declared fully closed. Its Consent exit condition (AUTH-005) is satisfied. The remaining items (AUTH-004, AUTH-006) are legitimately deferred/unspecified by the project constitution.
**Trace anchors:** CP-8 trust · J-3 → J-6 readiness · VP-6 confidence · WF (Feature Pipeline) · Arch CTX Identity / DATA / SEC.

- **AUTH-000 — Sign up**
  - **Goal:** password-credentialed identity · **Value:** gate to every recurring capability · **CP:** CP-8 · **J:** J-3/J-6 · **VP:** VP-6 · **WF:** Pipeline 3 · **Req:** PRD / NFR-S1 · **Arch:** CTX Identity / DATA / SEC
  - **Desc:** register user with password policy; record Identity and a Consent record (INV-02).
  - **Pri:** P0 · **Time:** 45m · **Dep:** ENG-000 · **Owner:** Auth · **Status:** 🟦
  - **Accept:** new user registers and receives a role · **Validate:** integration auth test · **DoD:** MPL
  - **Backend:** auth-service · **DB:** users, roles, consent · **API:** sign-up · **Sec:** S-1,4,5,8 · **Perf:** — · **Test:** T-1,2,7 · **Obs:** O-1 · **Release:** REL-A · **Rollback:** flag · **Risk:** credential leak → hard vault · **Block:** — · **Checklist:** MPL
- **AUTH-001 — Sign in / session** — atomic hardened session. P0 60 🟩 (Review Complete / CTO approved)
  - **REOPENED during CTO audit — RESOLVED: audit remediation ACCEPTED by CTO — CLOSED.** Reason: CTO-audit hardening touched this milestone's HTTP behavior in two spots — (a) trailing-slash URIs (`POST /api/sessions/`) bypassed the public allowlist and returned 401; (b) syntactically invalid emails surfaced as 500 instead of 400, eroding the documented anti-enumeration uniformity. Executable evidence: filter path-matching code + controller handler gap. Remediation: path normalization + `InvalidEmailException` → 400 (well-formed addresses keep the uniform 401). Focused auth suites + full regressions green. Prior sign-off preserved above. Final state: PREVIOUSLY SIGNED OFF → AUDIT DEFECT DISCOVERED → REMEDIATION VERIFIED → **CTO ACCEPTED → CLOSED**.
- **AUTH-002 — User identity entity** — DATA User+Role. P0 60 🟦 (identity+role realized via AUTH-001; verified + tested)
- **AUTH-003 — Role model** — Owner/Analyst/Viewer, least-authority. P0 60 🟦 (D5 see/act semantics; least-authority VIEWER)
- **AUTH-004 — Password reset.** P1 60 ⬜ **Status: ⬜ Not Started · Decision: ⏸️ DEFERRED/BLOCKED**
  - **Blocker:** No approved MailSent-internal password-reset requirement exists in the frozen documentation (references describe customer transactional email deliverability, not an internal recovery capability). Do not implement.
- **AUTH-005 — Consent record for an action** — explicit, INV-02. P0 45 ✅ **Status: ✅ Done**
  - **REOPENED during CTO audit — RESOLVED: audit remediation ACCEPTED by CTO — CLOSED.** Reason: audit evidence showed `ConsentService.requireConsent(domainId,…)` accepted a GRANTED row regardless of which organization granted it — an INV-09×INV-02 seam hole in the gate this milestone owns. Executable evidence: signature analysis + new test `requireConsent_RejectsForeignTenantConsent`. Remediation: gate now tenant-bound (`organizationId` parameter, consent-org equality filter); three observation call sites updated. Focused 99/99 incl. new security test + full regressions green. Prior sign-off preserved above. Final state: PREVIOUSLY SIGNED OFF → AUDIT DEFECT DISCOVERED → REMEDIATION VERIFIED → **CTO ACCEPTED → CLOSED**.
  - **Dependency:** Organization foundation (Phase 03).
  - **Blocker:** Consent is owned by the Organization aggregate (DATA: "Protects ... the Consent it gives (INV-02)"); Identity and Access "Depends On: the definition of an Organization" (03_BOUNDED_CONTEXTS). Consent requires an existing Organization identity to be recorded against; User/Role alone cannot represent the ownership boundary. Implementation must wait for the Organization foundation.
- **AUTH-006 — Current-user contract** — per API 08. P1 45 ⬜ **Status: ⬜ Not Started · Decision: ⏸️ DEFERRED / UNSPECIFIED**
  - **Reason:** Checklist task exists without an executable source-of-truth contract. 08_API_PHILOSOPHY defines principles only; no frozen Functional Requirement or architecture contract defines a concrete current-user/session-introspection endpoint, DTO, or model. No implementation is authorized until an approved requirement/API contract exists. Do not invent GET /me, session introspection, or token introspection.
- **AUTH-007 — Authentication Module Foundation** — DDD module boundaries only (config/domain/application/infrastructure/interfaces; frontend feature/service/hooks/types; no controllers/endpoints/DB/JWT). P0 30 **Status:** 🟦
  - **Accept:** package structure follows DDD; naming per Glossary; project builds · **Validate:** backend verify + frontend build · **Dep:** ENG-000 · **Owner:** Auth

---

# PHASE 03 — Organizations
**Purpose:** Organization aggregate. **Deps:** 02. **Exit:** org, membership, isolation. **Execution:** ORG-001 ✅ · ORG-002 ✅ · ORG-003 ✅ · ORG-004 ✅. Phase 03 fully verified/closed (CTO verification).
**Trace anchors:** CP-1 · VP-4/5 · Arch DATA Organization / CTX Portfolio.

- **ORG-001 — Create/read/list/close org** — P0 60 · Data DATA Organization · Status ✅ Done (aggregate+persistence; no API contract in frozen docs)
- **ORG-002 — Membership** — user joins an org. P0 60 ✅
- **ORG-003 — Org→Domain ownership** — registry authorization. P0 60 ✅
- **ORG-004 — Isolation invariant** — INV-09 test. P0 45 ✅ Done
  - CTO verified: frozen INV-09 ("one organization's health and data never mix with another's") proven by `IsolationInvariantPersistenceIntegrationTest` over real PostgreSQL (containment / no-overlap / totality across org-scoped domain reads; user home-org binding never crosses tenants) plus existing service-layer cross-tenant rejection (`DomainService.register(User,…)` → `AccessDeniedException`; `DomainQueryService` ownership filter). Focused run 2/0/0; full PostgreSQL regression 1048/0/0/0 BUILD SUCCESS. Test-only task — no new mechanism invented; V1–V20 untouched.

---

# PHASE 04 — Teams
**Purpose:** roles, invite, least-authority, audit. **Deps:** 02,03. **Exit:** a team admin adds an analyst who acts in scope. **Progress:** 100% · 4/4 · 0.

- **TEAM-001 — Invite by email & accept** — P0 60 · Arch CTX Identity · FUN-D5 · Status ✅ Done
  - CTO verified (frozen v8 contract): creation `invite` is internal application-layer only (no HTTP surface, authorization deferred to AUTH-006/TEAM-002); acceptance is the single `POST /api/invitations/accept`. Token: 32-byte CSPRNG → 64-hex raw, SHA-256 hash stored only. Single-use enforced by the atomic conditional UPDATE (`markAcceptedIfPending`) inside one transaction; frozen acceptance order incl. existing-user check and home-org equality (INV-09/13 — acceptance creates NO membership and grants NOTHING). Duplicate PENDING prevented race-safely by the V6 partial unique index `(organization_id, invitee_email) WHERE status='PENDING'`; no memberships table. Focused run 44/0/0 (InvitationServiceTest 13, InvitationTest 7, JpaInvitationRepositoryAdapterTest 4, TeamPersistenceIntegrationTest 20 on real PostgreSQL incl. concurrent acceptance/duplicate-creation races, schema/FK/index assertions, and HTTP error-contract without metadata leakage); full regression 1048/0/0/0 BUILD SUCCESS.
  - **REOPENED during CTO audit — RESOLVED: audit remediation ACCEPTED by CTO — CLOSED.** Reason: audit evidence showed the ACCOUNT_REQUIRED response echoed `ex.getMessage()` containing the invited email — violating this milestone's own frozen "no invitation metadata" boundary. Executable evidence: exception message construction vs controller body mapping (integration tests asserted code only, so the leak was untested). Remediation: generic message, email retained server-side only. Focused team suites + full regressions green. Prior sign-off preserved above. Final state: PREVIOUSLY SIGNED OFF → AUDIT DEFECT DISCOVERED → REMEDIATION VERIFIED → **CTO ACCEPTED → CLOSED**.
- **TEAM-002 — Role→capability map (least-authority)** — P0 60 ✅
- **TEAM-003 — Access audit record** — P1 60 · NFR-AU ✅
  - **REOPENED during CTO audit — RESOLVED: audit remediation ACCEPTED by CTO — CLOSED.** Reason: audit evidence showed `AuthorizationService.recordAudit` swallowed sink failures in an empty catch — a broken audit sink silently dropped access-decision records, contradicting the N3 posture this milestone claims. Executable evidence: empty-catch code path. Remediation: loud `log.error` with record context (availability-over-completeness retained). Full regressions green. Prior sign-off preserved above. Final state: PREVIOUSLY SIGNED OFF → AUDIT DEFECT DISCOVERED → REMEDIATION VERIFIED → **CTO ACCEPTED → CLOSED**.
- **TEAM-004 — Team list query** — P1 45 ✅

---

# PHASE 05 — Clients
**Purpose:** Client relationship (agency). **Deps:** 03,04. **Exit:** client created, assigned, isolated. **Progress:** 100% · 3/3 · 0.

- **CLIENT-001 — Client CRUD** — P0 60 · DATA Client/Portfolio ✅ Done
  - CTO verified: `ClientRelationship` aggregate mirrors the DATA "Client Relationship" aggregate (root Client) with the frozen lifecycle formed → sustained → ended (`form` ACTIVE / `end` irreversible, no physical delete, no generic update, self-relationship rejected); organization-to-organization only — never references a User (no membership mechanism). Service `form/findById/end`; both organizations must pre-exist (INV-14; CLIENT-001 never creates an Organization). No HTTP API in frozen docs — none invented. V7 `client_relationships`: exact schema, dual FKs → organizations(id), no speculative constraints. Original commit deferred its PostgreSQL integration run (Docker unavailable then); now executed: focused run 33/0/0 (ClientRelationshipTest 8, ClientRelationshipServiceTest 17 incl. CLIENT-002 authz cases, JpaClientRelationshipRepositoryAdapterTest 3, ClientRelationshipPersistenceIntegrationTest 5 on live PostgreSQL incl. schema/FK/no-membership assertions and lifecycle-does-not-alter-organizations); full regression 1048/0/0/0 BUILD SUCCESS.
- **CLIENT-002 — Assign client to agency** — P0 60 ✅
  - **REOPENED during CTO audit — RESOLVED: audit remediation ACCEPTED by CTO — CLOSED.** Reason: audit evidence showed the idempotency lookup used an `Optional`-returning derived query (`findBy…AndStatus`) that throws `IncorrectResultSizeDataAccessException` once >1 ACTIVE row exists for a pair — and duplicate ACTIVE rows are contract-legal (V7 has no uniqueness constraint), reachable via `form()` or concurrent `assign()`. Executable evidence: repository signature + V7 DDL. Remediation: deterministic `findFirstBy…OrderByFormedAtAscIdAsc`. Focused client suites + full 1058 regression green. Prior sign-off preserved above. Final state: PREVIOUSLY SIGNED OFF → AUDIT DEFECT DISCOVERED → REMEDIATION VERIFIED → **CTO ACCEPTED → CLOSED**.
- **CLIENT-003 — Client isolation (INV-09)** — P0 45 ✅

---

# PHASE 06 — Portfolio
**Purpose:** portfolio-scale view. **Deps:** 05. **Exit:** an agency sees a portfolio with risk. **Progress:** 100% · 3/3 · 0.

- **PORT-001 — Portfolio container** — P0 60 · FUN-D4 ✅
- **PORT-002 — Portfolio overview data** — P0 90 · FUN-D4 ✅ Done
  - **REOPENED during CTO audit — RESOLVED: audit remediation ACCEPTED by CTO — CLOSED.** Reason: post-signoff commit b3b29b5 (HEALTH-001/V16) made the service javadoc's "no health pipeline exists" claim false while the frozen trace requirement FUN-D4 ("who is at risk") remained unimplemented by this milestone. Executable evidence: new unit + real-PostgreSQL tests demonstrate signals existed but were never consumed by the overview. Remediation implemented: `DomainOverview` projection carrying the canonical indication verbatim (`null` = explicit absence), INV-09 ownership guard, stale javadocs corrected. Focused 42/0/0; full regression 1058/0/0/0. Prior ✅ sign-off preserved above. **CTO SPECIAL RULING:** this is contract-completing work, not an adjacent enhancement — FUN-D4 mandates portfolio-scale health/risk visibility, so once HEALTH-001 became authoritative the overview was required to consume the canonical signal. Implementation retained in full (DomainOverview, canonical indication projection, latest-signal selection, explicit null absence, INV-09 guard, tests, PostgreSQL verification). Final state: PREVIOUSLY SIGNED OFF → AUDIT DEFECT DISCOVERED → REMEDIATION VERIFIED → **CTO ACCEPTED → CLOSED**.
- **PORT-003 — Portfolio isolation** — P1 45 · INV-09 ✅

---

# PHASE 07 — Domains
**Purpose:** Domain entity + registry. **Deps:** 03,06. **Exit:** add/import/list domains with ownership. **Progress:** 0% · 0/4 · 0.

- **DOM-001 — Add domain (manual)** — P0 60 · FUN-D1 ✅
- **DOM-002 — Bulk CSV import** — P1 90 · FUN-D2 ✅ *(authorization remediation CLOSED per CTO; CSV sub-scope = BLOCKED / UNSPECIFIED — deferred pending executable CSV contract; see notes below)*
  - **Audit remediation (CTO-authorized batch) — CLOSED.** Defect: import took a bare `organizationId` with zero authorization (cross-tenant write seam; inconsistent with DOM-001). Fix: authenticated `importDomains(User caller, …)` enforcing INV-09 + `DOMAIN_MANAGE` via `AuthorizationService` (authz precedes existence — no org-id probing); unauthenticated overload removed. CSV file-parsing remains an explicit non-implementation (no frozen format/upload contract; documented deferral stands). Tests: unauthorized/cross-tenant/least-authority rejections added. Focused domain suites green; final regression **1084/0/0/0**.
  - **DOM-002 CSV SUB-SCOPE: BLOCKED / UNSPECIFIED — implementation deferred pending an approved executable CSV contract (CTO closure directive).** Frozen Product Truth names the capability ("Multi-domain management (CSV bulk import, manual add)" — MAILSENT_MASTER_WORKFLOW.md:112, V1 scope, no reduction), but NO executable specification exists for: columns/schema · header rules · encoding · delimiter/dialect · file transport/API · maximum size · malformed-row behavior · duplicate-row behavior · partial-success semantics · transaction/persistence semantics. No parser was invented. The already-implemented domain-layer behavior (`List<String>` import behind an authenticated caller) is intentionally unchanged — it violates no currently specified executable CSV contract. This sub-scope must NOT be marked complete. Decisions required before any implementation authorization: (1) columns/schema + header/encoding/dialect/size constraints, (2) transport/API boundary, (3) malformed-row semantics, (4) duplicate-row semantics, (5) partial-failure/transaction semantics.
- **DOM-003 — Domain list/state** — P0 60 · FUN-D3 ✅
- **DOM-004 — Registry ownership** — P0 60 · FUN-D4 ✅

---

# PHASE 08 — Infrastructure Observation
**Purpose:** observe infrastructure into Observation Evidence, Reverse DNS Observation, SMTP Greeting Observation, Reputation Observation, and Blacklist Observation (raw observed facts). **Deps:** 07. **Exit:** domain records read and stored as those snapshots, not inferred, not Health Signal, and not Phase 16 proof of health. **Progress:** 75% · 9/12 · 0.
**Trace anchors:** CP-1/2 · VP-1/2 · WF-2 + Scanner · Arch CTX Delivery Observation / DATA / INFO.

- **INF-001 — DNS read (MX/SPF/TXT/DKIM)** — P0 90 · FUN-H2 · INV-01 · Status ✅
  - **REOPENED during CTO audit — RESOLVED: audit remediation ACCEPTED by CTO — CLOSED.** Reason: CTO-audit evidence showed this adapter alone omitted `Lookup.setCache(null)` (all sibling adapters set it), so dnsjava's default cache served stale records stamped with fresh `observedAt` — eroding the INV-01 freshness this milestone owns. Executable evidence: code diff vs sibling adapters; fix is one line (`setCache(null)`); focused DNS tests + full 1058 regression green. Prior sign-off preserved above. Final state: PREVIOUSLY SIGNED OFF → AUDIT DEFECT DISCOVERED → REMEDIATION VERIFIED → **CTO ACCEPTED → CLOSED**.
  - Freeze note: the title's DKIM is leftover. DKIM facts are not Phase 08 Observation Evidence outputs. Observation Evidence remains frozen to MX/TXT/SPF/observedAt/identity. DKIM observation is not in OE. Do not infer DKIM, DMARC, or alignment from Phase 08 DNS TXT. INF-001 implementation is not reopened.
- **INF-002 — Observation lifecycle → Observation Evidence** — P0 90 · no fabrication · not Phase 16 proof · ✅
- **INF-003 — Reputation ingest (mocked first, grounded)** — P0 90 · FUN-H3 · ✅
- **INF-004 — Re-observe (schedule)** — P0 60 · FUN-M1 · ✅
  - Delivery Observation scheduled re-read of Observation Evidence (DNS) and Reputation Observation. Not HEALTH-001 (Health Signal unification) and not MON-001 (Phase 13 Monitoring of health over time). Default daily `mailsent.observation.reobserve.interval` (86400000 ms); same-instance non-overlap via `spring.task.scheduling.pool.size=1` plus an AtomicBoolean skip. No Flyway V10.
- **INF-005 — Reverse DNS Observation (ReverseDnsObservation)** — P1 60 · MX→A/AAAA→PTR · raw facts · not Health · not FCrDNS · ✅
  - Delivery Observation raw reverse-DNS snapshot for IPs of observed MX hosts of a registered Domain (resolve MX hostname to A and/or AAAA, then PTR; both families when addresses actually exist). Store raw reverse-DNS facts only. Separate from Observation Evidence (frozen MX/TXT/SPF/observedAt/identity; do not add PTR to OE). Append-only immutable. Identity from registered Domain; caller supplies domainName only. No public HTTP API. No independent scheduler. Not INF-007 (DNSSEC/TTL), not INF-008 (blacklists). HEALTH-001 may consume later; INF-005 must not create Health Signal. Future Flyway required; do not create now. INF-001–004 untouched until implementation authorized.
  - **Audit remediation (CTO-authorized batch) — CLOSED.** Consent seam: PTR probing of the domain's own mail infrastructure falls under `OBSERVE_DOMAIN_INFRASTRUCTURE`; service now gates via `requireConsent(organizationId, domainId, …)` (mirrors INF-001/006). Missing/revoked consent ⇒ empty result, nothing persisted. Missing-consent test added; real-PostgreSQL IT seeds owner consent for every observed domain (production semantics). Final regression **1084/0/0/0**.
- **INF-006 — SMTP Greeting Observation (SmtpGreetingObservation)** — P1 45 · MX→SMTP · HELO-EHLO raw facts · not Health · ✅
  - **REOPENED during CTO audit — RESOLVED: audit remediation ACCEPTED by CTO — CLOSED.** Reason: audit evidence showed `SocketSmtpProbe.readReply()` read unbounded bytes/lines from remote MX input — a hostile MX could stream indefinitely (remote DoS) and oversized data risked persisting as a "fact". Executable evidence: code path analysis; remediation caps replies at 64 KiB / 256 lines with overflow treated as no-usable-reply (persist nothing). Focused probe tests + full 1058 regression green. Prior sign-off preserved above. Final state: PREVIOUSLY SIGNED OFF → AUDIT DEFECT DISCOVERED → REMEDIATION VERIFIED → **CTO ACCEPTED → CLOSED**.
  - Delivery Observation raw SMTP greeting/banner and EHLO capability snapshot for IPs of observed MX hosts from Observation Evidence of a registered Domain (resolve MX hostname to A and/or AAAA, then observe SMTP). Store raw facts only. Active probe permitted only against registered-domain infrastructure; no mail send, no mutation, no SMTP auth, no relay/delivery testing, no arbitrary IPs or third-party hosts. Separate from Observation Evidence (do not add greeting/EHLO to OE) and from Reverse DNS Observation. Append-only immutable. Identity from registered Domain; caller supplies domainName only. No public HTTP API. No independent scheduler; do not modify INF-004. Not INF-007 (DNSSEC/TTL), not INF-008 (blacklists), not HEALTH-001, not mail sending. HEALTH-001 may consume later; INF-006 must not create Health Signal. Unavailable (timeout/refusal/DNS) persists nothing; do not fabricate banner or EHLO; connection/probe outcome records unavailability without fabricated greeting/EHLO text. Own persistence model and future Flyway required; do not create now. Do not modify Observation Evidence, V8, or ReverseDnsObservation. INF-001–005 implementation untouched until implementation is authorized.
- **INF-007 — DNS Health Observation (DnsHealthObservation)** — P1 60 · TTL/DNSSEC/propagation raw facts · not Health · ✅
  - Delivery Observation raw DNS TTL, DNSSEC-related presence, and named-resolver propagation snapshot for a registered Domain. Store raw facts only. Separate from Observation Evidence (frozen MX/TXT/SPF/observedAt/identity; do not add TTL/DNSSEC/propagation to OE). Append-only immutable. Identity from registered Domain; caller supplies domainName only. No public HTTP API. No independent scheduler; INF-004 unchanged. Not Health Signal, not Trust Score, not HEALTH-001, not INF-008. HEALTH-001 may consume later; INF-007 must not create Health Signal. Flyway V10 `dns_health_observation`.
  - **Audit remediation (CTO-authorized batch) — CLOSED.** Consent seam: reading the domain's DNS health is `OBSERVE_DOMAIN_INFRASTRUCTURE` per the Consent definition; service now gates before observing — missing/revoked consent raises `ConsentRequiredException` and persists nothing. Missing-consent test added; real-PostgreSQL IT seeds owner consent for every observed domain (production semantics). Final regression **1084/0/0/0**.
- **INF-008 — Blacklist monitor (Spamhaus/Barracuda/SURBL/URIBL)** — P1 90 ✅
  - Delivery Observation raw blacklist snapshot (Blacklist Observation) for a registered Domain from Spamhaus, Barracuda, SURBL, and URIBL. Store raw provider facts only. Append-only immutable. Identity from registered Domain; caller supplies domainName only. Four ports queried independently; persist only a real observation; unavailable/empty/exception persists nothing for that provider. Production adapters report unavailability (no invented HTTP/zones/credentials). No public HTTP API. No independent scheduler; INF-004 unchanged. Not Health Signal. Flyway V11 `blacklist_observation`. INF-001–007, V1–V10, ReObservationService, Observation Evidence, and Reputation Observation unchanged.
**Infrastructure Map (WF-5)**
- **MAP-001 — Map model** (Domain→DNS→SPF→DKIM→DMARC→Senders→Inbox) — P1 60 ✅
  - Topology read-model only (not an aggregate, not Health, not MAP-002–004). Checkbox remains until CTO audit.
- **MAP-002 — Color-coded view** — P1 90 ✅
  - Consumes canonical HEALTH-001 HealthSignal (HEALTHY -> Green, AT_RISK -> Red/Warning, UNKNOWN -> Neutral/Gray). Preserves independent observation availability (OBSERVED, NOT_OBSERVED, UNAVAILABLE) and strict multi-tenant isolation (INV-09). Verified with hermetic unit tests and full integration gate.
- **MAP-003 — Node detail panel** — P1 90 ✅
  - Opened by selecting/clicking a MAP-001/MAP-002 node (WF-5 click-to-detail). Displays factual node identity, node type, observation availability, canonical health indication, and real attached evidence references without fabricating unobserved details or recalculating health. Fully verified with unit tests and frontend build.
- **MAP-004 — PNG/SVG export** — P2 90 ✅
  - Exports the client-rendered MAP-001/MAP-002 topology as standalone SVG and PNG image assets directly using standard DOM, Canvas, and SVG generation without third-party rendering bloat. Preserves canonical HEALTH-001/MAP-002 health indications and observation availability. Verified with frontend build and full regression gate.

---

# PHASE 09 — Diagnosis Engine
**Purpose:** derive cause from signals. **Deps:** 08. **Exit:** root cause with lineage. **Progress:** 100% · 4/4 · 0.
**Trace anchors:** CP-6 · WF Analyzer (DIAG-001 Alignment Engine) + WF-3 Alignment Intelligence (DIAG-002/003) · Arch CTX Diagnosis.
**CTO freeze (DIAG-001 / Phase 09–10):** Health Signal is produced by HEALTH-001 (Phase 10 / Delivery Observation health engine), **not** by Diagnosis. Diagnosis must not fabricate Health Signal. Program order remains 08→09→10 for ingest facts (INT-003–006 + Aggregate Evidence); HEALTH-001 code after its freeze need not wait for DIAG-002/003/004. DIAG-001 Alignment derivation does not start from health and does not wait on HEALTH-001 as a fabricated input. DIAG-001 in-memory Alignment is not a HEALTH-001 input. DIAG-003 (root cause / Issue) remains blocked until **Java** assigns **healthy vs at-risk** per the HEALTH-001 interpretation freeze, not merely **unknown** (do not implement DIAG-003). MAP-002 still waits for a persisted real Health Signal **and** a later MAP-002 color freeze; MAP-002/003/004 remain ⬜ and unimplemented.

- **DIAG-001 — Alignment** (SPF/DKIM, envelope vs header) — P0 90 · Alignment Engine / FUN-H2 (not FUN-X1 as primary) ✅
  - CTO freeze & Ratification: Analyzer-class Alignment derivation (Constitution Analyzer: SPF/DKIM/DMARC parsing + alignment). Ratified once INT-003–006 produced evidence in code. Does not persist Alignment table. Does not create Health Signal or Trust Score. Unit tested in `AlignmentDerivationServiceTest`.
- **DIAG-002 — Sender detection** (recognized providers) — P0 90 · FUN-X2 ✅
- **DIAG-003 — Root cause determination** — P0 90 · FUN-X3 · INV-11 ✅
- **DIAG-004 — Issue entity lifecycle** — P0 60 · DATA Issue ✅

---

# PHASE 10 — Health Signal Engine
**Purpose:** unified Health Signal + trend. **Deps:** 09 ingest facts (INT-003–006 + DMARC Aggregate Evidence); DIAG-002/003/004 remain ⬜ and are **not** HEALTH-001 inputs. **Exit:** single, truthful signal with trend; no fabricated rank. Phase 10 exit “signal with trend” is **phase exit**, not HEALTH-001 item exit. **Progress:** 75% · 3/4 · 0.
**Trace anchors:** CP-3 · WF-4 · GLOS Health Signal.
**CTO freeze (HEALTH-001 interpretation, 2026-08-15):** Delivery Observation **owns** Health Signal and Health Trend (trend is HEALTH-002). Diagnosis does **not** own Health Signal. DIAG-001 does not create Health Signal. HEALTH-001 consumes **persisted snapshots only** — never `Alignment.lastDerived()` / in-memory DIAG-001.

- **HEALTH-001 — Unify Health Signal (components)** — P0 60 · FUN-H1 · INV-01/05 ✅ (not INF-004)
  - **Owner:** Delivery Observation. Diagnosis does not own Health Signal. DIAG-001 does not create Health Signal.
  - **V1 required persisted snapshots** (latest per Domain; identity from registered Domain). Missing **any** ⇒ **persist nothing** (not a healthy/at-risk/unknown row):
    - **Observation Evidence (V8)** — Java/SQL fields actually stored: `id`, `organization_id`, `domain_id`, `domain_name`, `observed_at`, `mx_records`, `txt_records`, `spf_record`. Frozen DNS records, **not** SPF-pass. Latest OE by `observedAt` then `id` (current Java).
    - **DMARC Aggregate Evidence (V15)** — Java/SQL fields actually stored: `id`, `organization_id`, `domain_id`, `domain_name`, `receipt_id`, `report_id`, `source_ip`, `utc_day`, `message_count` (`count` in Java), `spf_result`, `dkim_result`, `spf_aligned`, `dkim_aligned`, `envelope_from`, `header_from`, `dkim_domains`, `disposition`. Cite **all rows of the latest report** as current Java: greatest non-null `utc_day`, then max `report_id` on that day, then every row with that `report_id`.
    - **Reputation Observation (V9)** — Java/SQL fields actually stored: `id`, `organization_id`, `domain_id`, `domain_name`, `source` (`POSTMASTER` | `SNDS`), `observed_at`, `payload` (opaque TEXT, required non-null). At least **one** persisted row for the Domain (current Java: latest per source that exists). Production `UnavailableGooglePostmasterAdapter` / `UnavailableMicrosoftSndsAdapter` return empty and INF-003 **persists nothing** — no row is not a snapshot.
  - **V1 not required** (must not wait; must not invent from them): Blacklist Observation, Reverse DNS Observation, SMTP Greeting Observation, DNS Health Observation, DIAG-001 Alignment VO.
  - **Alignment intake:** HEALTH-001 reads DMARC Aggregate Evidence row fields. It does **not** persist Alignment, create an Alignment table, recompute RFC alignment, string-compare `envelope_from`/`header_from`, or treat DIAG-001 as a stored input.
  - **INT-004 stored tokens (quoted from parser + tests; raw XML text, trimmed; empty node ⇒ null):**
    - `spf_result` / `dkim_result` = first `auth_results/spf/result` and first `auth_results/dkim/result` (additional DKIM auth results are **not** stored in `dkim_result`). Tests persist lowercase `pass` and `fail`. RFC 7208/6376 may also yield `none`, `neutral`, `softfail`, `temperror`, `permerror` — stored as-is if present; **not** mapped.
    - `spf_aligned` / `dkim_aligned` = raw `policy_evaluated/spf` and `policy_evaluated/dkim`. Tests persist `pass` and `fail`. RFC 7489 PolicyEvaluated is typically `pass`/`fail`.
    - `disposition` = raw `policy_evaluated/disposition`. Tests persist `none` and `quarantine`. RFC 7489 is typically `none` / `quarantine` / `reject`. **Do not** use `policy_published/p` (parsed on the report, **not** a V15 row column).
    - Compare tokens **case-insensitively** after trim. Do not invent other tokens.
  - **Voters vs lineage (explicit — do not guess):**
    - **OE does not vote** healthy/at-risk from MX/TXT/SPF strings (that would invent SPF validity). OE is **lineage-required presence**. Present OE never by itself yields healthy. Missing OE ⇒ persist nothing.
    - **Reputation Observation is lineage-required presence only, not an indication voter.** V9 has **no** health-like fact (no grade/score/listed column). Opaque `payload` is **not** parsed (would invent provider scores). A persisted row (including a real payload) **does not** create at-risk and **does not** block healthy solely due to reputation. Unavailable adapters persist **no** row: that is missing snapshot ⇒ persist nothing, not unknown. An honest empty observation **is** a snapshot only if a row was actually persisted.
    - **Only cited V15 judgment fields vote:** `disposition`, `spf_result`, `dkim_result`, `spf_aligned`, `dkim_aligned`. Do not vote from `envelope_from`, `header_from`, `dkim_domains`, `source_ip`, `utc_day`, `count`/`message_count`, `report_id`, `receipt_id` (selection/lineage only).
  - **Row evaluation** (every cited row of the latest report; no last-win; no discarding failing rows; no averaging):
    1. **At-risk row** if any of (case-insensitive): `disposition` ∈ {`quarantine`, `reject`}; `spf_result` = `fail`; `dkim_result` = `fail`; `spf_aligned` = `fail`; `dkim_aligned` = `fail`. A known failure token on a row **wins over nulls on that same row** (e.g. `disposition=reject` with null `spf_result` is at-risk).
    2. Else **unknown row** if any of those five fields is **null**, **or** any non-null value is not a healthy token below (including `none`/`neutral`/`softfail`/`temperror`/`permerror` on auth/alignment, mixed `pass`+`none`, or any other token). **Only `fail` is at-risk** for auth/alignment — `softfail`/`none`/etc. are **unknown for that aspect**, not at-risk.
    3. Else **healthy row** iff all five are non-null **and** `disposition` = `none` **and** `spf_result` = `pass` **and** `dkim_result` = `pass` **and** `spf_aligned` = `pass` **and** `dkim_aligned` = `pass`. SPF-pass with DKIM-`none` is **unknown**, not healthy (cannot claim both authentications succeeded). Disposition `pass` is **not** RFC PolicyEvaluated and is **not** healthy.
  - **Unified indication** (required snapshots present):
    - Any cited row at-risk ⇒ **at-risk** (union of failures; known failure wins over pass **and** over unknown on other IPs).
    - Else every cited row healthy ⇒ **healthy**.
    - Else ⇒ **unknown**.
    - Never persist **unknown** (or any indication) when a required **snapshot** is missing — persist nothing.
  - **Stale:** **no TTL**. HEALTH-001 does **not** treat age as at-risk or unknown. Staleness is HEALTH-002 / later. Do not invent “older than 4 hours”.
  - **No scoring / colors / Trust Score / G/Y/R** in HEALTH-001.
  - **Explainability:** V16 has **no** reason column (`id`, `organization_id`, `domain_id`, `domain_name`, `indication`, `observation_evidence_id`, `dmarc_aggregate_evidence_ids`, `reputation_observation_ids`, `observed_at` only). **Do not add reason columns.** Explanation is the deterministic rule that fired, derived at read time from cited evidence (application-level names, e.g. `dmarc_disposition_reject`, `spf_fail`, `insufficient_auth_facts`). Indication + lineage ids suffice on the snapshot.
  - **INV-11 lineage** on every persisted Health Signal (including persisted unknown): organizationId, domainId, domainName; observationEvidenceId; dmarcAggregateEvidence ids actually used; reputationObservation ids actually used; observedAt of the Health Signal. Must **not** cite Observation Evidence as the source of alignment identities. Must **not** cite an Alignment table. Must **not** cite DIAG-001 memory.
  - **Trigger / idempotency / API (unchanged envelope):** explicit in-process `record`/`derive` by domainName. **Not** scheduled. **Not** INF-004. **Not** INT-006. No public HTTP. Append-only allowed only when at least one cited source snapshot id differs from the latest Health Signal; same cited ids ⇒ persist nothing new. This freeze does **not** authorize overwrite or backfill of existing always-unknown rows; they remain until a cited source id changes.
  - **Owns:** Health Signal (unified indication + lineage). **Does not own:** Issues, Root cause, Alignment, Trust Score, Diagnosis, MAP-001–004, Health Trend (HEALTH-002).
  - **HEALTH-002:** trend later. HEALTH-001 completion does **not** require trend. Do not start HEALTH-002.
  - **MAP-002** consumes healthy/at-risk/unknown; **colors later**. An **unknown** indication is not a color source. Do not start MAP-002.
  - **DIAG-003** may later consume Health Signal; HEALTH-001 does not diagnose. DIAG-003 remains blocked until **Java** assigns healthy vs at-risk per this freeze (current always-unknown does not unblock). Do not implement DIAG-003.
  - **Implementation impact:** Prefer **no V16 change**. Later Java must assign healthy/at-risk/unknown per these rules. This freeze does **not** implement Java. Checklist remains ⬜.
  - **Acceptance criteria (HEALTH-001 remains ⬜; docs freeze only):**
    - Missing latest OE **or** latest aggregate report rows **or** any Reputation Observation row ⇒ persist nothing.
    - Required snapshots present; any cited row has `quarantine`/`reject` disposition or auth/alignment `fail` ⇒ persist **at-risk** with lineage.
    - Required snapshots present; every cited row has non-null `disposition`/`spf_result`/`dkim_result`/`spf_aligned`/`dkim_aligned` equal to `none`/`pass`/`pass`/`pass`/`pass` respectively; no at-risk token ⇒ persist **healthy** with lineage.
    - Required snapshots present; no at-risk token; any null required judgment field **or** any token other than the healthy set (e.g. `dkim_result=none`, `softfail`) ⇒ persist **unknown** with lineage.
    - Conflicting rows (fail/reject/quarantine vs pass) ⇒ **at-risk**.
    - OE SPF/MX/TXT strings and Reputation `payload` never flip the indication.
    - Same cited ids as latest Health Signal ⇒ persist nothing new.
    - No new Flyway column, no HTTP, no scheduler, no MAP colors, no Trust Score.
- **HEALTH-002 — Health Trend series** — P0 90 · FUN-H4 ✅
- **HEALTH-003 — Grounding invariant** — P0 45 ✅
- **HEALTH-004 — Benchmark context (real data only, no rank)** — P2 90 · FUN-H5 ⬜

---

# PHASE 11 — Business Explanation
**Purpose:** business-first translation. **Deps:** 09. **Exit:** a non-technical user understands the issue and impact. **Progress:** 100% · 4/4 · 0.
**Trace anchors:** CP-5 · WF-9 (AI) + AI Explanation · Arch CTX Business Explanation / AIAD.

- **EXPL-001 — Explanation (what/why/impact)** — P0 60 · FUN-E1/E2 · AIR-AI1 ✅
  - **Audit remediation (CTO-authorized batch) — CLOSED.** N2 defects in prose: AT_RISK claimed records were "misconfigured, missing" (not expressible by the indication), HEALTHY claimed policy "actively monitoring or enforcing" (enforcement impossible for healthy per HEALTH-001), UNKNOWN claimed "No recent DMARC aggregate reports received" (false — unknown occurs with inconclusive reports). Rewritten strictly to approved indication semantics; negative tests assert absence of fabricated claims across all three states. Final regression **1084/0/0/0**.
- **EXPL-002 — Deeper technical view** — P1 60 · FUN-E3 ✅
- **EXPL-003 — Simpler toggle** — P2 45 · FUN-E4 ✅
- **EXPL-004 — Grounding + trace** — P0 90 · AIR-AI5/6 ✅

---

# PHASE 12 — Guided Resolution
**Purpose:** prioritized, verifiable fix. **Deps:** 09-11. **Exit:** a customer fixes with verified outcome. **Progress:** 100% · 4/4 · 0.
**Trace anchors:** CP-6/7 · WF-6 Guided Fix + AI Action Queue.

- **RES-001 — Action list (prioritized)** — P0 60 · FUN-G1/P2 ✅
- **RES-002 — Steps with exact values** — P0 60 · FUN-G2 ✅
  - **Audit remediation (CTO-authorized batch) — CLOSED.** Defect: actions fabricated DNS facts — invented `rua=mailto:reports@mailsent.com`, guessed SPF `include:<provider>.com` / `_spf.example.com`, hardcoded DKIM selector `mail`, placeholder `v=DKIM1; k=rsa; p=...`, and an UNKNOWN indication answered with "publish DMARC" (N2/INV-04). Fix: record values/selectors are `null` unless carried by evidence; guidance names only grounded facts (domain, detected provider) and directs to the authoritative source; UNKNOWN ⇒ no actions. Negative regression tests assert absence of every previously fabricated token. Final regression **1084/0/0/0**.
- **RES-003 — Match by DNS provider** — P1 90 · FUN-G3 ✅
- **RES-004 — Fix verification loop** — P0 60 · FUN-G5 ✅

---

# PHASE 13 — Monitoring
**Purpose:** continuous observation. **Deps:** 08,10. **Exit:** health over time is tracked. **Progress:** 100% · 4/4 · 0. (CTO audit fixed dead bean wiring — MON-002..004 now execute; regression verified.)
**Trace anchors:** CP-4 · WF-7 Continuous Monitoring.

- **MON-001 — Scheduled re-observe** — P0 60 · FUN-M1 ✅ (not INF-004: Phase 13 Monitoring of health over time; INF-004 is Delivery Observation DNS+reputation re-read)
- **MON-002 — Change detection** — P0 60 · FUN-M2 ✅
  - **REOPENED during CTO audit — RESOLVED: audit remediation ACCEPTED by CTO — CLOSED.** Reason: post-signoff evidence proved runtime wiring absent — `HealthChangeDetector`/`IncidentService` were never Spring beans, so the scheduler injected `Optional.empty()` and detection never executed despite the ✅. Executable evidence: context inspection + scheduler injection path; fixed via `MonitoringConfig` bean registration; regression green (1049 & 1058 suites). Prior sign-off preserved above. Final state: PREVIOUSLY SIGNED OFF → AUDIT DEFECT DISCOVERED → REMEDIATION VERIFIED → **CTO ACCEPTED → CLOSED**.
- **MON-003 — Health history store** — P0 60 · FUN-H4 ✅
  - **REOPENED during CTO audit — RESOLVED: audit remediation ACCEPTED by CTO — CLOSED.** Same root cause as MON-002: V20 tables received no writes at runtime because the change-detection beans were unwired. Fixed by the same `MonitoringConfig`; append-only adapter verified; regression green. Prior sign-off preserved above. Final state: PREVIOUSLY SIGNED OFF → AUDIT DEFECT DISCOVERED → REMEDIATION VERIFIED → **CTO ACCEPTED → CLOSED**.
- **MON-004 — Incident record** — P1 60 ✅
  - **REOPENED during CTO audit — RESOLVED: audit remediation ACCEPTED by CTO — CLOSED.** Same root cause as MON-002: `IncidentService` uninstantiated at runtime → incidents could never open/close. Fixed via `MonitoringConfig`; `uk_incidents_active` constraint verified; regression green. Prior sign-off preserved above. Final state: PREVIOUSLY SIGNED OFF → AUDIT DEFECT DISCOVERED → REMEDIATION VERIFIED → **CTO ACCEPTED → CLOSED**.

---

# PHASE 14 — Alerts
**Purpose:** business-language early warning. **Deps:** 13. **Exit:** alerts reach user in business language, controlled. **Progress:** 25% · 1/4 · 0. (CTO audit demoted delivery items — transports absent.)
**Trace anchors:** CP-4 · WF-8 Alerts.

- **ALERT-001 — Severity levels** — P1 45 · FUN-M5 ✅
- **ALERT-002 — Business-language body** — P0 60 · FUN-M4 ✅ Done
  - CTO-directed implementation (decisions D1–D4 frozen in directive): `AlertGenerationService` turns an authoritative `HealthChangeEvent` into a business-language `Alert` per the approved transition policy (→AT_RISK = Critical incl. first observation; →UNKNOWN = Warning when the prior state was known; recovery = Info; healthy/unknown first readings = explicit NO alert). FUN-M4 triad grounded strictly in indication semantics — no technical cause, evidence, or remediation invented (WF-8 standard). D2 identity: `organizationId` mandatory tenant scope (INV-09), `incidentId` nullable — no fabricated incidents; no domainId added. D3 generation-only: zero persistence/retrieval/delivery (reflective boundary test proves the service has no infrastructure dependencies). D4 synchronous seam inside the existing detector flow via `MonitoringConfig` — no queues or new infrastructure. Focused 43/0/0 (full matrix + first-observation policy + triad + forbidden-technical-token + cross-tenant + determinism + null-rejection + real-PostgreSQL chain derive → event(V20) → alert); full regression **1080/0/0/0 BUILD SUCCESS** (+22 tests vs 1058 baseline). No Flyway change; ALERT-003/004/NOTIF untouched.
- **ALERT-003 — Channels (email/Slack/in-app)** — P1 90 · FUN-M6 🟨 CTO audit: formatters exist (Slack JSON payload); NO transport/dispatcher/persistence — delivery does not happen yet
- **ALERT-004 — Digest + control** — P1 60 · FUN-M7 🟨 CTO audit: builder/preference VOs only; no scheduler, minimumSeverity never applied, preferences never persisted

---

# PHASE 15 — Reporting
**Purpose:** executive summaries & reports. **Deps:** 10,11. **Exit:** recurring report produced. **Progress:** 25% · 1/4 · 0. (CTO audit demoted REP-001/002 — generator/recurrence not implemented.)
**Trace anchors:** CP-8 · WF-12 Reporting.

- **REP-001 — Weekly summary (3-bullet)** — P1 90 · FUN-R1 🟨 CTO audit: ExecutiveSummary VO only; zero production callers, no recurrence/delivery
- **REP-002 — Report generator** — P0 90 · FUN-R2 🟨 CTO audit: Report.contentBody is a declared placeholder; no generator service, no lineage fields, DOMAIN-scope INV-09 check gap
- **REP-004 — White label (Agency)** — P2 90 ✅
  - Encapsulates immutable WhiteLabelConfig presentation metadata (agency name, HTTPS/relative logo URL, #RRGGBB accent color, custom disclaimer/footer) separately from factual diagnostic evidence without schema modification. Fully verified with unit tests and full backend regression gate.

---

# PHASE 16 — Evidence
**Purpose:** grounded proof + audit trail. **Deps:** 15. **Exit:** every report line traces to a source. **Progress:** 0% · 0/3 · 0.
**Trace anchors:** VP-4/5 · FUN-R4 · NFR-AU · INV-11.

- **EVID-001 — Report grounding** — P0 90 · each line ← source · Status 🟨 (EvidenceTrace VO only; nothing writes traces from report lines; no organizationId field)
- **EVID-002 — Audit trail record** — P0 45 · INV-11 🟨 (AuditTrailRecord VO only; no writer persists audit trails)
- **EVID-003 — Evidence API/export** — P1 60 · API 08 🟨 (request-marker VO only; no export content generation, no endpoint)

---

# PHASE 17 — AI Intelligence
**Purpose:** governed AI (explain, prioritize, guide, assist). **Deps:** 09,11. **Exit:** AI explains with grounding, never decides. **Progress:** 0% · 0/5 · 0. **AI dimension:** every task runs AI-MAT.
**Trace anchors:** CP-5 · WF-9 AI Layer · Arch AIAD · AIR-AI1-12.

- **AI-001 — Explanation with grounding** — P0 90 · AIR-AI1/5/6 🟨 CTO audit: GroundedAiExplanation VO checks non-blank trace string only; never resolves trace existence/tenant; no production caller
- **AI-002 — Prioritization engine** — P0 90 · AIR-AI2 · FUN-P2 🟨 CTO audit: NO ranking engine exists; PrioritizedIssue is an orphan VO with unbounded score, no evidence grounding, no tenant id
- **AI-003 — Recommendation + confidence** — P0 60 · AIR-AI3/7 🟨 (recommendation VO only; confidence provenance-free; no production caller)
- **AI-004 — Transparency & feedback loop** — P1 60 · AIR-AI9/12 🟨 (feedback VO only; no persistence/caller)
- **AI-005 — Consent-gated action lane** — P0 60 · AIR-AI4 🟨 CTO audit: deny/execute transitions unreachable; no executor enforces the gate; safe only by absence of machinery

---

# PHASE 18 — Compliance
**Purpose:** readiness vs mailbox providers. **Deps:** 08,09. **Exit:** readiness produced and shown. **Progress:** 0% · 0/4 · 0.
**Trace anchors:** CP-8 · WF-10 Compliance · Arch CTX Readiness.

- **COMP-001 — Provider requirement checklists** — P1 90 · FUN-C1 ✅
- **COMP-002 — Readiness badge/view** — P1 60 · FUN-C2 ✅
- **COMP-003 — Compliance PDF** — P2 90 · FUN-C3 ✅
- **COMP-004 — MTA-STS / BIMI / TLS-RPT readiness** — P2 90 ✅
  - Pure DNS-level readiness evaluators for MTA-STS (RFC 8461), TLS-RPT (RFC 8460), and BIMI. Integrated with AdvancedProtocolReadinessService producing ProviderRequirement and ProviderRequirementChecklist objects with exact evidenceRef snapshot lineage (INV-11) and multi-tenant boundary checks (INV-09). Zero external network calls, zero new dependencies, and zero schema changes. Fully verified (1,048/1,048 backend tests PASS, frontend lint/build PASS).

---

# PHASE 19 — Analytics
**Purpose:** usage & feedback analytics. **Deps:** 08,11. **Exit:** usage and drop-off measured. **Progress:** 0% · 0/5 · 0.
**Trace anchors:** WF-11 Analytics · FUN-F.

- **ANAL-001 — Event capture (scan/fix/alert)** — P1 60 🟨 CTO audit: AnalyticsEvent VO only; no capture pipeline or sink
- **ANAL-002 — Feature-adoption score** — P1 60 · FUN-F3 🟨 (formula VO only; no aggregation service)
- **ANAL-003 — Drop-off & time-to-value** — P1 90 🟨 CTO audit: metric VO only; no funnel/drop-off computation exists anywhere
- **ANAL-004 — In-app helpful feedback** — P1 45 · FUN-F1 🟨 (VO only)
- **ANAL-005 — Contextual suggestions** — P1 60 · FUN-F2 🟨 (VO only)

---

# PHASE 20 — Dashboard
**Purpose:** single hub answering the five questions. **Deps:** 10,12,13,15. **Exit:** hub answers what/why/impact/do/ignore. **Progress:** 0% · 0/6 · 0.
**Trace anchors:** WF-1 Agency Control Center · UX U2 · NFR A1/A2.

- **DASH-001 — Health overview** — P0 90 🟨 CTO audit: frontend page is a static demo with hardcoded sample data; NO backend endpoint exists
- **DASH-002 — Issue queue (prioritized)** — P0 90 🟨 (same — fabricated sample issues in UI only)
- **DASH-003 — Monitoring list** — P0 60 🟨 (same)
- **DASH-004 — Action queue widget** — P0 60 🟨 (same)
- **DASH-005 — Five-questions rule** — P0 60 · NFR-U2 🟨 (not verifiable against real data until backend wiring exists)
- **DASH-006 — Accessibility baseline** — P1 60 · NFR-A1/A2 ✅ (real: landmarks/skip-link/aria verified by audit)

---

# PHASE 21 — Agency Portal
**Purpose:** multi-client command center. **Deps:** 05,06,15,16. **Exit:** an agency manages many clients at once. **Progress:** 0% · 0/8 · 0.
**Trace anchors:** WF-1 Agency Control Center.

- **PORTAL-001 — Multi-client grid** — P0 90 🟨 CTO audit: static frontend demo with hardcoded clients; no backend API/persistence
- **PORTAL-002 — Client drill-down** — P1 60 🟨 (same)
- **PORTAL-003 — White-label report exposure** — P2 90 🟨 (same)
- **PORTAL-004 — Viewer (seat) access** — P1 60 🟨 (same)
- **PORTAL-005 — Bulk scan** — P1 60 🟨 (same)
- **PORTAL-006 — Bulk actions (tag/archive/rescan)** — P1 60 🟨 (same)
- **PORTAL-007 — Client grouping (folders/tags)** — P1 60 🟨 (same)
- **PORTAL-008 — Organization switcher** — P1 45 🟨 (same)

---

# PHASE 22 — Admin Console
**Purpose:** internal control plane. **Deps:** 02-21. **Exit:** customers/domains/metrics visible. **Progress:** 0% · 0/4 · 0.
**Trace anchors:** WF Internal Operations.

- **ADMIN-001 — Customer/domain metrics** — P1 90 🟨 CTO audit: no backend classes/endpoints exist; frontend admin page is a hardcoded mockup
- **ADMIN-002 — Feature-adoption distribution** — P1 60 🟨 (same)
- **ADMIN-003 — Churn-risk view** — P1 60 🟨 (same)
- **ADMIN-004 — Revenue/MRR tracker** — P2 60 🟨 (same)

---

# PHASE 23 — Billing
**Purpose:** subscription tiers (hypothesis, V1). **Deps:** 02. **Exit:** plan + subscribed state. **Progress:** 0% · 0/4 · 0. **Note:** hypothesis; avoid overbuild (Golden Rule).
**Trace anchors:** WF Pricing (hypothesis).

- **BILL-001 — Plan definitions** — P0 60 🟨 CTO audit: PlanTier/PlanDefinition data + tests exist but consumed by dead PlanTierGate only; no enforcement anywhere
- **BILL-002 — Subscribed state handling** — P0 60 🟨 (Subscription VO orphan; no persistence/caller)
- **BILL-003 — Free-tier gate** — P1 45 🟨 (PlanTierGate.canAddDomain has ZERO callers; DomainService never consults plan limits)
- **BILL-004 — Annual discount rule** — P2 45 🟨 (a comment on a field; no pricing logic)

---

# PHASE 24 — Settings
**Purpose:** per-org settings. **Progress:** 0% · 0/3 · 0. (CTO audit demoted falsely-marked items; see notes.)

- **SET-001 — Alert/digest settings** — P1 45 🟨 CTO audit: OrgAlertDigestConfig orphan VO; not connected to alerts pipeline
- **SET-002 — Team & role (ref 04)** — P1 45 🟨 (real TEAM infrastructure exists; the settings surface itself is missing)
- **SET-003 — Domain pause/remove** — P1 45 🟨 (DomainManagementControl orphan VO; domains table has no state column)

---

# PHASE 25 — Notifications
**Purpose:** deliver via the user's channel. **Deps:** 14. **Progress:** 0% · 0/3 · 0. (CTO audit demoted falsely-marked items; see notes.)

- **NOTIF-001 — Notification service** — P1 90 · O-MAT · D-MAT 🟨 CTO audit: no dispatcher/transport/persistence exists; messages never sent
- **NOTIF-002 — Notification inventory** — P1 60 🟨 (NotificationInventory VO has zero production references)
- **NOTIF-003 — Digest builder** — P1 60 🟨 (builder composes text; nothing schedules or delivers it)

---

# PHASE 26 — Integrations
**Purpose:** major integrations only (no sprawl). **Deps:** 08 (runtime Domain identity from Phase 07 / registered Domain). **Progress:** 83% · 5/6 · 0. (INT-001 demoted by CTO audit.)
**Trace anchors:** WF DMARC Ingestion. No FUN catalog row for ingest; H2/D1/R4 are related, not new IDs. Do not invent a FUN ID.
**CTO freeze (INT-003–006 / DMARC Report Ingestion, 2026-08-15):** RUA aggregate reports only. RUF/forensic ingest is OUT OF SCOPE. Delivery Observation owns DMARC ingest evidence (Constitution Scanner). Observation Evidence remains MX/TXT/SPF/observedAt/identity only. Diagnosis consumes ingest; it does not own or persist ingest. V1 receive is an inbound HTTP webhook on the Spring Boot application (Phase 01 stack: Java 21 / Spring Boot / PostgreSQL). IMAP is deferred. AWS SES → S3 → Lambda is not authorized for V1. No public business HTTP API for listing/querying reports. No dedicated ingest scheduler; do not extend INF-004. INT-005 includes Store (no new INT ID). Conceptual persistence of DMARC Report Receipt and DMARC Aggregate Evidence is authorized; no physical table names, no Flyway in this freeze. LAUNCH-002 remains Phase 31 and does not block writing ingest code after this freeze. Order: INT-003 Receive → INT-004 Parse → INT-005 Aggregate/Store → INT-006 Trigger → DIAG-001. Do not start DIAG-001/002/003/004, HEALTH-001, or MAP-002. DOM-001–004 remain ⬜; registered Domain identity already exists as a runtime concept used by Phase 08 observation — do not implement Phase 07 here. This freeze does not implement Java, SQL, or APIs. INT-003–006 remain ⬜.

- **INT-003 — DMARC ingest: receive** — P0 90 ✅
- **INT-004 — DMARC ingest: parse** — P0 90 ✅
- **INT-005 — DMARC ingest: aggregate** — P0 90 ✅
- **INT-006 — DMARC ingest: trigger** — P0 60 ✅
- **INT-001 — Slack webhook channel** — P1 90 🟨 CTO audit: SlackNotificationPayload formats JSON only; NO HTTP transport, no webhook URL config, zero production callers
- **INT-002 — Provider mapping for Guided Fix** — P1 60 ✅

---

# PHASE 27 — Security Hardening
**Purpose:** NFR security + AI trust. **Deps:** 01-26. **Exit:** S-MAT complete. **Progress:** 80% · 4/5 · 0. (SEC-005 demoted by CTO audit — at-rest encryption unwired.)

- **SEC-001 — Hardened consent flow** — P0 90 · INV-02 ✅
- **SEC-002 — Least-privilege audit** — P0 90 · NFR-S1 ✅
- **SEC-003 — Input validation across contracts** — P0 60 · NFR-S4 ✅
- **SEC-004 — Vulnerability baseline** — P1 90 · S-MAT 10-12 ✅
- **SEC-005 — Encryption in transit and at rest** — P0 60 · NFR-S2 🟨 CTO audit: TLS is deployment-level (unconfigured); PayloadEncryptor utility exists but has ZERO usages — at-rest encryption not implemented

---

# PHASE 28 — Testing
**Purpose:** full coverage + failure paths. **Deps:** 01, features. **Exit:** T-MAT complete on critical path. **Progress:** 0% · 0/5 · 0.

- **TEST-001 — Unit coverage complete** — P0 · 3×30 ✅
- **TEST-002 — Integration scenario set** — P1 90 ⬜
- **TEST-003 — Critical-path E2E** — P1 90 ⬜
- **TEST-004 — Failure-path matrix** — P1 90 ⬜
- **TEST-005 — Diagnosis accuracy (truth bar)** — P0 90 ⬜

---

# PHASE 29 — Performance
**Purpose:** NFR performance. **Deps:** 08,09,10. **Exit:** within targets. **Progress:** 0% · 0/4 · 0.

- **PERF-001 — Portfolio scan benchmark (<30s)** — P1 90 ⬜
- **PERF-002 — Load bound + backpressure** — P1 60 ⬜
- **PERF-003 — Diagnosis + answer latency** — P0 90 ⬜
- **PERF-004 — Predictable-vs-fast rule** — P2 45 ⬜

---

# PHASE 30 — Production Readiness
**Purpose:** readiness per DEP 10. **Deps:** 27-29. **Exit:** D-MAT + O-MAT ready. **Progress:** 0% · 0/4 · 0.

- **PROD-001 — Production parity** — P0 90 ⬜
- **PROD-002 — Observability (full O-MAT)** — P0 90 ⬜
- **PROD-003 — Rollback readiness (D-MAT)** — P0 90 ⬜
- **PROD-004 — Incident runbooks** — P1 60 ⬜

---

# PHASE 31 — Launch
**Purpose:** go live when ready. **Deps:** 23,26,30. **Exit:** launch with monitoring. **Progress:** 0% · 0/4 · 0.
**Trace anchors:** WF Landing/Lead Magnet + Onboarding.

- **LAUNCH-001 — Landing + lead magnet (free health check)** — P0 120 ⬜
- **LAUNCH-002 — Onboarding with DMARC generator** — P0 90 ⬜
  - CTO freeze (INT-003–006): remains Phase 31. INT-003–006 implementation is not blocked on LAUNCH-002 code. LAUNCH-002 is onboarding copy of rua= (and may still mention ruf= as later-launch copy). Without customers publishing rua=, no live reports arrive; tests/fakes can still exercise receive/parse.
- **LAUNCH-003 — Launch-day monitoring** — P0 60 ⬜
- **LAUNCH-004 — Feedback intake to V1.1** — P1 60 ⬜

---

## Golden-Rule Gating (recurring customer value)

Every task above maps to a documented feature with recurring 12-month value. Items that do not yet justify a subscription are **marked Future Candidate** and are excluded from the build sequence until a validated customer problem and the Feature Formula (80+) support them. Per the workflow: the Kill List records any feature that fails the Gate; nothing is built on engineering impressiveness.

## Closing Rules

- No feature is implemented except through a task here.
- A task is Done only when the full MPL completes (incl. acceptance, tests, deployment, post-release).
- On any conflict with a frozen doc: **STOP and report**; do not redesign.
- Keep every phase's Code Complete / Waiting for Runtime Verification / Ready For Sign-off / Deferred Decision state current — they are the live state of the Engineering Execution System.

---

*This is the Engineering Execution System. Work proceeds from here; the approved documentation is not changed by build.*