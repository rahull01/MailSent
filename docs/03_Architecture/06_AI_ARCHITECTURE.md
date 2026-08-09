# MailSent — AI Architecture

> The constitutional architecture of AI inside MailSent.
>
> This document defines **AI authority, AI boundaries, AI reasoning, the AI trust model, and AI governance**. It is constitutional — it says what role AI is allowed to play and what it must never be — not how a model or an agent is built. AI here is not an LLM, not an agent framework, and not a prompt; it is a capability of the business, governed by the same truth that governs the whole company.
>
> It is subordinate to the [Project Constitution](../00_Foundation/PROJECT_CONSTITUTION.md), the [Master Workflow](../00_Foundation/MAILSENT_MASTER_WORKFLOW.md), and the [Glossary](../00_Foundation/GLOSSARY.md), and is consistent with the [Domain Model](01_DOMAIN_MODEL.md), the [Ubiquitous Language](02_UBIQUITOUS_LANGUAGE.md), the [Bounded Contexts](03_BOUNDED_CONTEXTS.md), the [Information Architecture](04_INFORMATION_ARCHITECTURE.md), and the [System Architecture](05_SYSTEM_ARCHITECTURE.md). Terminology uses only the Glossary; every AI capability traces through a system responsibility to a customer problem.

---

**Document governance**

- **Status:** Draft
- **Version:** 1.0
- **Owner:** Founders
- **Review Status:** Pending Founder Review

Only the founders may freeze this document after review.

**Next review trigger:**
- Product philosophy changes
- The system responsibilities change
- The AI governance model changes

---

## 1. Purpose

AI is a core capability of MailSent, but it is a capability under rule, not a rule in itself. This document is the constitution of AI inside the company. It defines what AI is allowed to do, what it is prevented from doing, how it reasons, how it is trusted, and how it is governed.

The purpose is to make the boundaries of AI explicit and durable, so that the customer's Confidence is never built on an invention, and so that a change of model or vendor never changes the role AI plays. AI is replaceable; the constitution that governs it is not.

## 2. AI Architecture Principles

- **Truth before fluency.** A clear, truthful answer beats a fluent, invented one. AI is never chosen because it sounds right; it is trusted only when it is true.
- **Evidence before inference.** AI reasons from evidence before it infers. An inference that is not grounded is set aside, not used.
- **Confidence before certainty.** AI expresses a needed confidence or degree of certainty; it never presents a guess as settled fact.
- **Explain before recommend.** AI first makes the customer understand why, and only then makes a recommendation — explanation is the ground of a recommendation.
- **Human accountability.** A human owns the outcome of anything AI tells a customer; AI decides nothing that a person does not own.
- **Consent before action.** Any action on a customer's live infrastructure is gated on the customer's Consent; AI does not act, and does not cause an action, without it.
- **Transparency over illusion.** When AI is at work, that is clear. The product never hides the role of the model or substitutes a fabricated appearance for a grounded result.

These principles bind every AI capability in the same way, so no capability of the future can act against them.

## 3. AI Responsibilities

The boundaries of AI are stated as three clear sets.

**AI May**
- Explain every diagnosis in the language of the business.
- Prioritize the issues of a resolution, by the risk and value to the customer.
- Recommend a fine, with a reason and a confidence.
- Simplify or deepen the demonstration of its reasoning, whether the customer asks.
- Use the feedback and history of the business to get better at explaining and recommending.
- Assist the human who owns the outcome, and no more.

**AI Assists**
- The human who decides. AI does the work of **translating, ordering, recommending, and explaining**, so the human can decide well.
- The product's own understanding of the customer — it helps the product be accurate and honest, without changing the truth.

**AI Must Never**
- Invent a delivery state, a root cause, an evidence, a rank, or a forecast.
- Author an explanation that turns a conclusion on invented grounds.
- Decide the truth of a delivery state.
- Take an action on a customer's live infrastructure, or a consequential action, without the customer's consent.
- Act or present as if it were a human owner; it is always the assistant.

Any of these that is broken is a failure of the contract, regardless of how the AI is built.

