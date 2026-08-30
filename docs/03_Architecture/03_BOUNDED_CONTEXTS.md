# MailSent — Bounded Contexts

> The definition of the business boundaries of MailSent.
>
> A Bounded Context is a **business boundary**, a **language boundary**, and an **ownership boundary** — the place where a set of the business's concepts is given one consistent meaning and one owner. It is **not** a microservice, a repository, a deployment unit, or a team. This document defines the business boundaries so that every later decision, including any decision about software structure, has a clear business map to read from.
>
> It is subordinate to the [Project Constitution](../00_Foundation/PROJECT_CONSTITUTION.md), the [Master Workflow](../00_Foundation/MAILSENT_MASTER_WORKFLOW.md), and the [Glossary](../00_Foundation/GLOSSARY.md), and is consistent with the [Domain Model](01_DOMAIN_MODEL.md) and the [Ubiquitous Language](02_UBIQUITOUS_LANGUAGE.md). Terminology uses only the Glossary; every context traces through the [Requirements Traceability Matrix](../02_Product_Design/REQUIREMENTS_TRACEABILITY_MATRIX.md) to a requirement area and a customer problem.

---

**Document governance**

- **Status:** Draft
- **Version:** 1.0
- **Owner:** Founders
- **Review Status:** Pending Founder Review

Only the founders may freeze this document after review.

**Next review trigger:**
- Product philosophy changes
- The domain model changes
- The business boundaries change

---

## 1. Purpose

The business is too large to hold in one unbroken vocabulary. A Bounded Context gives a part of the business a stable boundary in which its terms, its rules, and its ownership are clear and consistent. Inside a context, every concept means one thing; across contexts, the boundaries say who owns a concept, who consumes it, and how they relate.

This document makes the business boundaries explicit and durable, so that each context has a single responsibility and an owner, concepts are owned once with predictable consumers, and contexts can evolve independently without tearing the business apart. It defines boundaries, not software.

## 2. Bounded Context Principles

- **High cohesion.** A context is one subject matter with one way of thinking about it. Its responsibilities belong together as parts of one business responsibility.
- **Low coupling.** A context depends on other contexts as little as possible, and only through shared, owned concepts — never through entanglement of its internals.
- **Single business responsibility.** A context has exactly one reason to change: a change in its own subject matter.
- **Independent evolution.** A context can change its own rules without forcing a change elsewhere, as long as its boundaries and its shared concepts stay honored.
- **Language ownership.** Each context owns the meaning of the concepts inside it, and the shared concept owners anchor the rest. No concept has two owners.

These principles keep the boundaries stable, so they survive the details of any later implementation.

## 3. Candidate Contexts

The candidate contexts align with the Domain Model's context preview and the requirement capabilities. Each candidate may stand or merge; the final set is a governance decision. For each: Purpose, Responsibilities, Owns, Does Not Own, Depends On, Success.

### Delivery Observation

