# MailSent — Requirements Traceability Matrix

> The official governance model for how MailSent requirements relate and stay connected.
>
> This document defines the **system** that keeps every requirement traced to its source, its user, its value, and its acceptance — so that nothing exists without lineage. It does **not** define requirements, and it is **not** a populated spreadsheet. It is the rulebook and the table structure that the requirement workflow fills in.
>
> It is subordinate to the [Project Constitution](../00_Foundation/PROJECT_CONSTITUTION.md), the [Master Workflow](../00_Foundation/MAILSENT_MASTER_WORKFLOW.md), and the [Glossary](../00_Foundation/GLOSSARY.md), and is consistent with every document it references. Terminology here follows the Glossary (for example, capability, Health Signal, and the separate meanings of Confidence, Reliability, and Trust).

---

**Document governance**

- **Status:** Draft
- **Version:** 1.0
- **Owner:** Founders
- **Review Status:** Pending Founder Review

Only the founders may freeze this document after review.

**Next review trigger:**
- Product philosophy changes
- The structure of the requirements set changes
- The traceability model changes

---

## 1. Purpose

Traceability is the guarantee that no requirement exists by accident. Every requirement in MailSent must answer two questions: **where did this come from** (its source), and **how will we know it is met** (its acceptance). The traceability system is the mechanism that enforces these two questions and keeps every requirement connected to the documents that justify and define it.

Without traceability, three failures are inevitable: requirements that serve no reason, requirements that are never validated, and requirements that silently contradict the strategy. This document exists to make those failures visible and preventable.

It defines the philosophy of traceability, the rules that bind it, the levels at which it operates, the official structure of the traceability record, how the system can be validated, and how changes to one document ripple through others.

## 2. Traceability Philosophy

Traceability at MailSent is built on one belief: **a requirement is not real until it can be traced.** A requirement with a source is a decision; a requirement without a source is a guess, and the Golden Rule forbids guessing.

The philosophy rests on four commitments:

- **Backward traceability.** Every requirement must trace to a validated customer problem and a primary user outcome. If the source is absent, the requirement does not exist.
- **Forward traceability.** Every requirement must trace to its acceptance — the demonstration that it behaves correctly, is truthful, is business-first, is secure, and passes the feature gate.
- **One requirement, one place.** Each capability-level requirement is defined once, in its owning document, and referenced everywhere else — never restated with drift.
- **A single vocabulary.** The Glossary is the shared language. A requirement cannot use a term against the Glossary and remain valid.

The system is a direction, not a box: it runs from the customer and the strategy downward into requirements, and downward expectation from requirements into acceptance. Both directions must be present or the loop is broken.

## 3. Traceability Rules

- **Rule 1 — Everything has a source.** Every requirement exists, reference, or acceptance claims a source in the authoritative document set. No source, no requirement.
- **Rule 2 — No orphans.** A requirement that cannot be traced backward to a customer problem or forward to an acceptance is orphaned and must be rejected, not kept.
- **Rule 3 — One map, many records.** The traceability matrix is one logical record, held across the requirement documents; it is not a parallel, separate description that a drift.
- **Rule 4 — Terminology is binding.** The Glossary's terms are used; a requirement that uses a non-canonical term is out of compliance.
- **Rule 5 — Truth is traceable.** A requirement that affects a reported state or conclusion must trace to how its truthfulness is demonstrated. (See the AI and Non-Functional requirement catalogs.)
- **Rule 6 — Material reflects source.** When a source changes, every requirement it feeds is reviewed; when a requirement changes, the record and the downstream acceptance is updated.
- **Rule 7 — No invented links.** A traceability entry can never be added after the fact to make a requirement "look" connected; it must reflect a real decision.
- **Rule 8 — The Golden Rule binds trace.** A requirement can be traced to a validated problem and its recurring value is the beginning; a technical-impressive without a problem is rejected.

## 4. Traceability Levels

Traceability is organized into levels of artifact. A requirement lives at one level and traces to the levels above and below.

- **Strategic layer** — the reason Mail sent exists. Contains the Product Vision, the Customer Problems, the Customer Journey, the Value Proposition, and the Product Positioning. This is the level of the Customer Promise and the identity.
- **Outcome layer** — the primary user outcomes the product must produce. It is where problems become a direction about what a user should be able to do or know.
- **Requirement layer** — the capability-level requirements that bind the deliverable. This is the layer of the Functional Requirements catalog, grouped by capability, and it is the level at which traceability is central.
- **Quality layer** — the Non-Functional Requirements (quality attributes) and the AI Requirements (behavior and accountability). It cross-cuts every capability.
- **Sequencing layer** — the Release Strategy, which groups capabilities into the phases that deliver them.
- **Acceptance layer** — the Definition of Done and the Acceptance Criteria that confirm a requirement is truthfully met.

The layers are ordered, but the stronger relations are cross-layer: a requirement at the capability layer is tied up to an outcome and a problem, and across to a quality obligation and an acceptance, and over to a release phase.

## 5. Requirement Relationships

The documents connect in one direction, from the customer and the strategy to the acceptance of a requirement.

**Vision → Problems → Journey → Value → Goals → Requirements → Acceptance**

- **Vision** sets the purpose of the company (tell and of value, the trusted layer of email). It is the reason the whole chain exists.
- **Problems** name what is wrong for the customer (the reason MailSent exists: invisible delivery failure). Every requirement must map to a validated problem it addresses.
- **Journey** shows how the customer becomes aware and ready to change. It explains the timing, not just the reason.
- **Value** states what the customer gains from knowing their delivery (the reason they would change).
- **Goals** turn the above into the product's purpose (what the product must achieve, expressed as the product goals).
- **Requirements** are the concrete units of delivery (the capabilities), defined precisely and grouped by a capability.
- **Acceptance** is the proof that a requirement is met (the Definition of Done and the Acceptance Criteria).

