# MailSent — System Architecture (Logical)

> The logical architecture of MailSent — how the system is organized as business responsibilities.
>
> This document explains **how the system is organized logically**, not how it is deployed, not its infrastructure, and not its codebase. A logical component is a responsibility; it is **not** a microservice, a repository, or a framework module. If the technology changed completely, this architecture would still be true.
>
> It is subordinate to the [Project Constitution](../00_Foundation/PROJECT_CONSTITUTION.md), the [Master Workflow](../00_Foundation/MAILSENT_MASTER_WORKFLOW.md), and the [Glossary](../00_Foundation/GLOSSARY.md), and is consistent with the [Domain Model](01_DOMAIN_MODEL.md), the [Ubiquitous Language](02_UBIQUITOUS_LANGUAGE.md), the [Bounded Contexts](03_BOUNDED_CONTEXTS.md), and the [Information Architecture](04_INFORMATION_ARCHITECTURE.md). Terminology uses only the Glossary; every responsibility traces through a context to a requirement and a customer problem.

---

**Document governance**

- **Status:** Draft
- **Version:** 1.0
- **Owner:** Founders
- **Review Status:** Pending Founder Review

Only the founders may freeze this document after review.

**Next review trigger:**
- Product philosophy changes
- The domain model or the information architecture changes
- The system responsibilities change

---

## 1. Purpose

An architecture is the map of responsibilities: what each part of the system is for, what it owns, and how the parts work together. MailSent's system is organized as a set of logical components, each holding one business responsibility. This document defines those components, how they interact, where AI sits, where trust moves, and how the whole behaves when a part cannot do its job.

It is a logical architecture. It says what the system must be able to do and how its responsibilities relate, not which technology realizes them.

## 2. System Architecture Principles

- **Business-first.** The system is organized by business responsibility, not by technology or by deployment. A component's reason to exist is a business reason.
- **Trust-first.** The system is built so that the customer's Confidence rests on truth. The most important component is the one that produces the truth of delivery, and everything else serves it.
- **Explainability.** The system must be able to account for what it reports. It is organized so that every conclusion can be traced to the evidence that produced it.
- **Loose coupling.** Components depend on each other as little as possible, and always through the knowledge they expose, never by reaching into another's internals.
- **Clear ownership.** Every responsibility and every concept is owned by exactly one component. There is no shared, unowned area.
- **Independent evolution.** A component can change its own workings without forcing a change in another, as long as its boundaries and the knowledge it exposes stay honored.

These principles produce an architecture that survives a change of technology, because they bind the business of the system, not its machinery.

## 3. System Responsibilities

The system has the following core responsibilities, stated as what the product must do.

- **Know the truth of delivery.** Observe a domain's delivery infrastructure and hold its truthful Health Signal.
- **Understand why.** Diagnose delivery at risk and find the Root cause of an Issue.
- **Speak to the business.** Explain an issue in business language and offer the deeper technical view.
- **Decide what matters most.** State business impact and assign a priority order of risk.
- **Act and verify.** Guide a step-by-step fix and verify that it holds.
- **Keep watch.** Observe over time, detect change, and alert in business language.
- **Prove.** Produce reporting and evidence of health to a client, an auditor, or a board.
- **Assess readiness.** Measure a domain against the requirements of the mailbox providers that decide inbox entry.
- **Hold the portfolio.** Manage the domains, organizations, and clients and the relationship of domains to them.
- **Learn.** Capture feedback to improve explanation and guidance.
- **Govern who acts.** Hold the users, roles, and consent of who may.
- **Serve confidence.** Produce the Confidence the customer holds and protect the Trust of their clients.

Every responsibility traces through a bounded context and a capability to a customer problem; none is an orphan.

## 4. Major System Components

Each component is a logical responsibility. For each: Purpose, Responsibilities, Consumes, Produces, Owns, Does Not Own.

### Delivery Observation

- Purpose: To establish the truth of a domain's delivery.
- Responsibilities: add a domain to observation; record its Health Signal and its Health Trend.
- Consumes: the domain it is asked to observe, from the portfolio.
- Produces: Health Signal, Health Trend.
- Owns: Health Signal, Health Trend.
- Does Not Own: the meaning of an Issue, an explanation, or a decision.

### Diagnosis

- Purpose: to know why delivery is at risk.
- Responsibilities: reason across the signals health reflects; identify the Issue and its Root cause.
- Consumes: Health Signal.
- Produces: Issue, Root cause.
- Owns: Issue, Root cause.
- Does Not Own: the business explanation or the guidance.

### Business Explanation

- Purpose: to make a diagnosis understood in business terms.
- Responsibilities: explain an issue, state what happened and why it matters, and hold the deeper technical view.
- Consumes: Issue and Root cause.
- Produces: the explanation of the issue and the business language.
- Owns: the explanation.
- Does Not Own: the cause itself or the priority.