- Purpose: To establish the truth about a domain's delivery health.
- Responsibilities: Add a domain to health observation; read its infrastructure; persist Observation Evidence, Reverse DNS Observation, SMTP Greeting Observation, DNS Health Observation, Reputation Observation, and Blacklist Observation as immutable factual snapshots; persist DMARC Report Receipt, Parsed DMARC Report, and DMARC Aggregate Evidence from RUA ingest (INT-003–006); produce and keep its Health Signal and Health Trend. Reverse DNS Observation, SMTP Greeting Observation, DNS Health Observation, and Blacklist Observation have no public HTTP API and no independent scheduler. INF-004 does not yet re-observe DNS Health Observation or Blacklist Observation and is not extended to DMARC ingest. DMARC ingest has no public report-list HTTP API; V1 receive is an inbound webhook (ingest transport only). HEALTH-001 Health Signal has **no public HTTP API**; derivation is explicit in-process (`record`/`derive` by domainName), not scheduled, not INF-004, not INT-006.
- Owns: Observation Evidence, Reverse DNS Observation, SMTP Greeting Observation, DNS Health Observation, Reputation Observation, Blacklist Observation, DMARC Report Receipt, Parsed DMARC Report, DMARC Aggregate Evidence, Health Signal, Health Trend.
- Does Not Own: Report / Proof Evidence (Evidence / proof of health); the meaning of an Issue or an explanation; Alignment as a table; Issues (Diagnosis / DIAG-004); Trust Score; MAP-001–004. HEALTH-001 owns the Health Signal envelope (unified indication + lineage), not Issues. Consumers may read Observation Evidence, Reverse DNS Observation, SMTP Greeting Observation, DNS Health Observation, Reputation Observation, Blacklist Observation, and DMARC Aggregate Evidence; they must not rewrite or mutate them. DMARC ingest evidence is not Observation Evidence. Reputation Observation is not a Reputation aggregate and is not Health Signal. Reverse DNS Observation is not Observation Evidence, not FCrDNS, and not Health Signal. SMTP Greeting Observation is not Observation Evidence, not Reverse DNS Observation, not HELO/EHLO validation, and not Health Signal. DNS Health Observation is not Observation Evidence, not Health Signal, not Trust Score, and not HEALTH-001. Blacklist Observation is not Health Signal and not a listed/not-listed conclusion. MAP-001 remains topology/read-model only. MAP-002 does not calculate or create Health Signal; HEALTH-001 is the authoritative health state. MAP-002 **consumes** Health Signal; HEALTH-001 does not freeze G/Y/R. MAP-003 is WF-5 click-to-detail on a MAP-001 node: raw identity, type, observation availability, and real facts/evidence refs when they exist; it does not calculate, create, infer, or own Health Signal. MAP-004 is export of the Infrastructure Map visualization (PNG and SVG both frozen); it is not Phase 16 proof, not EVID-003, not Phase 15 PDF/email reporting, and must not merge into FUN-R2. MAP-004 does not calculate or create Health Signal. Phase 08 does not implement Health Signal, MAP-002, MAP-003, or MAP-004. HEALTH-001 V1 required persisted inputs are Observation Evidence (V8), DMARC Aggregate Evidence, and Reputation Observation (V9) — not in-memory DIAG-001 Alignment.
- Depends On: none (the first step of the flow).
- Success: a customer can answer "is my email working?" truthfully.

### Diagnosis

- Purpose: to understand why delivery is at risk.
- Responsibilities: derive Analyzer-class Alignment (DIAG-001) from DMARC Aggregate Evidence when INT-003–006 facts exist; when Health Signal exists **and can state healthy vs at-risk (not merely unknown)**, reason across the signals health reflects and identify the Root cause of a delivery failure (DIAG-003). Diagnosis must not fabricate Health Signal. DIAG-001 does not create Health Signal. Diagnosis does not own or persist DMARC ingest.
- Owns: Issue, Root cause.
- Does Not Own: Alignment as a persisted entity or table (Alignment is a derived/value concept); Health Signal; Trust Score; DMARC Report Receipt; Parsed DMARC Report; DMARC Aggregate Evidence; the business explanation, the impact, or the guidance. Alignment is not an Issue. DIAG-001 does not own Issue lifecycle. DIAG-001 is not sender detection (DIAG-002) and not root cause (DIAG-003).
- Depends On: Delivery Observation for Observation Evidence (DNS/SPF facts) and, for Issue/root-cause, Health Signal **when Java can state healthy vs at-risk per the HEALTH-001 interpretation freeze** — an **unknown** indication does not unblock DIAG-003. Alignment (DIAG-001) depends on DMARC Aggregate Evidence (INT-003–006), not on Health Signal and not on Phase 08 DNS TXT inferred DKIM/DMARC. HEALTH-001 does not consume DIAG-001 in-memory Alignment.
- Success: a problem is stated as a real root cause, not a symptom; Alignment, when derived, traces to DMARC Aggregate Evidence.

