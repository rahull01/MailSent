# MailSent — Data Model (Conceptual)

> The conceptual business data model of MailSent.
>
> This document models the business data of the product: the aggregates, the entities, the value objects, and how ownership, identity, lineage, and the invariants hold. It is **not** a database model. An aggregate is not a table, an entity is not a row, and a value object is not a primitive. Nothing here presumes a storage technology.
>
> It is subordinate to the [Project Constitution](../00_Foundation/PROJECT_CONSTITUTION.md), the [Master Workflow](../00_Foundation/MAILSENT_MASTER_WORKFLOW.md), and the [Glossary](../00_Foundation/GLOSSARY.md), and is consistent with the [Domain Model](01_DOMAIN_MODEL.md), the [Ubiquitous Language](02_UBIQUITOUS_LANGUAGE.md), the [Bounded Contexts](03_BOUNDED_CONTEXTS.md), the [Information Architecture](04_INFORMATION_ARCHITECTURE.md), the [System Architecture](05_SYSTEM_ARCHITECTURE.md), and the [AI Architecture](06_AI_ARCHITECTURE.md). Terminology uses only the Glossary; every aggregate traces through the domain model and a context to a requirement and a customer problem.

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
- The data boundaries change

---

## 1. Purpose

The business data model is the agreed shape of the product's information, seen as a set of owning wholes. It answers, for each whole: what is it, what does it own, what does it protect, and what keeps it true. It is the model the software will later persist and serve, and it is drawn purely from the business — not from any storage engine.

The model exists so that the business's data is owned, consistent, and traceable: every piece of data belongs to a whole, every whole protects a rule, and every conclusion can be traced to the evidence that produced it.

## 2. Data Modeling Principles

- **One owner per whole.** An aggregate owns its entities; no entity is shared between two owning wholes.
- **Truth at the root.** The value a whole protects is stated at its root, and the whole holds the business rules that keep that value true.
- **Immutable values.** A value object is a fact that does not change; a changed fact is a new value, not an edited one.
- **Identity is stable.** An entity keeps the same identity through its life; identity never changes, and no two entities share one.
- **Everything has a lineage.** Every assessment and conclusion traces to evidence; nothing stands without a source.
- **No orphans.** Every entity and every reference belongs to an aggregate and a context; nothing floats.
- **The Glossary is the language.** Terms are used as the Glossary defines them, never redefined here.

## 3. Core Aggregates

For each aggregate: Purpose, Aggregate Root, Owns, Protects, Lifecycle, Business Invariants.

### Organization

- Purpose: To hold a business entity, its users, and its owned domains as one consistent whole.
- Aggregate Root: Organization.
- Owns: its Users, its Domains, and in an agency its Clients and the Portfolio.
- Protects: the identity and the separation of the organization (INV-09) and the Consent it gives (INV-02).
- Lifecycle: an Organization is formed, grows by adding domains and users, and persists; it ends when the customer closes it.
- Consistency Boundary: the users, domains, and Consent of an organization hold as one unit — a change to ownership or consent is whole, never partial, and the organization never appears half-formed.
- Business Invariants: INV-01 (truth in what it holds), INV-02 (consent), INV-06 (confidence and trust), INV-09 (isolation), INV-10 (identity), INV-13 (ownership).

### Domain Health

- Purpose: To hold the truth of a single domain's delivery.
- Aggregate Root: Domain.
- Owns: the Health Signal, the Health Trend, and the issues of that domain.
- Protects: the truth of the health (INV-01) and the single-health rule (INV-05).
- Lifecycle: a Domain is added to observation, receives its first health, is watched and improved, and is retired when it leaves observation.
- Consistency Boundary: the Health Signal, the Health Trend, and the issues of a domain are always consistent with the observed truth — a domain is never both healthy and at risk in one view, and its health always reflects its issues.
- Business Invariants: INV-01, INV-04, INV-05, INV-10, INV-11, INV-14.

### Issue

- Purpose: To hold one delivery problem and its full journey to resolution.
- Aggregate Root: Issue.
- Owns: the Root cause, the Business impact, the Priority, the Guidance, and the Fix verification.
- Protects: the closure rule (INV-03) and the grounding of the cause (INV-04).
- Lifecycle: an Issue is discovered, diagnosed, explained, prioritized, guided, verified, and closed — or reopened if it recurs.
- Consistency Boundary: an Issue's cause, impact, priority, guidance, and verification form one coherent resolution — an Issue is never resolved while unverified, and its parts never contradict each other.
- Business Invariants: INV-03, INV-04, INV-10, INV-11, INV-12, INV-14.

