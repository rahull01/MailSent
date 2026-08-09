# MailSent — API Philosophy

> The philosophy that governs every future API of MailSent.
>
> An API is a **business contract**, not a technology. It is the agreed way one part of the business asks another for its knowledge. It is not REST, not GraphQL, not HTTP, and not JSON — those are transports that serve the contract. This document defines the principles that every contract must follow, so that the APIs that later emerge are stable, trustworthy, and faithful to the business.
>
> It is subordinate to the [Project Constitution](../00_Foundation/PROJECT_CONSTITUTION.md), the [Master Workflow](../00_Foundation/MAILSENT_MASTER_WORKFLOW.md), and the [Glossary](../00_Foundation/GLOSSARY.md), and is consistent with the [Domain Model](01_DOMAIN_MODEL.md), the [Ubiquitous Language](02_UBIQUITOUS_LANGUAGE.md), the [Bounded Contexts](03_BOUNDED_CONTEXTS.md), the [Information Architecture](04_INFORMATION_ARCHITECTURE.md), the [System Architecture](05_SYSTEM_ARCHITECTURE.md), the [AI Architecture](06_AI_ARCHITECTURE.md), and the [Data Model](07_DATA_MODEL.md). Terminology uses only the Glossary; every API concept traces through the data model to a domain concept and a customer problem.

---

**Document governance**

- **Status:** Draft
- **Version:** 1.0
- **Owner:** Founders
- **Review Status:** Pending Founder Review

Only the founders may freeze this document after review.

**Next review trigger:**
- Product philosophy changes
- The domain model or the data model changes
- The integration boundaries change

---

## 1. Purpose

An API is the boundary through which the business's knowledge is exchanged. When it is well governed, a consumer can trust the contract and rely on it. When it is not, a change to one part breaks another, and the whole product loses coherence.

This document is the constitution of the API. It defines what a contract is, how it is owned, how it stays stable, how it evolves, how it fails honestly, and how it inherits the truth of the business. It states principles, not specifications — a future API architect will read this and know how to build every contract the same way.

## 2. API Philosophy Principles

- **Business before transport.** The contract is defined by the business it serves. The transport is an implementation detail chosen later and changed without changing the contract.
- **Stable contracts.** A contract that is published is a promise. Once a consumer relies on it, the contract does not change underneath them.
- **Explicit ownership.** Every contract and every resource has one canonical owner. A contract is changed by its owner, and only by its owner.
- **Backward compatibility.** A change to a contract must not break a consumer who has relied on it. Compatibility is the default; breaking is the exception that is justified.
- **Consistency over convenience.** A slightly less convenient but consistent contract wins over a convenient one that behaves differently in different places.
- **Version only when justified.** A new version is created only when compatibility genuinely cannot be preserved. Versioning is a cost, not a feature.
- **Predictable evolution.** A consumer can see that the contract evolves by stated rules, so that change is expected and safe rather than surprising.

These principles make the contract a thing a consumer can build on, which is the whole purpose of an API.

## 3. Resource Philosophy

A **business resource** is a piece of the business's knowledge exposed at a contract — an Organization, a Domain, a Health Signal, an Issue, a Report. It is **not** a database table. A table is a storage decision; a resource is a business thing with a meaning, an owner, and a purpose.

The difference matters because it keeps the contract faithful. A resource is named and shaped by the business (per the Glossary), not by storage. It is owned by the context that owns the underlying concept, and it exposes the business knowledge, never a storage shape.

Every resource must trace back through the same chain as every other business object:

**Domain Model → Bounded Context → Requirements Traceability Matrix → Customer Problem.**

A resource that cannot be traced is an orphan contract and must not exist. This rule keeps the API as faithful to the business as the domain model itself.

## 4. Resource Ownership

For each resource category: Canonical owner, Producers, Consumers, Ownership rule. Business language only.

### Identity and Party Resources
- Canonical owner: Identity and Access, with the Organization and Client in Portfolio and Client Management.
- Producers: the customer on account, the agency on a client.
- Consumers: every context that must know who is acting.
- Ownership rule: established once, changed only by the owner.

### Portfolio and Domain Resources
- Canonical owner: Portfolio and Client Management.
- Producers: the customer, the agency.
- Consumers: Delivery Observation, Diagnosis, Reporting, Readiness.
- Ownership rule: the registry is authoritative; a Domain is owned once.

### Health Resources
- Canonical owner: Delivery Observation.
- Producers: Delivery Observation.
- Consumers: Diagnosis, Monitoring, Reporting, Readiness.
- Ownership rule: the Health Signal is produced by the owner; consumers read it, never change it.

### Diagnosis Resources
- Canonical owner: Diagnosis.
- Producers: Diagnosis.
- Consumers: Business Explanation, Impact, Guidance, Reporting.
- Ownership rule: the Root cause is authoritative in the owner; no consumer revises it.

### Explanation Resources
- Canonical owner: Business Explanation.
- Producers: Business Explanation.
- Consumers: Impact and Prioritization, the customer, the AI.
- Ownership rule: the explanation is owned for its meaning, not for the cause.

### Prioritization Resources
- Canonical owner: Impact and Prioritization.
- Producers: Impact and Prioritization.
- Consumers: Guidance, Reporting.
- Ownership rule: the Priority is set once by the owner and read by the rest.

