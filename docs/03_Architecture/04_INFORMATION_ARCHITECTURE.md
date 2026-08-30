# MailSent — Information Architecture

> The business information architecture of MailSent.
>
> This document defines **what information exists**, **who owns it**, **how it flows**, **when it is authoritative**, and **how it evolves**. It models business information, not software. Information is not a database, not an API, and not a UI — so this document says nothing about tables, interfaces, or screens.
>
> It is subordinate to the [Project Constitution](../00_Foundation/PROJECT_CONSTITUTION.md), the [Master Workflow](../00_Foundation/MAILSENT_MASTER_WORKFLOW.md), and the [Glossary](../00_Foundation/GLOSSARY.md), and is consistent with the [Domain Model](01_DOMAIN_MODEL.md), the [Ubiquitous Language](02_UBIQUITOUS_LANGUAGE.md), and the [Bounded Contexts](03_BOUNDED_CONTEXTS.md). Terminology uses only the Glossary; every information category traces through a context to a requirement and a customer problem.

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
- The information boundaries change

---

## 1. Purpose

Information is the substance of the business. Delivery health, the issues found, the explanations given, the guidance offered, and the proof produced are pieces of information the business keeps, owns, and passes on. When the information is well organized, the product is coherent; when it is not, the business loses track of what is true.

This document is the map of that information. It says, for each kind of information, what it is for, who owns it, who reads it, and when it is the authority. It is the bridge from the business model to whatever software later stores or moves this information, and it stays true whatever that software is.

## 2. Information Architecture Principles

- **Information is owned once.** Every piece of information has one canonical owner, and it is authoritative there. Elsewhere it is read or acted on, never independently revised.
- **Information is business first.** A category is named by its business meaning, not by a storage or interface detail.
- **Information has a source.** Every category traces to a business concept in the Domain Model and to the context that owns it. No orphan information.
- **Truth is the ground.** The most important information is the truthful state of delivery; the value of everything else rests on that truth.
- **Authority is explicit.** A category is authoritative in one place. When it is derived, the derivation is a known, named relationship.
- **Information serves the business.** Information exists to produce the customer's Confidence and protect the Trust of their clients. Information that serves neither does not belong.

## 3. Information Categories

For each category: Purpose, Canonical Owner, Producers, Consumers, Authority Rule.

### 3.1 Identity and Party Information

- Purpose: To know who the participants are — the customer, the organization, the client, and the user.
- Canonical Owner: Identity and Access, with the Organization and Client relationship in Portfolio and Client Management.
- Producers: The customer on account; the agency on a client.
- Consumers: Every context that must know who is acting and over which portfolio.
- Authority Rule: established at creation, changed only by the owner.

### 3.2 Portfolio and Domain Registry Information

- Purpose: To know which domains the customer owns and which an agency holds across its clients.
- Canonical Owner: Portfolio and Client Management.
- Producers: The customer; the agency on a client.
- Consumers: Delivery Observation, Diagnosis, Reporting, Readiness, and every context that acts on a domain.
- Authority Rule: the registry is authoritative; a domain enters it once and is an owned identity.

### 3.3 Delivery Health Information

- Purpose: To know the truth of a domain's delivery — the answer to "is my email working?".
- Canonical Owner: Delivery Observation.
- Producers: Delivery Observation.
- Consumers: Diagnosis, Monitoring, Reporting, Readiness.
- Authority Rule: Observation Evidence is the immutable factual snapshot of what was actually observed (Phase 08 DNS frozen to MX, TXT, SPF, observedAt, and identity; DKIM, DMARC, and alignment are not added); consumers may read it and must not rewrite it. Reverse DNS Observation is the separate immutable reverse-DNS snapshot (MX→A/AAAA→PTR); it is not Observation Evidence, not FCrDNS, and not Health Signal. SMTP Greeting Observation is the separate immutable SMTP greeting/EHLO snapshot (MX from Observation Evidence → A/AAAA → SMTP); it is not Observation Evidence, not Reverse DNS Observation, not HELO/EHLO validation, and not Health Signal. DNS Health Observation is the separate immutable TTL/DNSSEC/propagation snapshot; it is not Observation Evidence, not Health Signal, not Trust Score, and not HEALTH-001. Blacklist Observation is the separate immutable Spamhaus/Barracuda/SURBL/URIBL snapshot; it is not Health Signal and not a listed/not-listed conclusion. DMARC Report Receipt, Parsed DMARC Report, and DMARC Aggregate Evidence are observation-adjacent ingest snapshots owned here; they are not Observation Evidence and not Alignment. HEALTH-001 V1 required persisted inputs are Observation Evidence (DNS/SPF), DMARC Aggregate Evidence (H2 alignment facts), and Reputation Observation — never in-memory DIAG-001 Alignment. The Health Signal remains the later unified indication of delivery health (healthy | at-risk | unknown), produced by HEALTH-001 (the owner) as an append-only snapshot envelope with lineage; mapping healthy vs at-risk is out of scope for the HEALTH-001 freeze. Diagnosis must not fabricate Health Signal. Observation Evidence is not Report / Proof Evidence.