### Impact and Prioritization

- Purpose: to turn understanding into a decision.
- Responsibilities: state the business impact of an issue and order issues by priority of risk.
- Consumes: the explanation.
- Produces: Business impact, Priority.
- Owns: Business impact, Priority.
- Does Not Own: the guidance that follows.

### Guidance and Resolution

- Purpose: to turn a decision into a resolved state.
- Responsibilities: provide prioritized, step-by-step guidance, and verify that a fix holds.
- Consumes: the Priority, and a second read of health for verification.
- Produces: Guidance, Fix verification.
- Owns: Guidance, Fix verification.
- Does Not Own: the priority of the issue.

### Monitoring and Alerting

- Purpose: to keep the truth current and to draw attention.
- Responsibilities: sustain the observation, detect change, and raise an Alert in business language.
- Consumes: Health Signal.
- Produces: Alert, and the standing state of the watch.
- Owns: Alert.
- Does Not Own: the decision to fix.

### Reporting and Evidence

- Purpose: to let the customer prove, not assert.
- Responsibilities: create a recurring summary and a grounded account of health.
- Consumes: Health Signal and Diagnosis.
- Produces: Report, Evidence.
- Owns: Report, Evidence.
- Does Not Own: the health it reports.

### Readiness and Compliance

- Purpose: to show what is required to be accepted.
- Responsibilities: measure a domain against mailbox-provider requirements and raise a readiness statement.
- Consumes: Health Signal and the provider's requirements.
- Produces: Readiness.
- Owns: Readiness.
- Does Not Own: a guarantee of delivery.

### Portfolio and Client Management

- Purpose: to manage the scale of the customer's delivery.
- Responsibilities: hold the domains, organizations, and for an agency the clients and portfolio, and the relationship of domains to them.
- Consumes: the customer's act of adding a domain.
- Produces: the registry of what is owned.
- Owns: Organization, Client, Portfolio, Domain.
- Does Not Own: the health of a domain.

### Feedback and Learning

- Purpose: to improve the product from use.
- Responsibilities: capture whether an explanation or a piece of guidance was useful, and collect what was wrong.
- Consumes: explanations and guidance.
- Produces: Feedback, and the learning it enables.
- Owns: Feedback.
- Does Not Own: the explanation itself.

### Identity and Access

- Purpose: to govern who may see and act.
- Responsibilities: hold the users and roles of an organization and the rules of who may act; honor Consent.
- Consumes: the definition of an organization.
- Produces: who may act, and the consent given.
- Owns: User, Role, Consent.
- Does Not Own: a domain's health or the portfolio.

## 5. System Interaction Model

The components relate in the natural flow of the product, stated in the business knowledge that passes between them.

- **Portfolio** is the source of the domains that delivery Observation is asked to watch.
- **Observation** advances the truth to **Diagnosis**.
- **Diagnosis** passes the issue and its cause to **Business Explanation**.
- **Business Explanation** passes the meaning to **Impact and Prioritization**.
- **Impact and Prioritization** passes the priority to **Guidance and Resolution**, which acts and verifies.
- **Observation** also advances its state to **Monitoring and Alerting**, which keeps the customer aware.
- **Observation and Diagnosis** feed **Reporting and Evidence** and **Readiness and Compliance**.
- **Explanation and Guidance** feed **Feedback and Learning**, which returns its learning to them.
- **Identity** grants or withholds the ability to act across all the others.

The interactions are the flows of the business meaning. They are described as the words that pass between components, not as calls or messages.

## 6. AI Placement

AI is a deliberate guest in the system, with a clear home and clear limits.

**Where AI belongs.**
- In **Business Explanation**, to translate a diagnosis into business language and to offer the plain or deeper account.
- In **Impact and Prioritization**, to help order issues by the risk to the business.
- In **Guidance and Resolution**, to propose a fix with a reason and a confidence.
- In **Feedback and Learning**, to turn feedback and history into better explanations and guidance over time.

**Where AI does not belong.**
- It does not decide the underlying diagnosis alone; the truth of delivery is grounded in observed facts, and AI must not invent a cause.
- It does not own an authoritative fact — the Health Signal and the Root cause belong to observation and diagnosis, not to a model.
- It does not take action on a customer's live infrastructure; it proposes, and it never acts without the customer's Consent.

**What AI may decide.** It may decide how to explain, how to order, and how to recommend — always within the truth the grounded facts.

**What AI must never decide.** It must never decide the truth of a delivery state, and never decide which action takes effect on a live system. For anything that affects what a customer believes or decides, a human owns the outcome (see the Constitution and the AI requirements).

