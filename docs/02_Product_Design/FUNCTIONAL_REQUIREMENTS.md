# MailSent — Functional Requirements

> The complete functional requirement catalog for MailSent.
>
> This document has **one responsibility**: the engineering-grade catalog of what the product must enable, grouped by capability. It is not architecture, not an API specification, not a UI specification, and not the product overview. Product intent, goals, scope, user types, user flows, success criteria, and acceptance criteria live in the [Product Requirements](PRODUCT_REQUIREMENTS.md).
>
> It is subordinate to the [Project Constitution](../00_Foundation/PROJECT_CONSTITUTION.md), the [Master Workflow](../00_Foundation/MAILSENT_MASTER_WORKFLOW.md), the [Product Vision](../01_Product/PRODUCT_VISION.md), and the [Product Requirements](PRODUCT_REQUIREMENTS.md), and never contradicts them. Each requirement is a statement of what the product must enable, written so that multiple teams could build the same product from it.

---

**Document governance**

- **Status:** Draft
- **Version:** 1.0
- **Owner:** Founders
- **Review Status:** Pending Founder Review
- **Next review trigger:** Product scope changes, or a functional capability changes.

---

## 1. Domain and Portfolio Management

- **D1. Domain addition.** The product must let a user add a domain and observe its delivery health, with clear guidance on any configuration needed to begin health observation.
- **D2. Multiple domains.** The product must hold and distinguish many domains for one organization, and must present each domain's state individually.
- **D3. Multiple organizations.** The product must let a single user relate to more than one organization, and must keep each organization's domains and health separate.

  > **Governing clarification (CTO-approved, 2026-08-09):** D3's reference to a user relating to more than one organization describes organization-level agency/client/portfolio relationships and authorized cross-organization scope; it does not mean that a User belongs to multiple Organizations. User membership remains exactly one home Organization as defined by D6 and the Domain/Data models.
- **D4. Portfolio overview.** The product must present the health and risk across an organization's domains, and for a management organization across all its clients, so the user can see at a glance who is at risk.
- **D5. Access control.** The product must let an organization limit who on its team can view and act, with roles that reflect who can see health and who can make changes.
- **D6. Signup establishes one Organization.** A successful new user signup must create exactly one Organization, assign the signing user to it, and leave that user belonging to exactly one Organization. That Organization becomes the user's initial ownership and data-isolation boundary.

## 2. Health Measurement

- **H1. Health signal.** The product must produce a unified, legible health indication for a domain that reflects the state of its delivery infrastructure.

  > **CTO freeze (HEALTH-001 interpretation, 2026-08-15):** Canonical term is **Health Signal** (not Trust Score). Unified indication: **healthy** | **at-risk** | **unknown**. No scoring model, 0–100, weights, G/Y/R, or Trust Score. Missing any V1 required snapshot (latest OE, latest DMARC Aggregate Evidence report, at least one Reputation Observation row): persist nothing. When snapshots exist, indication is assigned from cited V15 tokens only: at-risk on any `disposition` quarantine/reject or auth/alignment `fail`; healthy only when every cited row has disposition `none` and SPF+DKIM result+alignment all `pass`; otherwise unknown (including SPF-pass/DKIM-none). OE and Reputation Observation are lineage-required presence, not voters. Current Java always-unknown is insufficient vs this freeze; Java is not implemented in this docs freeze. FUN-H1 is **IMPLEMENTATION-READY** for the three values.

- **H2. Health components.** The health indication must reflect the several signals that determine delivery, including authentication, the alignment of those signals, and the associated reputation.

  > **CTO freeze (DIAG-001, 2026-08-15):** DIAG-001 Alignment derivation traces to Analyzer / Alignment Engine semantics and **H2** (health includes alignment of authentication signals). DIAG-001 does not create Health Signal; HEALTH-001 produces Health Signal. Alignment of authentication signals is a derived value, not an Issue.
  > **CTO freeze (HEALTH-001 interpretation, 2026-08-15):** H2 is a **meaningful unified indication** plus **cited** persisted facts — Observation Evidence (V8 MX/TXT/SPF/observedAt/identity — lineage presence only; DNS strings are not SPF-pass), **DMARC Aggregate Evidence** (the only indication voter: stored `disposition`, `spf_result`, `dkim_result`, `spf_aligned`, `dkim_aligned`; INT-004 tokens pass/fail/none/quarantine/reject as stored), and Reputation Observation (V9 source + opaque payload — **lineage-required presence only, not an indication voter**; do not parse payload for grades). Not DIAG-001 Alignment VO / `lastDerived()`. No Alignment table. No separately persisted component scores. Conservative: SPF-pass with DKIM-none is **unknown**, not healthy.
- **H3. Reputation signals.** The product must incorporate external reputation signals where they can be observed, as one part of health.

  > **CTO freeze (HEALTH-001 interpretation, 2026-08-15):** H3 intake for HEALTH-001 is **presence** of at least one persisted Reputation Observation (V9). Opaque `payload` is not parsed into a grade. Reputation does not vote healthy/at-risk. Unavailable production adapters persist no row ⇒ persist no Health Signal.
