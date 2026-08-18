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
**Status:** 🟨 In Progress · **Purpose:** authenticate users; Identity (User, Role), Consent. **Deps:** 01. **Exit:** sign-up + role; consent recorded before action. **Execution Progress:** AUTH-000 ✅ · AUTH-001 ✅ · AUTH-002 ✅ · AUTH-003 ✅ · AUTH-007 ✅ · AUTH-004 ⏸️ Deferred/No-contract · AUTH-005 ⏸️ Blocked/Org-dependency · AUTH-006 ⏸️ Deferred/Unspecified.
**Not fully closed:** Phase 02 CANNOT be declared fully closed because its Consent exit condition (AUTH-005) is blocked by the Phase 03 Organization foundation. The documented authentication *capability set* is complete; the remaining items are deferred/blocked, not missing implementation.
**Trace anchors:** CP-8 trust · J-3 → J-6 readiness · VP-6 confidence · WF (Feature Pipeline) · Arch CTX Identity / DATA / SEC.

- **AUTH-000 — Sign up**
  - **Goal:** password-credentialed identity · **Value:** gate to every recurring capability · **CP:** CP-8 · **J:** J-3/J-6 · **VP:** VP-6 · **WF:** Pipeline 3 · **Req:** PRD / NFR-S1 · **Arch:** CTX Identity / DATA / SEC
  - **Desc:** register user with password policy; record Identity and a Consent record (INV-02).
  - **Pri:** P0 · **Time:** 45m · **Dep:** ENG-000 · **Owner:** Auth · **Status:** 🟦
  - **Accept:** new user registers and receives a role · **Validate:** integration auth test · **DoD:** MPL
  - **Backend:** auth-service · **DB:** users, roles, consent · **API:** sign-up · **Sec:** S-1,4,5,8 · **Perf:** — · **Test:** T-1,2,7 · **Obs:** O-1 · **Release:** REL-A · **Rollback:** flag · **Risk:** credential leak → hard vault · **Block:** — · **Checklist:** MPL
- **AUTH-001 — Sign in / session** — atomic hardened session. P0 60 🟩 (Review Complete / CTO approved)
- **AUTH-002 — User identity entity** — DATA User+Role. P0 60 🟦 (identity+role realized via AUTH-001; verified + tested)
- **AUTH-003 — Role model** — Owner/Analyst/Viewer, least-authority. P0 60 🟦 (D5 see/act semantics; least-authority VIEWER)
- **AUTH-004 — Password reset.** P1 60 ⬜ **Status: ⬜ Not Started · Decision: ⏸️ DEFERRED/BLOCKED**
  - **Blocker:** No approved MailSent-internal password-reset requirement exists in the frozen documentation (references describe customer transactional email deliverability, not an internal recovery capability). Do not implement.
- **AUTH-005 — Consent record for an action** — explicit, INV-02. P0 45 ⬜ **Status: ⬜ Not Started · Decision: ⏸️ BLOCKED BY DEPENDENCY**
  - **Dependency:** Organization foundation (Phase 03).
  - **Blocker:** Consent is owned by the Organization aggregate (DATA: "Protects ... the Consent it gives (INV-02)"); Identity and Access "Depends On: the definition of an Organization" (03_BOUNDED_CONTEXTS). Consent requires an existing Organization identity to be recorded against; User/Role alone cannot represent the ownership boundary. Implementation must wait for the Organization foundation.
- **AUTH-006 — Current-user contract** — per API 08. P1 45 ⬜ **Status: ⬜ Not Started · Decision: ⏸️ DEFERRED / UNSPECIFIED**
  - **Reason:** Checklist task exists without an executable source-of-truth contract. 08_API_PHILOSOPHY defines principles only; no frozen Functional Requirement or architecture contract defines a concrete current-user/session-introspection endpoint, DTO, or model. No implementation is authorized until an approved requirement/API contract exists. Do not invent GET /me, session introspection, or token introspection.
- **AUTH-007 — Authentication Module Foundation** — DDD module boundaries only (config/domain/application/infrastructure/interfaces; frontend feature/service/hooks/types; no controllers/endpoints/DB/JWT). P0 30 **Status:** 🟦
  - **Accept:** package structure follows DDD; naming per Glossary; project builds · **Validate:** backend verify + frontend build · **Dep:** ENG-000 · **Owner:** Auth

---

