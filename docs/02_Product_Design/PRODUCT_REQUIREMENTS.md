# MailSent — Product Requirements (Master PRD)

> The complete product requirements for MailSent, at the product and business level.
>
> This document defines **what the product must achieve** and is the entry point for the whole requirements set. It is not architecture, not an API specification, not a database design, not a UI specification, and not a sprint plan. The engineering-grade catalogs live in dedicated documents referenced throughout, so that each document has exactly one responsibility.
>
> It is subordinate to the [Project Constitution](../00_Foundation/PROJECT_CONSTITUTION.md), the [Master Workflow](../00_Foundation/MAILSENT_MASTER_WORKFLOW.md), the [Product Vision](../01_Product/PRODUCT_VISION.md), the [Customer Problems](../01_Product/CUSTOMER_PROBLEMS.md), the [Customer Journey](../01_Product/CUSTOMER_JOURNEY.md), the [Value Proposition](../01_Product/VALUE_PROPOSITION.md), and the [Product Positioning](../01_Product/PRODUCT_POSITIONING.md), and never contradicts them.

---

**Document governance**

- **Status:** Draft
- **Version:** 1.0
- **Owner:** Founders
- **Review Status:** Pending Founder Review

After founder approval, update to **Status: Approved** and **Review Status: 🔒 Frozen**. Only the founders may freeze this document.

**Next review trigger:**
- Product philosophy changes
- Product scope changes
- Business model changes

---

## The Requirements Document Set

The requirements are organized so that every document has exactly one responsibility. This document is the business-level master; the catalogs hold the detail.

| Document | Responsibility |
| --- | --- |
| **Product Requirements** (this document) | Product intent, goals, scope, users, flows, business-level commitments, success and acceptance criteria |
| **Functional Requirements** | The functional requirement catalog, grouped by capability (what the product must enable) |
| **Non-Functional Requirements** | The quality attributes: security, reliability, performance, usability, scalability, accessibility, auditability |
| **AI Requirements** | The behavior, explainability, confidence, transparency, and human-accountability rules for AI |
| **Release Strategy** | The phased, validation-based sequencing of delivery |

## 1. Product Overview

MailSent is an **AI Email Infrastructure Intelligence Platform**. It exists to give businesses and agencies confidence that their email reaches the inbox, by revealing what is breaking delivery, explaining why it matters in business language, and guiding a prioritized, verifiable resolution before customers notice.

The product observes a customer's email delivery infrastructure, reasons across the many signals that determine inbox delivery, and turns that understanding into knowledge and action the customer can rely on. It serves the natural flow defined in the Master Workflow: from observing the state of delivery, through analyzing and explaining it, to prioritizing, guiding a fix, monitoring continuously, and reporting.

MailSent is not a sending platform, not a communication tool, and not a single-record checker. Its purpose is intelligence about delivery, expressed in the terms of the business that depends on it.

## 2. Product Goals

- **Sell confidence.** Give customers certainty that their email reaches the inbox, that infrastructure stays healthy, that problems surface early, and that teams know exactly what to fix.

- **Make the invisible visible.** Turn the silent, unobservable world of delivery into a clear, legible picture a customer can trust.

- **Translate, not just report.** Convert technical signals into business language — what happened, what it costs, and what to do — before any technical detail.

- **Detect early, resolve reliably.** Surface problems while there is still time to act, guide a prioritized resolution, and verify that the resolution holds.

- **Prove, don't assert.** Give customers a defensible, evidence-based answer that health is real — for themselves, their clients, their auditors, and their boards.

- **Serve the portfolio.** Enable agencies and multi-domain businesses to manage delivery across many clients at once, not one domain at a time.

## 3. Product Scope

The product covers the full journey from observing delivery health to guiding its improvement and proving it over time. At a capability level, the scope includes:

- **Health measurement** — establishing the state of a domain's delivery infrastructure: authentication, alignment, records, and reputation, plus a unified health signal with a trend over time.
- **Diagnosis** — identifying the root cause of a delivery problem across the assessed signals, not just its symptom.
- **Business explanation** — translating diagnostic results into the language of business: what happened, why it matters, and what is at stake.
- **Impact and prioritization** — ordering problems by the risk they pose to the business, so the customer knows what to do first.
- **Guidance and resolution** — a prioritized set of actions, step-by-step resolution, and verification that a fix has held.
- **Continuous monitoring and alerting** — keeping watch over health over time and alerting on changes and failures in business language.
- **Reporting and evidence** — recurring, executive-level summaries and materials that demonstrate health.
- **Portfolio management** — managing many domains and, for agencies, many client organizations at once.
- **Readiness and compliance** — assessing readiness against the standards set by the mailbox providers that decide inbox entry.
- **Feedback and adoption** — capturing whether explanations and guidance are useful, so the product improves.
- **Onboarding** — guiding the customer from first use to a working, monitored state, including any configuration required to begin health observation.