## 4. AI Capability Layers

The logical AI capabilities, from the least to the deepest. Each is a business capability, and each is traceable to a system responsibility.

- **Observation support.** AI supports the reading of delivery infrastructure — the parsing of the many signals of delivery that a human cannot hold. It does not invent the signals; it brings them into the picture. It maps to Delivery Observation.

- **Diagnosis reasoning.** AI supports the finding of the root cause — the alignment, the sender, the issue. It reasons across the evidence but does not stand in for the evidence. It maps to Diagnosis.

- **Business translation.** AI translates a diagnosis into the language of a business, and offers the plain or deeper view. This is its most central layer. It maps to Business Explanation.

- **Prioritization judgment.** AI weighs and orders the issues by their risk to the business, so a busy owner is not given a wall of noise. It maps to Impact and Prioritization.

- **Guidance and verification support.** AI proposes a prioritized fix and supports a way to know it was verified — and does not do that verification against a fabricated data. It maps to Guidance and Resolution.

- **Learning and improvement.** AI uses the gathered feedback and the kept history to improve the explanation, the order, and the guidance over time. It maps to Feedback and Learning.

Each layer is an AI capability of the business, and none of them is a model; a model may offer them but it is not them.

## 5. AI Decision Authority Matrix

For every major decision, the decision says who has authority and who is final. The roles of authority are the roles of the business defined in the sources — primarily the customer and the humans who own the outcome, and the product's own judge. No specific person is named that is not in the sources.

| Decision | AI authority | Human authority | Final accountability |
| --- | --- | --- | --- |
| State of delivery health | None (AI reports only) | The product states it truthfully | The product, bound by truth (business-confirmed) |
| Root cause of an issue | Assists by proposing | Confirms the ground truth | A human owner of the account |
| Business impact / priority | Assists by proposing | Confirms and orders | A human of the business |
| Explanation of the view | Makes the plain or deep explanation | Reads and acts on it | The human who owns the explanation |
| Recommendation to fix | Proposes with confidence | Decides whether to act | The human who decides |
| Action on live infrastructure | Conditions nothing | Decides, with the product | The customer, before any action; a human and the consent |
| Evidence for a report | None (uses the evidence) | Sets and owns the report | The human who publishes |

The matrix is stable in the balance: AI can recommend and explain, human decides and owns, and only a human says what a customer believes, and only consent lets an action be.

## 6. AI Confidence Model

The three terms have the meanings the Glossary gives them; this document shows the relationship.

- **Reliability** is the standard MailSent holds over its own system — that its own intelligence and its reports are dependable, truthful, observable, and secure. It is the foundation.
- **Confidence** is the outcome the business sells — the certainty a customer holds that email reaches the inbox. It rests on Reliability.
- **Trust** is what a customer's own clients and stakeholders hold in them, which a missed message can break. It is the deeper currency, and it is held by the relation of the customer, and it is protected by the customer's Confidence.

