# MailSent — AI Requirements

> The behavior and accountability requirements for any AI-enabled capability in MailSent.
>
> This document has **one responsibility**: the requirements that govern how AI behaves and who is accountable for its output. It is not the functional catalog and not the quality-attribute set. It defines the rules AI must follow so that the product's confidence remains grounded.
>
> It is subordinate to the [Project Constitution](../00_Foundation/PROJECT_CONSTITUTION.md), the [Master Workflow](../00_Foundation/MAILSENT_MASTER_WORKFLOW.md), and the [Product Requirements](PRODUCT_REQUIREMENTS.md), and never contradicts them. It applies to any AI-enabled behavior, wherever it appears.

---

**Document governance**

- **Status:** Draft
- **Version:** 1.0
- **Owner:** Founders
- **Review Status:** Pending Founder Review
- **Next review trigger:** Product philosophy changes, or an AI capability changes.

---

## Behavior

- **AI1.** AI must explain. It translates technical input into what happened, why it matters, and what to do, in business language.
- **AI2.** AI must prioritize. It must order what matters most, so the user is not given an unprioritized wall of findings.
- **AI3.** AI must guide. It must propose a fix with a reason and a confidence, so the user can act.
- **AI4.** AI must assist. It must support the user's decision; it must not take meaningful action on its own, and it must not act on a customer's live systems without explicit, informed consent.

## Explainability

- **AI5.** For any conclusion, if a user or auditor asks "why," the underlying evidence and reasoning must be apparent.
- **AI6.** Root-cause and priority statements must be traceable to concrete evidence, not merely asserted with confidence. A conclusion the user cannot verify is a failure of this requirement.

## Confidence

- **AI7.** Any time AI offers a recommendation or guidance, it must indicate its confidence in a way a user can weigh.
- **AI8.** AI must never present a guess, or a plausible-sounding but ungrounded statement, as fact; if it is not sure, it must say it is not sure.

## Transparency

- **AI9.** When AI produces a result, that must be clear to the user; the product must not hide that a model produced it.
- **AI10.** The user must understand the basis for what AI tells them, and must be able to look one layer behind a conclusion to the cause.

## Human accountability

- **AI11.** For anything that affects what a customer believes or decides, a human must own the outcome. AI informs, prioritizes, guides, and assists; it does not decide.
- **AI12.** AI outputs that affect a customer's understanding must be reviewable, and a user must be able to report one as wrong.

---

*This document governs how AI behaves and who is accountable. It states the rules, not the model.*
