<!--
SYNC IMPACT REPORT:
- Version change: Uninitialized Template -> v3.1-speckit-sync
- Synchronized from authoritative sources:
  1. docs/00_Foundation/PROJECT_CONSTITUTION.md (v3.1)
  2. docs/00_Foundation/MAILSENT_MASTER_WORKFLOW.md (V1 Final)
  3. implementation/00_IMPLEMENTATION_MASTER_CHECKLIST.md
- Removed: Generic template placeholder principles (Library-First, CLI Interface, etc.)
- Retained: 100% of MailSent ratified principles, invariants, non-negotiable rules, and authority hierarchy.
-->

# MailSent — Spec Kit Constitution Memory

> **AUTHORITY DECLARATION & GOVERNANCE STATUS: DERIVED EXECUTION MEMORY**
>
> This document is a **derived execution-memory representation** of MailSent's supreme governing engineering constitution. It exists solely to guide Spec Kit agents, subagents, and skills during execution.
>
> **The supreme, authoritative governing documents of MailSent are:**
> 1. [`docs/00_Foundation/MAILSENT_MASTER_WORKFLOW.md`](file:///C:/Users/Pradeep%20Singh/Desktop/MailSent/docs/00_Foundation/MAILSENT_MASTER_WORKFLOW.md) — Operational & Product Source of Truth.
> 2. [`docs/00_Foundation/PROJECT_CONSTITUTION.md`](file:///C:/Users/Pradeep%20Singh/Desktop/MailSent/docs/00_Foundation/PROJECT_CONSTITUTION.md) — Engineering Truth & Supreme Philosophy (v3.1).
> 3. [`implementation/00_IMPLEMENTATION_MASTER_CHECKLIST.md`](file:///C:/Users/Pradeep%20Singh/Desktop/MailSent/implementation/00_IMPLEMENTATION_MASTER_CHECKLIST.md) — Engineering Execution System.
>
> **Subordination Clause:**  
> **Spec Kit is a tooling accelerator and is subordinate to MailSent's authoritative documentation.** This derived constitution memory must **never** override, weaken, contradict, or substitute for the authoritative documents. If any conflict arises between Spec Kit memory/templates and the authoritative foundation docs, the authoritative foundation docs win automatically.

---

## 1. The Supreme Governing Rule: The Golden Rule

> **"No feature is approved because it is technically impressive. Every feature must solve a validated customer problem and create recurring value. If not — reject."**

Before anything is specified, planned, or built, ask with evidence:
> **"Will a customer pay a monthly subscription for this, for 12 months or more?"**

If **NO** $\rightarrow$ **REJECT immediately. No debate, no exceptions.** Technical impressiveness is never a reason to build; a validated problem that creates recurring value is the only reason.

---

## 2. Authority Hierarchy & Product/Engineering Separation

Per [PROJECT_CONSTITUTION.md](file:///C:/Users/Pradeep%20Singh/Desktop/MailSent/docs/00_Foundation/PROJECT_CONSTITUTION.md) §3.5 and [MAILSENT_MASTER_WORKFLOW.md](file:///C:/Users/Pradeep%20Singh/Desktop/MailSent/docs/00_Foundation/MAILSENT_MASTER_WORKFLOW.md):

$$\text{Level 0: The Golden Rule} \longrightarrow \text{Level 1: Master Workflow (Product) / Constitution (Engineering)} \longrightarrow \text{Level 2: Glossary} \longrightarrow \text{Level 3: Product Strategy} \longrightarrow \text{Level 4: Requirements} \longrightarrow \text{Level 5: Architecture} \longrightarrow \text{Level 6: Master Checklist} \longrightarrow \text{Level 7: Code \& Tests} \longrightarrow \text{Level 8: Spec Kit Tooling}$$

### Product vs. Engineering Authority:
- **Product Truth (What we build and for whom):** Governed exclusively by `MAILSENT_MASTER_WORKFLOW.md` (12 Core Modules, ICP hierarchy P0/P1, Pricing hypotheses, Feature Approval Formula [80+ threshold], Kill List in `REJECTED_FEATURES.md`, V1 Exclusions, V2 triggers [$10-15k MRR, 50+ customers, PMF]).
- **Engineering Truth (How we build to world-class standard):** Governed exclusively by `PROJECT_CONSTITUTION.md` (Architecture, Non-Negotiable Rules, Definition of Done, Security, Testing, DoD).
- **Execution Truth:** Governed exclusively by `00_IMPLEMENTATION_MASTER_CHECKLIST.md`.

---

## 3. Product Identity & Natural Flow DNA

MailSent is an **AI Email Infrastructure Intelligence Platform**. It is **NOT** a DMARC checker, **NOT** a warmup tool, **NOT** a marketing automation platform, and **NOT** generic email software.

### The Natural Flow (Product DNA):
The product is one ordered, natural flow (data $\rightarrow$ understanding $\rightarrow$ meaning $\rightarrow$ decision $\rightarrow$ action $\rightarrow$ verification $\rightarrow$ communication $\rightarrow$ scale):
$$\text{Scanner} \rightarrow \text{Analyzer} \rightarrow \text{Alignment Intelligence} \rightarrow \text{AI Explanation} \rightarrow \text{Business Impact} \rightarrow \text{AI Action Queue} \rightarrow \text{Guided Fix} \rightarrow \text{Continuous Monitoring} \rightarrow \text{Alerts} \rightarrow \text{Reports} \rightarrow \text{Agency Dashboard}$$

*Changing this order damages the philosophy. Truth before claims, and value before action.*

---

## 4. Non-Negotiable Engineering Rules (N1 – N8)

Absolute. No exceptions, no overrides for deadlines or convenience:
- **N1 — No unvalidated feature:** Nothing ships unless it solves a validated problem and creates recurring value (The Golden Rule).
- **N2 — No fabricated or guessed delivery state:** Reported states must trace back to a real, observed event (INV-01, INV-05).
- **N3 — No silent data loss:** Failures are detected, logged, and surfaced — never dropped.
- **N4 — No secrets in code or repositories:** Credentials, tokens, keys, and PII never appear in source code, logs, docs, or test fixtures.
- **N5 — No untested production path:** Anything affecting diagnostic reporting, calculation, or mutations requires automated tests.
- **N6 — No lying to the user:** Errors, reports, and UI always describe reality truthfully without dark patterns or AI-washing.
- **N7 — No bypassing the approval framework:** Features that skip the feature approval framework do not ship.
- **N8 — No security review shortcuts:** Anything touching customer data or the diagnostic path must pass security review.

---

## 5. Frozen Architectural Guardrails & Stack Constraints

The technical stack is **LOCKED** and must not be altered by Spec Kit plans:
- **Backend:** Java 21, Spring Boot, Maven, PostgreSQL, Redis, Flyway migrations.
- **Frontend:** Next.js (App Router), TypeScript, Tailwind CSS, React.
- **Infrastructure:** Docker Compose, hermetic test profiles.

### Architecture Invariants & Frozen Decisions:
1. **Separation of Concerns:** Boundaries follow the 15 Bounded Contexts defined in `docs/03_Architecture/03_BOUNDED_CONTEXTS.md`.
2. **Delivery Observation vs. Diagnosis:** Delivery Observation *owns* raw observation facts (Observation Evidence, DMARC Aggregate Evidence, Reputation Observation, DNS Health, Blacklist, Reverse DNS, SMTP Greeting) and unified Health Signal (`HEALTH-001`). Diagnosis *consumes* observations to derive alignment and root cause (`DIAG-001`, `DIAG-002`, `DIAG-003`). Diagnosis does *not* own or mutate Observation Evidence.
3. **Health Signal Integrity (`HEALTH-001`):** Health Signal is produced by Delivery Observation from persisted snapshots only. Missing snapshots $\rightarrow$ persist nothing (never guess or fabricate unknown). Observation Evidence strings and Reputation payloads never flip health indication directly.
4. **Tenant Isolation (`INV-09`):** Strict multi-tenant isolation by `organization_id` across all queries, models, caches, and storage.
5. **Deterministic Lineage (`INV-11`):** Every health state, diagnosis, issue, explanation, and report line must cite explicit lineage IDs linking back to raw evidence.
6. **No Sprawl:** Integrations are limited to authorized ESPs. No arbitrary third-party endpoints or unsolicited outbound probes.

---

## 6. Documentation-Before-Implementation Principle

The direction of truth in MailSent is strictly one-way and never reverses:
$$\text{Implementation follows documentation.} \quad \text{Documentation never follows implementation.}$$
$$\text{Code follows architecture.} \quad \text{Architecture follows requirements.} \quad \text{Requirements follow customer value.}$$

- Spec Kit tools must never write code for a feature that lacks a frozen requirement and checklist task.
- Implementation must **never** contradict authoritative docs. When code and docs disagree, the code is wrong, and the disagreement must be surfaced—never papered over by silently modifying the documentation.

---

## 7. Traceability Requirements

Every Spec Kit artifact (`spec.md`, `plan.md`, `tasks.md`) must carry explicit traceability anchors:
- **CP:** Customer Problem ID (`CP-1` to `CP-8` from `CUSTOMER_PROBLEMS.md`)
- **J / VP:** Customer Journey (`J-1` to `J-6`) and Value Proposition (`VP-1` to `VP-6`)
- **Req:** Functional (`FUN-xxx`), AI (`AIR-AIx`), or Non-Functional (`NFR-xxx`) Requirement ID
- **Arch:** Architecture document and Bounded Context reference
- **WF:** Master Workflow module number (Modules 1–12 or DMARC Ingest)
- **Checklist Task:** Explicit task identifier from `00_IMPLEMENTATION_MASTER_CHECKLIST.md` (e.g., `DOM-001`, `MAP-002`, `DIAG-002`)

---

## 8. Definition of Done (DoD) & Mandatory Matrices

A task, feature, or phase is **Done** only when **all 10 conditions** from Constitution §19 are satisfied:
1. **Validated:** Solves a real customer problem with 12+ month recurring value (Golden Rule).
2. **Works:** Implemented and functioning strictly per contract.
3. **Tested:** Covered by automated tests across the **T-MAT** Testing Matrix (happy, edge, negative, failure-recovery, regression, security, integration).
4. **Verified:** Checks executed with empirical evidence recorded.
5. **Reviewed:** Passed code, architecture, and quality review.
6. **Documented:** Documentation updated, accurate, and in place.
7. **Observable:** Metrics, traces, logs (**O-MAT**) implemented.
8. **Secured:** Passed security standards (**S-MAT**), least privilege, input validation, no secrets (**N4**).
9. **Honest:** Truthful status reported; AI outputs grounded and attributable (**AI-MAT**).
10. **Committed & Clean:** No dead code, no compiler warnings, no migrations broken.

---

## 9. AI Engineering & UX Principles

### AI Principles:
- **AI never replaces thinking:** Grounded in analysis, never substitute for domain logic.
- **AI never fabricates truth:** No hallucinated diagnostics, delivery ranks, or benchmark scores.
- **AI explains, prioritizes, guides, and assists:** Humans remain accountable for real actions.
- **Business Language First:** Technical jargon is translated into plain business impact (e.g. *"Password reset emails may not reach customers"* instead of *"SPF include exceeded 10 lookups"*).

### UX Screen Standard (The 5 Questions in Order):
Every screen, alert, and diagnostic view must answer:
1. **What happened?**
2. **Why?**
3. **What is the business impact?**
4. **What should I do? (Concrete next action)**
5. **What happens if I ignore it?**

---

## 10. Spec Kit Operational Protocol

When executing Spec Kit skills (`speckit-specify`, `speckit-plan`, `speckit-tasks`, `speckit-implement`, `speckit-analyze`):
1. **Scoping:** Spec Kit operates on discrete, unblocked task slices defined in `implementation/00_IMPLEMENTATION_MASTER_CHECKLIST.md`.
2. **Consistency:** All task breakdowns in `specs/<feature>/tasks.md` must be sized for 30–90 minutes and must synchronize their status with `00_IMPLEMENTATION_MASTER_CHECKLIST.md`.
3. **Subordination:** No Spec Kit skill may create architecture, change database schemas, or implement features outside the boundaries ratified in MailSent's foundation documentation.

---

**Version:** 3.1-speckit-sync | **Authoritative Base:** MailSent Constitution v3.1 & Master Workflow V1 Final | **Ratified Status:** Active Derived Execution Memory