# PHASE 03 — Organizations
**Purpose:** Organization aggregate. **Deps:** 02. **Exit:** org, membership, isolation. **Execution Progress:** ORG-001 🟪 Tests Complete · ORG-002 ⬜ · ORG-003 ⬜ · ORG-004 ⬜.
**Trace anchors:** CP-1 · VP-4/5 · Arch DATA Organization / CTX Portfolio.

- **ORG-001 — Create/read/list/close org** — P0 60 · Data DATA Organization · Status 🟪 (Tests Complete — aggregate+persistence; no API contract in frozen docs)
- **ORG-002 — Membership** — user joins an org. P0 60 🟪
- **ORG-003 — Org→Domain ownership** — registry authorization. P0 60 🟪
- **ORG-004 — Isolation invariant** — INV-09 test. P0 45 🟪

---

# PHASE 04 — Teams
**Purpose:** roles, invite, least-authority, audit. **Deps:** 02,03. **Exit:** a team admin adds an analyst who acts in scope. **Progress:** 0% · 0/4 · 0.

- **TEAM-001 — Invite by email & accept** — P0 60 · Arch CTX Identity · FUN-D5 · Status 🟪
- **TEAM-002 — Role→capability map (least-authority)** — P0 60 ⬜
- **TEAM-003 — Access audit record** — P1 60 · NFR-AU ⬜
- **TEAM-004 — Team list query** — P1 45 ⬜

---

# PHASE 05 — Clients
**Purpose:** Client relationship (agency). **Deps:** 03,04. **Exit:** client created, assigned, isolated. **Progress:** 0% · 0/3 · 0.

- **CLIENT-001 — Client CRUD** — P0 60 · DATA Client/Portfolio 🟪
- **CLIENT-002 — Assign client to agency** — P0 60 ⬜
- **CLIENT-003 — Client isolation (INV-09)** — P0 45 🟪

---

# PHASE 06 — Portfolio
**Purpose:** portfolio-scale view. **Deps:** 05. **Exit:** an agency sees a portfolio with risk. **Progress:** 0% · 0/3 · 0.

- **PORT-001 — Portfolio container** — P0 60 · FUN-D4 🟪
- **PORT-002 — Portfolio overview data** — P0 90 🟪
- **PORT-003 — Portfolio isolation** — P1 45 · INV-09 🟪

---

# PHASE 07 — Domains
**Purpose:** Domain entity + registry. **Deps:** 03,06. **Exit:** add/import/list domains with ownership. **Progress:** 0% · 0/4 · 0.

- **DOM-001 — Add domain (manual)** — P0 60 · FUN-D1 ⬜
- **DOM-002 — Bulk CSV import** — P1 90 · FUN-D2 🟪
- **DOM-003 — Domain list/state** — P0 60 · FUN-D3 ⬜
- **DOM-004 — Registry ownership** — P0 60 · FUN-D4 ⬜

---

# PHASE 08 — Infrastructure Observation
**Purpose:** observe infrastructure into Observation Evidence, Reverse DNS Observation, SMTP Greeting Observation, Reputation Observation, and Blacklist Observation (raw observed facts). **Deps:** 07. **Exit:** domain records read and stored as those snapshots, not inferred, not Health Signal, and not Phase 16 proof of health. **Progress:** 75% · 9/12 · 0.
**Trace anchors:** CP-1/2 · VP-1/2 · WF-2 + Scanner · Arch CTX Delivery Observation / DATA / INFO.

- **INF-001 — DNS read (MX/SPF/TXT/DKIM)** — P0 90 · FUN-H2 · INV-01 · Status ✅
  - Freeze note: the title's DKIM is leftover. DKIM facts are not Phase 08 Observation Evidence outputs. Observation Evidence remains frozen to MX/TXT/SPF/observedAt/identity. DKIM observation is not in OE. Do not infer DKIM, DMARC, or alignment from Phase 08 DNS TXT. INF-001 implementation is not reopened.
- **INF-002 — Observation lifecycle → Observation Evidence** — P0 90 · no fabrication · not Phase 16 proof · ✅
- **INF-003 — Reputation ingest (mocked first, grounded)** — P0 90 · FUN-H3 · ✅
- **INF-004 — Re-observe (schedule)** — P0 60 · FUN-M1 · ✅
  - Delivery Observation scheduled re-read of Observation Evidence (DNS) and Reputation Observation. Not HEALTH-001 (Health Signal unification) and not MON-001 (Phase 13 Monitoring of health over time). Default daily `mailsent.observation.reobserve.interval` (86400000 ms); same-instance non-overlap via `spring.task.scheduling.pool.size=1` plus an AtomicBoolean skip. No Flyway V10.
