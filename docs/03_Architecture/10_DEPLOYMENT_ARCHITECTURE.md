# MailSent — Deployment Architecture (Business)

> The business deployment architecture of MailSent, where **deployment is business readiness** — not infrastructure.
>
> This document governs how the product is released into the customer's world: the philosophy of deployment, release, environment, reliability, rollback, and business continuity. It is not about servers, clouds, containers, or tooling. If the entire platform were rebuilt on different technology, this architecture would still be true.
>
> It is subordinate to the [Project Constitution](../00_Foundation/PROJECT_CONSTITUTION.md), the [Master Workflow](../00_Foundation/MAILSENT_MASTER_WORKFLOW.md), and the [Glossary](../00_Foundation/GLOSSARY.md), and is consistent with the [Domain Model](01_DOMAIN_MODEL.md), the [Ubiquitous Language](02_UBIQUITOUS_LANGUAGE.md), the [Bounded Contexts](03_BOUNDED_CONTEXTS.md), the [Information Architecture](04_INFORMATION_ARCHITECTURE.md), the [System Architecture](05_SYSTEM_ARCHITECTURE.md), the [AI Architecture](06_AI_ARCHITECTURE.md), the [Data Model](07_DATA_MODEL.md), the [API Philosophy](08_API_PHILOSOPHY.md), and the [Security Architecture](09_SECURITY_ARCHITECTURE.md). Terminology uses only the Glossary; every deployment principle traces through the Release Strategy to a customer problem.

---

**Document governance**

- **Status:** Draft
- **Version:** 1.0
- **Owner:** Founders
- **Review Status:** Pending Founder Review

Only the founders may freeze this document after review.

**Next review trigger:**
- Product philosophy changes
- The release strategy changes
- The deployment boundaries change

---

## 1. Purpose

Deployment is the act of bringing the product to the customer's world, ready to serve. It is not the choosing of a machine; it is the decision that the product is ready to be relied upon, and the care with which it is introduced. When deployment is governed well, the business can change safely and the customer is never surprised by a failure. When it is not, a change breaks the trust the whole company exists to protect.

This document is the business deployment architecture. It states how the product is released, what readiness means, how environments and releases are treated, and how the business protects the customer when it must step back. It is a map of readiness and trust, written in the language of the business, and it stays true whatever technology carries it.

## 2. Deployment Principles

- **Business continuity.** The customer's delivery confidence is never interrupted by a change to the product. A release that would break the business is not what the business does.
- **Safe evolution.** The product grows without breaking what the customer already depends on. Change is a journey that keeps the ground firm.
- **Predictable deployment.** A release behaves as expected. The customer is never taken by surprise by what was released.
- **Independent deployability.** A part of the product can be released on its own, without forcing the rest to change with it. The boundaries of the business are the boundaries of a release.
- **Minimal disruption.** The customer's work is disturbed as little as possible by the act of releasing. The least disruption is the goal.
- **Traceability.** Every release is traced to the reason it exists and the problem it serves. A release with no traceable reason is not released.
- **Rollback readiness.** The business is always ready to step back from a release to protect the customer. Readiness to go back is as important as readiness to go forward.

These principles treat deployment as a business action with consequences, never as a mechanical event. They govern a decision as strictly as they govern a line of code.

## 3. Deployment Boundaries

Deployment is bounded by the business, not by a machine. The boundaries follow the structure of the product.

- **The boundary of the capability.** A capability of the product can be released, and only its own boundary holds. One capability does not bring the whole with it.
- **The boundary of the context.** A release respects the owning context of the part of the business it changes. A change does not reach into a context it does not own.
- **The boundary of the customer.** A release does not cross into the customer's live infrastructure without honoring the customer's position in it and the Consent that guards any action.
- **The boundary of the contract.** A release respects the API contract; it does not break a contract a customer relies on (consistent with the API Philosophy).
- **The boundary of the truth.** A release does not change what the business has reported as truth without the truth being kept; the evidence and the Health Signal are never disturbed by a release.

These boundaries say where a release may go and where it must stop. They are the map of readiness, drawn from the business.

## 4. Environment Philosophy

Environments are not machines; they are positions of readiness. Each serves a business purpose.

