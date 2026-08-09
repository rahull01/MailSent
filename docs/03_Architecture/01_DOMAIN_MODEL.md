# MailSent — Business Domain Model

> The model of the business, not of the software.
>
> This document models the concepts of MailSent's business — what they are, what they are responsible for, and how they relate — in business language. It is **not** a database schema, an API design, a class diagram, or a technology plan. If the technology changed completely, this model would still be true.
>
> It is subordinate to the [Project Constitution](../00_Foundation/PROJECT_CONSTITUTION.md), the [Master Workflow](../00_Foundation/MAILSENT_MASTER_WORKFLOW.md), and the [Glossary](../00_Foundation/GLOSSARY.md), and is consistent with every product and product-design document. Terminology follows the Glossary. Every concept here traces to a validated customer problem and a requirement area in the [Requirements Traceability Matrix](../02_Product_Design/REQUIREMENTS_TRACEABILITY_MATRIX.md).

---

**Document governance**

- **Status:** Draft
- **Version:** 1.0
- **Owner:** Founders
- **Review Status:** Pending Founder Review

Only the founders may freeze this document after review.

**Next review trigger:**
- Product philosophy changes
- The business model changes
- The requirement structure changes

---

## 1. Purpose

A business domain model is a shared, unambiguous description of what the business is about. It answers, for each important business concept, three questions: **what it is**, **what it is responsible for**, and **how it relates to the other concepts**. It is the language and the map that engineering, product, and every future decision draw from.

The model exists for two reasons. First, to make the business's thinking explicit and stable, so that a decade from now the same concepts mean the same thing. Second, to give software a target: software is a faithful embodiment of a business model, and it cannot be faithful if the model is not first agreed. This document is that agreement — before any line of software.

## 2. Domain Modeling Principles

- **Model the business, not the machinery.** The concepts are things of the business: a domain, its health, an issue, a client. They are not tables, services, or channels.
- **Business language only.** Relationships are described in the words a business owner would use, not in the vocabulary of a database or an event bus.
- **One concept, one meaning.** The Glossary is the authority; this model reuses it and never redefines it.
- **Grounded in trace.** Every concept must be traceable to a validated customer problem and a requirement area. A concept that cannot be traced does not belong.
- **Timeless.** The model describes the business as it is and will be; it does not describe a current technology.
- **Confidence is the product.** The model is organized around producing the customer's confidence in delivery; every concept serves that end.
- **Truth is non-negotiable.** The model includes the rule that every reported state is grounded; it is part of the business, not a software detail.

## 3. Core Business Concepts

For each concept: Definition, Responsibility, Relationships, and Not.

### Identity and boundaries

**Customer**
- Definition: The business or agency that uses MailSent and whose revenue and trust depend on delivery.
- Responsibility: To be the beneficiary of the confidence the product produces, and the party who gives consent for any action on live infrastructure.
- Relationships: Holds an organization; is served by users; is accountable to its own clients and customers.
- Not: The end recipient of an email.

**Organization**
- Definition: A distinct business entity that owns domains and is served as a unit.
- Responsibility: To own its domains and their delivery health as a single scope, separate from every other organization.
- Relationships: Owns domains; holds users; may be a client of another organization in an agency context.
- Not: A product tenant or workspace (that is the software view, not the business view).

**User / Role**
- Definition: A person acting within an organization — agency owner, operator, analyst, decision-maker, or a role that views health.
- Responsibility: To act on health, decide, guide, and own the outcome of any conclusion they act on.
- Relationships: Belongs to an organization; acts on its domains and portfolio; carries a role that bounds what they may see and do.
- Not: A login or an account record.

**Client**
- Definition: An organization that an agency serves; the relationship through which an agency is accountable for delivery.
- Responsibility: To be the party whose delivery and trust the agency protects, and to whom health is proven.
- Relationships: Is managed by an agency; owns domains; is part of the agency's portfolio.
- Not: The end customer of a business.

**Portfolio**
- Definition: The set of client domains and organizations an agency is accountable for.
- Responsibility: To be the scale at which the agency sees health, prioritizes risk, and proves delivery.
- Relationships: Contains client organizations and their domains; is owned by the agency.
- Not: A single domain.

### Delivery health

