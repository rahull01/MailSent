# MailSent — Implementation Execution Framework

> The operating manual for the MailSent implementation phase.
>
> This is **not** product documentation, **not** architecture, and **not** implementation. It is the rulebook that governs how the approved, frozen documentation becomes working software — how engineers and AI agents work, how progress is tracked, how quality is enforced, and how the implementation stays faithful to the source of truth.
>
> It is subordinate to the [Project Constitution](../docs/00_Foundation/PROJECT_CONSTITUTION.md), the [Master Workflow](../docs/00_Foundation/MAILSENT_MASTER_WORKFLOW.md), the [Glossary](../docs/00_Foundation/GLOSSARY.md), and the [Requirements Traceability Matrix](../docs/02_Product_Design/REQUIREMENTS_TRACEABILITY_MATRIX.md). Those documents govern; this document explains how to work within them. It does not copy or rewrite them.

---

## 1. Purpose

The implementation phase turns an approved plan into a working product. It is a place where thousands of small decisions are made, and where the risk of drift is highest — a feature added on impulse, a name changed casually, a requirement quietly dropped. This framework exists to keep every one of those decisions faithful to the source.

The `implementation/` folder is the single execution system for MailSent. It is where every engineering task is defined, tracked, and completed. Nothing is built outside it; nothing on it is skipped or invented. This README is the instruction manual for working inside that system.

## 2. Folder Structure

The `implementation/` folder holds the execution artifacts of the project. Each file has one responsibility.

- **`README.md`** — this document. The operating manual: how the folder is used, how implementation is controlled, and how quality is enforced.
- **`00_IMPLEMENTATION_MASTER_CHECKLIST.md`** — the Engineering Execution System. The single, exhaustive list of every implementation task, organized into phases, with traceability, acceptance, and status for each. It is the source that every task originates from.
- **`01_IMPLEMENTATION_RULES.md`** *(planned)* — the standing engineering rules and conventions that bind all work (task sizing, naming, branch and review practice, the quality gate). This is the reference an engineer or AI agent consults before starting any task.
- **`02_PROGRESS_DASHBOARD.md`** *(planned)* — the live state of delivery: per-phase progress, completed, remaining, and blocked counts, and the overall build status. It is the single visible picture of where the project stands.
- **`03_TRACEABILITY_INDEX.md`** *(planned)* — the map from every requirement and architecture element to its implementation task. It makes the traceability requirement verifiable at a glance and is the guard against orphan work.
- **`04_TASK_TEMPLATE.md`** *(planned)* — the canonical template every task block must follow, so no task is ambiguous and no field is ever missing.
- **`phases/`** *(planned)* — one file per phase (00–31) holding the phase-specific task list, dependencies, and exit criteria, mirroring the Master Checklist for focused execution.

Each file exists because it serves one purpose: the checklist defines the work, the rules govern how it is done, the dashboard shows the state, the index proves traceability, and the template keeps every task unambiguous.

## 3. Engineering Philosophy

The direction of truth in MailSent is one-way, and it never reverses:

**Implementation follows documentation.**
**Documentation never follows implementation.**

**Code follows architecture.**
**Architecture follows requirements.**
**Requirements follow customer value.**
**Everything follows the Master Workflow.**

A change that would flow the wrong way — a feature added in code before it exists in requirements, an architecture changed because the code was easier — is a violation, not an improvement. The approved documents are the cause; the implementation is the effect. When the two disagree, the implementation is wrong, and the disagreement is reported, never papered over by changing the source.

## 4. Golden Rules

These rules are absolute. No deadline, no convenience, and no excitement overrides them.

- **Never skip tasks.** The checklist is exhaustive; a task is either done or in progress, never quietly skipped.
- **Never implement undocumented work.** A feature that is not in the approved documentation and not in the checklist does not get built.
- **Never change architecture.** Architecture is frozen.
- **Never change documentation.** Documentation is frozen.
- **Never bypass testing.** No task completes without its tests passing.
- **Never bypass traceability.** Every task must trace to a requirement, an architecture element, and a customer problem.
- **Never bypass review.** Nothing merges without review.
- **The recurring-value test.** Every feature must satisfy: *"Would the customer happily continue paying for this capability after 12 months?"* If **no**, do not implement it. If uncertain, mark it a Future Candidate; it is not built now.

## 5. Execution Flow

Every task passes through the same chain, from customer need to running production.

**Customer Problem → Requirements → Architecture → Implementation Task → Backend → Frontend → AI → Testing → Deployment → Production**

Each step is the gate of the one after it. The customer problem justifies the requirement; the requirement is placed by the architecture; the architecture defines the task; the task produces its parts; the parts are tested; the tested whole is deployed; the deployed whole serves production. Skipping a step breaks the chain, and a broken chain is a broken promise.

## 6. Implementation Folder Overview

The folder is a control center, not a pile of files. Each element has a role.

- **README** — the manual: how to work inside the system.
- **Master Checklist** — the exhaustive task list: what exists, what is left, and its status.
- **Implementation Rules** — the standing conventions: how any task is done correctly.
- **Progress Dashboard** — the live state: how far delivery has come.
- **Traceability Index** — the proof: every task traces to its source.
- **Templates** — the shape: the canonical task form that removes ambiguity.
- **Phase Files** — the granular view: each phase's tasks, dependencies, and exit criteria.

Together they form one system: a new engineer reads the README, follows the Rules, picks an unblocked task from a Phase File, fills it in using the Template, and updates the Dashboard and Traceability Index as it completes.

## 7. Progress Tracking

Progress is tracked honestly, on every task and every phase.

