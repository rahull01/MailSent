# MailSent — Glossary

> The single authoritative vocabulary of MailSent.
>
> This document assigns one official meaning to every important word the company uses, so that engineers, product managers, AI agents, and every future document share the same language. It prevents terminology drift.
>
> It is subordinate to the [Project Constitution](PROJECT_CONSTITUTION.md) and the [Master Workflow](MAILSENT_MASTER_WORKFLOW.md), and is consistent with every document in `docs/01_Product/` and `docs/02_Product_Design/`. Every definition is grounded in those documents; nothing here is invented.

---

**Document governance**

- **Status:** Draft
- **Version:** 1.0
- **Owner:** Founders
- **Review Status:** Pending Founder Review

After founder approval, update to **Status: Approved** and **Review Status: 🔒 Frozen**. Only the founders may freeze this document.

**Next review trigger:**
- Product philosophy changes
- Terminology changes
- Category changes

---

## 1. Purpose

The purpose of this glossary is to give every term one meaning and to bind every future document to it. Language is how a company stays coherent: when two documents use the same word for different things, or different words for the same thing, the product and the customer's understanding both suffer.

This glossary is the arbiter. When a future document needs a term, it uses the definition here. When a term is missing, it is added here before it is used. When two sources conflict over a term, that conflict is recorded here and left for the founders to decide — never silently resolved.

## 2. Usage Rules

- **One term per concept.** Never use two words for the same thing, and never use one word for two things.
- **Define on first use.** A new document introduces a term by naming it and, if the reader may not know it, linking to this glossary.
- **Use the exact term.** Keep the Glossary's spelling and capitalization; do not invent synonyms.
- **Prefer the product-level term.** In product and requirements documents, use the authoritative term over a more execution-specific one (see the conflicts recorded in Section 10).
- **Do not invent.** If a new term is needed, propose it, add it here, and get it reviewed before using it widely.
- **Report, do not resolve.** When a document uses a term against this glossary, flag it; do not quietly pick a meaning.
- **The Glossary is the source of truth for wording**, not for strategy; strategy lives in the Constitution and the workflow.

## 3. Core Business Terms

**Confidence**
- Definition: The certainty a customer holds that their email reaches the inbox, that infrastructure stays healthy, that problems surface early, and that teams know what to fix.
- Purpose: It is the outcome the company sells; it is the apex of the value hierarchy.
- Not: A feature, a report, or a check.
- Related: Reliability, Trust, Value proposition.

**Reliability**
- Definition: The standard the product itself holds — that its own intelligence is dependable, truthful, observable, and secure.
- Purpose: It is what earns the confidence we sell; it is an internal quality bar, not the thing sold.
- Not: The product's value promise. (The Constitution separates it from confidence deliberately.)
- Related: Confidence, Non-functional requirements.

**Trust**
- Definition: The confidence a business's own customers, clients, and stakeholders place in it, which a missed message can break.
- Purpose: It is the deeper currency, especially for agencies; losing it is the most expensive outcome.
- Not: Confidence (which is what the customer holds about delivery); trust is what their stakeholders hold about them.
- Related: Confidence.

**The Golden Rule**
- Definition: The first governing rule of the company — no feature is approved because it is technically impressive; every feature must solve a validated customer problem and create recurring value.
- Purpose: It is the supreme test every product decision must pass.
- Not: A feature formula (which is execution detail in the workflow).
- Related: Validated customer problem, Recurring value.

**Recurring value**
- Definition: Value a customer receives month after month, such that they would keep paying for it at month twelve.
- Purpose: It distinguishes a platform from a one-time trick, and it is a test in the Golden Rule.
- Not: A single result or a one-off gain.

**Validated customer problem**
- Definition: A real problem a real customer has, confirmed by evidence, not imagined or assumed.
- Purpose: It is the first test of any feature; nothing is built without it.
- Related: The Golden Rule.

**The Kill List**
- Definition: The living record of rejected features, `REJECTED_FEATURES.md`, listing each rejection, its reason, its date, and any condition that would justify revisiting it.
- Purpose: It turns "no" into a discipline and a shared record.
- Not: A list of things we might build later.