The detailed requirements for each capability are catalogued in the [Functional Requirements](FUNCTIONAL_REQUIREMENTS.md).

## 4. Explicitly Out of Scope

The following are out of scope and must never become part of the product. These boundaries come from the authoritative sources and are permanent.

- **Sending messages.** The product does not compose or deliver email.
- **Single-record checking.** It is not a scanner of one record; it is a reasoning layer across the whole picture.
- **Reputation manipulation.** It does not warm up or artificially game reputation.
- **General communication.** It is not a chat, inbox, or communication platform.
- **Ungrounded prediction.** It will not present a rank, forecast, or number it cannot ground in real data.
- **Autonomous changes to live infrastructure.** It will not take action on a customer's live production systems without explicit, informed consent.
- **Replacement of the human owner.** It explains, prioritizes, guides, and assists; it does not remove the accountability of the customer or their team.

## 5. Core User Types

- **Agency owner / operator** — the person who runs an agency and is accountable for delivery across every client domain. Needs to see all clients at once, act before a client notices, and prove health to clients.
- **Agency team member** — an analyst or specialist within an agency who does the daily health work, reviews guidance, and acts on it.
- **Business decision-maker** — a founder, operator, or executive who owns revenue and trust but is not technical. Needs the business meaning, prioritization, and confidence, not raw data.
- **Technical operator** — a person within the business or agency who wants to go deeper into the mechanism behind a business answer.
- **Auditor / client representative** — a person who receives proof that delivery is healthy and must be able to trust it.

The needs of the first four drive the core product. The last is served through the reporting and evidence capabilities.

## 6. Primary User Outcomes

Every requirement in the set exists to produce one or more of these outcomes for a user:

- **Know.** The user can answer, at any moment, whether their email is reaching the people it should.
- **Learn early.** The user is told about a problem while there is still time to act, not after a customer notices.
- **Understand the cost.** The user knows why a problem matters to the business, not just what is wrong.
- **Act with direction.** The user knows what to do, and in the order that protects the business most.
- **Confirm the fix.** The user can verify that a resolution held and that the problem did not silently return.
- **Prove health.** The user can demonstrate delivery health to a client, auditor, or board, not merely assert it.
- **Feel confident.** The user carries confidence instead of worry about an unmanaged channel.
- **Manage a portfolio.** The user can see and act on many domains and clients, not one at a time.

## 7. Core User Flows

The following are the journeys the product supports a user through. They are flows of a person's intention, not screens.

- **Becoming aware and onboarded.** A customer who has decided the risk is real moves from adding their first domain, to authorizing the health observation, to reaching a first understandable picture of their delivery. The flow ends when the customer can see their health and knows the next most important thing to do.

- **Answering "is my email working?"** A user asks the single most important question and receives a truthful, legible answer: what the state is, why it is what it is, what it costs, and what to do if it is not healthy.

- **Resolving a discovered problem.** A user is told a problem exists, then understands it, sees its impact, chooses a prioritized action, completes it, and verifies it held. The flow ends with a closed loop: the problem is gone, or the state is honestly known to still be at risk.

- **Responding to a change or alert.** A user is notified of a change or a failure in business language. They understand its severity, know whether to act now, and are given guidance if action is warranted.

- **Reporting health to a client or stakeholder.** A user produces an account of health in business terms that a non-technical person can read and trust. The flow ends when the user can show, not just claim, that delivery is healthy.

- **Managing the portfolio.** An agency user sees the state of every client at a glance, identifies who is at risk, and acts across the portfolio without losing client-by-client visibility.

Each flow ends in a defined outcome for the user. The flows are deliberately few because they are the journeys that matter to the customer.

## 8. Functional Requirements

The product must enable a set of capabilities, each of which produces one or more of the primary user outcomes. The capabilities are:

- **Domain and portfolio management** — add, distinguish, and govern domains and organizations.
- **Health measurement** — produce a unified, legible health signal with a trend.
- **Diagnosis and root cause** — reason across signals to identify the true cause.
- **Business explanation** — express issues in the language of the business.
- **Impact and prioritization** — order issues by risk to the business.
- **Guidance and resolution** — guide a prioritized, verifiable fix.
- **Continuous monitoring and alerting** — maintain health and alert on change in business language.
- **Reporting and evidence** — produce executive summaries and proof of health.
- **Readiness and compliance** — assess readiness against mailbox-provider standards.
- **Feedback and adoption** — capture usefulness and drive improvement.

