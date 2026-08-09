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
- **ORG-002 — Membership** — user joins an org. P0 60 ⬜
- **ORG-003 — Org→Domain ownership** — registry authorization. P0 60 ⬜
- **ORG-004 — Isolation invariant** — INV-09 test. P0 45 ⬜

---

# PHASE 04 — Teams
**Purpose:** roles, invite, least-authority, audit. **Deps:** 02,03. **Exit:** a team admin adds an analyst who acts in scope. **Progress:** 0% · 0/4 · 0.

- **TEAM-001 — Invite by email & accept** — P0 60 · Arch CTX Identity · FUN-D5 · Status ⬜
- **TEAM-002 — Role→capability map (least-authority)** — P0 60 ⬜
- **TEAM-003 — Access audit record** — P1 60 · NFR-AU ⬜
- **TEAM-004 — Team list query** — P1 45 ⬜

---

# PHASE 05 — Clients
**Purpose:** Client relationship (agency). **Deps:** 03,04. **Exit:** client created, assigned, isolated. **Progress:** 0% · 0/3 · 0.

- **CLIENT-001 — Client CRUD** — P0 60 · DATA Client/Portfolio ⬜
- **CLIENT-002 — Assign client to agency** — P0 60 ⬜
- **CLIENT-003 — Client isolation (INV-09)** — P0 45 ⬜

---

# PHASE 06 — Portfolio
**Purpose:** portfolio-scale view. **Deps:** 05. **Exit:** an agency sees a portfolio with risk. **Progress:** 0% · 0/3 · 0.

- **PORT-001 — Portfolio container** — P0 60 · FUN-D4 ⬜
- **PORT-002 — Portfolio overview data** — P0 90 ⬜
- **PORT-003 — Portfolio isolation** — P1 45 · INV-09 ⬜

---

# PHASE 07 — Domains
**Purpose:** Domain entity + registry. **Deps:** 03,06. **Exit:** add/import/list domains with ownership. **Progress:** 0% · 0/4 · 0.

- **DOM-001 — Add domain (manual)** — P0 60 · FUN-D1 ⬜
- **DOM-002 — Bulk CSV import** — P1 90 · FUN-D2 ⬜
- **DOM-003 — Domain list/state** — P0 60 · FUN-D3 ⬜
- **DOM-004 — Registry ownership** — P0 60 · FUN-D4 ⬜

---

# PHASE 08 — Infrastructure Observation
**Purpose:** observe infrastructure into truth (Evidence). **Deps:** 07. **Exit:** domain records read and stored as evidence, not inferred. **Progress:** 0% · 0/12 · 0.
**Trace anchors:** CP-1/2 · VP-1/2 · WF-2 + Scanner · Arch CTX Delivery Observation / DATA / INFO.

- **INF-001 — DNS read (MX/SPF/TXT/DKIM)** — P0 90 · FUN-H2 · INV-01 · Status ⬜
- **INF-002 — Observation lifecycle → Evidence** — P0 90 · no fabrication · ⬜
- **INF-003 — Reputation ingest (mocked first, grounded)** — P0 90 · FUN-H3 · ⬜
- **INF-004 — Re-observe (schedule)** — P0 60 · FUN-M1 · ⬜
- **INF-005 — Reverse DNS (PTR)** — P1 60 ⬜
- **INF-006 — HELO/EHLO validation** — P1 45 ⬜
- **INF-007 — DNS health (prop/TTL/DNSSEC)** — P1 60 ⬜
- **INF-008 — Blacklist monitor (Spamhaus/Barracuda/SURBL/URIBL)** — P1 90 ⬜
**Infrastructure Map (WF-5)**
- **MAP-001 — Map model** (Domain→DNS→SPF→DKIM→DMARC→Senders→Inbox) — P1 60 ⬜
- **MAP-002 — Color-coded view** — P1 90 ⬜
- **MAP-003 — Node detail panel** — P1 90 ⬜
- **MAP-004 — PNG/SVG export** — P2 90 ⬜