## 7. Trust Boundaries

Trust moves through the system by the evidence the components hand each other.

- **The truth boundary.** The Health Signal is the agreed truth. Everything that comes after trusts that signal. The boundary is that no component turns a later derived view into a changed truth.
- **The cause boundary.** The Root cause is the agreed cause. It is created once, by Diagnosis, and the downstream trusts it and does not rewrite it.
- **The explanation boundary.** The explanation is trusted as meaning, but not as a factual record of the cause; it restates the cause in business terms.
- **The decision boundary.** Impact and Priority are the decided view of risk. They are trusted as the order of attention, not as new facts.
- **The proof boundary.** Reporting and Evidence is trusted only because it reflects the truth and diagnosis it reports. Its trust comes from what it carries.
- **The consent boundary.** No action on a customer's live infrastructure crosses into the trust boundary without the customer's Consent; the system stops at the consent gates (INV-02).

Trust is produced at the source of truth and passed downstream with each component's own value. A component that cannot be trusted with a conclusion is not given that conclusion — business trust, not a security mechanism.

## 8. Failure Philosophy

The business behaves in a known, honest way when a part of its world is unavailable or incomplete. No engineering mechanism is described; only the business behavior.

**Observation unavailable.** If the truth of health cannot be read, the system does not pretend to know it. It states that health is not known. No health is asserted where none was observed (INV-01).

**Evidence incomplete.** If the evidence for a root cause or a priority is incomplete, the conclusion is presented as incomplete. The system says what it knows and what it does not, and holds any claim that the missing evidence would change.

**AI unavailable.** If the AI that explains or orders is not available, the system still tells the truth it observed. The diagnosis, the cause and the health remain; only the translation and the ordering are reduced. The customer is told the AI's phrasing is unavailable, never given a fabricated answer.

**Customer consent absent.** If a customer's Consent for an action is absent, the action does not happen. Nothing is done to a live system without it (INV-02), and it is the customer who holds the right to give or withhold.

In every case the underlying business truth requires the most faithful: the system fails a claim before it fails the truth. It it cannot show the truth, it states the limit.

## 9. System Evolution Principles

- **How responsibilities evolve.** A responsibility changes only when the underlying business responsibility changes, and by the same governance as the domain model. When it changes, the change is recorded and reviewed.
- **How components split.** A component is split only when it is proven that it holds more than one business responsibility — that it can change for more than one reason. An aesthetic split (for scale) is not reason to split the logical component.
- **How cohesion is preserved.** Cohesion is kept by the single-business-responsibility rule: a component stays one subject. When a component's concern would grow into a new subject, the new subject is a candidate for a new component under the context governance.
- **Consistency of knowledge.** The knowledge a component exposes is its published language (from the Ubiquitous Language); the law of one owner keeps the system honest.

The system stays small in the number of its components, because each component is a real responsibility and is not multiplied by an architecture style.

## 10. Dependencies & Successors

### Depends on

- [PROJECT_CONSTITUTION.md](../00_Foundation/PROJECT_CONSTITUTION.md)
- [GLOSSARY.md](../00_Foundation/GLOSSARY.md)
- [REQUIREMENTS_TRACEABILITY_MATRIX.md](../02_Product_Design/REQUIREMENTS_TRACEABILITY_MATRIX.md)
- [01_DOMAIN_MODEL.md](01_DOMAIN_MODEL.md)
- [02_UBIQUITOUS_LANGUAGE.md](02_UBIQUITOUS_LANGUAGE.md)
- [03_BOUNDED_CONTEXTS.md](03_BOUNDED_CONTEXTS.md)
- [04_INFORMATION_ARCHITECTURE.md](04_INFORMATION_ARCHITECTURE.md)

### Feeds into

As a predecessor for the future documents `06_AI_ARCHITECTURE.md`, `07_DATA_MODEL.md`, `08_API_PHILOSOPHY.md`, `09_SECURITY_ARCHITECTURE.md`, and `10_DEPLOYMENT_ARCHITECTURE.md`.

## 11. Governance

- **Status.** Draft until the founders review. Only the founders freeze this document after approval.
- **Changes to the responsibilities.** Changing a component, its responsibility, or its boundaries is a proposal to be reviewed by the founders or the chief architect.
- **Traceability.** Every responsibility traces through a component and a context to a capability and a problem; an orphan is removed.
- **Authority.** This document is subordinate to the Constitution and the workflow, and consistent with the model and the information architecture.
- **Not a technology decision.** The framework, the database, the deployment, the messaging, the APIs, and the language are decided elsewhere, not here.

---

*This document defines the logical system of MailSent as a set of business responsibilities. It proposes no service, no framework, and no database — it states how the system is organized and who is responsible for what.*