### 3.4 Diagnosis Information

- Purpose: To know why delivery is at risk.
- Canonical Owner: Diagnosis.
- Producers: Diagnosis.
- Consumers: Business Explanation, Impact, Guidance, Reporting.
- Authority Rule: the Root cause is authoritative in Diagnosis; no other context revises it. Diagnosis owns Issue and Root cause. Alignment is a derived/value concept, not an Issue and not a persisted Diagnosis resource. DIAG-001 Alignment derivation is Analyzer-class, from DMARC Aggregate Evidence when INT-003–006 facts exist, not from Health Signal. Diagnosis consumes ingest; it does not own or persist DMARC Report Receipt or DMARC Aggregate Evidence. Diagnosis consumes Health Signal for Issue/root-cause **only when it can state healthy vs at-risk**; an **unknown** envelope does not unblock DIAG-003. Diagnosis must not fabricate Health Signal. HEALTH-001 does not consume DIAG-001 memory.

### 3.5 Business Explanation Information

- Purpose: To make a diagnosis understood in business terms.
- Canonical Owner: Business Explanation.
- Producers: Business Explanation.
- Consumers: Impact and Prioritization, the customer, and the AI.
- Authority Rule: the explanation is authoritative for meaning, not for the underlying cause.

### 3.6 Impact and Prioritization Information

- Purpose: To turn understanding into a decision.
- Canonical Owner: Impact and Prioritization.
- Producers: Impact and Prioritization.
- Consumers: Guidance, Reporting.
- Authority Rule: the Business impact and the Priority are set once by the owner and read by the rest.

### 3.7 Resolution Information

- Purpose: To know the guidance, the action taken, and whether the fix was verified.
- Canonical Owner: Guidance and Resolution.
- Producers: Guidance and Resolution.
- Consumers: Monitoring, Feedback, the customer.
- Authority Rule: the Fix verification is authoritative; it closes or reopens the issue honestly.

### 3.8 Monitoring and Alert Information

- Purpose: To know what is changing and to draw attention.
- Canonical Owner: Monitoring and Alerting.
- Producers: Monitoring and Alerting.
- Consumers: every customer-facing surface.
- Authority Rule: the Alert is owned by the context; it is stated once and not revised by a consumer.

### 3.9 Reporting and Evidence Information

- Purpose: To prove health to a client, an auditor, or a board.
- Canonical Owner: Reporting and Evidence.
- Producers: Reporting and Evidence.
- Consumers: Clients, auditors, boards, the customer.
- Authority Rule: the Report (Evidence / proof of health, Report / Proof Evidence) follows from what it reports and is authoritative for the proof it states. It is not Observation Evidence and does not own or mutate the raw observation snapshot.

### 3.10 Readiness and Compliance Information

- Purpose: To know readiness against a mailbox provider's requirements.
- Canonical Owner: Readiness and Compliance.
- Producers: Readiness and Compliance.
- Consumers: Reporting.
- Authority Rule: Readiness is produced once, against the required standards.

### 3.11 Feedback Information

- Purpose: To improve explanations and guidance over time.
- Canonical Owner: Feedback and Learning.
- Producers: The customer.
- Consumers: Business Explanation, Guidance.
- Authority Rule: Feedback is recorded by the owner and feeds the learning, not the truth state.