---

# PHASE 09 — Diagnosis Engine
**Purpose:** derive cause from signals. **Deps:** 08. **Exit:** root cause with lineage. **Progress:** 0% · 0/4 · 0.
**Trace anchors:** CP-6 · WF-3 Alignment Intelligence · Arch CTX Diagnosis.

- **DIAG-001 — Alignment** (SPF/DKIM, envelope vs header) — P0 90 · FUN-X1 ⬜
- **DIAG-002 — Sender detection** (recognized providers) — P0 90 · FUN-X2 ⬜
- **DIAG-003 — Root cause determination** — P0 90 · FUN-X3 · INV-11 ⬜
- **DIAG-004 — Issue entity lifecycle** — P0 60 · DATA Issue ⬜

---

# PHASE 10 — Health Signal Engine
**Purpose:** unified Health Signal + trend. **Deps:** 09. **Exit:** single, truthful signal with trend; no fabricated rank. **Progress:** 0% · 0/4 · 0.
**Trace anchors:** CP-3 · WF-4 · GLOS Health Signal.

- **HEALTH-001 — Unify Health Signal (components)** — P0 60 · FUN-H1 · INV-01/05 ⬜
- **HEALTH-002 — Health Trend series** — P0 90 · FUN-H4 ⬜
- **HEALTH-003 — Grounding invariant** — P0 45 ⬜
- **HEALTH-004 — Benchmark context (real data only, no rank)** — P2 90 · FUN-H5 ⬜

---

# PHASE 11 — Business Explanation
**Purpose:** business-first translation. **Deps:** 09. **Exit:** a non-technical user understands the issue and impact. **Progress:** 0% · 0/4 · 0.
**Trace anchors:** CP-5 · WF-9 (AI) + AI Explanation · Arch CTX Business Explanation / AIAD.

- **EXPL-001 — Explanation (what/why/impact)** — P0 60 · FUN-E1/E2 · AIR-AI1 ⬜
- **EXPL-002 — Deeper technical view** — P1 60 · FUN-E3 ⬜
- **EXPL-003 — Simpler toggle** — P2 45 · FUN-E4 ⬜
- **EXPL-004 — Grounding + trace** — P0 90 · AIR-AI5/6 ⬜

---

# PHASE 12 — Guided Resolution
**Purpose:** prioritized, verifiable fix. **Deps:** 09-11. **Exit:** a customer fixes with verified outcome. **Progress:** 0% · 0/4 · 0.
**Trace anchors:** CP-6/7 · WF-6 Guided Fix + AI Action Queue.

- **RES-001 — Action list (prioritized)** — P0 60 · FUN-G1/P2 ⬜
- **RES-002 — Steps with exact values** — P0 60 · FUN-G2 ⬜
- **RES-003 — Match by DNS provider** — P1 90 · FUN-G3 ⬜
- **RES-004 — Fix verification loop** — P0 60 · FUN-G5 ⬜

---

# PHASE 13 — Monitoring
**Purpose:** continuous observation. **Deps:** 08,10. **Exit:** health over time is tracked. **Progress:** 0% · 0/4 · 0.
**Trace anchors:** CP-4 · WF-7 Continuous Monitoring.

- **MON-001 — Scheduled re-observe** — P0 60 · FUN-M1 ⬜
- **MON-002 — Change detection** — P0 60 · FUN-M2 ⬜
- **MON-003 — Health history store** — P0 60 · FUN-H4 ⬜
- **MON-004 — Incident record** — P1 60 ⬜

---

# PHASE 14 — Alerts
**Purpose:** business-language early warning. **Deps:** 13. **Exit:** alerts reach user in business language, controlled. **Progress:** 0% · 0/4 · 0.
**Trace anchors:** CP-4 · WF-8 Alerts.

