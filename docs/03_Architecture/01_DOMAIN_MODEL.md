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
- Responsibility: To be identified and its authentication checked, because alignment depends on who is sending. Sender detection is DIAG-002 (Alignment Intelligence), not DIAG-001.
- Relationships: Sends for a domain; its alignment with the domain determines part of health.
- Not: The domain itself.

**Mailbox provider**
- Definition: The external gatekeeper (for example a recipient's email service) that decides whether a message reaches the inbox.
- Responsibility: To set the standards that readiness is measured against, and to be the arbiter of inbox entry.
- Relationships: Receives messages from domains; its requirements define readiness; its behavior defines whether delivery succeeds.
- Not: Part of the customer's owned infrastructure.

**Health Signal**
- Definition: The unified, legible indication of a domain's delivery health; the canonical measure.
- Responsibility: To answer the customer's question, "is my email working?", in one truthful indication (**healthy** | **at-risk** | **unknown**) assigned by HEALTH-001 from persisted DMARC Aggregate Evidence tokens when required snapshots exist.
- Relationships: Reflects a domain's infrastructure state; carries a Health Trend; is the basis for issues, reports, and alerts.
- Not: Trust Score, which is a deprecated alias; not a raw record; not MAP-002 color invented from observation availability. HEALTH-001 is the authoritative source of this state.

**Health Trend**
- Definition: The time-based representation of a Health Signal, showing improvement, stability, or decline.
- Responsibility: To show how health moves, so that decline is visible before it becomes a failure.
- Relationships: Belongs to a Health Signal; is the basis for early detection.
- Not: The whole Health Signal.

**Reputation**
- Definition: External signals about a domain's sending reputation, as observed where possible.
- Responsibility: To be one later input to health, reflecting how providers perceive the domain.
- Relationships: Belongs to a domain; contributes to its Health Signal when health is later produced. Raw provider reads are recorded as Reputation Observation, not as this concept.
- Not: A manipulated or warm-up number; not Reputation Observation (the persisted raw snapshot).

**Reputation Observation**
- Definition: An immutable, persisted snapshot of one raw external reputation observation (Google Postmaster or Microsoft SNDS) for a registered Domain.
- Responsibility: To hold only the facts actually returned by that provider — source identity, observation instant, and opaque raw payload — so later interpretation can be traced to a real read.
- Relationships: Produced by observing a registered Domain; identified by organization and domain (identity and tenant isolation); owned by Delivery Observation. It is not a Reputation aggregate.
- Not: Health Signal, Trust Score, diagnosis, Finding, Assessment, recommendation, compliance, blacklist result, scores, grades, risk, or Evidence / proof of health.

**Blacklist Observation**
- Definition: An immutable, persisted snapshot of one raw external blacklist observation (Spamhaus, Barracuda, SURBL, or URIBL) for a registered Domain.
- Responsibility: To hold only the facts actually returned by that provider — source identity, observation instant, and opaque raw payload — so later interpretation can be traced to a real read.
- Relationships: Produced by observing a registered Domain; identified by organization and domain (identity and tenant isolation); owned by Delivery Observation. Each provider is a separate fact.
- Not: Health Signal, Trust Score, diagnosis, Finding, Assessment, recommendation, compliance, listed/not-listed conclusion, Reputation Observation, scores, grades, risk, or Evidence / proof of health.

**Alignment**
- Definition: The consistency between who authenticates a message and who the domain says is sending.
- Responsibility: To determine whether a provider accepts a message as legitimate. DIAG-001 derives this as Analyzer-class Alignment (Constitution Analyzer: SPF/DKIM/DMARC parsing + alignment). Real Alignment requires DMARC evidence from INT-003–006; it cannot be computed from Phase 08 DNS TXT. Alignment is a derived value, not an Issue, not a persisted table, and not a Health Signal.
- Relationships: Connects a Sender to a Domain; a misalignment is a common root cause of delivery failure. Alignment itself is not the Issue; DIAG-003 names Root cause when HEALTH-001 exists.
- Not: A configuration file; sender detection (DIAG-002); Root cause; Issue; Observation Evidence; Health Signal; Trust Score.

### Issues and resolution

**Issue**
- Definition: A delivery problem detected on a domain, with a root cause.
- Responsibility: To be the unit of diagnosis and resolution — the thing a customer understands, prioritizes, and fixes. Diagnosis owns Issue and Root cause. Alignment is not an Issue. DIAG-001 does not own Issue lifecycle.
- Relationships: Belongs to a domain; has a root cause, a business impact, a priority, guidance, and a verification state.
- Not: A raw technical finding without meaning; Alignment.

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

**Observation Evidence**
- Definition: An immutable, persisted snapshot of factual data obtained from a real infrastructure observation.
- Responsibility: To hold only the facts actually returned by the observation, so later interpretation can be traced to a real read. Phase 08 DNS is frozen to MX, TXT, SPF, observedAt, and identity; PTR is not Observation Evidence.
- Relationships: Produced by observing a Domain's Email Infrastructure; identified by organization and domain (identity and tenant isolation); consumed later by health, diagnosis, and reporting. Bounded-context ownership is Delivery Observation; the Domain aggregate does not own Observation Evidence.
- Not: Health Signal, Diagnosis, Finding, Assessment, Recommendation, AI conclusion, Report, compliance conclusion, inferred health, fabricated validity, Reverse DNS Observation, SMTP Greeting Observation, DNS Health Observation, or Evidence / proof of health.

**Reverse DNS Observation**
- Definition: An immutable, persisted snapshot of one raw reverse-DNS (PTR) observation for an IP of an observed MX host of a registered Domain.
- Responsibility: To hold only the facts actually returned — identity from the registered Domain, source MX hostname, IP (IPv4 and/or IPv6 when actually available), PTR values as observed, and observedAt — so later interpretation can be traced to a real MX→A/AAAA→PTR read. It does not perform FCrDNS and does not create Health Signal.
- Relationships: Produced by observing a registered Domain; identified by organization and domain (identity and tenant isolation); owned by Delivery Observation as a snapshot separate from Observation Evidence. The caller supplies only the domain name.
- Not: Observation Evidence, SMTP Greeting Observation, FCrDNS, pass/fail, Health Signal, diagnosis, score, deliverability, Trust Score, DNSSEC/TTL, blacklist result, or Evidence / proof of health.

**SMTP Greeting Observation**
- Definition: An immutable, persisted snapshot of one raw SMTP greeting/banner and EHLO capability observation for a resolved IP of an observed MX host of a registered Domain.
- Responsibility: To hold only the facts actually returned — identity from the registered Domain, source MX hostname from Observation Evidence, resolved IP and address family, observedAt, connection/probe outcome, raw SMTP greeting as returned, and raw EHLO response/capabilities if EHLO was actually accepted — so later interpretation can be traced to a real MX→A/AAAA→SMTP read. It is an active probe only against registered-domain MX infrastructure; it does not send mail, authenticate, test relay or delivery, or create Health Signal.
- Relationships: Produced by observing a registered Domain; identified by organization and domain (identity and tenant isolation); owned by Delivery Observation as a snapshot separate from Observation Evidence and Reverse DNS Observation. The caller supplies only the domain name.
- Not: Observation Evidence, Reverse DNS Observation, HELO/EHLO validation, valid/invalid, healthy, deliverability score, open relay finding, security diagnosis, compliance, Health Signal, Trust Score, diagnosis, recommendation, report/proof, mail sending, or Evidence / proof of health.

**DNS Health Observation**
- Definition: An immutable, persisted snapshot of one raw DNS health observation (TTL, DNSSEC-related records, named-resolver propagation) for a registered Domain.
- Responsibility: To hold only the facts actually returned — identity from the registered Domain, observedAt, per-record TTL, DNSKEY/DS presence or unavailability without cryptographic validation, and per-resolver raw answers or unavailability — so later interpretation can be traced to a real DNS-health read. It does not create Health Signal, Trust Score, or HEALTH-001.
- Relationships: Produced by observing a registered Domain; identified by organization and domain (identity and tenant isolation); owned by Delivery Observation as a snapshot separate from Observation Evidence. The caller supplies only the domain name.
- Not: Observation Evidence, Health Signal, Trust Score, HEALTH-001, INF-008, cryptographic DNSSEC validation, pass/fail health, diagnosis, score, recommendation, or Evidence / proof of health.

**Infrastructure Map**
- Definition: The topology read-model of a registered Domain along Domain → DNS → SPF → DKIM → DMARC → Senders → Inbox (MAP-001). MAP-001 remains topology/read-model only.
- Responsibility: To show which of those nodes are observed, not observed, or unavailable from facts that already exist. Observation Evidence supplies DNS and SPF; nodes without a snapshot stay unobserved. It does not store, score, or color infrastructure. MAP-002 is a later color-coded view of HEALTH (not observation availability); MAP-001 OBSERVED/NOT_OBSERVED/UNAVAILABLE must never map directly to G/Y/R. MAP-002 depends on MAP-001 topology plus HEALTH-001 Health Signal and does not calculate or create Health Signal.
- Relationships: Built for one registered Domain; tenant-bound to that Domain's organization; consumes Observation Evidence and may reference Reputation Observation, DNS Health Observation, or Blacklist Observation as evidence behind DNS. Not owned as an aggregate. MAP-002, when later authorized, consumes existing Health Signal from HEALTH-001; it does not own health. MAP-003, when later authorized, is opened by selecting or clicking a MAP-001 node; it does not own health. MAP-004, when later authorized, exports the MAP-001 topology visualization; it does not own health.
- Not: An observation aggregate; a table or graph database; Health Signal; MAP-002–004; color, score, health, or severity. Raw INF-001–008 observations and Trust Score are not MAP-002 color sources.

**MAP-003 (Node detail panel)**
- Definition: A later panel opened by selecting or clicking a MAP-001 node (WF-5 click-to-detail). It may show node identity, type, observation availability/status, and real raw observation facts or evidence references when those facts exist.
- Responsibility: To present raw node metadata and existing observations, not to compute health. MAP-003 must not calculate, create, infer, or own Health Signal. Health meaning and G/Y/R are owned by MAP-002 consuming HEALTH-001. OBSERVED / NOT_OBSERVED / UNAVAILABLE must never be translated into health, severity, warning, critical, or color. NOT_OBSERVED nodes must not receive fabricated details. DKIM, DMARC, Senders, and Inbox currently have no observed facts and must not display invented details. Evidence references must not automatically become arbitrary raw-payload dumps; a detailed evidence-access/API contract is frozen separately before implementation. This freeze implements nothing: no table, Flyway, scheduler, independent backend health service, HTTP API, or frontend. INF-001–008, MAP-001, MAP-002, V1–V11, and HEALTH-001 are unchanged.
- Relationships: Opened from a MAP-001 node for one registered Domain. May read existing Observation Evidence and other raw observations when those facts exist. Depends on MAP-001 topology. Does not depend on inventing Health Signal. MAP-002 owns health color when HEALTH-001 exists.
- Not: Health Signal; MAP-002 G/Y/R; Trust Score; diagnosis; recommendation; compliance; fabricated explanation; a translation of observation availability into health or color; a new aggregate, table, or health service.

**MAP-004 (PNG/SVG export)**
- Definition: A later export of the Infrastructure Map visualization. PNG and SVG are both frozen output formats.
- Responsibility: To export MAP-001 topology as a visualization. MAP-004 is not Phase 16 proof and does not replace EVID-003. It is not Phase 15 PDF/email reporting and must not merge into FUN-R2. Keep it separate from Phase 15/16 reporting and proof. If health colors eventually appear in the export, they come only from MAP-002 consuming real HEALTH-001 Health Signal. Never derive G/Y/R from raw INF-001–008 observations, OBSERVED / NOT_OBSERVED / UNAVAILABLE status, Trust Score, or deprecated scoring. NOT_OBSERVED nodes must not receive fabricated health, facts, or details. MAP-004 must not calculate or create Health Signal. This freeze implements nothing: no table, Flyway, scheduler, graph database, or persistence model; no invented HTTP/API contract; no selected or added rendering/export library; no frontend or backend export code. INF-001–008, MAP-001, MAP-002, MAP-003, V1–V11, and HEALTH-001 are unchanged. MAP-004 remains ⬜.
- Relationships: Exports the MAP-001 topology for one registered Domain. Depends on MAP-001 structure. Any later health color depends on MAP-002 consuming HEALTH-001 Health Signal. Does not depend on inventing Health Signal. Not a Reporting or Proof Evidence artifact.
- Not: Phase 16 proof; EVID-003; Phase 15 PDF/email reporting; FUN-R2; Health Signal; a G/Y/R derivation from raw INF observations, observation availability, Trust Score, or deprecated scoring; fabricated health/facts/details for NOT_OBSERVED nodes; a new aggregate, table, graph store, scheduler, API, or export library.

**Alert**
- Definition: A notification of a change or failure, in business language, with severity and whether to act.
- Responsibility: To keep the customer ahead of a problem, on their chosen channel.
- Relationships: Arises from a Health Signal or an Issue; is directed to a user; reflects a change in the portfolio.
- Not: A raw technical message.

**Report / Evidence**
- Definition: A grounded account of health, in business terms, that can be shown to a client, auditor, or board. This is Evidence / proof of health (Report / Proof Evidence), not Observation Evidence.
- Responsibility: To let the customer prove health rather than assert it.
- Relationships: Derived from Health Signals and resolved Issues; serves a client or stakeholder; is built from real Observation Evidence.
- Not: A marketing document; not Observation Evidence (the raw factual snapshot).

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

**Observation.** A Domain has Email Infrastructure. The product reads that Infrastructure — its authentication, alignment, senders, and reputation — and records Observation Evidence, Reverse DNS Observation (MX hosts → A/AAAA → PTR), SMTP Greeting Observation (MX hosts from Observation Evidence → A/AAAA → SMTP greeting/EHLO), DNS Health Observation (TTL, DNSSEC presence, named-resolver answers), where a provider actually returns a reputation read, Reputation Observation, and where a blacklist provider actually returns a read, Blacklist Observation. These are immutable persisted snapshots of facts actually returned. Health Signal and Health Trend are produced later from those facts. Observation is the spine that produces truth: without a real read of infrastructure, there is no health. Finding and Assessment are not Observation Evidence, Reverse DNS Observation, SMTP Greeting Observation, DNS Health Observation, Reputation Observation, or Blacklist Observation; they are later interpretation, not part of the observation snapshot. Reverse DNS Observation is not FCrDNS and is not Health Signal. SMTP Greeting Observation is not HELO/EHLO validation and is not Health Signal. DNS Health Observation is not Health Signal and is not Trust Score. Blacklist Observation is not Health Signal and is not a listed/not-listed conclusion.

**Diagnosis.** When Health declines (Health Signal produced by HEALTH-001), an Issue arises on the Domain. The Issue has a Root Cause, a Business Impact, and a Priority. Diagnosis connects decline to understanding. That Issue/root-cause path is DIAG-003 and consumes Health Signal **when Java can state healthy vs at-risk per the HEALTH-001 interpretation freeze**; an **unknown** indication does not start DIAG-003. DIAG-001 Alignment derivation does not start from health: it is Analyzer-class derivation from DMARC evidence (INT-003–006), not from Health Signal, and Alignment is not an Issue. Diagnosis must not fabricate Health Signal.

**Resolution.** Guidance is attached to an Issue, the customer acts on it, and Fix Verification closes the issue or keeps it open honestly. Resolution connects understanding to a changed state.

**Watch and reporting.** A Health Signal or an Issue produces an Alert to a User, and Health produces Report/Evidence for a Client or Stakeholder. Readiness/Compliance is assessed against Mailbox Provider requirements. Watch connects state to attention; reporting connects state to proof.

**Outcome.** All of these serve the Customer's Confidence, which in turn protects the Trust that Clients and end customers hold. The chain ends in trust; that is the business's reason.

## Business Relationship Map

A text map of the business relationships. It uses business concepts only; it is not a diagram in any modeling notation.

```
Organization ──owns──> Domain ──has──> Email Infrastructure
     │                        │                │
     │                        ├──identified in──> Observation Evidence, Reverse DNS Observation, SMTP Greeting Observation, DNS Health Observation, Reputation Observation, Blacklist Observation (Delivery Observation owns; Domain/org = identity/tenant only)
     │                        ├──has──> Health Signal ──has──> Health Trend  (later, from Observation Evidence)
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
- **Domain ↔ Observation Evidence:** a domain accumulates many Observation Evidence snapshots over time (append-only; a later observation is a new snapshot). Each snapshot is identified by one organization and one domain. Bounded-context ownership remains Delivery Observation; the Domain aggregate is not the owner.
- **Domain ↔ Reputation Observation:** a domain may accumulate many Reputation Observation snapshots (append-only; Postmaster and SNDS are separate facts; a later read is a new snapshot). Identity and tenant isolation come from the registered Domain. Delivery Observation owns Reputation Observation; it is not a Reputation aggregate.
- **Domain ↔ Blacklist Observation:** a domain may accumulate many Blacklist Observation snapshots (append-only; Spamhaus, Barracuda, SURBL, and URIBL are separate facts; a later read is a new snapshot). Identity and tenant isolation come from the registered Domain. Delivery Observation owns Blacklist Observation; it is not Health Signal.
- **Domain ↔ Reverse DNS Observation:** a domain may accumulate many Reverse DNS Observation snapshots (append-only; a later read is a new snapshot). Each snapshot is identified by one organization and one domain, from the registered Domain. Delivery Observation owns Reverse DNS Observation; it is not Observation Evidence and not Health Signal.
- **Domain ↔ SMTP Greeting Observation:** a domain may accumulate many SMTP Greeting Observation snapshots (append-only; a later read is a new snapshot). Each snapshot is identified by one organization and one domain, from the registered Domain. Delivery Observation owns SMTP Greeting Observation; it is not Observation Evidence, not Reverse DNS Observation, and not Health Signal.
- **Domain ↔ DNS Health Observation:** a domain may accumulate many DNS Health Observation snapshots (append-only; a later read is a new snapshot). Each snapshot is identified by one organization and one domain, from the registered Domain. Delivery Observation owns DNS Health Observation; it is not Observation Evidence, not Health Signal, and not Trust Score.
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

1. **Observation begins.** The Domain is added to health observation, and the organization consents to reading its delivery infrastructure. This is the moment the Domain enters the business's care. The observation produces Observation Evidence, Reverse DNS Observation, SMTP Greeting Observation, and DNS Health Observation — immutable persisted snapshots of the facts actually returned — not a Health Signal, diagnosis, FCrDNS, HELO/EHLO validation, Trust Score, or customer-facing proof.
2. **First health.** Later, the Domain receives its first Health Signal and Health Trend. The customer sees their state and the next most important thing to do.
3. **Diagnosis and guidance.** As health is understood (when HEALTH-001 exists), Issues are diagnosed, explained, prioritized, and guided; the customer acts, and the fix is verified. The loop of understanding and action is open. Alignment (DIAG-001) is Analyzer-class derivation from DMARC evidence when those facts exist; it does not wait on a fabricated Health Signal and does not create an Issue.
4. **Standing confidence.** Health is maintained through continuous observation. Changes surface early, alerts reach the customer, and confidence begins to replace worry.
5. **Evidence and trust.** Reports and readiness statements (Evidence / proof of health) prove health to clients, auditors, and boards. Trust is held and deepened. This proof is not Observation Evidence.
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

The business uses one shared vocabulary. The authoritative definitions of every term are in the [Glossary](../00_Foundation/GLOSSARY.md) and are reused here without redefinition. The terms this model relies on — Customer, Organization, Client, Portfolio, Domain, Observation Evidence, Reverse DNS Observation, SMTP Greeting Observation, DNS Health Observation, Reputation Observation, Blacklist Observation, Infrastructure Map, MAP-003 (Node detail panel), MAP-004 (PNG/SVG export), Health Signal, Health Trend, Issue, Root Cause, Business Impact, Priority, Guidance, Fix Verification, Alert, Report, Evidence / proof of health, Readiness, Consent, Confidence, Trust, Sender, Alignment, Reputation, Mailbox Provider — are all defined there. Any new term must be added to the Glossary before it is used widely; a term used against the Glossary is out of compliance.

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