### Business Explanation

- Purpose: to translate understanding into the language of business.
- Responsibilities: explain an issue in business language, state what happened and why it matters, and offer the deeper technical view one layer beneath.
- Owns: the explanation, and the deeper view.
- Does Not Own: the diagnosis itself or the priority.
- Depends On: Diagnosis.
- Success: a non-technical owner reads an issue and understands it.

### Impact and Prioritization

- Purpose: to turn understanding into a decision.
- Responsibilities: state the business impact of an issue; assign a priority that orders issues by business risk.
- Owns: Business impact, Priority.
- Does Not Own: the guidance a decision leads to.
- Depends On: Business Explanation.
- Success: a customer knows what to do first, in business terms.

### Guidance and Resolution

- Purpose: to move a decision into a resolved state.
- Responsibilities: provide prioritized, step-by-step guidance matched to where the customer's configuration lives; verify that a fix holds.
- Owns: Guidance, Fix verification.
- Does Not Own: the priority of the issue, only the action to resolve it.
- Depends On: Impact and Prioritization, and Monitoring for verification.
- Success: a fix is verified and does not silently return.

### Monitoring and Alerting

- Purpose: to keep watch and to put the customer ahead of a problem.
- Responsibilities: observe health over time, detect change and issues, and send an Alert in business language with severity and whether to act.
- Owns: Alert, the state of continuous observation.
- Does Not Own: the decision to fix.
- Depends On: Delivery Observation.
- Success: a problem surfaces while there is still time to act.

### Reporting and Evidence

- Purpose: to let the customer prove rather than assert.
- Responsibilities: produce recurring executive summaries and a grounded account of health that can be shown to a client, auditor, or board.
- Owns: Report, Evidence / proof of health (Report / Proof Evidence).
- Does Not Own: Observation Evidence; the health it reflects. Consumers of Observation Evidence must not rewrite it. MAP-004 (PNG/SVG export of the Infrastructure Map visualization) is not Report / Proof Evidence, not Phase 16 proof, not EVID-003, and not Phase 15 PDF/email reporting; it must not merge into FUN-R2.
- Depends On: Delivery Observation, and Diagnosis where it states a cause.
- Success: a customer can show proof of a healthy delivery.

### Readiness and Compliance

- Purpose: to show what is required to be accepted.
- Responsibilities: assess a domain's readiness against the requirements of the mailbox providers that decide inbox entry; produce a readiness statement.
- Owns: Readiness.
- Does Not Own: a guarantee of delivery.
- Depends On: Delivery Observation (the state under assessment).
- Success: a customer and an auditor know where readiness stands.

### Portfolio and Client Management

- Purpose: to manage the scale at which the customer owns delivery.
- Responsibilities: hold the domains and organizations and, for an agency, the clients and the portfolio; govern the relationship of domains to organizations.
- Owns: Organization, Client, Portfolio, Domain.
- Does Not Own: the health of a domain.
- Depends On: Identity and Access, to say who is a customer.
- Success: an agency manages many clients at once, each with a visible domain.

### Feedback and Learning

- Purpose: to improve explanations and guidance from use.
- Responsibilities: capture whether an explanation or guidance is useful, and collect what is wrong.
- Owns: Feedback.
- Does Not Own: the explanation itself.
- Depends On: Business Explanation and Guidance.
- Success: quality improves because usefulness is known.

### Identity and Access

- Purpose: to govern who may see and act.
- Responsibilities: hold the users and roles within an organization, and the rules of who may see health and who may make changes.
- Owns: User, Role, Consent.
- Does Not Own: the portfolio or a domain's health.
- Depends On: the definition of an Organization.
- Success: the right person holds the right access, with explicit consent.

### Candidate Summary and Traceability