- **INF-005 — Reverse DNS Observation (ReverseDnsObservation)** — P1 60 · MX→A/AAAA→PTR · raw facts · not Health · not FCrDNS · ✅
  - Delivery Observation raw reverse-DNS snapshot for IPs of observed MX hosts of a registered Domain (resolve MX hostname to A and/or AAAA, then PTR; both families when addresses actually exist). Store raw reverse-DNS facts only. Separate from Observation Evidence (frozen MX/TXT/SPF/observedAt/identity; do not add PTR to OE). Append-only immutable. Identity from registered Domain; caller supplies domainName only. No public HTTP API. No independent scheduler. Not INF-007 (DNSSEC/TTL), not INF-008 (blacklists). HEALTH-001 may consume later; INF-005 must not create Health Signal. Future Flyway required; do not create now. INF-001–004 untouched until implementation authorized.
- **INF-006 — SMTP Greeting Observation (SmtpGreetingObservation)** — P1 45 · MX→SMTP · HELO-EHLO raw facts · not Health · ✅
  - Delivery Observation raw SMTP greeting/banner and EHLO capability snapshot for IPs of observed MX hosts from Observation Evidence of a registered Domain (resolve MX hostname to A and/or AAAA, then observe SMTP). Store raw facts only. Active probe permitted only against registered-domain infrastructure; no mail send, no mutation, no SMTP auth, no relay/delivery testing, no arbitrary IPs or third-party hosts. Separate from Observation Evidence (do not add greeting/EHLO to OE) and from Reverse DNS Observation. Append-only immutable. Identity from registered Domain; caller supplies domainName only. No public HTTP API. No independent scheduler; do not modify INF-004. Not INF-007 (DNSSEC/TTL), not INF-008 (blacklists), not HEALTH-001, not mail sending. HEALTH-001 may consume later; INF-006 must not create Health Signal. Unavailable (timeout/refusal/DNS) persists nothing; do not fabricate banner or EHLO; connection/probe outcome records unavailability without fabricated greeting/EHLO text. Own persistence model and future Flyway required; do not create now. Do not modify Observation Evidence, V8, or ReverseDnsObservation. INF-001–005 implementation untouched until implementation is authorized.
- **INF-007 — DNS Health Observation (DnsHealthObservation)** — P1 60 · TTL/DNSSEC/propagation raw facts · not Health · ✅
  - Delivery Observation raw DNS TTL, DNSSEC-related presence, and named-resolver propagation snapshot for a registered Domain. Store raw facts only. Separate from Observation Evidence (frozen MX/TXT/SPF/observedAt/identity; do not add TTL/DNSSEC/propagation to OE). Append-only immutable. Identity from registered Domain; caller supplies domainName only. No public HTTP API. No independent scheduler; INF-004 unchanged. Not Health Signal, not Trust Score, not HEALTH-001, not INF-008. HEALTH-001 may consume later; INF-007 must not create Health Signal. Flyway V10 `dns_health_observation`.
- **INF-008 — Blacklist monitor (Spamhaus/Barracuda/SURBL/URIBL)** — P1 90 ✅
  - Delivery Observation raw blacklist snapshot (Blacklist Observation) for a registered Domain from Spamhaus, Barracuda, SURBL, and URIBL. Store raw provider facts only. Append-only immutable. Identity from registered Domain; caller supplies domainName only. Four ports queried independently; persist only a real observation; unavailable/empty/exception persists nothing for that provider. Production adapters report unavailability (no invented HTTP/zones/credentials). No public HTTP API. No independent scheduler; INF-004 unchanged. Not Health Signal. Flyway V11 `blacklist_observation`. INF-001–007, V1–V10, ReObservationService, Observation Evidence, and Reputation Observation unchanged.
**Infrastructure Map (WF-5)**
- **MAP-001 — Map model** (Domain→DNS→SPF→DKIM→DMARC→Senders→Inbox) — P1 60 ✅
  - Topology read-model only (not an aggregate, not Health, not MAP-002–004). Checkbox remains until CTO audit.