Two further documents constrain the chain from the side rather than along it: the **Positioning** and the **Constitution** bound what the product is and how it is built, and the **Glossary** fixes the language that the whole chain uses.

A trace for a requirement runs: from a problem (Problem ID), through a user outcome, to the capability that delivers it, to the requirement that defines it, to the quality and AI obligation it carries, to the release phase that ships it, and to its acceptance. Each step is one that can be inspected and rejected if it does not hold.

## 6. Official Traceability Matrix

The following defines the structure of the traceability record. It is a template, not a filled matrix — the table is populated by the requirement workflow, not this document.

### Column structure

Each requirement is recorded once, in a row, with these columns:

| Column | Meaning | Source document |
| --- | --- | --- |
| Requirement ID | The unique identifier of the requirement. Use the owning catalog's system; this document does not invent IDs. | Product Requirements (master index) |
| Requirement | A one-line statement of what the product enables. | Functional / Non-Functional / AI Requirements |
| Capability | The capability the requirement belongs to (per the capability model). | Functional Requirements |
| Source problem | The validated customer problem the requirement addresses. | Customer Problems |
| User outcome | The primary user outcome the requirement produces. | Product Requirements |
| Value link | The reason the requirement creates the value the customer changes for. | Value Proposition |
| Primary user | The user type that receives the outcome. | Primary User Outcomes |
| Quality obligation | The Non-Functional or AI requirement it must satisfy. | Non-Functional / AI Requirements |
| Acceptance | How the requirement is shown to be met (definition of done). | Product Requirements (Acceptance Criteria) |
| Release phase | The phase that ships the requirement. | Release Strategy |

The **cannot**: the column set is deliberately small and matches the requirement set. A warning against adding columns without review.

### Placeholder example (not a real row)

| Requirement | Capability | Source | Sample of possible expectations |
| --- | --- | --- | --- |
| `[REQUIREMENT_ID]` | `[CAPABILITY]` | `[PROBLEM]` | `[OUTCOME]` suggested, `[ACCEPTANCE]` |

The placeholders above are tokens, not values. They exist only to show the shape of a row. Real requirements, their IDs, and their mappings are drawn from the requirement catalogs and the acceptance framework — not this document, and not invented here.

### Governance and currency

The matrix is **current** only when it matches the requirement catalogs and the source documents. It is maintained as the source change (see Section 8). It is the index that holds the requirement set together; it is not a second definition of the set.

## 7. Validation Rules

The traceability system is validated on three checks:

- **Completeness.** Every requirement has at least one trace to a problem and one trace to an acceptance. A requirement with either direction missing fails.
- **Consistency.** Every requirement uses the Glossary's terms, and no requirement contradicts the Constitution or the Positioning. A contradiction fails.
- **Lineage.** Every requirement's source is real and current. A requirement whose drain/document changed without a re-trace fails.

Validation is done:
- **On change.** When any source or requirement changes, the affected traceability is re-checked.
- **On review.** When a requirement is reviewed for acceptance, its traceability is reviewed at the same time.
- **On rejection.** An un-traced requirement is rejected (Rule 2); it is closed, not tracked-on.

The outcome of validation is a set of these: each requirement is confirmed to have a source, a user, an acceptance, and a current home in one catalog. If any is missing, the requirement is either re-traced or rejected.

## 8. Change Management

Documents change; the priority of the model is that a change to one connection is applied to everything that depends on it. The following maps the review that a change triggers.

- **Constitution or Positioning changes** → review the Product Vision, the customer problem set, the Value Proposition, and the requirement set, because the identity and constraints of the product have changed.
- **Customer Problems changes** → review the requirement set and the Release Strategy, because the source of the requirements has moved.
- **Product Vision or Value Proposition changes** → review the Product Goals, the Success Criteria, and the requirement set.
- **Customer Journey changes** → review the user flows and the primary user outcomes.
- **Product Requirements changes** → review the Functional, Non-Functional, and AI catalogs, the Release Strategy, and the traceability record.
- **A requirement catalog changes** → review the Product Requirements and the traceability record, and the Release Strategy if a requirement must move to a new phase.
- **Quality or AI obligations change** → review every requirement that carries them, and the traceability record.
- **Glossary changes** → review every document for terminology, and the traceability record for the fields that use the term.
- **Constitution changes** → review the entire set, because the Constitution is the engineering truth and the highest governing standard.
- **Release Strategy changes** → review the requirement set for phase placement, and the sequence of acceptance.

In every case, the rule is the same: a change is fully delivered only when the traceability record reflects it. A change that is not recorded is not complete.

## 9. Governance

This document governs the traceability model and is subordinate to the Constitution and the Master Workflow.

- **Changes to the model.** A proposal is written down with the change and why, and reviewed before adoption. This document does not silently change the structure or the rules.
- **Structure is slow.** The list of matrix columns and the levels of traceability change only when the product's structure changes, not on the flow of a single requirement.
- **Status.** Draft until the founders review. Only the founders freeze this document after approval.
- **Review of the model.** The model is re-validated when the product scope, the requirements structure, or a business decision changes, and it is checked against the Glossary and its terms.

The traceability system does the job: it keeps every requirement honest — each one something it comes from, something it gives, and a plain way to know it is true.

---

*This document defines the governance that keeps every requirement connected. It assigns no requirement and no mapping; it sets the structure and the law under each requirement that it owns.*