| Context | Owns | Requirement capability |
| --- | --- | --- |
| Delivery Observation | Observation Evidence, Reverse DNS Observation, SMTP Greeting Observation, DNS Health Observation, Reputation Observation, Blacklist Observation, DMARC Report Receipt, Parsed DMARC Report, DMARC Aggregate Evidence, Health Signal, Health Trend | Health Measurement |
| Diagnosis | Issue, Root cause (Alignment is derived, not owned as persistence) | Diagnosis and Root Cause; DIAG-001 traces to Alignment Engine / H2 |
| Business Explanation | Explanation | Business Explanation |
| Impact and Prioritization | Business impact, Priority | Impact and Prioritization |
| Guidance and Resolution | Guidance, Verification | Guidance and Resolution |
| Monitoring and Alerting | Alert | Continuous Monitoring and Alerting |
| Reporting and Evidence | Report, Evidence / proof of health | Reporting and Evidence |
| Readiness and Compliance | Readiness | Readiness and Compliance |
| Portfolio and Client Management | Organization, Client, Portfolio, Domain | Domain and Portfolio Management |
| Feedback and Learning | Feedback | Feedback and Adoption |
| Identity and Access | User, Role, Consent | Domain and Portfolio Management (access) |

Every candidate traces to a requirement capability and, through it, to a customer problem; none is an orphan.

## 4. Context Relationships

Contexts collaborate through the business words they share, in the natural ordered flow of the product: Delivery Observation reads infrastructure and records Observation Evidence, Reverse DNS Observation, SMTP Greeting Observation, DNS Health Observation, Reputation Observation, and Blacklist Observation, and records DMARC Aggregate Evidence from RUA ingest, then produces a Health Signal **when HEALTH-001 exists**; Diagnosis consumes Health Signal **when it exists (HEALTH-001)** for Issue/root-cause, and derives Alignment (DIAG-001) from DMARC Aggregate Evidence rather than from Health Signal; Diagnosis does not own ingest; Business Explanation turns the issue into business language; Impact and Prioritization sets the Business impact and Priority; Guidance and Resolution acts on the priority and verifies; Monitoring and Alerting watches the health and raises an Alert; Reporting and Evidence reflects health and diagnosis into Report / Proof Evidence; Readiness and Compliance assesses against mailbox requirements; Portfolio and Client Management holds the domains and clients across all of this; Feedback and Learning gathers usefulness back into the explanation; and Identity and Access governs who may act.

The relationships are stated in the business words that pass between contexts — the shared concepts — not as calls, messages, or data. A context reaches another only by the concept it owns, and never into another's internals. The flow is upstream to downstream, and each step yields a fact to the next.

## Business Context Map

A text map of the business contexts and how they relate, in the natural flow of the product.

```
Portfolio & Client Management
  (owns: Domain, Client, Organization, Portfolio)
      │  owns the domains that are observed
      ▼
Delivery Observation ──produces: Observation Evidence, Reverse DNS Observation, SMTP Greeting Observation, DNS Health Observation, Reputation Observation, Blacklist Observation, DMARC Aggregate Evidence, then Health Signal / Health Trend (HEALTH-001)──▶ Diagnosis (Issue/root-cause when Health Signal exists; Alignment from DMARC Aggregate Evidence)
      ▲                                                          │
      │  re-observed for verification                            │  produces: Issue / Root cause (DIAG-003); derives Alignment (DIAG-001) from DMARC Aggregate Evidence, not from Health Signal; does not persist ingest
      │                                                          ▼
Guidance & Resolution ◀─produces: Priority── Impact & Prioritization ◀──produces: explanation── Business Explanation

Delivery Observation ──feeds──▶ Monitoring & Alerting ──produces: Alert
Delivery Observation + Diagnosis ──feed──▶ Reporting & Evidence ──produces: Report / Proof Evidence
Delivery Observation ──feeds──▶ Readiness & Compliance ──produces: Readiness
Business Explanation + Guidance ──feed──▶ Feedback & Learning ──returns: learning
Identity and Access ──governs who may act (User, Role, Consent), across all of the above
```