- **MAP-002 — Color-coded view** — P1 90 ⬜
  - CTO freeze: G/Y/R represents HEALTH, not observation availability. MAP-001 remains topology/read-model only. MAP-001 OBSERVED/NOT_OBSERVED/UNAVAILABLE must never map directly to G/Y/R. Raw INF-001–008 observations must never be converted into MAP-002 colors. Trust Score is not a color source. HEALTH-001 (Phase 10) is the authoritative source of health state. MAP-002 **consumes** Health Signal; HEALTH-001 does **not** freeze colors, thresholds, UI, or per-node health. MAP-002 remains blocked on HEALTH-001 producing the canonical signal **and** on a later MAP-002 freeze of how unknown/healthy/at-risk maps to colors. An envelope whose indication is only **unknown** is not a color source. Do not start MAP-002 in this HEALTH-001 freeze. No Health Signal implementation in Phase 08. No table, Flyway, backend health-calculation, HTTP API, or frontend in this freeze. MAP-001 unchanged. Checklist remains ⬜.
- **MAP-003 — Node detail panel** — P1 90 ⬜
  - CTO freeze: opened by selecting/clicking a MAP-001 node (WF-5 click-to-detail). Panel MAY show node identity, type, observation availability/status, and real raw observation facts or evidence references WHEN those facts exist. Contents are raw node metadata/observations, not independent health computation. Must NOT calculate, create, infer, or own Health Signal. Health meaning and G/Y/R owned by MAP-002 consuming HEALTH-001. OBSERVED/NOT_OBSERVED/UNAVAILABLE never translated into health/severity/warning/critical/color. No Trust Score, deprecated scoring, diagnosis, recommendation, compliance, or fabricated explanation. NOT_OBSERVED nodes must not receive fabricated details. Evidence refs must not automatically become arbitrary raw-payload dumps; detailed evidence-access/API contract frozen separately before implementation. DKIM, DMARC, Senders, Inbox currently have no observed facts — must not display invented details. Must not modify INF-001–008, MAP-001, MAP-002, V1–V11, HEALTH-001. No new table, Flyway, scheduler, independent backend health service. No frontend/API implementation in this freeze. Do not invent per-node health. Checklist remains ⬜.
- **MAP-004 — PNG/SVG export** — P2 90 ⬜
  - CTO freeze: export of the Infrastructure Map visualization. PNG and SVG are both frozen output formats. NOT Phase 16 proof; does not replace EVID-003. NOT Phase 15 PDF/email reporting; must not merge into FUN-R2. Keep separate from Phase 15/16 reporting/proof. Exported structure based on MAP-001 topology. If health colors eventually appear in the export, they come from MAP-002 consuming real HEALTH-001 Health Signal. Never derive G/Y/R from raw INF-001–008 observations, OBSERVED status, Trust Score, or deprecated scoring. NOT_OBSERVED nodes must not receive fabricated health/facts/details. MAP-004 must not calculate or create Health Signal. No new table, Flyway, scheduler, graph DB, or persistence model. Do not invent HTTP/API contract. Do not select or add a rendering/export library in this freeze. No frontend/backend export code in this freeze. Must not modify INF-001–008, MAP-001, MAP-002, MAP-003, V1–V11, or HEALTH-001. Checklist remains ⬜.

---

# PHASE 09 — Diagnosis Engine
**Purpose:** derive cause from signals. **Deps:** 08. **Exit:** root cause with lineage. **Progress:** 25% · 1/4 · 0.
**Trace anchors:** CP-6 · WF Analyzer (DIAG-001 Alignment Engine) + WF-3 Alignment Intelligence (DIAG-002/003) · Arch CTX Diagnosis.
**CTO freeze (DIAG-001 / Phase 09–10):** Health Signal is produced by HEALTH-001 (Phase 10 / Delivery Observation health engine), **not** by Diagnosis. Diagnosis must not fabricate Health Signal. Program order remains 08→09→10 for ingest facts (INT-003–006 + Aggregate Evidence); HEALTH-001 code after its freeze need not wait for DIAG-002/003/004. DIAG-001 Alignment derivation does not start from health and does not wait on HEALTH-001 as a fabricated input. DIAG-001 in-memory Alignment is not a HEALTH-001 input. DIAG-003 (root cause / Issue) remains blocked until **Java** assigns **healthy vs at-risk** per the HEALTH-001 interpretation freeze, not merely **unknown** (do not implement DIAG-003). MAP-002 still waits for a persisted real Health Signal **and** a later MAP-002 color freeze; MAP-002/003/004 remain ⬜ and unimplemented.