- **Development.** The place where the product is explored and built. Its purpose is learning; the change is not trusted beyond its maker, and it is never relied on by a real delivery decision.
- **Validation.** The place where a change is shown to hold, before it goes near the customer. Its purpose is evidence: is the change true, is it good, is it ready?
- **Staging.** The place that stands in for the customer's world, before the change is live. Its purpose is to show how the change will arrive, against the shape of delivery.
- **Production.** The place where the customer relies on the product. Its purpose is the delivery of Confidence itself, and a change reaches it only when it has earned the readiness for this place.

The readiness of the environments rises from development to production. A change moves through them only when it is shown ready at each step, and it reaches production only when it can be trusted by the customer.

## 5. Release Philosophy

A release is a promise made with evidence. It is governed by four commitments.

- **Release intent.** Every release states its purpose — the customer problem it is addressing and the value it brings. A release without a stated intent is not a release.
- **Traceability.** Every release is tied to the work it came from, to the problem it solves, and to the decision that approved it (the Golden Rule and the Release Strategy). The reason for the release is known and recorded.
- **Customer trust.** Every release respects the customer's Confidence. It never breaks a contract, never disturbs a truth, and never harms the customer's delivery without telling the customer honestly about it.
- **Reversibility.** Every release can be undone. The business keeps the state as it was, so that if the release harms, it can step back to the ready.

The whole of the release is that the customer can trust the change. A change that has not earned the customer's trust is not released.

## 6. Rollback Philosophy

Stepping back is a business action to protect the customer. It is described here for the things that happen, not for technology.

- **When the delivery health of a customer is reduced by a release,** the business steps back to the state that did not reduce it, and it does not maintain the reduced state.
- **When a customer is proven harmed by a release,** the harm is not downplayed; the release is withdrawn, and the customer is told the truth of what happened.
- **When the evidence a release rests on is shown incorrect,** the conclusion that the release made is not kept; the release is withdrawn or made honest, and the truth is told.
- **When the release has raised business risk beyond what was intended,** the release is halted or stepped back; the business does not carry a risk it did not choose to pay.

The step back is not a failure to hide. It is the very protection of the customer's trust; the business would rather honestly step back than keep a change that is harming.

## 7. Reliability Philosophy

Business reliability is the customer's ability to rely on the product, and it is not a measure of time or machine. It is the state of the promise.

- **The promise must hold.** The customer relies on the product to be truthful and continuous. A release that breaks the continuity of the truth is an unreliability of the business.
- **Reliability is a virtue of the whole.** The Health Signal the product reports, the truth of delivery, and the evidence it holds are each part of the reliability the customer relies on.
- **Reliability is shown, not claimed.** The product earns reliability by what it can trace, tell, and keep; a claim made without the showing of it is not a reliable promise.

The reliability of MailSent is the reliability of the Confidence that holds the whole business, and it is a promise shown by what the product can trace, tell, and keep.

## 8. Evolution Principles

- **Deployment evolves with the business.** The way the product is released changes only when the business responsibility it serves changes, and by the same governance as the rest.
- **The principle before the platform.** The philosophy of deployment is a business matter first and a platform matter later; the value the customer relies on does not change because a platform changed.
- **The readiness is a way of the business, not of the machine.** A change to the readiness of the business is a change to the business; a change of a machine beneath it is not.
- **The map is kept current.** The deployment boundaries and the environments stay consistent with the release strategy and the architecture; a change to those is reflected here.

The deployment of the product evolves with the readiness of the business, never at the pace of a tool.

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
- [08_API_PHILOSOPHY.md](08_API_PHILOSOPHY.md)
- [09_SECURITY_ARCHITECTURE.md](09_SECURITY_ARCHITECTURE.md)

### Feeds into

As a predecessor for the implementation, the technology selection, the infrastructure design, the DevOps, and the operations.

## 10. Governance

- **Status.** Draft until the founders review; only the founders freeze this document after approval.
- **Changes to the deployment.** Changing a deployment principle, a release rule, or a boundary is a written proposal and a review by the founders or the chief architect.
- **Traceability.** Every deployment principle traces through the release strategy to a customer problem; a principle that cannot is removed.
- **Consistency.** The deployment is bound by the Glossary and the ubiquitous language, and it is consistent with the architecture it serves.
- **Not a technology decision.** The cloud provider, the infrastructure, the runtime, the provisioning, the networking, and the databases are decided elsewhere, not here.

---

*This document defines the deployment of MailSent, in the language of readiness and trust. It proposes no infrastructure, no server, and no cloud. It states how the product is released to the customer's world, which the technology will later carry.*