The map shows the spine — observe, diagnose, explain, prioritize, guide — and the surrounding contexts that watch, prove, and learn from it. Identity governs from the side, not along the spine.

## Context Classification

The strategic value of each context, in one sentence each.

| Context | Classification | Why |
| --- | --- | --- |
| Delivery Observation | Supporting Domain | It is the foundational source of truth, but the scanning it performs is not where MailSent differentiates. |
| Diagnosis | Core Domain | It holds the alignment intelligence and root-cause reasoning that is MailSent's moat. |
| Business Explanation | Core Domain | It is the intelligence signature of the category — the translation that no single-check tool provides. |
| Impact and Prioritization | Supporting Domain | It turns the core into a decision, but it does not itself differentiate the product. |
| Guidance and Resolution | Supporting Domain | It enables action after the core, without being the source of the differentiation. |
| Monitoring and Alerting | Supporting Domain | It maintains health and puts the customer ahead, supporting the core rather than being it. |
| Reporting and Evidence | Supporting Domain | It lets the customer prove health; it is a needed support, not the reason the business exists. |
| Readiness and Compliance | Supporting Domain | It measures against provider requirements and supports the customer; it is not the moat. |
| Portfolio and Client Management | Supporting Domain | It serves agencies at scale, an important support, but not where MailSent wins. |
| Feedback and Learning | Supporting Domain | It improves the core over time but is not itself the differentiation. |
| Identity and Access | Generic Domain | It is well-trodden identity and role governance, not unique to MailSent. |

The classification says where value is concentrated — the Core holds the reason the company exists; the Supporting makes the core usable and complete; the Generic is standard and replaceable. It guides attention, not implementation.

## 5. Shared Concepts

A concept used across contexts must have one canonical owner and named consumers.