**Domain**
- Definition: A sender identity whose delivery health the product observes and protects.
- Responsibility: To be the unit of observation — the thing whose health is measured, whose issues are diagnosed, and whose delivery is reported.
- Relationships: Owned by an organization; has a Health Signal; is observed across its infrastructure; relates to senders and mailbox providers.
- Not: The sending platform or the content of a message.

**Email infrastructure**
- Definition: The configuration and signals that determine a domain's delivery — authentication, alignment, records, and reputation.
- Responsibility: To be the reality the product reads and reasons across to judge health.
- Relationships: Belongs to a domain; is assessed to produce its Health Signal; sits partly with the domain and partly with external providers.
- Not: The product's own software.

**Sender**
- Definition: A recognized party (for example an email service provider) that sends on behalf of a domain.
- Responsibility: To be identified and its authentication checked, because alignment depends on who is sending.
- Relationships: Sends for a domain; its alignment with the domain determines part of health.
- Not: The domain itself.

**Mailbox provider**
- Definition: The external gatekeeper (for example a recipient's email service) that decides whether a message reaches the inbox.
- Responsibility: To set the standards that readiness is measured against, and to be the arbiter of inbox entry.
- Relationships: Receives messages from domains; its requirements define readiness; its behavior defines whether delivery succeeds.
- Not: Part of the customer's owned infrastructure.

**Health Signal**
- Definition: The unified, legible indication of a domain's delivery health; the canonical measure.
- Responsibility: To answer the customer's question, "is my email working?", in one truthful indication.
- Relationships: Reflects a domain's infrastructure state; carries a Health Trend; is the basis for issues, reports, and alerts.
- Not: Trust Score, which is a deprecated alias; not a raw record.

**Health Trend**
- Definition: The time-based representation of a Health Signal, showing improvement, stability, or decline.
- Responsibility: To show how health moves, so that decline is visible before it becomes a failure.
- Relationships: Belongs to a Health Signal; is the basis for early detection.
- Not: The whole Health Signal.

**Reputation**
- Definition: External signals about a domain's sending reputation, as observed where possible.
- Responsibility: To be one input to health, reflecting how providers perceive the domain.
- Relationships: Belongs to a domain; contributes to its Health Signal.
- Not: A manipulated or warm-up number.

**Alignment**
- Definition: The consistency between who authenticates a message and who the domain says is sending.
- Responsibility: To determine whether a provider accepts a message as legitimate.
- Relationships: Connects a Sender to a Domain; a misalignment is a common root cause of delivery failure.
- Not: A configuration file.

### Issues and resolution

**Issue**
- Definition: A delivery problem detected on a domain, with a root cause.
- Responsibility: To be the unit of diagnosis and resolution — the thing a customer understands, prioritizes, and fixes.
- Relationships: Belongs to a domain; has a root cause, a business impact, a priority, guidance, and a verification state.
- Not: A raw technical finding without meaning.

**Root cause**
- Definition: The underlying cause of an issue, not its symptom.
- Responsibility: To be what diagnosis aims at, so that resolution addresses the cause and not a symptom.
- Relationships: Explains an Issue; is the basis for the business explanation.
- Not: A symptom.

**Business impact**
- Definition: What an issue threatens — revenue, trust, or a client relationship — in business terms.
- Responsibility: To make an issue a business decision, not a technical curiosity.
- Relationships: Attaches to an Issue; is the basis for its priority.
- Not: A technical severity rating.

**Priority**
- Definition: The order in which issues should be addressed, by business risk.
- Responsibility: To tell the customer what to do first.
- Relationships: Assigned to an Issue based on its business impact.
- Not: An order by recency or difficulty.

**Guidance / Guided fix**
- Definition: Step-by-step, prioritized direction a customer can act on to resolve an issue.
- Responsibility: To move the customer from knowing to acting, with steps matched to where their configuration lives.
- Relationships: Belongs to an Issue; leads to a fix; ends in verification.
- Not: A single recommendation without steps.

**Fix verification**
- Definition: Confirmation that a resolution held and the issue did not return.
- Responsibility: To close the loop, so a fix is not a guess.
- Relationships: Attaches to an Issue after guidance is acted on; updates health.
- Not: An assumption.

### Monitoring and evidence

**Alert**
- Definition: A notification of a change or failure, in business language, with severity and whether to act.
- Responsibility: To keep the customer ahead of a problem, on their chosen channel.
- Relationships: Arises from a Health Signal or an Issue; is directed to a user; reflects a change in the portfolio.
- Not: A raw technical message.

**Report / Evidence**
- Definition: A grounded account of health, in business terms, that can be shown to a client, auditor, or board.
- Responsibility: To let the customer prove health rather than assert it.
- Relationships: Derived from Health Signals and resolved Issues; serves a client or stakeholder; is built from real observations.
- Not: A marketing document.

**Readiness / Compliance**
- Definition: Assessment of a domain against the standards set by the mailbox providers that decide inbox entry.
- Responsibility: To show whether a domain meets the requirements to be accepted, and what that means for the business.
- Relationships: Measured against Mailbox Provider requirements; produces a readiness statement.
- Not: A guarantee of delivery.

**Feedback**
- Definition: A customer's indication of whether an explanation or guidance was useful, or whether something was wrong.
- Responsibility: To improve the quality of explanations and guidance over time.
- Relationships: Attaches to an explanation or a piece of guidance; reflects on the product's quality.
- Not: A support ticket.

### Trust and safety

**Confidence**
- Definition: The certainty a customer holds that their email reaches the inbox, infrastructure is healthy, and problems surface early.
- Responsibility: To be the outcome the whole model produces — the reason the customer changes.
- Relationships: Is the result of truth, early detection, and proof of health across all the concepts above.
- Not: A feature or a report.

**Trust**
- Definition: The confidence a customer's own clients and stakeholders hold in them, which a missed message can break.
- Responsibility: To be the deeper currency the product protects, especially for agencies.
- Relationships: Is protected by the customer's confidence; is held by Clients and end customers.
- Not: Confidence, which the customer themselves holds.

**Consent**
- Definition: Explicit, informed agreement by the customer before any action is taken on their live infrastructure.
- Responsibility: To be the required gate for any real-world action; no action happens without it.
- Relationships: Attaches to the Customer and to any action on a Domain's live infrastructure.
- Not: An implied permission.

## 4. Concept Relationships

The concepts relate to each other in the business's natural terms.

**Ownership.** An Organization owns Domains. A Client is an Organization served by an Agency. An Agency owns a Portfolio, which contains Clients and their Domains. Ownership is the spine: it tells who is responsible for what.

**Observation.** A Domain has Email Infrastructure. The product reads that Infrastructure — its authentication, alignment, senders, and reputation — to produce the Domain's Health Signal and Health Trend. Observation is the spine that produces truth: without a real read of infrastructure, there is no health.

**Diagnosis.** When Health declines, an Issue arises on the Domain. The Issue has a Root Cause, a Business Impact, and a Priority. Diagnosis connects decline to understanding.

**Resolution.** Guidance is attached to an Issue, the customer acts on it, and Fix Verification closes the issue or keeps it open honestly. Resolution connects understanding to a changed state.

**Watch and reporting.** A Health Signal or an Issue produces an Alert to a User, and Health produces Report/Evidence for a Client or Stakeholder. Readiness/Compliance is assessed against Mailbox Provider requirements. Watch connects state to attention; reporting connects state to proof.

**Outcome.** All of these serve the Customer's Confidence, which in turn protects the Trust that Clients and end customers hold. The chain ends in trust; that is the business's reason.

## Business Relationship Map

A text map of the business relationships. It uses business concepts only; it is not a diagram in any modeling notation.

```
Organization ──owns──> Domain ──has──> Email Infrastructure
     │                        │                │
     │                        ├──has──> Health Signal ──has──> Health Trend
     │                        │                │
     │                        ├──has──> Issue ──has──> Root Cause
     │                        │                │          │
     │                        │                ├──has──> Business Impact
     │                        │                ├──has──> Priority
     │                        │                └──has──> Guidance ──leads to──> Fix Verification
     │                        │
     │                        └──relates to──> Sender, Mailbox Provider, Reputation, Alignment
     │
     ├──has──> User / Role
     │
     └──(Agency)──has──> Portfolio ──contains──> Client ──owns──> Domain
                                             │
                                             └──receives──> Report / Evidence

Health Signal ──triggers──> Alert ──delivered to──> User
Domain ──measured against──> Readiness / Compliance ──set by──> Mailbox Provider
Explanation and Guidance ──receive──> Feedback

All of the above ──produce──> Confidence ──protects──> Trust (of Clients and end customers)
```

## Concept Cardinality

The business relationships in quantity, in business language — not database terminology.

- **Organization ↔ Domain:** one organization owns many domains; a domain is owned by exactly one organization.
- **Organization ↔ User:** one organization has many users; a user belongs to exactly one organization. The signup lifecycle establishes this single membership: a new user's registration creates its one Organization and assigns the user to it. The Organization is the ownership boundary (INV-09/13).
- **Agency ↔ Client:** one agency serves many clients; a client is served by exactly one agency.
- **Portfolio ↔ Client:** one portfolio contains many clients; a client belongs to exactly one portfolio (its agency's).
- **Domain ↔ Health Signal:** a domain has one Health Signal at a time; a Health Signal belongs to exactly one domain.
- **Health Signal ↔ Health Trend:** a Health Signal carries one Health Trend; a Health Trend reflects exactly one Health Signal.
- **Domain ↔ Issue:** a domain has many issues over time; an issue belongs to exactly one domain.
- **Issue ↔ Root Cause / Impact / Priority:** an issue has one root cause, one business impact, and one priority.
- **Issue ↔ Guidance:** an issue has many guidance actions over its life, ending in one verification.
- **Domain ↔ Sender:** a domain relates to many senders; a sender may send for many domains.
- **Domain ↔ Mailbox Provider:** a domain is assessed against the requirements of many mailbox providers; a provider sets standards for many domains.
- **Issue ↔ Alert:** an issue or a health change produces alerts to many users; a user receives many alerts.
- **Report ↔ Client:** a report or evidence is produced for one domain or portfolio and is shared with one or many clients or stakeholders; a client receives many reports.
- **Organization ↔ Consent:** consent is granted by one organization and gates many actions on its live infrastructure.

## 5. Domain Boundaries

**Inside the MailSent domain**

- Delivery health and its measurement (the Health Signal and its components).
- Diagnosis, root cause, business impact, and prioritization.
- Guidance, resolution, and fix verification.
- Continuous monitoring, change detection, and alerting.
- Reporting, evidence, and readiness/compliance assessment.
- Portfolio and multi-client management for agencies.
- Feedback and adoption.
- The trust-and-safety rules that govern all of the above.

**Explicitly outside the MailSent domain**

- Sending messages (composing or delivering email).
- The end recipients' mailboxes and their internal behavior.
- General business communication.
- Reputation manipulation or warming.
- The mailbox providers' internal operations (the product observes their requirements and outcomes, not their internals).
- The customer's live infrastructure configuration as something the product owns or changes without consent.
- Any prediction or rank not grounded in real data.

The boundary is the same as the product boundary in the requirements: MailSent reasons about delivery and its outcomes; it does not own the channel or take unconsented action.

## 6. Aggregate Candidates

These are DDD candidates only — the consistency boundaries the business reasoning suggests. They are named conceptually, not as software.

- **Organization** — the boundary around a business entity, its Domains, its Users, and (for an agency) its Clients and Portfolio. Consistency here means: one organization's data and health never bleed into another's.
- **Domain Health** — the boundary around a single Domain, its Infrastructure, its Health Signal, its Issues, and its verification. Consistency here means: a Domain's reported health is always grounded and never self-contradictory.
- **Issue** — the boundary around one Issue, its Root Cause, its Impact, its Priority, its Guidance, and its verification state. Consistency here means: an Issue is not resolved unless verified.
- **Client Relationship** — the boundary around an agency's relationship with one Client and that Client's domains. Consistency here means: what the agency reports to a Client is grounded in that Client's real health.

These candidates are suggestions from the business model, not an implementation decision.

## 7. Business Events

Only business events — things that happen in the business's world, not in its infrastructure.

- A domain is added to health observation.
- A domain's Health Signal changes, or its Health Trend declines.
- An issue is discovered on a domain.
- A root cause is identified for an issue.
- A business impact and priority are assigned to an issue.
- Guidance is acted on by the customer.
- A fix is verified (or fails verification).
- An issue recurs after being considered resolved.
- An alert is sent to a user about a change or failure.
- A client's delivery health declines or recovers.
- A readiness/compliance assessment changes.
- A report or evidence is produced and shared with a client or stakeholder.
- Consent is granted or withdrawn by the customer.
- A user records feedback on an explanation or guidance.

Each event is a change in the business that the model must keep true; it is not a message to a queue.

## Domain Lifecycle

The lifecycle of a Domain, from the first observation to a standing state of confidence. It is a cycle, not a line.

1. **Observation begins.** The Domain is added to health observation, and the organization consents to reading its delivery infrastructure. This is the moment the Domain enters the business's care.
2. **First health.** The Domain receives its first Health Signal and Health Trend. The customer sees their state and the next most important thing to do.
3. **Diagnosis and guidance.** As health is understood, Issues are diagnosed, explained, prioritized, and guided; the customer acts, and the fix is verified. The loop of understanding and action is open.
4. **Standing confidence.** Health is maintained through continuous observation. Changes surface early, alerts reach the customer, and confidence begins to replace worry.
5. **Evidence and trust.** Reports and readiness statements prove health to clients, auditors, and boards. Trust is held and deepened.
6. **Continuous renewal.** The loop returns to observation: health is re-read, re-diagnosed, and re-verified. Confidence is earned each day, not achieved once.

The lifecycle does not end in a finished state. It ends in a maintained state — a domain that is known, watched, and trusted — which is exactly the outcome the business sells.

## 8. Business Invariants

Timeless business rules that must always hold.

- **INV-01 — Truth.** A Domain's reported health and every reported state must trace to a real observation. No fabricated state is ever reported.
- **INV-02 — Consent.** No action is taken on a customer's live infrastructure without explicit, informed consent.
- **INV-03 — Closure.** An Issue is resolved only when its fix is verified; an unverified fix leaves the issue honestly open.
- **INV-04 — Grounding.** Every explanation, impact, and priority is grounded in the real diagnosis; a conclusion that cannot be verified is never presented as fact.
- **INV-05 — Single health.** A Domain has one Health Signal at a time, and it is never both healthy and at risk in the same observation.
- **INV-06 — Confidence and trust.** The product sells Confidence, not reliability or technology; and it protects the Trust of clients and customers.
- **INV-07 — Human accountability.** A human owns the outcome of any conclusion that affects what a customer believes or decides.
- **INV-08 — Identity.** MailSent is never a sender, a single-check tool, a reputation game, or a communication platform.
- **INV-09 — Isolation.** One organization's health and data never mix with another's.

## 9. Ubiquitous Language Preview

The business uses one shared vocabulary. The authoritative definitions of every term are in the [Glossary](../00_Foundation/GLOSSARY.md) and are reused here without redefinition. The terms this model relies on — Customer, Organization, Client, Portfolio, Domain, Health Signal, Health Trend, Issue, Root Cause, Business Impact, Priority, Guidance, Fix Verification, Alert, Report, Readiness, Consent, Confidence, Trust, Sender, Alignment, Reputation, Mailbox Provider — are all defined there. Any new term must be added to the Glossary before it is used widely; a term used against the Glossary is out of compliance.

## Context Map Preview

The following are candidate bounded contexts — the future homes of the business concepts, identified here and not yet explained or designed. A future context map will explore and define them.

- **Delivery Observation**
- **Diagnosis**
- **Business Explanation**
- **Impact and Prioritization**
- **Guidance and Resolution**
- **Monitoring and Alerting**
- **Reporting and Evidence**
- **Portfolio and Client Management**
- **Readiness and Compliance**
- **Feedback and Learning**
- **Identity and Access**

These names are a preview only. They are listed to record that these areas exist in the business, and nothing more.

## 10. Governance

- **Changes to the model.** A change is written down with its reason and reviewed before adoption; the model is not silently altered.
- **Traceability.** Every concept must trace to a validated problem and a requirement area. A concept that cannot be traced is removed.
- **Alignment.** The model is subordinate to the Constitution and the Master Workflow, and must remain consistent with the Glossary and the requirement set.
- **Timelessness.** The model describes the business, not the technology; a change in technology does not change the model.
- **Status.** Draft until the founders review; only the founders freeze this document after approval.

---

*This document models the business of MailSent — its concepts, their responsibilities, and their relationships — in business language. It proposes no table, no API, and no service; it sets the truth the software will later embody.*