The complete, engineering-grade functional requirement catalog (requirements D1–F3, grouped by capability) is in the [Functional Requirements](FUNCTIONAL_REQUIREMENTS.md) document.

## 9. Non-Functional Requirements

The product must hold to a defined set of quality attributes — security, reliability, performance, usability, scalability, accessibility, and auditability. The non-negotiables include truth (no fabricated state), safe handling of customer data, no harm to live infrastructure without consent, business-first usability, and full auditability of every reported conclusion.

The complete, engineering-grade non-functional requirements (requirements S1–AU3) are in the [Non-Functional Requirements](NON_FUNCTIONAL_REQUIREMENTS.md) document.

## 10. AI Requirements

AI is core to the product and must explain, prioritize, guide, and assist — while humans remain accountable. AI must never fabricate truth, must state its confidence, must be transparent about its role, and must be reviewable by the user.

The complete AI behavior and accountability requirements (AI1–AI12) are in the [AI Requirements](AI_REQUIREMENTS.md) document.

## 11. Trust & Safety Requirements

- **TS1.** The product must never guess about delivery state; truth is the floor under all trust.
- **TS2.** The product must never mislead the user — no false statuses, no false reassurance, no invented ranks.
- **TS3.** The product must respect the user's data and privacy, with data minimization and clear retention.
- **TS4.** The product must not act on a customer's live system without explicit, informed consent, and any real-world action must be safe and reversible in principle.
- **TS5.** The product must not use deception or dark patterns to drive usage.
- **TS6.** The product must not use AI to present an ungrounded answer as confirmation; grounding is mandatory.
- **TS7.** When a user reports that something is wrong, the product must treat it seriously and surface the issue, not hide it.

## 12. Success Criteria

The success of the product is measured in the outcomes the customer now controls, not in a list of capabilities. The product succeeds when:

- **Truthful confidence.** A customer can answer "Is my email working?" with a truthful, verifiable answer.
- **Early detection.** A customer learns about a problem while there is still time to act, because the product surfaced it before the customer's customers did.
- **Business decisions.** A customer understands why an issue matters and what to do, in order of impact, without needing to translate.
- **Closed loops.** A customer's fix is verified, and the problem does not silently return.
- **A portfolio under control.** An agency can see, act on, and report on the health of every client at once, rather than assume it.
- **The worry lifted.** A customer carries confidence instead of a standing anxiety about the unknown.
- **Truth holds.** Every conclusion the product has given has been grounded, and no fabricated answer was ever presented.

The product holds itself to these outcomes and to the Golden Rule: nothing is built without a validated problem and recurring value.

## 13. Release Strategy

Release is phased by capability and by validation, not by calendar. Each phase is a set of capabilities that together deliver a complete, tested, meaningful step a customer can live with, and no phase ships until it truthfully works.

The detailed phase-by-phase sequencing (Phases A–F and the gating principle) is in the [Release Strategy](RELEASE_STRATEGY.md) document.

## 14. Acceptance Criteria

A requirement is accepted only when it is demonstrated, not merely implemented. For each requirement or its cluster, acceptance requires all of the following:

- **Traces to a problem.** The requirement can be shown to address a real customer problem, and its satisfaction supports a primary user outcome (Section 6). A requirement without a problem is not accepted.
- **Behaves correctly.** The requirement behaves as described in the relevant catalog, including the failure and edge cases, verified with evidence.
- **Is truthful.** Any health indication, explanation, or report the product produces is correct and can be grounded in the data, as tested. A requirement that fails the truth bar is not accepted.
- **Is business-first.** A non-technical user can read and understand the output of the requirement in business terms, as tested. A requirement its own user cannot read is not accepted.
- **Is verifiable.** The requirement's behavior and any conclusion are observable and auditable, so the outcome can be confirmed.
- **Is secure.** The requirement has passed a security review where it touches customer data or the diagnostic path (per the Constitution).
- **Is safe.** The requirement satisfies the trust-and-safety requirements (Section 11).
- **Is approved.** The requirement has passed the feature gate defined in the Constitution and the Master Workflow.

A requirement is deferred or not released when any of these cannot be met. A requirement is accepted only when it behaves correctly and produces the confidence the product promises.

---

*This document defines what the product must achieve, in terms of customer problems and outcomes, at the product and business level. The engineering-grade detail lives in the catalogs it references; each document has exactly one responsibility.*