### 3.12 Reference and Standards Information

- Purpose: Relatively stable material that helps interpret the rest — a mailbox provider's requirement, a sender's authentication manner, a readiness standard.
- Canonical Owner: the context that uses it (Readiness, Diagnosis).
- Producers: External sources and standards.
- Consumers: the interpreting context.
- Authority Rule: Reference is adopted as the standard, authoritative for what it defines.

## 4. Information Relationships

Information becomes knowledge through a flow of meaning. Each step turns one piece of information into the next.

- **Observation produces truth.** Delivery Observation reads infrastructure and records Observation Evidence, Reverse DNS Observation, SMTP Greeting Observation, DNS Health Observation, Reputation Observation, and Blacklist Observation, records DMARC ingest evidence (Receipt / Parsed / Aggregate) from RUA, then produces the Health Signal **when HEALTH-001 persists a snapshot** (required sources present; indication healthy | at-risk | unknown per interpretation freeze). Health Signal is later interpretation of those facts, not the raw snapshot, and is not produced by Diagnosis. HEALTH-001 V1 cites OE, DMARC Aggregate Evidence, and Reputation Observation; only V15 judgment fields vote; it does not wait on Blacklist / Reverse DNS / SMTP Greeting / DNS Health Observation. DMARC ingest evidence is not Observation Evidence.
- **Diagnosis yields cause.** Diagnosis consumes Health Signal for Issue/root-cause **when it can state healthy vs at-risk**. Alignment (DIAG-001) is Analyzer-class derivation from DMARC Aggregate Evidence, not from Health Signal. Alignment is not an Issue. Diagnosis does not persist ingest. HEALTH-001 does not take DIAG-001 Alignment as a stored input.
- **Explanation turns cause into meaning.** Business Explanation makes the cause understood in business terms.
- **Impact and Priority turns meaning into a decision.** The Impact and the Priority order the issues by risk.
- **Guidance turns a decision into action.** Guidance and Resolution guide the customer to a fix and verify it.
- **Monitoring keeps the truth current.** Monitoring holds the health a state and sends an Alert to the attention of a change.
- **Reporting and Evidence turn state into proof.** Reporting and Evidence give Report / Proof Evidence; Readiness measures against the reference. That proof is not Observation Evidence.
- **History and Feedback become knowledge.** Kept states and the customer's Feedback return as learning and become the knowledge that improves the product.

In every relationship, the information flows so that the outcome is the customer's Confidence and the protection of the Trust of their clients. That is how business information becomes business knowledge.

## 5. Information Lifecycle

Business information passes through stages of life, from creation to retirement.

- **Capture.** Information is created — a domain is added, a health state is observed, a consent is given.
- **Validate and ground.** Information becomes trustworthy and authoritative when it is grounded in a real observation (INV-01, INV-04).
- **Derive.** It is combined into derived knowledge — the Health Trend, the Impact, the Priority, the Alert, the Readiness, the Report.
- **Consume.** It is read by its consumers, who act on it.
- **Maintain.** It is kept current with a return to the source — health is re-observed, a fix is re-verified.
- **Accumulate.** It grows into history — the Health Trend, the Report, the evidence of what happened.
- **Age and retire.** When it is no longer current, the state is closed and what is proof is kept; the obsolete state is retired.

The lifecycle makes sure that information is neither kept forever nor lost: it is created, grounded, consumed, accumulated, and retired in a known way.

## 6. Information Classification

Business information, classified by its role.

- **Authoritative.** Ground truth the business stands on and others trust — the Domain registry, the Health Signal as produced, Consent, the Root cause, Observation Evidence, DMARC Report Receipt, DMARC Aggregate Evidence.
- **Derived.** Information assembled from authoritative sources — the Health Trend, the Impact, the Priority, the Alert, the Readiness, the Report, Alignment (DIAG-001; not persisted).
- **Reference.** Relatively stable material used to interpret — a Provider's requirement, a sender's rule, a readiness standard.
- **Historical.** Past states kept for context and proof — health history, past Reports, evidence of past failures and fixes.
- **Temporary.** Transient information with little lasting value — the in-progress state of a change.
- **Configuration.** What the customer chooses — which domains are observed, how to be alerted, a digest choice, a consent.
- **Knowledge.** The by-now understood model and the learned rule — the knowledge that improves accuracy over time through Feedback and history.
- **Operational.** The working information of this moment — the active alert, the current state, the open issue.