## 4. Customer Terms

**Customer**
- Definition: The business or agency that uses MailSent and whose revenue and trust are at stake in delivery.
- Purpose: The focus of every decision; the reason the product exists.
- Not: The end user who receives email (their customer).

**Business decision-maker**
- Definition: A founder, operator, or executive who owns revenue and trust but is not technical.
- Purpose: A primary user type; the product must speak to them in business terms.
- Related: Business language.

**Agency / MSP**
- Definition: A company (digital marketing agency, email marketing agency, or managed service provider) that manages email delivery across many client domains.
- Purpose: A primary customer; the product is proven for them first.
- Related: Portfolio.

**Portfolio**
- Definition: The set of client domains and organizations an agency or multi-domain business is accountable for.
- Purpose: The scale at which the product's value compounds; agencies manage many at once.
- Not: A single domain.

**Business language**
- Definition: Plain, consequence-first expression of a delivery issue — what happened, what it costs, what to do — before any technical mechanism.
- Purpose: The signature of the category; it is how a non-technical person can act.
- Not: Jargon, or leading with a technical term.
- Related: Business decision-maker.

**The black box**
- Definition: The condition of email delivery being invisible to the business that depends on it.
- Purpose: It is the problem the company refuses to accept and the thing the product opens.
- Not: A product state.

**Early detection**
- Definition: Surfacing a problem while there is still time to act, before a customer notices.
- Purpose: A primary user outcome and a core value; early is worth more than correct-but-late.

**Primary / Secondary customer (P0 / P1)**
- Definition: P0 — digital marketing agencies, email marketing agencies, MSPs, and multi-domain SaaS companies, served first. P1 — e-commerce, B2B companies, and startups, served after P0 is solid.
- Purpose: It fixes who the product is built for, in order.
- Not: Invented or additional segments.

## 5. Product Terms

**AI Email Infrastructure Intelligence Platform**
- Definition: The identity of MailSent — software that reasons across everything that determines inbox delivery, explains it in business language, and turns it into a decision the customer can act on.
- Purpose: It is the final identity; the product is never defined otherwise.

**Email infrastructure intelligence (the category)**
- Definition: The software category of reasoning across the whole delivery picture, rather than checking one record or reporting raw state.
- Purpose: It is the category MailSent belongs to and the category MailSent defines.
- Related: Intelligence.

**Delivery / deliverability**
- Definition: Whether email reaches the intended inbox; the health of that condition.
- Purpose: It is what the product protects and what confidence is about.
- Not: Sending the message.

**Email infrastructure**
- Definition: The configuration and signals that determine delivery — authentication, alignment, records, and reputation.
- Purpose: It is what the product observes and reasons across.
- Not: The sending platform.

**Inbox**
- Definition: The destination of a delivered message, as accepted by the recipient's mailbox provider.
- Purpose: Reaching it is the definition of successful delivery.

**Root cause**
- Definition: The underlying cause of a delivery failure, not its symptom.
- Purpose: Diagnosis aims at the root cause; a statement that names a symptom is a failure.
- Related: Diagnosis.

**Prioritization**
- Definition: Ordering issues by the risk they pose to the business, so the customer knows what to do first.
- Purpose: It turns a wall of equal findings into a decision.
- Not: Ordering by technical difficulty or recency.

**Health Signal**
- Definition: The unified, legible indication of a domain's delivery health. This is the canonical product term.
- Purpose: It is how a customer can answer "is my email working?"
- Not: Trust Score, which is a deprecated alias.
- Related: Health Trend, Trust Score (deprecated).

**Health Trend**
- Definition: The time-based representation of Health Signal, showing change (improving, stable, declining) over time.
- Purpose: It expresses how health moves across time.
- Not: The whole Health Signal; it is only the time dimension.
- Related: Health Signal.

**Trust Score (deprecated)**
- Definition: A legacy alias for Health Signal, retained only where historical references require it.
- Purpose: It allows reading of older documents; it is not used in new writing.
- Not: The canonical term.
- Related: Health Signal.