The relationship is a chain. **Reliability** (the standard on the AI's own truth) produces **Confidence** (the outcome the customer holds), which protects **Trust** (what a client holds). AI contributes to Reliability by being grounded and honest; the interaction is that a small error in Reliability is a small crack in Confidence and a larger crack in Trust. Hence every answer of the AI aims at Reliability first.

## 7. Evidence Grounding

**Evidence AI is allowed to use.**
- The observed state of delivery, its Health (Signal).
- The stored diagnosis and root cause, and the history of trends and reports.
- The reference it was given — a requirement of a provider, a rule, a standard.
- The feedback and the learning the product has already collected from the customer.
- Only the information the architecture permits (ties with the Information Architecture): what has a source and an owner.

**Evidence AI must never fabricate.**
- A health signal that was not observed.
- A root cause that was not confirmed, an evidence that does not exist.
- A rank or a forecast not grounded in real data.
- The mechanism, the step, or the value of a correction that is not real.

**Grounding philosophy.** Every AI claim carries or cite to the evidence that it came from, and the product must show the basis of a conclusion if asked. A claim that cannot be traced to evidence is not made. The AI is grounded the same way the whole company is grounded — truth everywhere.

## 8. Uncertainty Communication

- **How it communicates.** AI states in plain business terms what the evidence says, what it does not, and what it cannot conclude.
- **How it avoids overclaiming.** AI does not make a claim that the evidence does not support. If uncertain, it says the hypothesis and the uncertainty, and does not present it as fact.
- **How it explains limitations.** AI is clear about what it knows, what it cannot know, and what would make the ground honest — the missing evidence that would change the answer.

The uncertainty is the honest mirror of the evidence: it tells the customer how far the ground to the answer, and how far it stops.

## 9. AI Failure Philosophy

**AI unavailable.** If the AI that explains is not present, the product still says the observed health and the diagnosis truthfully; it is only the translation and the order that is reduced, and the customer is told that.
**Evidence incomplete.** If the evidence for a claim is incomplete, the claim is stated as incomplete; the customer is told what is known and what is missing.
**Evidence conflicting.** If two observations conflict, the product does not smooth them over; it holds the best-supported state, and it is honest that the evidence conflicts.
**Confidence low.** If the AI has low confidence, it says so and does not act as if it were certain; if it cannot recommend, it says so.
**Consent absent.** If a customer's consent is missing for an action, the action does not happen; the product halts and asks, and it does not assume the consent.

In every failure, the product fails toward truth (see the Constitution). It cannot prove the truth with a partial one; it can only state what it has and be its limit.

## 10. AI Evolution Principles

- **Business need before technology.** The product adds an AI capability only when a business need and a confirmed customer problem (the Golden Rule) call for it; it is not adopted merely because a model appears.
- **A capability traces.** Every new AI capability maps to a system responsibility and to a customer outcome; an untraced capability is not added.
- **The truth bar is held.** No new capability changes the grounding, the consent, or the human accountability that govern it.
- **One built, then kept honest.** An AI capability is introduced as a draft for a human to review and own; it is not left alone, and it is only built when it stands behind the same truth as the machine.

The product's AI grows by depth, into the layer that the business needs, and never by a new territory the business does not need.

## 11. Dependencies & Successors

### Depends on

- [PROJECT_CONSTITUTION.md](../00_Foundation/PROJECT_CONSTITUTION.md)
- [GLOSSARY.md](../00_Foundation/GLOSSARY.md)
- [REQUIREMENTS_TRACEABILITY_MATRIX.md](../02_Product_Design/REQUIREMENTS_TRACEABILITY_MATRIX.md)
- [01_DOMAIN_MODEL.md](01_DOMAIN_MODEL.md)
- [02_UBIQUITOUS_LANGUAGE.md](02_UBIQUITOUS_LANGUAGE.md)
- [03_BOUNDED_CONTEXTS.md](03_BOUNDED_CONTEXTS.md)
- [04_INFORMATION_ARCHITECTURE.md](04_INFORMATION_ARCHITECTURE.md)
- [05_SYSTEM_ARCHITECTURE.md](05_SYSTEM_ARCHITECTURE.md)

### Feeds into

As a predecessor for the future documents `07_DATA_MODEL.md`, `08_API_PHILOSOPHY.md`, `09_SECURITY_ARCHITECTURE.md`, and `10_DEPLOYMENT_ARCHITECTURE.md`.

## 12. Governance

- **Status.** Draft until the founders review; only the founders freeze this document after approval.
- **Changes to the role of AI.** Changing AI authority, AI boundaries, or the trust model is a written proposal reviewed by the founders.
- **Traceability.** Every AI capability traces through a system responsibility to a customer problem; an orphan is removed.
- **Consistency.** The AI is bound by the same constitution and the same Glossary as the product; a term against the Glossary is out of compliance.
- **Not a technology decision.** The model, the vendor, the prompt, the agent, the runtime, and the infrastructure are decided elsewhere, not here.

---

*This document is the constitution of AI inside MailSent. It proposes no model and no prompt — it states the authority, the boundary, the reasoning and the trust that bind AI, so that the confidence the AI serves is never an invention.*