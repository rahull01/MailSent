# MailSent — Non-Functional Requirements

> The complete set of quality attributes the product must satisfy.
>
> This document has **one responsibility**: the non-functional requirements of MailSent. It is not the functional catalog (which lives in [FUNCTIONAL_REQUIREMENTS.md](FUNCTIONAL_REQUIREMENTS.md)) and not the product overview. It states the quality the product must hold across security, reliability, performance, usability, scalability, accessibility, and auditability.
>
> It is subordinate to the [Project Constitution](../00_Foundation/PROJECT_CONSTITUTION.md), the [Master Workflow](../00_Foundation/MAILSENT_MASTER_WORKFLOW.md), and the [Product Requirements](PRODUCT_REQUIREMENTS.md), and never contradicts them.

---

**Document governance**

- **Status:** Draft
- **Version:** 1.0
- **Owner:** Founders
- **Review Status:** Pending Founder Review
- **Next review trigger:** Product scope changes, or a quality standard changes.

---

## Security

- **S1.** The product must apply least-privilege access: every user and role sees only what its role requires.
- **S2.** The product must protect customer data for confidentiality and integrity, and must protect it in transit and at rest.
- **S3.** The product must protect the safety of customer live infrastructure: it must not take action on a production system without explicit, informed consent, and any consented action must be safe and reversible in principle.
- **S4.** The product must validate all inputs and not trust data sent to it by default.
- **S5.** The product must not store secrets in source code, logs, or any place a customer could access accidentally.

## Reliability

- **RL1.** The product must be truthful: any state it reports must trace to a real, observed event. It must never present a guess or a fabricated state as fact.
- **RL2.** The product must not silently lose data or a failure: any loss of reception, storage, or processing must be detected and surfaced.
- **RL3.** The product must fail loudly and gracefully. A component that cannot do its job must say so and degrade predictably, rather than crash obscurely or continue wrongly.

## Performance

- **PE1.** The product must provide diagnosis in a timely manner, so that a problem is actionable before it costs the customer anything.
- **PE2.** The product must perform predictably, not only occasionally well, so that a user can trust its response.
- **PE3.** The product must respect its own resource limits, so that one workload does not starve another.

## Usability

- **U1.** The product must always lead with the business meaning before the technical detail.
- **U2.** The product must answer, for each screen, what happened, why, what it costs, what to do, and what happens if it is ignored.
- **U3.** The product must communicate states clearly, so a user is never guessing about the health of their delivery.
- **U4.** The product must give honest feedback — success confirmed, failure explained, progress shown — with no false reassurance.

## Scalability

- **SC1.** The product must support a portfolio of many domains and many organizations, growing by adding capacity rather than only by making a single instance bigger.
- **SC2.** Each organization's health and data must be isolated, so that one organization's growth or failure does not affect another.
- **SC3.** Growth must not create a cascade that takes down unrelated traffic.

## Accessibility

- **A1.** The product must be usable by people with a range of abilities, with a baseline of contrast, keyboard, and assistive support.
- **A2.** Meaningful information (health, risk, urgency) must not be conveyed by color alone; it must be redundant so that anyone can perceive it.

## Auditability

- **AU1.** Every conclusion the product reports must have an audit trail back to the real data it was drawn from, so a customer can verify it.
- **AU2.** The product must keep a record of what it reported and when, retained with its context, so history is inspectable.
- **AU3.** Matters of significance, including what is not built or decided, must be recorded, so the basis for product decisions stays transparent.

---

*This document states the quality the product must hold. It does not describe how that quality is achieved.*