**Capability**
- Definition: What the product enables for a user, grouped by purpose (for example, diagnosis, prioritization, monitoring).
- Purpose: It is the unit used in requirements, chosen to stay independent of implementation.
- Not: A Module, which is a logical grouping containing one or more capabilities.
- Related: Module.

**Module**
- Definition: A logical product grouping or workspace that contains one or more capabilities.
- Purpose: It is the execution-level view of the product's organization.
- Not: A Capability, which is what the product enables.
- Related: Capability.

**Guidance / Guided fix**
- Definition: Step-by-step, prioritized direction a user can act on to resolve an issue.
- Purpose: It moves the customer from knowing to acting.
- Related: Prioritization, Verification.

**Continuous monitoring**
- Definition: Observing delivery health over time, so health is a maintained state rather than a single check.
- Purpose: It is how a fix is verified and how problems are caught early.

**Alert**
- Definition: A notification of a change or failure, stated in business language with severity and whether to act.
- Purpose: It is how the product keeps a customer ahead of a problem.
- Not: Raw technical output.

**Readiness / compliance**
- Definition: Assessment of a domain against the standards set by mailbox providers that decide inbox entry.
- Purpose: It lets a customer demonstrate to an auditor or partner that delivery is acceptable.

**Onboarding**
- Definition: The path from a customer's first use to a working, monitored state, including any configuration needed to begin health observation.
- Purpose: It is how a customer reaches their first answer.

## 6. AI Terms

**AI (in the product)**
- Definition: The capability that reasons across many signals and renders them in business terms; the enabler of the category.
- Purpose: It is why the reasoning at the required scale is possible.
- Not: A marketing label.

**AI explains / prioritizes / guides / assists**
- Definition: The four allowed behaviors of AI in the product.
- Purpose: They bound what AI may do — never decide, never act alone, never act on live systems without consent.
- Related: Human accountability.

**Explainability**
- Definition: The property that a conclusion's underlying evidence and reasoning are apparent when asked "why."
- Purpose: It keeps every conclusion verifiable.

**Grounded / grounding**
- Definition: The requirement that any statement or answer trace to real data; a guess presented as fact is a violation.
- Purpose: It is the condition that keeps an AI-powered confidence product honest.
- Related: AI confidence, Truth.

**AI confidence (indication)**
- Definition: AI's stated certainty accompanying a recommendation or guidance.
- Purpose: It lets a user weigh how much to rely on the recommendation.

**Transparency**
- Definition: The property that AI's role is clear to the user and that the basis for an output can be seen.
- Purpose: It prevents the product from hiding that a model produced a result.

**Human accountability**
- Definition: The rule that a human owns the outcome of anything that affects what a customer believes or decides.
- Purpose: It keeps AI as an assistant, not an authority.

## 7. Trust & Evidence Terms

**Truth / truthful**
- Definition: The property that every reported state traces to a real, observed event, and nothing is fabricated.
- Purpose: It is the floor under all trust and a non-negotiable.
- Related: Grounded.

**Observable / inspectable**
- Definition: The property that every state and failure can be seen, logged, and traced.
- Purpose: It is what makes the product trustworthy.

**Auditability / audit trail**
- Definition: The record that lets any reported conclusion be traced back to the data it was drawn from, and what was reported and when.
- Purpose: It lets a customer verify the product rather than trust it blindly.

**Verification (of a fix)**
- Definition: Confirming that a resolution held and the problem did not silently return.
- Purpose: It closes the loop; an unverified fix is a guess.

**Evidence / proof of health**
- Definition: A grounded demonstration of delivery health a customer can show to a client, auditor, or board.
- Purpose: It lets a customer prove rather than assert.
- Related: Reporting.

**Silent failure**
- Definition: A delivery failure that produces no immediate signal and is discovered, if at all, only after the cost is incurred.
- Purpose: It is the central problem the product exists to end.

**Silent data loss**
- Definition: A loss of reception, storage, or processing that is not detected or surfaced.
- Purpose: It is prohibited; the product must detect and surface any such loss.
- Related: Reliability.