### Client Relationship

- Purpose: To hold the relationship between an agency and a client, and the evidence that sustains its trust.
- Aggregate Root: Client.
- Owns: the client's domains, the Reports and Evidence produced for it, and the state of the relationship.
- Protects: the trust of the client through grounded evidence (INV-01, INV-06), never assertion.
- Lifecycle: a Client relationship is formed when an agency takes it on, is sustained through delivery health, and ends when the relationship ends.
- Consistency Boundary: the Reports and Evidence of a client always reflect that client's real health — the proof produced for a client never exceeds what is grounded for that client, keeping the relationship's trust consistent.
- Business Invariants: INV-01, INV-06, INV-09, INV-10, INV-13, INV-14.

### Aggregate Classification

The strategic value of each aggregate, in one sentence each.

| Aggregate | Classification | Why |
| --- | --- | --- |
| Domain Health | Core Domain | It holds the truth of delivery, which is the heart of the business. |
| Issue | Core Domain | It carries the diagnosis and the resolution, the differentiation of the product. |
| Organization | Supporting Domain | It holds identity and ownership, enabling the core without being the moat. |
| Client Relationship | Supporting Domain | It holds the trust evidence for agencies, a support to the core. |

The classification says where value is concentrated: the Core is the reason the business exists; the Supporting makes the core usable and complete.

## 4. Business Entities

For each entity: Identity, Responsibility, Lifecycle. No fields.

**Organization**
- Identity: its own, stable across its life.
- Responsibility: to own the domains and users of one business.
- Lifecycle: formed, grown, persisted, closed.

**Client**
- Identity: its own, as an organization served by an agency.
- Responsibility: to be the party whose delivery and trust the agency protects, and who receives proof.
- Lifecycle: formed into the relationship, sustained, ended.

**Domain**
- Identity: its own, as the owned identity whose delivery is observed.
- Responsibility: to be the unit of observation, diagnosis, and reporting.
- Lifecycle: added to observation, watched, retired.

**User**
- Identity: its own, as a person acting within an organization.
- Responsibility: to act on health, decide, and own the outcome of any conclusion acted on.
- Lifecycle: joined to the organization, active, removed.

**Issue**
- Identity: its own, as one delivery problem.
- Responsibility: to carry the problem from discovery to a verified resolution.
- Lifecycle: discovered, diagnosed, resolved, closed or reopened.

**Report**
- Identity: its own, as one produced account of health.
- Responsibility: to state the proof of health to a client, auditor, or board, grounded in the health it reflects.
- Lifecycle: produced, shared, kept as history.

## 5. Value Objects

For each: Meaning, Why immutable, Usage. Values reference the Glossary and are not redefined.

**Health Signal**
- Meaning: the unified, legible indication of a domain's delivery health (Glossary).
- Why immutable: a health indication is a snapshot of one observation; it never mutates, it is replaced by the next snapshot.
- Usage: the ground truth the Domain Health aggregate reports and all others read.

**Health Trend**
- Meaning: the time-based representation of a Health Signal (Glossary).
- Why immutable: it is a view of past snapshots; it is recomputed, never changed.
- Usage: to show improvement, stability, or decline.

**Root cause**
- Meaning: the underlying cause of an issue, not its symptom (Glossary).
- Why immutable: once confirmed, the cause is a settled fact; a changed cause is a new diagnosis.
- Usage: the target of diagnosis and the basis of explanation.

**Business impact**
- Meaning: what an issue threatens — revenue, trust, or a relationship — in business terms (Glossary).
- Why immutable: it states the consequence at a point in time; a changed consequence is a new assessment.
- Usage: the basis of priority.

**Priority**
- Meaning: the order in which issues are addressed, by business risk (Glossary).
- Why immutable: it is the decided order at a moment; a re-order is a new priority.
- Usage: tells the customer what to do first.

**Alignment**
- Meaning: the consistency between who authenticates a message and who the domain says is sending (Glossary).
- Why immutable: it is a factual relation at an observation; it changes only with the observed state.
- Usage: part of the diagnosis of delivery.