| Canonical term | Canonical owner | Consumers | Ownership rule |
| --- | --- | --- | --- |
| Observation Evidence | Delivery Observation | Diagnosis, Monitoring, Reporting, Readiness | the owner produces the raw factual snapshot; consumers read it and must not rewrite or mutate it. Phase 08 DNS is frozen to MX, TXT, SPF, observedAt, and identity. DKIM, DMARC, and alignment are not added. |
| Reverse DNS Observation | Delivery Observation | Diagnosis, Monitoring, Reporting, Readiness | the owner produces the raw reverse-DNS snapshot (MX→A/AAAA→PTR); consumers read it and must not rewrite or mutate it. It is not Observation Evidence, not FCrDNS, and not Health Signal. |
| SMTP Greeting Observation | Delivery Observation | Diagnosis, Monitoring, Reporting, Readiness | the owner produces the raw SMTP greeting/EHLO snapshot (MX from Observation Evidence → A/AAAA → SMTP); consumers read it and must not rewrite or mutate it. It is not Observation Evidence, not Reverse DNS Observation, not HELO/EHLO validation, and not Health Signal. |
| DNS Health Observation | Delivery Observation | Diagnosis, Monitoring, Reporting, Readiness | the owner produces the raw TTL/DNSSEC/propagation snapshot; consumers read it and must not rewrite or mutate it. It is not Observation Evidence, not Health Signal, not Trust Score, and not HEALTH-001. |
| Reputation Observation | Delivery Observation | Diagnosis, Monitoring, Reporting, Readiness | the owner produces the raw Postmaster/SNDS snapshot; consumers read it and must not rewrite or mutate it. It is not Health Signal. |
| Blacklist Observation | Delivery Observation | Diagnosis, Monitoring, Reporting, Readiness | the owner produces the raw Spamhaus/Barracuda/SURBL/URIBL snapshot; consumers read it and must not rewrite or mutate it. It is not Health Signal. |
| Health Signal / Health Trend | Delivery Observation | Diagnosis (Issue/root-cause when Health Signal can state healthy vs at-risk), Monitoring, Reporting, Readiness | HEALTH-001 produces Health Signal (indication assigned from V15 tokens when required snapshots exist). Diagnosis must not fabricate it. DIAG-001 Alignment is not a HEALTH-001 input. HEALTH-002 is trend derived from snapshots later. Consumers read health when it exists. |
| Domain | Portfolio and Client Management | Delivery, Diagnosis, Reporting, Readiness | the owner holds the domain; consumers act on it. |
| Issue / Root cause | Diagnosis | Explanation, Prioritization, Guidance, Monitoring, Reporting | the owner states the cause; consumers read it. Alignment is not an Issue. |
| Alignment | (derived value; no persistence owner) | Diagnosis (DIAG-001 derives it). HEALTH-001 does **not** consume Alignment VO; H2 cites DMARC Aggregate Evidence | Alignment is a derived/value concept; no Alignment table is authorized. DIAG-001 derives it from DMARC Aggregate Evidence when INT-003–006 facts exist, not from Observation Evidence DKIM/DMARC fields (those fields are not added). |
| DMARC Report Receipt | Delivery Observation | Parse/aggregate (INT-004/005) | the owner holds the received raw aggregate payload for a registered Domain; unknown uuid is discarded. Not Observation Evidence. |
| Parsed DMARC Report | Delivery Observation | Aggregate/Store (INT-005), DIAG-001 | the owner holds raw parsed aggregate facts; not Alignment; not OE. |
| DMARC Aggregate Evidence | Delivery Observation | Diagnosis (DIAG-001); HEALTH-001 (H2 alignment facts) | the owner produces the append-only aggregated ingest snapshot; DIAG-001 consumes it and must not persist ingest. HEALTH-001 cites it as the authoritative persisted source for H2 alignment (not DIAG-001 memory, not an Alignment table). Not Observation Evidence. |
| Priority | Impact and Prioritization | Guidance, Reporting | the owner sets the order once. |
| Alert | Monitoring and Alerting | all noticing surfaces | the owner states the alert; all act on it. |
| Report / Proof Evidence | Reporting and Evidence | Delivery, clients, portfolio | the owner produces the customer-facing proof; consumers read it. It is not Observation Evidence. |
| Readiness | Readiness and Compliance | Reporting | the owner draws readiness; no other revises it. |
| User / Role | Identity and Access | Portfolio, and all acting contexts | the identity owner manages who may; others obey. |

An agreed convention governs all shared concepts: **a concept is owned once, and everywhere else it is read or acted on, never revised.** When a shared concept must change, its owner changes it and the consumers reflect the change; no consumer revises the concept itself.

## 6. Context Boundaries

A context starts where a business responsibility begins and ends where it must not act. The principle is that a context does not reach into another's responsibility, and a concept is owned in one place.

- **Delivery Observation** starts at the first infrastructure observation and ends before explaining or judging; it records Observation Evidence, Reverse DNS Observation, SMTP Greeting Observation, DNS Health Observation, Reputation Observation, Blacklist Observation, and DMARC ingest evidence (Receipt / Parsed / Aggregate) and produces health and stops.
- **Diagnosis** starts at a derived health **when Health Signal can state healthy vs at-risk** for Issue/root-cause, and ends at the root cause; it does not translate into a guide. An **unknown** HEALTH-001 envelope does not start DIAG-003. Alignment (DIAG-001) is Analyzer-class derivation from DMARC Aggregate Evidence, not from Health Signal, and does not start the Issue lifecycle. Diagnosis must not fabricate Health Signal and must not persist ingest.
- **Business Explanation** starts at a finding and ends at the plain meaning; it does not decide what is next.
- **Impact and Prioritization** starts at the explanation and ends at the priority; it does not perform the guidance.
- **Guidance and Resolution** starts at the priority and ends at a verified fix; it does not watch over time.
- **Monitoring and Alerting** starts at observation and ends at the alert; it does not resolve the issue.
- **Reporting and Evidence** starts at the observed state and ends at the proof; it does not recommend repairs.
- **Readiness and Compliance** starts at the assessment and ends at the readiness statement; it does not affect the inbox.
- **Portfolio and Client Management** starts at ownership and ends at membership; it does not measure health.
- **Feedback and Learning** starts at the received explanation and ends at the learning it drives; it does not rewrite the explanation in place.
- **Identity and Access** starts at a person and ends at the action they may take; it does not own the domain's health.