- **ALERT-001 — Severity levels** — P1 45 · FUN-M5 ⬜
- **ALERT-002 — Business-language body** — P0 60 · FUN-M4 ⬜
- **ALERT-003 — Channels (email/Slack/in-app)** — P1 90 · FUN-M6 ⬜
- **ALERT-004 — Digest + control** — P1 60 · FUN-M7 ⬜

---

# PHASE 15 — Reporting
**Purpose:** executive summaries & reports. **Deps:** 10,11. **Exit:** recurring report produced. **Progress:** 0% · 0/4 · 0.
**Trace anchors:** CP-8 · WF-12 Reporting.

- **REP-001 — Weekly summary (3-bullet)** — P1 90 · FUN-R1 ⬜
- **REP-002 — Report generator** — P0 90 · FUN-R2 ⬜
- **REP-003 — Schedule & deliver** — P1 45 · FUN-R3 ⬜
- **REP-004 — White label (Agency)** — P2 90 ⬜

---

# PHASE 16 — Evidence
**Purpose:** grounded proof + audit trail. **Deps:** 15. **Exit:** every report line traces to a source. **Progress:** 0% · 0/3 · 0.
**Trace anchors:** VP-4/5 · FUN-R4 · NFR-AU · INV-11.

- **EVID-001 — Report grounding** — P0 90 · each line ← source · Status ⬜
- **EVID-002 — Audit trail record** — P0 45 · INV-11 ⬜
- **EVID-003 — Evidence API/export** — P1 60 · API 08 ⬜

---

# PHASE 17 — AI Intelligence
**Purpose:** governed AI (explain, prioritize, guide, assist). **Deps:** 09,11. **Exit:** AI explains with grounding, never decides. **Progress:** 0% · 0/5 · 0. **AI dimension:** every task runs AI-MAT.
**Trace anchors:** CP-5 · WF-9 AI Layer · Arch AIAD · AIR-AI1-12.

- **AI-001 — Explanation with grounding** — P0 90 · AIR-AI1/5/6 · AI-MAT full ⬜
- **AI-002 — Prioritization engine** — P0 90 · AIR-AI2 · FUN-P2 ⬜
- **AI-003 — Recommendation + confidence** — P0 60 · AIR-AI3/7 ⬜
- **AI-004 — Transparency & feedback loop** — P1 60 · AIR-AI9/12 ⬜
- **AI-005 — Consent-gated action lane** — P0 60 · AIR-AI4 ⬜

---

# PHASE 18 — Compliance
**Purpose:** readiness vs mailbox providers. **Deps:** 08,09. **Exit:** readiness produced and shown. **Progress:** 0% · 0/4 · 0.
**Trace anchors:** CP-8 · WF-10 Compliance · Arch CTX Readiness.

- **COMP-001 — Provider requirement checklists** — P1 90 · FUN-C1 ⬜
- **COMP-002 — Readiness badge/view** — P1 60 · FUN-C2 ⬜
- **COMP-003 — Compliance PDF** — P2 90 · FUN-C3 ⬜
- **COMP-004 — MTA-STS / BIMI / TLS-RPT readiness** — P2 90 ⬜

---

# PHASE 19 — Analytics
**Purpose:** usage & feedback analytics. **Deps:** 08,11. **Exit:** usage and drop-off measured. **Progress:** 0% · 0/5 · 0.
**Trace anchors:** WF-11 Analytics · FUN-F.

- **ANAL-001 — Event capture (scan/fix/alert)** — P1 60 ⬜
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
**Purpose:** major integrations only (no sprawl). **Deps:** 08. **Progress:** 0% · 0/6 · 0.
**Trace anchors:** WF DMARC Ingestion.

- **INT-003 — DMARC ingest: receive** — P0 90 ⬜
- **INT-004 — DMARC ingest: parse** — P0 90 ⬜
- **INT-005 — DMARC ingest: aggregate** — P0 90 ⬜
- **INT-006 — DMARC ingest: trigger** — P0 60 ⬜
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