The classification states how a category is treated — what is kept, computed, read, or retired. It is a business decision, not a storage one.

## 7. Information Quality Principles

- **Truth.** Reported health and every reported state must trace to a real observation (INV-01). A piece of information that states a fact without a source is not accepted.
- **One owner.** Each category has one canonical owner and one authoritative state. Duplication is an error.
- **Current.** Information presented as current must be current enough for the decision it serves; a stale state presented as current is a failure.
- **Traceable.** A piece of information must be traceable to the evidence it was drawn from, so the customer can verify it.
- **Minimal and private.** Only the information that serves the business is held, and private data is respected by consent and minimization.
- **Proportionate.** Information present for a decision is sufficient, current, and true for that decision; a false one is not offered.

The quality of the information is the quality of the business, because the customer's Confidence rests on it.

## 8. Information Access Principles

Business ownership and accountability — not a policy about how access is engineered.

- **Ownership.** Every category has one owner of the information, and everything else reads or acts on it.
- **The right context.** A piece of the business is held by the context that owns the corresponding business responsibility; the party who is the legitimate actor is granted the right to read the category it needs.
- **Separation preserved.** The health and data of one organization are separated from another (INV-09), except where the explicit relationship (agency-agency) and its owner allow.
- **Consent governs action.** Any action on a customer's live infrastructure is gated on the customer's Consent (INV-02).
- **The minimum needed.** A consumer receives the information it needs to do its part, and not what it does not need.

These are statements of business ownership and accountability. Whether and how to guarantee them is decided by software, not here.

## 9. Information Evolution Rules

- **Introduce a new category.** A new type of information is introduced only through the architecture. It must belong with a context, map to a domain concept, and trace to a problem. A category that cannot trace is not added.
- **Prevent duplication.** The single-owner rule is the guard. A category that duplicates information owned by another is a defect. One concept must not be copied to a second owner.
- **Retire obsolete information.** When information has served its purpose, it is retired under governance. The history needed for evidence is retained even when the working state is closed.
- **Grow slowly.** The information model is built from the same boundaries as the domain and the contexts; any change to it goes through the same governance as the domain model.

The model must not become sprawl. Every category added is real, owned, and traced to a business reason.

## 10. Dependencies & Successors

### Depends on

- [PROJECT_CONSTITUTION.md](../00_Foundation/PROJECT_CONSTITUTION.md)
- [GLOSSARY.md](../00_Foundation/GLOSSARY.md)
- [REQUIREMENTS_TRACEABILITY_MATRIX.md](../02_Product_Design/REQUIREMENTS_TRACEABILITY_MATRIX.md)
- [01_DOMAIN_MODEL.md](01_DOMAIN_MODEL.md)
- [02_UBIQUITOUS_LANGUAGE.md](02_UBIQUITOUS_LANGUAGE.md)
- [03_BOUNDED_CONTEXTS.md](03_BOUNDED_CONTEXTS.md)

### Feeds into

As a predecessor for the future documents `05_SYSTEM_ARCHITECTURE.md`, `06_AI_ARCHITECTURE.md`, `07_DATA_MODEL.md`, and `08_API_PHILOSOPHY.md`.

## 11. Governance

- **Status.** Draft until the founders review. Only the founders freeze this document after approval.
- **Changes to the information.** Changing a category, an owner, an information type, or a flow is done by written proposal and a review by the founders or the chief architect.
- **Traceability.** A new category must trace through a context to a concept and a problem; an orphan is removed.
- **Authority.** This document is subordinate to the Constitution and the workflow, and consistent with the Glossary and the domain model.
- **Not a decision about technology.** Whether the database, storage, APIs, or deployment are the responsibilities are decided elsewhere, not here.

---

*This document models the business information of MailSent, and no database and no API. It states what information exists, who owns it, and how it flows — so that the value the business creates has a map that the software can later hold.*