## 7. Integration Principles

The principles (business-level, conceptually) of how contexts work together.

- **Upstream / Downstream.** A context that produces a fact for others is upstream; the one that takes the fact and acts is downstream. Delivery Observation is upstream of Diagnosis, which is upstream of Guidance, and so on. The upstream owns the fact.
- **Stability of the upstream.** Because a change to an upstream context reaches its downstream, the upstream is expected to be stable, or to signal clearly when it changes.
- **Published language.** The shared concepts a context exposes are its published language — the set of named, stable terms from the Glossary. A context's internal language is theirs, not published.
- **Anti-corrosion layer.** Where a downstream meets an upstream concept that is not naturally its own, it protects itself from being pulled into the upstream's vocabulary by translating at the boundary rather than adopting it.

These are statement of business principle. They are not brought to bear by any queue or API.

## 8. Context Evolution Rules

- **When to create a new context.** Create one when a part of the business can be stated as a single responsibility with its own language and its own owner, needs to evolve independently, and is traceable. If it cannot be independently evolving and owned, it does not become a context.
- **When to extend.** Extend an existing context only when the new matter belongs to what it already owns — when it is more of the same responsibility, not other responsibility overriding the boundary.
- **Prevent context sprawl.** The number of contexts stays low through the rules:
  1. Every context demonstrates cohesion — one business subject with the owner.
  2. A new concept that is shared needs a single owner; an unowned "common" concept is an error.
  3. A context is only split when it proves a single cohesive responsibility; an aesthetic split is rejected.
  4. Any change to the set of contexts goes through the governance (Section 10) and is founder-reviewed.

The number of contexts is kept meaningful — each one is real and has an owner — never causing drift and never a reason to multiply the architecture.

## 9. Dependencies & Successors

### Depends on

- [PROJECT_CONSTITUTION.md](../00_Foundation/PROJECT_CONSTITUTION.md)
- [MAILSENT_MASTER_WORKFLOW.md](../00_Foundation/MAILSENT_MASTER_WORKFLOW.md)
- [GLOSSARY.md](../00_Foundation/GLOSSARY.md)
- [REQUIREMENTS_TRACEABILITY_MATRIX.md](../02_Product_Design/REQUIREMENTS_TRACEABILITY_MATRIX.md)
- [01_DOMAIN_MODEL.md](01_DOMAIN_MODEL.md)
- [02_UBIQUITOUS_LANGUAGE.md](02_UBIQUITOUS_LANGUAGE.md)

### Feeds into
A predecessor for the future documents `04_INFORMATION_ARCHITECTURE.md`, `05_SYSTEM_ARCHITECTURE.md`, `06_AI_ARCHITECTURE.md`, `07_DATA_MODEL.md`, and `08_API_PHILOSOPHY.md`.

## 10. Governance

- **Status.** Draft until the founders review. Only the founders freeze this document after approval.
- **Changes to the boundary.** Changing a context, a boundary, a shared concept, or the owner, through a written proposal and a founder-level review.
- **Set of contexts.** Adding or removing a context is a candidate decision under Section 8, made by governance.
- **Traceability.** Every context traces to a requirement and a problem; an orphan is removed.
- **Monolith or separate.** Whether the contexts are realized as a single monolith or divided is a separate, later decision; it is not decided here.
- **Next review trigger.** A change in the domain model, the requirements set, or a business decision.

---

*This document defines the business boundaries of MailSent. It proposes no microservice, no database, and no API — it defines the language and the ownership that the later software will inhabit.*