**Consent**
- Definition: Explicit, informed agreement by the customer before any action is taken on their live infrastructure.
- Purpose: It is the required gate for any real-world action; the product never acts on live systems without it.
- Related: Trust and safety.

## 8. Terms We Never Use

These terms are prohibited because they misrepresent the company, the product, or the promise. Each is avoided deliberately.

- **DMARC checker.** It reduces the product to a single-record check, which is the opposite of reasoning across the whole picture. The product is intelligence, not a check.
- **Warmup / warming.** It is reputation manipulation — a shortcut, not confidence, and it is legally gray. The product protects health; it does not game it.
- **Marketing automation platform.** It implies we send campaigns and manage audiences. We do not send; we protect what a business sends.
- **Communication platform.** It implies we own the channel. We do not; we ensure the channel is trustworthy.
- **AI-washing.** It is labeling something as AI that is not genuine, grounded intelligence — a lie about the product.
- **Fabricated rank / forecast.** A rank or forecast we cannot ground in real data is a lie. We present only what real data supports.
- **Guaranteed deliverability.** An absolute promise of inbox delivery overclaims what is real and contradicts the truth principle. We promise confidence and truth, not a guarantee no one can keep.
- **Lock-in.** We do not hold customers through artificial dependency; value is the only reason to stay.
- **Switching cost.** Same reason — we never rely on the difficulty of leaving as a reason to remain.

## 9. Naming Rules

How future documents should introduce and use terminology:

- **Name the term once and correctly.** When a document first uses an important term, write it exactly as in this glossary and link to it.
- **Use one term per concept.** Do not alternate between synonyms; if the document needs variety, vary the sentence, not the term.
- **Prefer the product-level term.** Where a workflow execution term and a product-level term both exist, use the product-level term in product and requirements documents; see the Canonical Naming Table.
- **Check before inventing.** If a needed term is not in the glossary, add it here and get it reviewed before using it broadly.
- **Flag drift.** If a source uses a term differently, note it here rather than quietly reinterpreting it.
- **Keep definitions concise.** A glossary entry states the meaning and purpose; it does not restate the whole product strategy.

## Canonical Naming Table

The preferred terms of the company and the terms to avoid in new writing. Use the Preferred Term; avoid the Avoid term for the reason given.

| Preferred Term | Avoid | Reason |
| --- | --- | --- |
| Health Signal | Trust Score | Trust Score is a deprecated legacy alias; Health Signal is the canonical measure. |
| Health Signal | Health (Trend) as the whole measure | Health Trend is only the time-based representation, not the full indication. |
| Health Trend | Health Signal to mean the trend | Health Signal is the overall indication; use Health Trend for the time series. |
| Capability | Module to mean what is enabled | A Capability is what the product enables; a Module is a grouping. |
| Module | Capability to mean a grouping | A Module is a logical grouping or workspace that contains capabilities. |
| Confidence | Reliability to mean the value sold | Reliability is the product's internal standard; Confidence is the outcome sold. |
| Trust | Confidence interchangeably | Trust is what a customer's stakeholders hold; Confidence is what the customer holds. |

## 10. Governance

This glossary changes deliberately, like the Constitution.

- **Adding a term.** A proposal is written down with the term, its definition, and why it is needed, then reviewed before adoption.
- **Changing a term.** Changes are recorded with what the old meaning was and why it changed.
- **Resolving conflicts.** An open conflict is listed below and resolved only by the founders, with the decision recorded.
- **Freezing.** Only the founders may freeze this document after review.

### Terminology Resolution Record

- **Resolved 2026-08-07 by founder decision:** **Health Signal** is the canonical product term; **Health Trend** is its time-based representation; **Trust Score** is deprecated and kept only as a legacy alias for historical references. **Capability** is what the product enables; **Module** is a logical product grouping or workspace that contains one or more capabilities.
- **Status:** No open terminology conflicts remain.

---

*This glossary is the single source of truth for the company's words. Every future document, engineer, product manager, and AI agent uses the same definitions — and when two sources disagree on a word, this document records it for the founders to settle.*