- **Status** — each task carries one status from the legend (Not Started, In Progress, Code Complete, Tests Complete, Review Complete, Done, Frozen). The status is updated only when its condition is actually true.
- **Progress** — each phase reports overall progress as a percentage, plus **Completed**, **Remaining**, and **Blocked** counts. These numbers are kept current because they are the live state of the build.
- **Blocked** — a task that cannot proceed is blocked and stays blocked until its blocker is resolved; it is never worked around silently.
- **Dependencies** — a task does not start until its dependencies are met; the dependency map is respected.
- **Reviews** — a task is not Done until it has passed review; the review state is part of the record.

## 8. Definition of Done

A task is complete only when **all** of the following pass. One false condition means the task is not done.

- **Code** — the code exists, compiles, and does what the task requires.
- **Tests** — the required tests are written and pass, including the failure paths.
- **Review** — the task has passed code, architecture, and founder review.
- **Documentation** — the necessary documentation is written and accurate.
- **Traceability** — the task traces to its requirement, architecture, and customer problem.
- **Acceptance** — the task's acceptance criteria are met, verified by evidence.
- **Deployment** — the task is ready to deploy under the release and rollback rules.

A task with any of these incomplete is in progress, not done. Claiming done when one is false is a lie, and lies are the one thing this framework does not tolerate.

## 9. Quality Gates

Quality is enforced at each stage, so a defect is caught where it is cheapest to fix.

- **Planning** — the task is verified against a customer problem, a requirement, the architecture, and the traceability matrix before it is built.
- **Implementation** — the work is produced within the architecture and the language of the Glossary, with no undocumented feature.
- **Testing** — the task passes its unit, integration, end-to-end, failure-path, performance, security, and (where relevant) AI-evaluation tests.
- **Review** — the task passes review before it is considered complete.
- **Deployment** — the task ships under the release, feature-flag, and rollback rules, with a migration reviewed.
- **Release** — the task is released with a note and a smoke test, and its health is observed after release.

A gate is a hard stop, not a formality. Work that does not pass a gate does not proceed.

## 10. Engineering Principles

The principles that shape every task and every engineer's work.

- **Small tasks.** Every task is sized to be completed in a short working session; large tasks are split.
- **No assumptions.** A task states everything it needs; an engineer never fills a gap by guessing.
- **No ambiguity.** The task, its acceptance, and its definition of done are explicit.
- **No shortcuts.** Every gate and every rule is honored.
- **No feature creep.** Only the documented feature is built; nothing extra is added because it is convenient.
- **No undocumented work.** Work that is not in the checklist is not done.
- **No orphan code.** Every line of code belongs to a task, and every task traces to a source.

## 11. AI Agent Operating Rules

Mandatory operating rules for every AI implementation agent. An AI agent is bound by them exactly as a human engineer is; it holds no exception the rules do not grant.

- **Read first.** Read the relevant implementation phase and its governing documentation before writing any code. An agent that writes before it reads is out of order.
- **Never skip incomplete tasks.** A task that is not done is in progress or blocked; it is never skipped.
- **Never mark a task complete without satisfying the Definition of Done.** Completion is claimed only when every DoD condition is truly met, verified, not declared.
- **Never modify frozen documentation.** The approved documentation is immutable; an agent that needs a change reports it, it does not edit it.
- **Never redesign architecture.** An agent builds within the architecture, never around it.
- **Never invent requirements.** An agent implements what is prescribed and nothing more.
- **Report conflicts instead of making implementation assumptions.** Where implementation and documentation appear to conflict, an agent stops and reports; it never silently resolves a contradiction by assuming.
- **Always preserve traceability.** Every result an agent produces keeps its trace to a requirement, an architecture element, and a customer problem.
- **Always follow the Master Workflow.** It is the highest implementation philosophy; no agent works against it.

These rules are timeless: they bind every AI implementation agent, for every task, for the life of the project.

## 12. Engineering Workflow

The lifecycle every implementation passes through, independent of any tool.

**Task → Branch → Implementation → Testing → Review → Merge → Checklist Update → Progress Dashboard Update → Release**

- **Task.** A traced piece of work is selected from the checklist.
- **Branch.** The work is made on its own line, so it cannot disturb shared state.
- **Implementation.** The work is built within the architecture and the language of the Glossary.
- **Testing.** The work passes its required tests, including the failure paths, before it proceeds.
- **Review.** The work is reviewed before it is accepted.
- **Merge.** The accepted work is brought into the shared line.
- **Checklist Update.** The task's status and acceptance are recorded in the checklist.
- **Progress Dashboard Update.** The phase and overall progress are updated.
- **Release.** The finished work is released under the governed release and rollback rules.

Each step is a gate; a task does not advance until the current step is genuinely complete.

## 13. Governance

Authority is clear, and conflict is handled honestly.

- **Founders own the documentation.** The approved documents are theirs and are frozen.
- **Engineering owns implementation.** Engineers and AI agents own the building of what the documentation specifies.
- **Conflicts are reported.** If implementation and documentation ever conflict, the conflict is stopped and reported; it is not resolved by editing the source.
- **Never silently modify approved documents.** No engineer or AI agent changes the approved documentation on their own. Any necessary change goes through the founders and the governance of the documents themselves.

## 14. Conclusion

The implementation folder is the single execution source for engineering. Every implementation decision must trace back to the approved documentation. The Master Workflow remains the highest implementation philosophy.

Documentation drives implementation. Implementation never drives documentation.

Work happens here — task by task, gate by gate, truthfully tracked — and whoever works inside this folder and honors the source of truth builds MailSent exactly as approved, and nothing else.

**Implementation follows documentation. Everything follows the customer. Nothing is built by accident.**

---

*This framework governs the implementation phase. The approved documentation is the source; this folder is the means; the customer's confidence is the end.*