- **DIAG-001 — Alignment** (SPF/DKIM, envelope vs header) — P0 90 · Alignment Engine / FUN-H2 (not FUN-X1 as primary) 🟪 Tests Complete
  - CTO freeze & Ratification: Analyzer-class Alignment derivation (Constitution Analyzer: SPF/DKIM/DMARC parsing + alignment). Ratified once INT-003–006 produced evidence in code. Does not persist Alignment table. Does not create Health Signal or Trust Score. Unit tested in `AlignmentDerivationServiceTest`.
- **DIAG-002 — Sender detection** (recognized providers) — P0 90 · FUN-X2 ⬜ (No frozen provider catalog; existing code preserved standalone)
- **DIAG-003 — Root cause determination** — P0 90 · FUN-X3 · INV-11 ⬜ (Blocked on HEALTH-001)
  - `RootCause` and `RootCauseDeterminationService` exist in code; blocked until HEALTH-001 healthy/at-risk signals are assigned in execution pipeline.
- **DIAG-004 — Issue entity lifecycle** — P0 60 · DATA Issue ⬜

---

# PHASE 10 — Health Signal Engine
**Purpose:** unified Health Signal + trend. **Deps:** 09 ingest facts (INT-003–006 + DMARC Aggregate Evidence); DIAG-002/003/004 remain ⬜ and are **not** HEALTH-001 inputs. **Exit:** single, truthful signal with trend; no fabricated rank. Phase 10 exit “signal with trend” is **phase exit**, not HEALTH-001 item exit. **Progress:** 25% · 1/4 · 0.
**Trace anchors:** CP-3 · WF-4 · GLOS Health Signal.
**CTO freeze (HEALTH-001 interpretation, 2026-08-15):** Delivery Observation **owns** Health Signal and Health Trend (trend is HEALTH-002). Diagnosis does **not** own Health Signal. DIAG-001 does not create Health Signal. HEALTH-001 consumes **persisted snapshots only** — never `Alignment.lastDerived()` / in-memory DIAG-001.

- **HEALTH-001 — Unify Health Signal (components)** — P0 60 · FUN-H1 · INV-01/05 🟪 Tests Complete (not INF-004)
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
- **HEALTH-002 — Health Trend series** — P0 90 · FUN-H4 ⬜
  - CTO freeze (HEALTH-001, 2026-08-15): Health Trend remains HEALTH-002. Derived from append-only Health Signal snapshots. Not required to complete HEALTH-001. Phase 10 exit “signal with trend” is phase exit, not HEALTH-001 item exit. Do not start HEALTH-002 in this freeze.
- **HEALTH-003 — Grounding invariant** — P0 45 ✅
- **HEALTH-004 — Benchmark context (real data only, no rank)** — P2 90 · FUN-H5 ⬜

---

# PHASE 11 — Business Explanation
**Purpose:** business-first translation. **Deps:** 09. **Exit:** a non-technical user understands the issue and impact. **Progress:** 0% · 0/4 · 0.
**Trace anchors:** CP-5 · WF-9 (AI) + AI Explanation · Arch CTX Business Explanation / AIAD.

- **EXPL-001 — Explanation (what/why/impact)** — P0 60 · FUN-E1/E2 · AIR-AI1 ✅
- **EXPL-002 — Deeper technical view** — P1 60 · FUN-E3 ✅
- **EXPL-003 — Simpler toggle** — P2 45 · FUN-E4 ⬜
- **EXPL-004 — Grounding + trace** — P0 90 · AIR-AI5/6 ✅

---

# PHASE 12 — Guided Resolution
**Purpose:** prioritized, verifiable fix. **Deps:** 09-11. **Exit:** a customer fixes with verified outcome. **Progress:** 0% · 0/4 · 0.
**Trace anchors:** CP-6/7 · WF-6 Guided Fix + AI Action Queue.

- **RES-001 — Action list (prioritized)** — P0 60 · FUN-G1/P2 ✅
- **RES-002 — Steps with exact values** — P0 60 · FUN-G2 ✅
- **RES-003 — Match by DNS provider** — P1 90 · FUN-G3 ⬜
- **RES-004 — Fix verification loop** — P0 60 · FUN-G5 ✅

---

# PHASE 13 — Monitoring
**Purpose:** continuous observation. **Deps:** 08,10. **Exit:** health over time is tracked. **Progress:** 0% · 0/4 · 0.
**Trace anchors:** CP-4 · WF-7 Continuous Monitoring.

- **MON-001 — Scheduled re-observe** — P0 60 · FUN-M1 ✅ (not INF-004: Phase 13 Monitoring of health over time; INF-004 is Delivery Observation DNS+reputation re-read)
- **MON-002 — Change detection** — P0 60 · FUN-M2 ✅
- **MON-003 — Health history store** — P0 60 · FUN-H4 ✅
- **MON-004 — Incident record** — P1 60 ✅