**Readiness**
- Meaning: the assessment of a domain against the requirements of the mailbox providers (Glossary).
- Why immutable: it is a verdict against a standard at a moment; a new assessment is a new Readiness.
- Usage: what the customer shows to an auditor.

**Alert**
- Meaning: a notification of a change or failure in business language (Glossary).
- Why immutable: an alert states an event; it is not edited after it is sent.
- Usage: what draws the customer's attention.

**Consent**
- Meaning: explicit, informed agreement before any action on live infrastructure (Glossary).
- Why immutable: a grant is a fact; it is given or withdrawn, not edited.
- Usage: the gate for any action (INV-02).

**Role**
- Meaning: a set of what a user may see and do within an organization (Glossary).
- Why immutable: a role is a defined right; a changed right is a new role.
- Usage: the bound of a user's access, conceptually.

**Fix verification**
- Meaning: the confirmation that a resolution held and the issue did not return (Glossary).
- Why immutable: it is a verdict on a fix at a moment; a recurrence is a new event.
- Usage: the close of an Issue.

**Business explanation**
- Meaning: the translation of a diagnosis into the language of the business (Glossary).
- Why immutable: it is a produced account; a better account is a new explanation.
- Usage: what the customer reads and acts on.

## 6. Aggregate Relationships

The ownership between the wholes, in business terms.

- **Organization owns Domain and User.** An Organization owns the domains it protects and the users who act for it. A Domain belongs to exactly one Organization. User → Organization is a single, mandatory relationship after signup (one User to exactly one Organization); both are persisted together as one consistent unit.
- **Agency owns Client, which owns Domain.** An Agency (an Organization) owns the Clients it serves; each Client owns its own Domains; together they form the Portfolio. A Client's domain is owned by the Client, and the relationship by the Agency.
- **Domain (Domain Health) owns its Health and its Issues.** The Domain Health aggregate owns the Health Signal, the Health Trend, and every Issue of the domain.
- **Issue owns its diagnosis and resolution.** An Issue owns its Root cause, its Business impact, its Priority, its Guidance, and its Fix verification.
- **Reporting draws on Domain Health and Diagnosis.** A Report is produced for a Client from the health and the diagnosis of its domains; the Report belongs to the Client Relationship.

The rule is single ownership: every entity is owned by exactly one aggregate, and no aggregate reaches into another to change what it owns.

## Aggregate Interaction Rules

- **Aggregates never modify other aggregates.** An aggregate changes its own state only. It cannot reach into another aggregate and alter its data; to influence another, it acts through a business interaction that the other aggregate owns.
- **Ownership never crosses aggregate boundaries.** An entity is owned by one aggregate and stays there. Crossing a boundary to own or change another's entity is a violation.
- **Coordination happens through business interactions, not shared ownership.** Aggregates cooperate by passing the business knowledge (value objects) that one produces and another consumes — a Health Signal passed to Diagnosis, a Priority to Guidance. They do not coordinate by sharing a piece of owned data.
- **Technology independence is preserved.** The interaction rules are principles of the business, stated without any mechanism. Whether the coordination is realized by a call, a message, or any other means is a later decision, not made here.

## 7. Business Invariants

The invariants the data model upholds. INV-01 through INV-09 are the permanent references established in the Domain Model; they are restated here because the data model must honor them. INV-10 onward are the data-model rules.

- **INV-01 — Truth.** Reported health and every reported state trace to a real observation; no fabricated state.
- **INV-02 — Consent.** No action on live infrastructure without explicit, informed consent.
- **INV-03 — Closure.** An Issue is resolved only when its fix is verified.
- **INV-04 — Grounding.** Every explanation, impact, and priority is grounded in the real diagnosis.
- **INV-05 — Single health.** A Domain has one Health Signal at a time, never both healthy and at risk.
- **INV-06 — Confidence and trust.** The business sells Confidence and protects the Trust of clients and customers.
- **INV-07 — Human accountability.** A human owns the outcome of any conclusion.
- **INV-08 — Identity.** The product is never a sender, a single-check tool, a reputation game, or a communication platform.
- **INV-09 — Isolation.** One organization's health and data never mix with another's.
- **INV-10 — Identity.** Every entity has one stable identity; no two entities share one.
- **INV-11 — Lineage.** Every Assessment, Health Signal, and Explanation traces to evidence with a source; a conclusion without lineage does not exist.
- **INV-12 — Immutability.** A value object never changes; a changed value is a new value.
- **INV-13 — Ownership.** Every entity is owned by exactly one aggregate root; there is no shared-ownership entity.
- **INV-14 — No orphans.** Every reference points to an existing, owned entity; nothing references what is not there.