### Resolution Resources
- Canonical owner: Guidance and Resolution.
- Producers: Guidance and Resolution.
- Consumers: Monitoring, Feedback, the customer.
- Ownership rule: the Fix verification is authoritative and closes or reopens the Issue.

### Monitoring Resources
- Canonical owner: Monitoring and Alerting.
- Producers: Monitoring and Alerting.
- Consumers: every noticing surface.
- Ownership rule: the Alert is stated once and not revised by a consumer.

### Reporting Resources
- Canonical owner: Reporting and Evidence.
- Producers: Reporting and Evidence.
- Consumers: Clients, auditors, boards.
- Ownership rule: the Report is owned for the proof it states; it follows the health it reflects.

### Readiness Resources
- Canonical owner: Readiness and Compliance.
- Producers: Readiness and Compliance.
- Consumers: Reporting.
- Ownership rule: Readiness is produced once against the required standards.

### Feedback Resources
- Canonical owner: Feedback and Learning.
- Producers: the customer.
- Consumers: Business Explanation, Guidance.
- Ownership rule: Feedback is recorded by the owner and feeds the learning.

In every category, the ownership rule is the same law: a resource is owned once and everywhere else is read or acted on, never independently revised.

## 5. API Contract Principles

- **Stability.** A published contract is a promise that is kept. The owner changes it only under the evolution rules.
- **Compatibility.** A consumer that works with the contract continues to work. The default of any change is that it is compatible; a breaking change is a rare, justified exception.
- **Deprecation.** When a contract or a part of it is to be retired, it is announced before it is removed, so consumers can move. Deprecation is a courtesy and a requirement, not an afterthought.
- **Explicit semantics.** A contract states clearly what it means and what it does, so a consumer does not have to guess. A contract that leaves its meaning implied is a failure.
- **Consumer trust.** The whole of the contract exists to give a consumer confidence to rely on it. Every principle serves that one outcome.

The contract is the handshake of the business: it is how one part of the business can trust another.

## 6. API Evolution Principles

- **When new resources are introduced.** A new resource is introduced when the business needs it and when it traces to a domain concept and a problem. It is introduced as an addition, never as a redefinition of an existing resource.
- **When contracts evolve.** A contract evolves by addition and by clarification — new knowledge, a clearer meaning — while keeping what exists intact. It does not change what a consumer already relies on.
- **When versioning is justified.** A new version is justified only when compatibility truly cannot be preserved and the change cannot be done as an addition. Otherwise versioning is avoided as a cost.
- **How compatibility is preserved.** Compatibility is preserved by never removing, redefining, or silently changing what a consumer relies on; by adding rather than replacing; and by announcing any retirement. A change that would break a consumer is held until it can be done compatibly.

The evolution of the contract is like the evolution of the business — it grows, and it does not break what it already supports.

## 7. Error Philosophy

- **Meaningful errors.** When a contract cannot do what is asked, the error states what is wrong in business terms and what the consumer can do. It is a message to a person, not a code to decode.
- **Honest uncertainty.** When the product does not know an answer, the error or the response says it does not know. It never fabricates a confident answer to avoid an error.
- **No implementation leakage.** An error never reveals how the product is built — no internals, no storage, no mechanism. It stays at the level of the business.
- **AI uncertainty.** When an AI-produced result carries uncertainty, the contract states that uncertainty, exactly as the AI must (per the AI requirements), so a consumer can weigh it.

The errors of the API follow the truth of the business: they are honest, business-first, and never disguised as knowledge they do not have.

## 8. AI API Philosophy

AI interfaces are contracts like any other, and they inherit the four commitments of the AI architecture.

- **Evidence grounding.** A contract that returns an AI result also returns or links the evidence it is grounded on, so the result can be traced to a source. An ungrounded AI result is a failure.
- **Explainability.** The contract makes it possible to ask "why" and receive the reasoning, one layer into the cause, so a consumer can understand the result.
- **Confidence.** A contract that returns a recommendation or guidance states the AI's confidence, so a consumer can weigh how much to rely on it.
- **Human accountability.** A contract that serves a conclusion that affects what a customer believes or decides is accountable to a human; the AI informs, and it does not decide.

The AI interface is not a special exception; it is a contract bound by the same truth as every other, with the AI's obligations made explicit.

## 9. Dependencies & Successors

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
- [07_DATA_MODEL.md](07_DATA_MODEL.md)

### Feeds into

As a predecessor for the future documents `09_SECURITY_ARCHITECTURE.md`, `10_DEPLOYMENT_ARCHITECTURE.md`, and for the future API specifications themselves.

## 10. Governance

- **Status.** Draft until the founders review. Only the founders freeze this document after approval.
- **Changes to the philosophy.** Changing a principle or an ownership rule is a written proposal reviewed by the founders or the chief architect.
- **Traceability.** Every API concept and resource traces through the data model to a domain concept and a problem; an orphan contract is removed.
- **Consistency.** The API is bound by the Glossary and the ubiquitous language; a term used against them is out of compliance.
- **Not a technology decision.** REST, GraphQL, gRPC, HTTP, JSON, authentication, and serialization are decided elsewhere, not here.

---

*This document defines the philosophy of the API of MailSent, in the terms of the business. It proposes no endpoint and no transport — it states the law the contracts follow, so that the APIs the company builds are stable, honest, and faithful to the business.*