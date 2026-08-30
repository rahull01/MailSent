# Ruflow — Swarm Governance & Spec Kit Integration Rules

> **AUTHORITY DECLARATION & SUBORDINATION**
>
> Ruflow (RuFlo) is a **subordinate multi-agent swarm execution engine**. It operates strictly under the supreme governance of:
> 1. [`docs/00_Foundation/MAILSENT_MASTER_WORKFLOW.md`](file:///C:/Users/Pradeep%20Singh/Desktop/MailSent/docs/00_Foundation/MAILSENT_MASTER_WORKFLOW.md) — Operational & Product Truth (V1 Final).
> 2. [`docs/00_Foundation/PROJECT_CONSTITUTION.md`](file:///C:/Users/Pradeep%20Singh/Desktop/MailSent/docs/00_Foundation/PROJECT_CONSTITUTION.md) — Engineering Truth & Supreme Philosophy (v3.1).
> 3. [`implementation/00_IMPLEMENTATION_MASTER_CHECKLIST.md`](file:///C:/Users/Pradeep%20Singh/Desktop/MailSent/implementation/00_IMPLEMENTATION_MASTER_CHECKLIST.md) — Engineering Execution System.
>
> **Spec Kit Integration:** Spec Kit (`.specify/`) is the **Specification & Planning Engine**. Ruflow is the **Multi-Agent Execution Engine**. Ruflow must NEVER invent requirements, architecture, or backlogs independently.

---

## 1. Division of Responsibility: Ruflow ↔ Spec Kit

```
┌─────────────────────────────────────────────────────────────┐
│                 MAILSENT FOUNDATION DOCS                    │
│   - MAILSENT_MASTER_WORKFLOW.md   - PROJECT_CONSTITUTION.md │
│   - GLOSSARY.md                   - 03_Architecture/        │
│   - 00_IMPLEMENTATION_MASTER_CHECKLIST.md                   │
└──────────────────────────────┬──────────────────────────────┘
                               │ Governs
┌──────────────────────────────▼──────────────────────────────┐
│                  SPEC KIT (Planning Engine)                 │
│   1. /speckit-specify  ──> Produce scoped spec.md           │
│   2. /speckit-clarify  ──> Resolve ambiguities via Q&A      │
│   3. /speckit-plan     ──> Create plan.md (locked stack)    │
│   4. /speckit-tasks    ──> Break down into tasks.md (30-90m)│
└──────────────────────────────┬──────────────────────────────┘
                               │ Dispatches Tasks
┌──────────────────────────────▼──────────────────────────────┐
│               RUFLOW SWARM (Execution Engine)               │
│   - Queen Agent        ──> Task coordination & status sync │
│   - Architect Agent    ──> Architecture & invariant checks  │
│   - Coder Agents       ──> Java 21 / Next.js implementation │
│   - Tester Agent       ──> Mandatory T-MAT automated tests  │
│   - Reviewer Agent     ──> 10-point DoD & §26 review gate   │
└──────────────────────────────┬──────────────────────────────┘
                               │ Synchronizes Status
┌──────────────────────────────▼──────────────────────────────┐
│         00_IMPLEMENTATION_MASTER_CHECKLIST.md               │
│   Update: 🟦 Code Complete -> 🟪 Tests Complete -> ✅ Done  │
└─────────────────────────────────────────────────────────────┘
```

---

## 2. Ruflow Non-Negotiable Operating Invariants

1. **The Golden Rule:** Every implemented capability must solve a validated customer problem with 12+ month subscription value. If not $\rightarrow$ **REJECT**.
2. **Never Fabricate Delivery Truth (`N2`):** Never guess or simulate health states, delivery status, or competitor benchmarks (`INV-01`, `INV-05`).
3. **No Silent Data Loss (`N3`):** All failures must be logged with MDC context and handled gracefully.
4. **Zero Secrets in Code/Logs (`N4`):** Credentials, keys, tokens, and PII must never appear in source code, logs, or test fixtures.
5. **No Untested Production Path (`N5`):** Automated tests are mandatory for all production logic.
6. **Documentation Precedes Code:** Direction of truth is one-way. Code follows architecture; architecture follows requirements. Code never alters documentation.
7. **Strict Multi-Tenant Isolation (`INV-09`):** All repository queries and state accesses must be scoped by `organization_id`.
8. **Deterministic Lineage (`INV-11`):** All health signals, diagnoses, issues, and explanations must cite explicit lineage IDs linking back to raw evidence.
9. **Flyway Migration Sequence:** New migrations must use `V19+`. Migrations `V1` through `V18` are immutable.
10. **10-Point Definition of Done:** No task is marked complete without passing all 10 criteria in Constitution §19.

---

## 3. Swarm Role Responsibilities

- **Queen (Coordinator):**
  - Consumes `tasks.md` from the active Spec Kit feature directory.
  - Ensures tasks are executed in strict dependency order without skipping.
  - Upon task completion, updates the parent task status in `00_IMPLEMENTATION_MASTER_CHECKLIST.md`.
- **Architect:**
  - Audits planned class locations against `com.mailsent.<bounded_context>` DDD packages.
  - Enforces Delivery Observation vs. Diagnosis separation.
- **Coder (Backend / Frontend):**
  - Implements clean, maintainable, single-responsibility components adhering to Ubiquitous Language in `GLOSSARY.md`.
- **Tester:**
  - Writes comprehensive unit, edge, negative, and failure-path tests (**T-MAT**).
  - Validates `mvn test` and `npm run build && npm run lint`.
- **Reviewer:**
  - Runs the §26 Review Checklist before any task or branch is marked complete.

---

**Status:** Ratified Ruflow Governance | **Authoritative Base:** MailSent Constitution v3.1 & Master Workflow V1 Final
