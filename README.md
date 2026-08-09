# MailSent

**AI Email Infrastructure Intelligence Platform.**

MailSent sells **confidence** — the certainty that email reaches the inbox, that infrastructure stays healthy, that problems surface early, and that teams know exactly what to fix. It is not a DMARC checker, not a warmup tool, and not a communication platform; it is the intelligence layer that reasons across everything that determines inbox delivery.

## Repository layout

- `docs/` — the **frozen**, authoritative documentation: foundation (constitution, master workflow, glossary), product, product design, and architecture. This is the single source of truth; it changes only by governance.
- `implementation/` — the **engineering execution system**: `README.md` (operating manual) and `00_IMPLEMENTATION_MASTER_CHECKLIST.md` (the exhaustive task list with status and traceability). This is where all implementation work is defined and tracked.
- `scripts/` — foundation tooling of the implementation (currently the repository secrets guard).

## The governing philosophy

- Implementation follows documentation. Documentation never follows implementation.
- Code follows architecture. Architecture follows requirements. Requirements follow customer value. Everything follows the Master Workflow.
- No feature is approved because it is technically impressive; every feature must solve a validated customer problem and create recurring value.

## Getting started (implementation)

Read `implementation/README.md`, then the Master Checklist, then begin at Phase 00. Do not modify any approved document in `docs/`; report conflicts rather than resolving them in code.

## Status

Architecture: approved · Documentation: approved · Implementation: in progress (Phase 00).