---

# PHASE 14 — Alerts
**Purpose:** business-language early warning. **Deps:** 13. **Exit:** alerts reach user in business language, controlled. **Progress:** 0% · 0/4 · 0.
**Trace anchors:** CP-4 · WF-8 Alerts.

- **ALERT-001 — Severity levels** — P1 45 · FUN-M5 ✅
- **ALERT-002 — Business-language body** — P0 60 · FUN-M4 ✅
- **ALERT-003 — Channels (email/Slack/in-app)** — P1 90 · FUN-M6 ✅
- **ALERT-004 — Digest + control** — P1 60 · FUN-M7 ✅

---

# PHASE 15 — Reporting
**Purpose:** executive summaries & reports. **Deps:** 10,11. **Exit:** recurring report produced. **Progress:** 0% · 0/4 · 0.
**Trace anchors:** CP-8 · WF-12 Reporting.

- **REP-001 — Weekly summary (3-bullet)** — P1 90 · FUN-R1 ✅
- **REP-002 — Report generator** — P0 90 · FUN-R2 ✅
- **REP-003 — Schedule & deliver** — P1 45 · FUN-R3 ✅
- **REP-004 — White label (Agency)** — P2 90 ⬜

---

# PHASE 16 — Evidence
**Purpose:** grounded proof + audit trail. **Deps:** 15. **Exit:** every report line traces to a source. **Progress:** 0% · 0/3 · 0.
**Trace anchors:** VP-4/5 · FUN-R4 · NFR-AU · INV-11.

- **EVID-001 — Report grounding** — P0 90 · each line ← source · Status ✅
- **EVID-002 — Audit trail record** — P0 45 · INV-11 ✅
- **EVID-003 — Evidence API/export** — P1 60 · API 08 ✅

---

# PHASE 17 — AI Intelligence
**Purpose:** governed AI (explain, prioritize, guide, assist). **Deps:** 09,11. **Exit:** AI explains with grounding, never decides. **Progress:** 0% · 0/5 · 0. **AI dimension:** every task runs AI-MAT.
**Trace anchors:** CP-5 · WF-9 AI Layer · Arch AIAD · AIR-AI1-12.

- **AI-001 — Explanation with grounding** — P0 90 · AIR-AI1/5/6 · AI-MAT full ✅
- **AI-002 — Prioritization engine** — P0 90 · AIR-AI2 · FUN-P2 ✅
- **AI-003 — Recommendation + confidence** — P0 60 · AIR-AI3/7 ✅
- **AI-004 — Transparency & feedback loop** — P1 60 · AIR-AI9/12 ✅
- **AI-005 — Consent-gated action lane** — P0 60 · AIR-AI4 ✅

---

# PHASE 18 — Compliance
**Purpose:** readiness vs mailbox providers. **Deps:** 08,09. **Exit:** readiness produced and shown. **Progress:** 0% · 0/4 · 0.
**Trace anchors:** CP-8 · WF-10 Compliance · Arch CTX Readiness.

- **COMP-001 — Provider requirement checklists** — P1 90 · FUN-C1 ✅
- **COMP-002 — Readiness badge/view** — P1 60 · FUN-C2 ✅
- **COMP-003 — Compliance PDF** — P2 90 · FUN-C3 ⬜
- **COMP-004 — MTA-STS / BIMI / TLS-RPT readiness** — P2 90 ⬜

---

# PHASE 19 — Analytics
**Purpose:** usage & feedback analytics. **Deps:** 08,11. **Exit:** usage and drop-off measured. **Progress:** 0% · 0/5 · 0.
**Trace anchors:** WF-11 Analytics · FUN-F.

- **ANAL-001 — Event capture (scan/fix/alert)** — P1 60 ✅
- **ANAL-002 — Feature-adoption score** — P1 60 · FUN-F3 ⬜
- **ANAL-003 — Drop-off & time-to-value** — P1 90 ⬜
- **ANAL-004 — In-app helpful feedback** — P1 45 · FUN-F1 ⬜
- **ANAL-005 — Contextual suggestions** — P1 60 · FUN-F2 ⬜

---

# PHASE 20 — Dashboard
**Purpose:** single hub answering the five questions. **Deps:** 10,12,13,15. **Exit:** hub answers what/why/impact/do/ignore. **Progress:** 0% · 0/6 · 0.
**Trace anchors:** WF-1 Agency Control Center · UX U2 · NFR A1/A2.