## 8. Evidence Lineage

The chain by which the product's data becomes a customer's decision, and the business meaning of each step.

- **Observation → Evidence.** The reading of the delivery infrastructure produces Evidence — the raw, recorded facts of a domain.
- **Evidence → Finding.** Evidence is interpreted into a Finding — a fact about delivery stated with its source.
- **Finding → Assessment.** Findings are weighed against the reference (standards, rules) to reach an Assessment.
- **Assessment → Health Signal.** The Assessment is expressed as the unified Health Signal, the truth of the domain.
- **Health Signal → AI Explanation.** The Health Signal is translated into the language of the business as an AI Explanation.
- **AI Explanation → Recommendation.** From the explanation, a prioritized Recommendation is made, with a reason and a confidence.
- **Recommendation → Customer Decision.** The customer decides, and a human owns the outcome of that decision.

Each step is a transformation that keeps its source: the Explanation carries the Health Signal, the Health Signal carries the Assessment, and so on back to the Evidence and the Observation. A customer can therefore walk a decision back to the truth it came from. That lineage is what lets the customer trust, and what lets the AI stay honest.

## 9. Model Evolution

- **Introducing a new aggregate, entity, or value object.** A new business object is introduced only when the business needs it, under the Golden Rule, and when it traces to a concept in the domain model and a context. An object that cannot trace is not introduced.
- **Preventing duplication.** The single-owner rule and the lineage rule prevent duplication: a value or entity that repeats information already owned by another aggregate is a defect, and a new object that would duplicate an existing one is rejected.
- **Preserving the invariants.** A change to the model must keep every invariant true. A change that would break an invariant is not made; the invariants are the floor.
- **Growing slowly.** The model is built from the same boundaries as the domain and the contexts; any change to it goes through the same governance as the domain model.

The data model must not become sprawl. Every object is real, owned, and traced to a business reason.

## 10. Dependencies & Successors

### Depends on

- [PROJECT_CONSTITUTION.md](../00_Foundation/PROJECT_CONSTITUTION.md)
- [GLOSSARY.md](../00_Foundation/GLOSSARY.md)
- [REQUIREMENTS_TRACEABILITY_MATRIX.md](../02_Product_Design/REQUIREMENTS_TRACEABILITY_MATRIX.md)
- [01_DOMAIN_MODEL.md](01_DOMAIN_MODEL.md)
- [02_UBIQUITOUS_LANGUAGE.md](02_UBIQUITOUS_LANGUAGE.md)
- [03_BOUNDED_CONTEXTS.md](03_BOUNDED_CONTEXTS.md)
- [04_INFORMATION_ARCHITECTURE.md](04_INFORMATION_ARCHITECTURE.md)
- [05_SYSTEM_ARCHITECTURE.md](05_SYSTEM_ARCHITECTURE.md)
- [06_AI_ARCHITECTURE.md](06_AI_ARCHITECTURE.md)

### Feeds into

As a predecessor for the future documents `08_API_PHILOSOPHY.md`, `09_SECURITY_ARCHITECTURE.md`, and `10_DEPLOYMENT_ARCHITECTURE.md`.

## 11. Governance

- **Status.** Draft until the founders review. Only the founders freeze this document after approval.
- **Changes to the model.** Changing an aggregate, an entity, a value object, or an invariant is a written proposal reviewed by the founders or the chief architect.
- **Traceability.** Every business object traces through a context to a concept and a problem; an orphan is removed.
- **Consistency.** The model is subordinate to the Constitution and consistent with the Glossary and the domain model.
- **Not a technology decision.** The database, the storage engine, the ORM, the serialization, and the APIs are decided elsewhere, not here.

---

*This document models the business data of MailSent — its aggregates, its entities, its value objects, and the invariants that hold them true. It proposes no database and no storage; it states the shape of the business's data, which the software will later realize.*