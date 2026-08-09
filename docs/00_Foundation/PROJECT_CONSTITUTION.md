# MailSent — Project Constitution

> **Authority: Highest.** This is the supreme governing text of MailSent. Every human and AI agent must read it before writing a line of code and must treat it as the final arbiter when requirements, conventions, or instincts conflict. Where a specific instruction contradicts this constitution, the constitution wins — and the conflict must be surfaced, not silently resolved.
>
> **Version:** 3.1 · **Status:** Ratified by the Founding Principal Software Architect, Chief Product Architect, and Engineering Culture Designer · **Audience:** All humans and AI agents on MailSent, now and for the next decade.
>
> **Companion.** This document defines *culture and philosophy* — the principles that endure. [MAILSENT_MASTER_WORKFLOW](MAILSENT_MASTER_WORKFLOW.md) defines *product and execution* — modules, pricing, feature scoring and version gates, the development pipeline, timelines, and V1 exclusions live there and may evolve freely. The workflow is the source of **product truth**; this constitution is the source of **engineering truth**. Where they appear to conflict, resolve by [Section 3.5](#35-identity-and-conflict-resolution).

---

## The Golden Rule — The First Governing Rule

> **"No feature is approved because it is technically impressive. Every feature must solve a validated customer problem and create recurring value. If not — reject."**

This is the first and highest governing rule of MailSent. It outranks every other consideration in this document, because it is the difference between shipping the impressive and shipping the important. Engineering excitement is seductive and cheap: any competent engineer can build a clever widget, but a clever widget no customer needs is a tax on the future — time, complexity, and attention stolen from the one feature that would have earned recurring value. The Golden Rule is the permanent counterweight to that excitement.

It applies to **everything** — every module, integration, internal tool, and AI enhancement. Before anything is built, ask with evidence:

> **"Will a customer pay a monthly subscription for this, for 12 months or more?"**

If no — reject. No debate, no exceptions. Technical impressiveness is never a reason to build; a validated problem that creates recurring value is the only one.

---

## How to Use This Document

- **Read it fully once** before contributing anything to the repository.
- **Consult it** whenever you are uncertain about a standard, trade-off, feature decision, or review.
- **Quote it** when explaining a decision; citing the constitution is the strongest justification in the repo.
- **Let it govern AI agents as strictly as humans.** It binds both with the same force.
- **Evolve it deliberately** — see [Section 29, Amendment Process](#29-amendment-process).

---

## 1. Vision

We are building **MailSent** to become the trust layer of email — the platform a business reaches for when a single undelivered email can cost it a customer, a contract, or its reputation. A decade from now, the answer to a business owner's quiet question — *"Am I sure my emails reach the inbox?"* — should be a confident **yes**, backed not by hope but by visible, verifiable intelligence. We do not aim to own the email; we aim to be the most dependable intelligence layer in front of it, guaranteeing the conditions for delivery before a message is at risk.

## 2. Mission

MailSent's mission is to **sell confidence** — the certainty that email delivery is healthy, monitored, and fixable before it fails. We exist to **reveal** what is breaking delivery, **explain** why it matters in business language, **guide** a prioritized fix, **guard** continuously so problems surface before customers notice, and **earn trust** through inspectable, verifiable conclusions. If a feature does not increase a customer's confidence that email reaches the inbox, it does not serve the mission — and it is not built.

## 3. Product Identity

### 3.1 Who We Are

MailSent is an **AI Email Infrastructure Intelligence Platform** — the intelligence layer that reads a business's email infrastructure (DNS, SPF, DKIM, DMARC, MX, reputation), detects what is breaking delivery, explains it in the language of business, and guides the fix before customers ever notice. We are not a tool that checks one thing; we are a platform that reasons across everything that determines inbox delivery and acts on it.

### 3.2 Who We Are Not

This identity is final. MailSent is **not**: a **DMARC checker** (one signal among many; a single-check tool stops where our reasoning begins), a **warmup tool** (commoditized and legally gray — it sells a shortcut, not confidence), a **marketing automation platform** (we do not send campaigns; we protect what a business sends), or **generic email software / a communication platform** (we do not own the channel; we ensure it is trustworthy). Clarity about what we are not is what makes our promise about what we are trustworthy.

### 3.3 Personality

MailSent speaks with **clarity, honesty, and calm**. Product, copy, errors, and code are direct; we never hide a failure behind "something went wrong." We tell the truth in plain business language at the moment it matters most — because a business that understands its situation is a business that can act.

### 3.4 What "Sent" Means to Us

The word in our name is a promise, not a boast. "Sent" does not mean "handed to a server and forgotten"; it means **delivered to the inbox, verified, and accounted for**. Anything less is not MailSent. Our entire platform exists to make that promise true — before the customer's customers are the first to know a problem exists.

### 3.5 Identity and Conflict Resolution

MailSent **sells confidence**. Reliability is not what we sell; it is the standard we hold over **our own intelligence system** — because a customer's entire business decision rests on what we report, so our platform must itself be reliable, truthful, observable, and secure. We earn the confidence we sell by making our own intelligence dependable.

When this constitution and the Master Workflow conflict: **the workflow is product truth (what we build and for whom); this constitution is engineering truth (how we build it).** Where a conflict touches *what the product is*, the workflow governs; where it touches *how we build to world-class standard*, this constitution governs. When in doubt, surface and resolve the conflict in writing — never silently. By design, the workflow changes often as the product and market evolve; the constitution changes rarely and only through [Section 29](#29-amendment-process).

### 3.6 The Product DNA — The Natural Flow

MailSent's product is one ordered, natural flow, and the order is philosophy, not preference:

**Scanner → Analyzer → Alignment Intelligence → AI Explanation → Business Impact → AI Action Queue → Guided Fix → Continuous Monitoring → Alerts → Reports → Agency Dashboard**

The flow moves **data → understanding → meaning → decision → action → verification → communication → scale**. Each stage is the necessary foundation for the next:

- **Scanner** — raw facts (DNS, DMARC ingestion, blacklists). We cannot explain what we have not observed; truth first.
- **Analyzer** — validated signals (SPF/DKIM/DMARC parsing, alignment). We cannot reason over noise.
- **Alignment Intelligence** — sender detection and root cause. The moat: where analysis becomes insight.
- **AI Explanation** — business-language translation. Only after true cause is known can we speak plainly.
- **Business Impact** — risk scoring and prioritization. Business language means nothing without a stake.
- **AI Action Queue** — a prioritized do-now list. Unprioritized action is busywork.
- **Guided Fix** — step-by-step resolution. A haphazard fix masks the underlying state.
- **Continuous Monitoring** — verification and ongoing health. A fix we do not verify is a guess.
- **Alerts** — escalation to where humans live, in business language.
- **Reports** — recurring executive summary. Trust compounds with cadence.
- **Agency Dashboard** — scaling the proven loop across many clients.

**Why changing this order damages the philosophy:** every stage depends on the one before it. Leap to AI Explanation without root cause and you produce plausible-but-ungrounded claims (a truth violation); leap to Guided Fix without impact and you deliver busywork; leap to the Dashboard before the single-client loop is sound and you scale a broken loop. The order encodes **truth before claims, and value before action**. Protect it.

## 4. Customer Promise

We promise every customer: **you will know exactly what is breaking your email delivery, why it matters to your business, and how to fix it before customers notice.** Supporting promises make that primary one trustworthy:

1. **We never guess about delivery.** If we do not know a true state, we say so and tell the customer how to find out.
2. **We never lie in an error.** Every reported failure is accurate, specific, and actionable — no opaque codes, no swallowed failures.
3. **We make the invisible visible.** Authentication, alignment, reputation, and health are surfaced through an honest, inspectable interface.
4. **We speak business first, technology second.** The consequence is understood before the mechanism.
5. **We respect the customer's data and people.** Privacy, consent, and security are built in from line one, not retrofitted.

## 5. Core Philosophy

- **Confidence is the product.** We do not sell DMARC checks or reports; we sell confidence — that emails reach the inbox, infrastructure stays healthy, problems surface early, and teams know exactly what to fix. If a feature does not increase that confidence, reject it.
- **Truth over comfort.** We report what is real, not what is reassuring. A status that lies to soothe is a bug; reality, surfaced promptly, is the only thing a business can act on.
- **Business language, not jargon.** Technical accuracy is not the same as useful communication. The business meaning always comes first; the mechanism always follows.
- **Early detection over reaction.** A misalignment caught before a campaign is worth a hundred times the same one discovered after complaints.
- **Focus and the long haul.** We execute a narrow set of capabilities with obsessive quality, prefer proven patterns over novelty, and write every line as a message to a future engineer. Focus compounds; sprawl and cleverness decay.

## 6. Golden Rules

The Golden Rule at the top — *solve a validated customer problem and create recurring value, or reject* — is the first and supreme governing rule. These supporting rules flow from it:

1. **Solve a validated problem, or reject.** Technical impressiveness is never a reason to build.
2. **Sell confidence.** We exist to give certainty that emails reach the inbox and problems surface early.
3. **Never fabricate truth.** If we do not know, we say we do not know.
4. **Inspectability is sacred.** Every state and conclusion we report must be observable and explainable.
5. **Trust is earned through verification.** Nothing ships on faith — about the customer's problem or our own system.
6. **Respect the message and its people.** Privacy, consent, and security come before convenience.
7. **Leave the camp cleaner than you found it.** Improve what you touch; never degrade it.

## 7. Non-Negotiable Rules

Absolute. No exception, no override — not for deadlines, executives, or excitement. If a feature cannot ship without violating one of these, it does not ship.

- **N1 — No unvalidated feature.** Nothing ships unless it solves a validated problem and creates recurring value. (The Golden Rule made non-negotiable.)
- **N2 — No fabricated or guessed delivery state.** Reported states trace back to a real, observed event.
- **N3 — No silent data loss.** Failures are detected, logged, and surfaced — never dropped.
- **N4 — No secrets in code or repos.** Credentials, keys, tokens, and PII never appear in source, logs, docs, or fixtures.
- **N5 — No untested production path.** Anything affecting what we report or diagnose requires automated tests.
- **N6 — No lying to the user.** Errors, reports, and UI always describe reality.
- **N7 — No bypassing the approval framework.** Features that skip [Section 20](#20-feature-approval-framework) do not ship.
- **N8 — No security review shortcuts.** Anything touching customer data or the diagnostic path passes security review.

## 8. Engineering Principles

Our system reports to real businesses, so our engineering must justify that trust.

- **Simplicity before cleverness.** The simplest inspectable design that meets the requirement beats a clever one; cleverness is a code smell until proven necessary.
- **Explicit over implicit.** Prefer explicit configuration, state, and contracts over magic.
- **Small, focused components.** One reason to exist, one job, explainable in a few sentences.
- **Fail loudly and gracefully.** Say so and degrade predictably — never crash obscurely or continue wrongly.
- **Measure before you optimize.** Performance work follows real measurements, not intuition.
- **Reduce, then abstract.** Extract a pattern only after it appears more than once.
- **Prefer boring, proven technology.** Choose well-understood tools unless a proven need justifies the risk.
- **Correctness before speed.** A fast wrong answer is worse than a correct slow one; never sacrifice diagnostic accuracy for throughput.

## 9. Product Principles

### 9.1 The Feature Philosophy Chain

Every feature must satisfy this chain, in order, **before engineering begins**: **Real Customer Problem → Business Impact → Recurring Value → Retention → Competitive Advantage → Engineering Cost.**

- **Real problem** maps to a validated pain point (ideally from the research and advisory work defined in the Master Workflow).
- **Business impact** protects or earns customer revenue; if it does not move money, it does not move us.
- **Recurring value** is worth paying for month after month; a one-time trick is a check, not a platform.
- **Retention** makes the customer likelier to stay at month twelve.
- **Competitive advantage** beats what incumbents already do; parity is not a reason to build.
- **Engineering cost** is weighed only after the five above — value precedes expense.

If any link fails, the feature is deferred or rejected.

### 9.2 Customer Hierarchy

MailSent is built in order. **P0 — Primary:** digital marketing agencies, email marketing agencies, MSPs, and multi-domain SaaS companies. **P1 — Secondary:** e-commerce brands, higher-revenue Shopify stores, B2B companies, and startups.

**Why we start with agencies and multi-domain businesses:** they manage many client domains at once, so they feel delivery failures multiplied across every client — making the problem urgent, recurring, and worth paying to solve. They are also the channel to scale: one agency protecting a hundred domains is a hundred defensible reasons to subscribe. We earn the confidence of the few who need it most first, prove the model, then expand to the many. Serving everyone before proving the platform to our core is how focus dies; we do not scatter.

### 9.3 Further Principles

- **Deliver value continuously.** Ship small, useful increments, not a monolithic reveal.
- **Design for the critical path.** The path from *signal in* to *business answer out* — the heart of confidence — is the smoothest, most tested, most optimized flow.
- **Failures are first-class.** Diagnosis and error handling are product features, as important as the success path.

## 10. UX Principles

MailSent is built for **business users** — agency owners and operators whose job is not to read DNS records but to protect deliverability.

### 10.1 Every Screen Answers Five Questions, in Order

Every screen, widget, and alert must answer, in this exact order:

1. **What happened?** A clear, plain-language statement of the state or event.
2. **Why?** The cause, understood by a non-technical person.
3. **What is the business impact?** What this costs today — revenue, reputation, risk.
4. **What should I do?** The concrete, prioritized next action.
5. **What happens if I ignore it?** The honest consequence, so the user decides with full information.

### 10.2 Business First, Technology Second

No jargon appears before the business explanation. The customer sees the consequence and the fix in plain language; the mechanism is always one layer deeper. We never lead with *"SPF include exceeded 10 lookups"* — we lead with *"password reset emails may not reach your customers"*.

### 10.3 Further Principles

- **Clarity beats cleverness.** The user always knows what is happening, what it means, and what to do next.
- **Honest feedback, always.** Success confirmed; failure explained; progress shown. No false reassurance.
- **Design for the decision, not the click.** Move the user toward a confident choice, not more options.
- **Guide the newcomer, trust the expert.** Safe defaults; advanced control available without getting in the way.

## 11. UI Design Principles

- **Consistency is a contract.** Visual language, spacing, typography, and components are consistent so users trust what they see.
- **Whitespace is a tool.** Deliberate spacing signals importance and reduces cognitive load.
- **Color communicates, never decorates alone.** Healthy/warning/critical carry meaning and are never the only channel — accessibility requires redundancy.
- **Accessible by default.** Contrast, keyboard navigation, screen-reader semantics, and touch targets meet accessible standards as a baseline.
- **Hierarchy through typography, not size inflation.** The most important information is the most prominent.
- **Interfaces are honest.** A button does what it says; a status shows what is true — especially the health of a customer's infrastructure.

## 12. AI Principles

AI is central to MailSent's identity — it is an *AI* Email Infrastructure Intelligence Platform. But AI is a servant of the philosophy, never its master. Every AI in the product is bound by these rules:

- **AI never replaces thinking.** It sharpens human judgment; it does not substitute for it. Reasoning that guides a customer's decision must trace to sound analysis, not unexamined model output.
- **AI never fabricates truth.** It must never present a guess or plausible-sounding conclusion as fact. The truth principle is absolute, even for the model.
- **AI explains.** Its highest value is translating technical signals into business language — turning *what* into *why it matters*.
- **AI prioritizes.** It orders what matters most, so the user never faces an unprioritized wall of noise.
- **AI guides.** It recommends fixes and next steps with confidence and rationale.
- **AI assists.** It amplifies the user's ability to act; it never acts on its own in matters of real consequence.
- **Humans remain accountable.** Every AI output that affects what a customer believes must be visible, attributable, and reviewable. No AI output is silently authoritative.

**Why:** AI's value is only as real as its grounding. A platform that sells confidence cannot afford an AI that invents an answer, because one confident hallucination destroys the trust the entire business is built on. We make AI explain, prioritize, guide, and assist — and keep humans accountable — because that is the only way an AI-powered confidence platform can remain worthy of the word *confidence*.

## 13. Architecture Principles

The architecture embodies the Product DNA: a clean, ordered flow from observation to action, with each stage decoupled so it can be built, tested, and scaled independently.

- **Separation of concerns.** Boundaries follow the natural flow — ingest, analyze, align, explain, prioritize, fix, monitor, alert, report, present.
- **Reliability at every layer.** Every component assumes those below it may fail, and handles that failure without corrupting the truth it reports.
- **Observability is built in.** Logging, metrics, and tracing are part of the design from the start, because what we report must itself be inspectable.
- **Stateless where possible, durable where it counts.** Compute is replicable; diagnostic state that matters is stored durably.
- **Make state explicit and inspectable.** Persistent state is identified, versioned, and readable.
- **Composability over monoliths.** Smaller, independently understandable pieces aid testing, scaling, and reasoning.
- **Design for failure as the default.** Outages and retries are normal; the architecture embraces them.
- **Contracts before implementation.** Interfaces — especially across analyzer, alignment, and AI layers — are agreed before deep implementation.

## 14. Documentation Principles

- **Document the why.** Code tells what; documentation explains why a thing exists and is shaped this way — what survives the context that created it.
- **Documentation is part of the deliverable.** A feature is not done until its docs are done.
- **Docs live with the code.** They sit beside what they describe so they change together and drift less.
- **Docs serve the reader who has a question**, not the writer proving they wrote it.
- **Keep it true or keep it out.** A stale or wrong document is worse than none.

## 15. Security Principles

MailSent handles sensitive customer infrastructure. Security is existential, not procedural.

- **Security is a design constraint, not a phase.** Considered from the first sketch, never bolted on.
- **Least privilege.** Every process, user, and component gets the minimum access needed — critical for anything touching customer DNS or mail infrastructure.
- **Defense in depth.** Multiple independent controls protect sensitive paths.
- **Encrypt in transit and at rest** as a default posture.
- **Never trust, always verify.** Inputs, outputs, and identities are validated, not assumed safe.
- **Fail closed on doubt.** When uncertain, default to the safer outcome.
- **Secrets are never in the repository** ([N4](#7-non-negotiable-rules)).
- **Privacy is an engineering requirement.** Data minimization, retention limits, and consent are designed in.
- **Never harm a customer's live infrastructure.** Actions with real consequence on production systems require human confirmation and consent.

## 16. Performance Principles

- **Latency is a feature of confidence.** A slow diagnosis is not timely, and timeliness is the value proposition.
- **Measure with evidence.** Optimize what is real, not what is imagined.
- **Predictable over merely fast.** Consistent performance beats occasional bursts.
- **Bounded resources.** Components declare and respect limits so one workload cannot starve another.
- **Optimize the critical path first.** The diagnostic flow from signal to business answer is the hot path.
- **No premature optimization.** Write clear, correct code first; optimize where measurements prove it matters.

## 17. Scalability Principles

Customers are agencies and multi-domain businesses, so scale is intrinsic — one customer may mean a hundred domains.

- **Design for growth from day one.** No component paints itself into a corner where horizontal growth is impossible.
- **Scale out, not just up.** Add capacity with more instances, not only a bigger one.
- **Fail independent of scale.** Load growth must not cascade into unrelated traffic.
- **Backpressure is a design feature.** At capacity, manage load with clear signal, not unbounded queueing and silent loss.
- **Data growth is anticipated.** Health history grows without end; storage and retention are designed for it.

## 18. Code Quality Standards

- **Readable over terse.** Code is written for the human who must debug it at 2 a.m.
- **Meaningful names.** Names reveal intent; obscurity is the exception.
- **Small functions and clear structure.** Each function does one thing, understood at a glance.
- **No dead code.** Unused code, commented-out blocks, and abandoned branches are removed.
- **Consistent formatting**, enforced by tooling so it is never a review debate.
- **Reviewed and tested code only.** Nothing merges without review and automated tests.
- **The code expresses intent.** When unclear, refactor; comments explain *why*, not *what*.

## 19. Definition of Done

A feature, fix, or task is **Done** only when **all** of the following are true:

1. **Validated.** It solves a real problem and creates recurring value (the Golden Rule).
2. **Works.** Implemented and functioning as specified.
3. **Tested.** Automated tests cover the behavior, failures, and edge cases.
4. **Verified.** The author ran the checks and they pass, with evidence.
5. **Reviewed.** A reviewer approved it.
6. **Documented.** The needed documentation is accurate and in place.
7. **Observable.** New behavior can be monitored, logged, or traced.
8. **Secured.** It passed security review where it touches customer data or the diagnostic path.
9. **Honest.** It reports state truthfully — and if it involves AI, it is grounded and attributable.
10. **Committed and clean.** It is on the correct branch, with no dead code, secrets, or regressions.

If any one is false, the work is **not done** — it is in progress. Calling it done is a violation of [N6](#7-non-negotiable-rules).

## 20. Feature Approval Framework

Not every good idea gets built — especially technically impressive ones. Every feature passes an explicit gate before resources are committed.

### 20.1 The Governing Questions

Two questions must be answered with evidence first: **(1)** Does it solve a validated problem and create recurring value (the Golden Rule)? **(2)** Will a customer pay a monthly subscription for 12+ months? If either is no, reject — no further scoring needed.

### 20.2 Weighing Value Against Cost

The philosophy of approval is that a feature's value must be weighed against its cost across five lenses: whether customers would pay for it, whether it keeps them to month twelve, whether it protects or earns revenue, whether it beats the incumbents, and what it honestly costs to build. **The exact scoring model, thresholds, and version gates live in the Master Workflow** and may evolve as we learn. The principle here — value precedes cost, and approval is a disciplined weighing — does not change.

### 20.3 Decision Rules

- Missing a validated customer problem → **deferred**, not approved.
- Does not advance the mission of selling confidence → **rejected**.
- Cannot ship without violating a Non-Negotiable Rule → **rejected**.
- Engineering cost not honestly understood → **deferred**.
- Merely matches a competitor, with no advantage → **rejected**. Parity is not a reason to build.

### 20.4 The Point of the Gate

The gate exists to say **no — or not yet — with a reason**, protecting focus and capacity. It is not paperwork; it is the mechanism that keeps MailSent aligned under relentless pressure to build everything. Saying no is a feature.

## 21. Decision Framework

Every decision — product or engineering — flows from the customer outward. The order is fixed, and reversing it is how companies build the wrong thing beautifully:

**Customer → Business → Product → Architecture → Engineering → Implementation**

- **Customer.** Who is this for, and what validated problem do they have?
- **Business.** Does it serve the mission and create recurring value?
- **Product.** How does it fit the Product DNA and the natural flow?
- **Architecture.** How is it placed cleanly, observably, and durably?
- **Engineering.** How is it built to world-class standard?
- **Implementation.** The concrete work, only after every layer above is settled.

**Why not the reverse:** starting with implementation and hoping the business and customer work out later produces technically impressive but commercially hollow output — precisely what the Golden Rule forbids. Making the customer's truth the first constraint aligns everything below it. Reversed, the customer is the last thing considered; the customer should be first.

## 22. Development Workflow

We build every feature through a disciplined, gated sequence that moves from validation to approval, anchored at each step to truth and value — not to convenience. **The exact pipeline stages live in the Master Workflow** and may evolve as the product matures. What does not change is the philosophy that governs the sequence: a feature is grounded in research before it is specified, agreed by contract before it is built, tested before it is integrated, reviewed for production readiness, and only then approved. Each step protects a truth or a value, so no step is optional — skipping one skips the protection it exists to provide. The pipeline is a loop, not a gauntlet; each pass should get smoother.

## 23. Git Workflow

- **Work on branches**, not the main branch; branch names state their purpose.
- **Commit in small, logical units**, each coherent and standing alone, with a *what* and *why* message.
- **Keep the main branch shippable.** Broken builds and unfinished work never land on it.
- **Review before merging.** Changes reach the main branch only through review ([Section 26](#26-review-checklist)).
- **Never rewrite shared history.** Force-pushing to shared branches is prohibited.
- **Refer to, don't store, secrets.** Git history is permanent ([N4](#7-non-negotiable-rules)).
- **Use atomic history.** Each merged change is one reviewable, revertable unit.

## 24. Testing Standards

- **Test behavior, not implementation.** Verify the system does the right thing — including in failure.
- **Test the failure paths.** For a confidence product, failures are as important as successes — edge cases, timeouts, retries, outages.
- **Test the accuracy of the diagnosis.** Because we tell customers *why* delivery is broken, we test that conclusions are correct, not just that the system runs.
- **Automate what is meaningful.** Regression-preventing tests run regularly, not on a whim.
- **Make tests deterministic and independent.** They pass for the right reason, every time.
- **Tests are code too.** They follow the same quality standards as production code.
- **A test that does not fail when broken is a lie.** It must be fixed.
- **Keep the suite runnable.** A suite nobody runs protects nobody.

## 25. Documentation Standards

- **Write in the product's voice:** clear, honest, direct, and business-first where it faces customers.
- **Every repository has a reading entry point** for newcomers.
- **Documentation is current or it is gone.** Outdated docs are corrected or removed.
- **Keep naming and structure consistent.**
- **Sensitive information never appears in docs.** Docs obey the same secrets rules as code.
- **Document architecture and decisions** so future contributors understand why the system is shaped as it is ([Section 21](#21-decision-framework)).
- **Reference, don't duplicate, the constitution and the workflow.** Principles are cited, not redefined, so standards never drift.
- **Record rejections** in `REJECTED_FEATURES.md` ([Section 27.2](#272-the-kill-list)) so future proposals must overcome them with new evidence.

## 26. Review Checklist

Every change is reviewed against this checklist; a reviewer must affirm each line.

- [ ] **Validated** — solves a real problem and creates recurring value, not merely impressive.
- [ ] **Correct** — does what it claims, including edge and failure cases.
- [ ] **Reliable** — no new failure modes; weakens no guarantee about the truth we report.
- [ ] **Honest** — reports state truthfully; never misleads the user.
- [ ] **Observable** — behavior can be logged, monitored, or traced.
- [ ] **Secure** — no secrets, no data leaks; passed security scrutiny.
- [ ] **Tested** — covers behavior, failures, and diagnostic accuracy.
- [ ] **Documented** — accurate and in place.
- [ ] **Clean** — no dead code, regressions, or bundled unrelated changes.
- [ ] **Consistent** — follows the constitution and surrounding conventions.
- [ ] **Small enough** — a coherent, reviewable unit.

A review that cannot affirm every line is a request for change, not an approval.

## 27. What We Never Build

MailSent has a permanent list of what it will never build. Some items are excluded forever because they contradict our identity; others now because they do not yet satisfy the Golden Rule. Every exclusion is a statement of what we are *for* by being against something else.

### 27.1 Identity Exclusions — What MailSent Never Is

- **Never a DMARC checker** — one signal among many; a single-check tool stops where our reasoning begins.
- **Never a warmup tool** — manipulation of reputation, not confidence; commoditized and legally gray.
- **Never a marketing automation platform** — we do not send campaigns; we protect what a business sends.
- **Never generic email software or a communication platform** — we do not own the channel; we ensure it is trustworthy.

### 27.2 The Kill List

Every rejected feature is recorded in a living **`REJECTED_FEATURES.md`** — feature name, reason, date, and any revisit condition. The Kill List is permanent governance.

**Why saying no is an engineering capability:** the discipline to refuse is as important as the skill to build. Every rejected feature is time and complexity saved for the ones that earn recurring value. A team that cannot say no drowns in its own good intentions; a team that says no with reasons becomes focused by design.

**Why focus creates billion-dollar companies:** focus is the compound interest of product. Dominance in one category compounds into scale; mediocrity in many does not. MailSent's path is to be trusted more deeply than any platform for one thing — confidence in email delivery — and the Kill List is the guardrail that keeps us on it.

### 27.3 The V1 Exclusions

The Master Workflow lists the current exclusions — auto DNS updates, revenue prediction, competitor benchmark, deliverability simulator, warmup, AI-washing, integration sprawl, mobile app, and Chrome extension. Their reasons are philosophy, not accident, and the deepest ones are stated here: **we will not touch a customer's live DNS without ironclad consent and safety; we will not present a prediction or rank we cannot ground in real data; we will not call something AI that is not genuine intelligence; and we will not integrate anything that does not earn its place through the Feature Formula.** See the Master Workflow for the full, current exclusion list and its revisit gates.

### 27.4 The Standing "Never"

Absolute for all time:

- **Never guess about delivery** — no fabricated statuses, no hopeful "delivered," no invented ranks.
- **Never silently lose a message or signal.**
- **Never lie to a user** — no false reassurance, dark patterns, or AI-washing.
- **Never trade honesty for engagement.**
- **Never sacrifice security for speed** — including the safety of a customer's live infrastructure.
- **Never ship untested diagnostic logic.**
- **Never build unobservable systems.**
- **Never build features that violate the mission** — anything that does not increase confidence in email delivery.
- **Never bypass the governance** — features that skip approval or review do not exist.

Saying "we don't do that" is a feature, not a limitation.

## 28. Long-Term Vision

Over the long arc of MailSent's life, we will:

- **Become the default trust layer of email** — reached for when a message genuinely cannot fail.
- **Make confidence an industry standard** — a truthful, verifiable, business-readable view of delivery that we normalized, not just offered.
- **Grow by depth, not sprawl** — held permanently by the Kill List and the Golden Rule.
- **Scale through the customers who need us most** — agencies and multi-domain businesses first, guided by validated problems over imagined ones.
- **Keep the constitution alive** — maintained with the same rigor as the code, evolving deliberately, never decaying.
- **Endure.** We are building a system and culture designed to outlast any person, team, or technology. When the tools of tomorrow replace today's, the values remain.

---

## 29. Amendment Process

The constitution is the highest authority but is not frozen. It changes deliberately:

- **Amendments are written down** with the change and its rationale, and **reviewed by the architects** before adoption.
- **Amendments are versioned**; the header version increments and the change history is preserved, recording what the old rule was and why it changed.
- **Non-Negotiable Rules change only with the highest scrutiny and consensus.**
- **The Golden Rule is amended with the highest possible scrutiny.** It is the first governing rule and the entire product depends on it holding; it changes rarely, and only when the evidence is overwhelming and recorded.

This document is the covenant. When you work on MailSent, you are bound by it — and when you improve it, you honor it.

---

*Ratified. This constitution is the foundation of MailSent: the confidence it sells, and the world-class engineering that earns it.*