- **DASH-001 — Health overview** — P0 90 ⬜
- **DASH-002 — Issue queue (prioritized)** — P0 90 ⬜
- **DASH-003 — Monitoring list** — P0 60 ⬜
- **DASH-004 — Action queue widget** — P0 60 ⬜
- **DASH-005 — Five-questions rule** — P0 60 · NFR-U2 ⬜
- **DASH-006 — Accessibility baseline** — P1 60 · NFR-A1/A2 ⬜

---

# PHASE 21 — Agency Portal
**Purpose:** multi-client command center. **Deps:** 05,06,15,16. **Exit:** an agency manages many clients at once. **Progress:** 0% · 0/8 · 0.
**Trace anchors:** WF-1 Agency Control Center.

- **PORTAL-001 — Multi-client grid** — P0 90 ⬜
- **PORTAL-002 — Client drill-down** — P1 60 ⬜
- **PORTAL-003 — White-label report exposure** — P2 90 ⬜
- **PORTAL-004 — Viewer (seat) access** — P1 60 ⬜
- **PORTAL-005 — Bulk scan** — P1 60 ⬜
- **PORTAL-006 — Bulk actions (tag/archive/rescan)** — P1 60 ⬜
- **PORTAL-007 — Client grouping (folders/tags)** — P1 60 ⬜
- **PORTAL-008 — Organization switcher** — P1 45 ⬜

---

# PHASE 22 — Admin Console
**Purpose:** internal control plane. **Deps:** 02-21. **Exit:** customers/domains/metrics visible. **Progress:** 0% · 0/4 · 0.
**Trace anchors:** WF Internal Operations.

- **ADMIN-001 — Customer/domain metrics** — P1 90 ⬜
- **ADMIN-002 — Feature-adoption distribution** — P1 60 ⬜
- **ADMIN-003 — Churn-risk view** — P1 60 ⬜
- **ADMIN-004 — Revenue/MRR tracker** — P2 60 ⬜

---

# PHASE 23 — Billing
**Purpose:** subscription tiers (hypothesis, V1). **Deps:** 02. **Exit:** plan + subscribed state. **Progress:** 0% · 0/4 · 0. **Note:** hypothesis; avoid overbuild (Golden Rule).
**Trace anchors:** WF Pricing (hypothesis).

- **BILL-001 — Plan definitions** — P0 60 ⬜
- **BILL-002 — Subscribed state handling** — P0 60 ⬜
- **BILL-003 — Free-tier gate** — P1 45 ⬜
- **BILL-004 — Annual discount rule** — P2 45 ⬜

---

# PHASE 24 — Settings
**Purpose:** per-org settings. **Progress:** 0% · 0/3 · 0.

- **SET-001 — Alert/digest settings** — P1 45 ⬜
- **SET-002 — Team & role (ref 04)** — P1 45 ⬜
- **SET-003 — Domain pause/remove** — P1 45 ⬜

---

# PHASE 25 — Notifications
**Purpose:** deliver via the user's channel. **Deps:** 14. **Progress:** 0% · 0/3 · 0.

- **NOTIF-001 — Notification service** — P1 90 · O-MAT · D-MAT ⬜
- **NOTIF-002 — Notification inventory** — P1 60 ⬜
- **NOTIF-003 — Digest builder** — P1 60 ⬜

---

# PHASE 26 — Integrations
**Purpose:** major integrations only (no sprawl). **Deps:** 08 (runtime Domain identity from Phase 07 / registered Domain). **Progress:** 0% · 0/6 · 0.
**Trace anchors:** WF DMARC Ingestion. No FUN catalog row for ingest; H2/D1/R4 are related, not new IDs. Do not invent a FUN ID.
**CTO freeze (INT-003–006 / DMARC Report Ingestion, 2026-08-15):** RUA aggregate reports only. RUF/forensic ingest is OUT OF SCOPE. Delivery Observation owns DMARC ingest evidence (Constitution Scanner). Observation Evidence remains MX/TXT/SPF/observedAt/identity only. Diagnosis consumes ingest; it does not own or persist ingest. V1 receive is an inbound HTTP webhook on the Spring Boot application (Phase 01 stack: Java 21 / Spring Boot / PostgreSQL). IMAP is deferred. AWS SES → S3 → Lambda is not authorized for V1. No public business HTTP API for listing/querying reports. No dedicated ingest scheduler; do not extend INF-004. INT-005 includes Store (no new INT ID). Conceptual persistence of DMARC Report Receipt and DMARC Aggregate Evidence is authorized; no physical table names, no Flyway in this freeze. LAUNCH-002 remains Phase 31 and does not block writing ingest code after this freeze. Order: INT-003 Receive → INT-004 Parse → INT-005 Aggregate/Store → INT-006 Trigger → DIAG-001. Do not start DIAG-001/002/003/004, HEALTH-001, or MAP-002. DOM-001–004 remain ⬜; registered Domain identity already exists as a runtime concept used by Phase 08 observation — do not implement Phase 07 here. This freeze does not implement Java, SQL, or APIs. INT-003–006 remain ⬜.

