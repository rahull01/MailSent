# MailSent — Agent Orchestration & Governance

> **SUPREME GOVERNANCE & SUBORDINATION:**
> 1. `docs/00_Foundation/MAILSENT_MASTER_WORKFLOW.md` (Product Truth, V1 Final)
> 2. `docs/00_Foundation/PROJECT_CONSTITUTION.md` (Engineering Truth, v3.1)
> 3. `implementation/00_IMPLEMENTATION_MASTER_CHECKLIST.md` (Engineering Execution System)
> Spec Kit (`.specify/`) and Ruflo (`.claude-flow/` / `.ruflow/`) are subordinate tooling layers.

## Project Overview

MailSent is an AI Email Infrastructure Intelligence Platform.

- **Backend**: Java 21, Spring Boot 3.x, Maven, Spring Data JPA, PostgreSQL, Redis
- **Frontend**: Next.js 14+ (App Router), TypeScript, Tailwind CSS, React
- **Architecture**: 15 Decoupled Bounded Contexts (DDD)
- **Database Migrations**: Flyway (New migrations must start at `V19+`; `V1–V18` are frozen/immutable)

## Core Non-Negotiable Invariants (N1 – N8)

1. **The Golden Rule:** Every feature must solve a validated customer problem with 12+ month subscription value. If not $\rightarrow$ **REJECT**.
2. **Never Fabricate Truth (N2):** Never guess delivery states or benchmark ranks. Report only real, observed evidence (`INV-01`, `INV-05`).
3. **No Silent Loss (N3):** Fail loudly, log with MDC context, handle gracefully.
4. **Zero Secrets in Code/Logs (N4):** Never commit or log credentials, tokens, or PII.
5. **Mandatory Automated Tests (N5):** Full T-MAT test coverage required for all production paths.
6. **Multi-Tenant Isolation (INV-09):** All queries and state strictly partitioned by `organization_id`.
7. **Deterministic Lineage (INV-11):** Outputs must cite source snapshot evidence IDs.
8. **Definition of Done:** All 10 criteria in Constitution §19 must be satisfied before calling any work Done.

## Tooling Workflow: Spec Kit ↔ Ruflo

- **Specification & Planning:** Spec Kit (`/speckit-specify`, `/speckit-plan`, `/speckit-tasks`).
- **Swarm Execution:** Ruflo coordinates tactical agents (queen, architect, backend coder, frontend coder, tester, reviewer).
- **Status Sync:** Every completed task must update `implementation/00_IMPLEMENTATION_MASTER_CHECKLIST.md`.

## Agent Coordination & Swarm Configuration

| Setting | Value | Purpose |
|---------|-------|---------|
| Topology | `hierarchical` | Queen-led coordination (anti-drift) |
| Max Agents | 8 | Optimal team size |
| Strategy | `specialized` | Clear role boundaries (Architect, Backend, Frontend, Tester, Reviewer) |
| Consensus | `raft` | Leader-based consistency |

### Tactical Agent Types & Boundaries

| Type | Role | MailSent Boundary |
|------|------|-------------------|
| `architect` | Architecture Guard | Enforces 15 Bounded Contexts, Flyway `V19+`, and Delivery vs Diagnosis boundaries |
| `coder` (backend) | Java Specialist | Implements Java 21 / Spring Boot domain entities, services, controllers |
| `coder` (frontend)| Next.js Specialist | Implements React / Next.js components answering the 5 UX questions |
| `tester` | T-MAT QA | Enforces mandatory automated tests (Rule N5, hermetic unit & failure-path tests) |
| `reviewer` | DoD Reviewer | Verifies Constitution §19 (10-point DoD) & §26 Review Checklist |

## MCP Integration

| Tool | Purpose | Example |
|------|---------|---------|
| `swarm_init` | Start coordination | `swarm_init({topology: "hierarchical"})` |
| `memory_store` | Save patterns | `memory_store({key: "auth", value: "JWT"})` |
| `memory_search` | Find patterns | `memory_search({query: "health patterns"})` |
| `task_orchestrate` | Assign work | `task_orchestrate({task: "implement"})` |