- **H4. Health trend.** The product must show how health changes over time, up to a historical view, so a trend (improving, stable, declining) is visible.
- **H5. Industry context.** The product may contextualize health against a benchmark, and only when real, anonymously available data supports it; it must never present a fabricated rank.

## 3. Diagnosis and Root Cause

- **X1. Cross-signal diagnosis.** The product must reason across the signals it assesses and identify the root cause of a delivery failure, not merely report each signal's state.

  > **CTO freeze (DIAG-001, 2026-08-15):** **X1** belongs primarily to root-cause diagnosis (**DIAG-003**). DIAG-001 is Analyzer-class Alignment derivation, not X1, not X2 (sender detection / DIAG-002), and not X3 (root cause stated / DIAG-003). Do not invent new FUN IDs.
- **X2. Sender recognition.** Where it affects delivery, the product must recognize which sender is involved and how that sender's authentication aligns with the domain.
- **X3. Root cause stated.** The product must express, in concrete terms, why delivery is at risk. A stated cause that is wrong, or that names only a symptom, is a failure of this requirement.

## 4. Business Explanation

- **E1. Plain-language explanation.** The product must explain a delivery issue in the language of a business, so a non-technical user understands what happened.
- **E2. Why it matters.** For each issue, the product must state why it matters to the business, in terms of impact and risk, before any technical detail.
- **E3. A deeper view.** For a user who wants the mechanism, the product must offer the underlying technical reality one layer deeper, without forcing it on the business user.
- **E4. Simplicity on demand.** The product must offer the ability to explain an issue in simpler terms, for a user who wants less technical detail.

## 5. Impact and Prioritization

- **P1. Impact scoring.** The product must score the business impact of each issue it identifies, so a user can see which matters most.
- **P2. Prioritization.** The product must present issues in order of priority, so a user knows what to do first, rather than an unprioritized list of equal findings.
- **P3. Cost of ignoring.** The product must state, in business terms, what is at stake if an issue is ignored, so a user can weigh inaction honestly.

## 6. Guidance and Resolution

- **G1. Action preparation.** The product must present a clear, prioritized set of actions a user can take to resolve the identified issues.
- **G2. Step-by-step guidance.** For each action, the product must provide step-by-step direction a user can complete, including any exact value to apply where that is relevant.
- **G3. Guidance suited to the environment.** Provided guidance must reflect where the user's configuration actually lives, so the instructions match the user's real context.
- **G4. Progress.** The product must let a user work through the open actions and record progress.
- **G5. Fix verification.** The product must verify that a fix holds after it is applied, or state clearly that it could not yet be verified.

## 7. Continuous Monitoring and Alerting

- **M1. Ongoing observation.** The product must observe delivery health over time on a recurring basis, so health is a maintained state and not a single moment.
- **M2. Change detection.** The product must notice when something that affects delivery has changed, and reflect that change clearly for the user.
- **M3. Issue detection.** The product must detect when an issue appears that can put delivery at risk, and surface it.
- **M4. Business-language alerts.** Alerts and notifications must be stated in business language, telling the user what happened, why it matters, and whether to act — not leading with raw technical terms.
- **M5. Alert severity.** The product must classify risks by severity so a user can judge urgency.
- **M6. Alert channels.** The product must deliver alerts through channels where the user actually is, chosen by the organization.
- **M7. Alert control.** The user must control what they are alerted to and how, including a digest option, so alerting does not become noise.

## 8. Reporting and Evidence

- **R1. Executive summary.** The product must provide a recurring, executive summary of health in plain business terms.
- **R2. Evidence of health.** The product must support producing a report that demonstrates the state of a domain or portfolio, so a user can show it to a client, auditor, or board.
- **R3. Report scope.** The product must produce a report for a single domain or for a portfolio, depending on the user's need.
- **R4. Trustworthy evidence.** Every piece of evidence must trace to a real observation. A report that states a health conclusion not grounded in real data is a failure of this requirement.

## 9. Readiness and Compliance

- **C1. Readiness assessment.** The product must assess a domain's readiness against the requirements of the mailbox companies that decide delivery, and present it in business terms.
- **C2. Requirement checklist.** The product must present readiness as a set of legible requirements, each satisfied or not, with the meaning for the business.
- **C3. Readiness output.** The product must let a user produce a statement of readiness, for an auditor or a partner, that is grounded in real assessment.

## 10. Feedback and Adoption

- **F1. Usefulness gathering.** The product must capture whether an explanation or guidance was genuinely useful to the user, without interrupting their work.
- **F2. Problem reporting.** The product must let a user easily report when a result seems wrong or unhelpful, so that quality can be improved.
- **F3. Adoption awareness.** The product must help operators know whether their team is actually using the guidance, so that value is not left unused.

---

*This document is the functional requirement catalog. It states what the product must enable, capability by capability, and nothing else.*