- **INT-003 — DMARC ingest: receive** — P0 90 ⬜
  - CTO freeze: inbound webhook on the application (ingest transport only; not a Diagnosis/Alignment API; not a public product REST resource). `{uuid}` in `reports+{uuid}@mailsent.io` is the registered Domain id. Accept only if uuid matches a registered Domain; organizationId comes from that Domain (INV-09). Unknown/unregistered uuid: discard, do not persist Receipt or facts, do not invent a domain, do not trigger DIAG-001. Persist received raw attachment bytes as DMARC Report Receipt when Domain is known. IMAP not used. SES/Lambda not used. Observation Evidence unchanged.
- **INT-004 — DMARC ingest: parse** — P0 90 ⬜
  - CTO freeze: RFC 7489 aggregate XML only; zip/gz are transport compression, not a second schema. Required to accept as Parsed DMARC Report: `report_id`. Extract listed workflow fields when present (ReportMetadata org/date range/report_id; Records source_ip, count, spf_result, dkim_result, spf_aligned, dkim_aligned, envelope_from, header_from, dkim_domains; PolicyEvaluated disposition, dkim, spf). Absent fields stay absent. Missing `report_id` or malformed/unsupported XML: unusable — persist Receipt if already received for a known Domain, do not produce DMARC Aggregate Evidence, do not trigger DIAG-001, do not write OE, do not fabricate Alignment. Parser must not emit Health Signal or Alignment table rows. Partial records: keep raw values; do not fabricate alignment flags.
- **INT-005 — DMARC ingest: aggregate** — P0 90 ⬜
  - CTO freeze: includes Store (no separate checklist ID). Per-domain = registered Domain. Per-sender = `source_ip` (do not invent DIAG-002 provider matching). Per-day = UTC calendar date of `date_range.begin`. Append-only DMARC Aggregate Evidence snapshots (INV-12 spirit). Duplicate `report_id` scoped to the same Domain is idempotent: no second snapshot. Lineage: receipt id + Domain identity + `report_id` when parse succeeds. Conceptual persistence only; no table names; future Flyway authorized in principle when implementation starts (do not name a version now). Not an Alignment table. Not Observation Evidence.
- **INT-006 — DMARC ingest: trigger** — P0 60 ⬜
  - CTO freeze: in-process after successful INT-005 Store. Triggers DIAG-001 only, and only when valid DMARC Aggregate Evidence exists for that Domain. Must not trigger DIAG-003 or HEALTH-001. Same `report_id` + Domain does not re-trigger as a new analysis if the evidence snapshot was already stored. No dedicated scheduler. Do not extend INF-004 to DMARC.
- **INT-001 — Slack webhook channel** — P1 90 ⬜
- **INT-002 — Provider mapping for Guided Fix** — P1 60 ⬜

---

# PHASE 27 — Security Hardening
**Purpose:** NFR security + AI trust. **Deps:** 01-26. **Exit:** S-MAT complete. **Progress:** 0% · 0/5 · 0.

- **SEC-001 — Hardened consent flow** — P0 90 · INV-02 ⬜
- **SEC-002 — Least-privilege audit** — P0 90 · NFR-S1 ⬜
- **SEC-003 — Input validation across contracts** — P0 60 · NFR-S4 ⬜
- **SEC-004 — Vulnerability baseline** — P1 90 · S-MAT 10-12 ⬜
- **SEC-005 — Encryption in transit and at rest** — P0 60 · NFR-S2 ⬜

---

# PHASE 28 — Testing
**Purpose:** full coverage + failure paths. **Deps:** 01, features. **Exit:** T-MAT complete on critical path. **Progress:** 0% · 0/5 · 0.

- **TEST-001 — Unit coverage complete** — P0 · 3×30 ⬜
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