# MailSent — Security Architecture (Business)

> The business security architecture of MailSent, where **security is business trust** — not cybersecurity.
>
> This document governs how **trust, ownership, consent, protection of information, and the boundaries of AI** work in the business. It is not about OAuth, TLS, encryption, or firewalls; it is about who may hold what, who may act on what, and who is accountable. If the technology changed completely, this architecture would still be true.
>
> It is subordinate to the [Project Constitution](../00_Foundation/PROJECT_CONSTITUTION.md), the [Master Workflow](../00_Foundation/MAILSENT_MASTER_WORKFLOW.md), and the [Glossary](../00_Foundation/GLOSSARY.md), and is consistent with the [Domain Model](01_DOMAIN_MODEL.md), the [Ubiquitous Language](02_UBIQUITOUS_LANGUAGE.md), the [Bounded Contexts](03_BOUNDED_CONTEXTS.md), the [Information Architecture](04_INFORMATION_ARCHITECTURE.md), the [System Architecture](05_SYSTEM_ARCHITECTURE.md), the [AI Architecture](06_AI_ARCHITECTURE.md), the [Data Model](07_DATA_MODEL.md), and the [API Philosophy](08_API_PHILOSOPHY.md). Terminology uses only the Glossary; every security principle traces through the Information Architecture and the Data Model to a customer problem.

---

**Document governance**

- **Status:** Draft
- **Version:** 1.0
- **Owner:** Founders
- **Review Status:** Pending Founder Review

Only the founders may freeze this document after review.

**Next review trigger:**
- Product philosophy changes
- The information or data model changes
- The trust boundaries change

---

## 1. Purpose

Security is the reason a customer can trust the business. It is not a set of technologies; it is the way the business holds what is entrusted to it. When the customer's data, their delivery, and their Consent are respected, the business is secure. When they are not, no technology can save it.

This document is the business security architecture. It states who may act on what, how ownership runs, how Consent governs, how information is protected, and how AI respects the boundaries. It is the map of trust, written in the language of the business, and it exists so that the customer's Confidence rests on a foundation that is itself trustworthy.

## 2. Security Principles

- **Trust before access.** A participant or a component acts only when the business can trust it to do so. Access follows trust; trust does not follow access.
- **Least authority.** Every participant acts with only the authority its role needs, never more. The default is the minimum.
- **Explicit Consent.** Any action on a customer's live infrastructure is governed by the customer's explicit, informed Consent, never by an assumed permission.
- **Evidence integrity.** The truth the business reports is intact and traceable. Evidence that is unchanged, not invented, and not a guess is the ground.
- **Privacy by design.** Respect for the customer's data and privacy is designed in from the start, not added at the end.
- **Accountability.** A human owns the outcome of any action or conclusion that affects what a customer believes or decides.
- **Separation of responsibility.** One organization's health and data are never mixed with another's (INV-09); responsibility is held separately.

These principles are the business meaning of security. They govern a decision as strictly as they govern a line of code.

## 3. Trust Boundaries

Trust does not spread freely; it is held within boundaries, described in business language.

- **The boundary of the customer's authority.** The customer governs their own organization, its domains, and its consent. Nothing outside that authority acts within its bounds.
- **The boundary of an organization.** One organization's data and health are separated from another's, except where an explicit relationship (agency to its client) and the owner allow.
- **The boundary of the evidence.** Observation Evidence, Reverse DNS Observation, SMTP Greeting Observation, DNS Health Observation, Reputation Observation, and Blacklist Observation are the ground a DNS/SPF and raw-observation conclusion is drawn from; no conclusion exceeds what those snapshots hold. Alignment identities lineage to DMARC Aggregate Evidence / Parsed DMARC Report (INT-003–006), not to invented OE DKIM/DMARC/alignment fields (INV-11). HEALTH-001 Health Signal lineage cites observationEvidenceId, dmarcAggregateEvidence ids actually used, and reputationObservation ids actually used — not Observation Evidence for alignment identities, not an Alignment table, not DIAG-001 memory. DMARC ingest evidence is owned by Delivery Observation, not Diagnosis. A missing provider read is not a fabricated reputation or blacklist fact. An unavailable SMTP probe is not a fabricated greeting or EHLO. Reverse DNS Observation is not FCrDNS and not Health Signal. SMTP Greeting Observation is not HELO/EHLO validation and not Health Signal. DNS Health Observation is not Health Signal, not Trust Score, and not HEALTH-001. Blacklist Observation is not Health Signal and not a listed/not-listed conclusion. Report / Proof Evidence is a later customer-facing demonstration and is not the raw snapshot. Diagnosis must not fabricate Health Signal.
- **The boundary of a report.** A report is trustworthy because it reflects the health it reports; its trustworthiness is no stronger than the health it reflects.
- **The boundary of the consent.** An action the customer has not consented to does not happen; the boundary stops at the Consent.
- **The boundary of the AI.** AI proposes and explains; it does not cross into deciding the truth of a state or acting on a live system.

Each boundary defines where an authority may not reach. Trust moves across a boundary only where the owner allows.

## Business Trust Boundary Map

A text map of how trust and authority are bounded through the business.

```
Customer's Authority (the Consent gate)
    governs everything below it
            │
            ▼
Organization ──► Agency ──▶ Client
owns its own domains   an explicit, consented relationship
            │
            ▼
Evidence boundary — the truth; no conclusion exceeds it
            │
            ▼
Report boundary — trusted only as far as the evidence it reflects
            │
            ▼
AI boundary — AI proposes and explains; it never decides truth or acts on live systems
```

Trust descends from the Customer's Authority through the separated boundaries of organization, evidence, report, and AI. Each boundary is a line that the business does not cross without the owner and the Consent.

## 4. Ownership Principles

Just as trust is bounded, so is ownership. Ownership is the right to govern a part of the business. It is not storage: where something is kept is a technology matter; who owns it is a business matter.

- **Organization.** The customer owns their organization, its users, and its domains. Only the owner changes its membership or its consent.
- **Domain.** A domain's identity is owned by its organization through the authoritative portfolio registry; its health is owned by the Domain Health aggregate.
- **Observation Evidence.** Observation Evidence is owned by Delivery Observation, the observation-producing context. Consumers may read or reference it; they must not rewrite or mutate it. Existing snapshots are never overwritten. Phase 08 DNS is frozen to MX, TXT, SPF, observedAt, and identity; PTR is not added. DKIM, DMARC, and alignment are not added. INV-11 lineage for DNS/SPF facts is to Observation Evidence; lineage for alignment identities is to DMARC Aggregate Evidence / Parsed DMARC Report, not to a pretended OE field. HEALTH-001 must not cite Observation Evidence as the source of alignment identities.
- **Health Signal.** Health Signal is owned by Delivery Observation (HEALTH-001). Isolate by organizationId from the registered Domain (INV-09). A persisted Health Signal must carry INV-11 lineage to the source snapshot ids actually used. Missing a V1 required source: persist nothing (INV-01). When snapshots exist, indication is healthy | at-risk | unknown per the HEALTH-001 interpretation freeze (V15 tokens only; OE and Reputation Observation do not vote). Consumers may read it; they must not rewrite it. Existing snapshots are never overwritten. No public HTTP API in HEALTH-001. Not Trust Score. Not Issues.
- **DMARC ingest evidence.** DMARC Report Receipt, Parsed DMARC Report, and DMARC Aggregate Evidence are owned by Delivery Observation. Isolate by organization via Domain.organizationId (INV-09). RUA uuid must match a registered Domain; unknown uuid is discarded — no Receipt, no snapshot, no DIAG-001 trigger. Spoofed reports addressed to a valid Domain RUA are accepted in V1 without cryptographic proof of reporter identity (not specified); do not claim reporter DKIM/SPF of the inbound webhook payload is verified. INV-01: no fabricated parse or Alignment. INV-02: publishing rua= is customer DNS (LAUNCH-002 later); ingest does not create Consent objects. Diagnosis does not own ingest. INT-006 does not trigger HEALTH-001. HEALTH-001 cites Aggregate Evidence for H2 alignment facts.
- **Reverse DNS Observation.** Reverse DNS Observation is owned by Delivery Observation. It is a raw reverse-DNS fact (MX→A/AAAA→PTR), not Observation Evidence, not FCrDNS, and not Health Signal. Consumers may read it; they must not rewrite it. Existing snapshots are never overwritten.
- **SMTP Greeting Observation.** SMTP Greeting Observation is owned by Delivery Observation. It is a raw SMTP greeting/EHLO fact (MX from Observation Evidence → A/AAAA → SMTP), not Observation Evidence, not Reverse DNS Observation, not HELO/EHLO validation, and not Health Signal. The SMTP connection is an active probe permitted only against infrastructure associated with the registered Domain; it does not send mail, authenticate, test relay or delivery, or probe arbitrary IPs or third-party hosts. Consumers may read it; they must not rewrite it. Existing snapshots are never overwritten. An unavailable probe (timeout, refusal, DNS failure) stores nothing; greeting and EHLO text are never fabricated.
- **DNS Health Observation.** DNS Health Observation is owned by Delivery Observation. It is a raw TTL, DNSSEC-presence, and named-resolver fact, not Observation Evidence, not Health Signal, not Trust Score, and not HEALTH-001. Consumers may read it; they must not rewrite it. Existing snapshots are never overwritten. If observation cannot be obtained, nothing is persisted. A failed named resolver is recorded as unavailable without fabricated answers.
- **Reputation Observation.** Reputation Observation is owned by Delivery Observation. It is a raw Postmaster/SNDS fact, not a Reputation aggregate and not Health Signal. Consumers may read it; they must not rewrite it. Existing snapshots are never overwritten. An unavailable provider stores nothing.
- **Blacklist Observation.** Blacklist Observation is owned by Delivery Observation. It is a raw Spamhaus/Barracuda/SURBL/URIBL fact, not Health Signal and not a listed/not-listed conclusion. Consumers may read it; they must not rewrite it. Existing snapshots are never overwritten. An unavailable provider stores nothing.
- **Report.** A report (Evidence / proof of health, Report / Proof Evidence) is owned by Reporting and Evidence and reflects the health it reports. Its truth rests in the health it reflects, not in any later edit. Reporting does not own Observation Evidence.
- **AI Explanation.** The explanation is owned by the business context for its meaning, not by the AI that drew upon the underlying truth.
- **Recommendation.** A recommendation is offered by the context that makes it; the customer owns the decision whether to act.

The rule is single ownership: every part of the business has one owner of its meaning, and every other holder is a reader or an agent, never a second owner.

## 5. Consent Philosophy

Consent is the gate through which ownership becomes action. It governs what the product may do, and the absence of Consent ends an action.

- **Consent governs observation.** A domain is observed only with the customer's Consent for that observation. SMTP Greeting Observation is an active SMTP probe and is gated by that same Consent; it is limited to MX hosts of the registered Domain as recorded in Observation Evidence.
- **Consent governs AI reasoning.** AI reasons only within the customer's Consent to use the knowledge it rests on.
- **Consent governs reporting.** A report may be produced and shared with a client or a third party only with the customer's Consent.
- **Consent is the customer's.** It can be given or withdrawn and is never assumed rather than granted (see the definition of Consent in the Glossary).

**No Consent.**
↓
**No action.**

The rule is unconditional: without Consent, no component acts on the customer's live infrastructure and nothing is shared. Consent is not one step within a flow; it is the gate that the flow passes through.

## 6. Information Protection Philosophy

The same discipline that gates action also governs how information is protected. The business protects each category of information according to the trust it carries, following the Information Architecture classification. No encryption is described; these are the expectations of business behavior.

- **Authoritative.** Observation Evidence, Reverse DNS Observation, SMTP Greeting Observation, DNS Health Observation, Reputation Observation, Blacklist Observation, DMARC Report Receipt, DMARC Aggregate Evidence, the Health Signal, the Consent, and the registry are the sources of truth.
  - Why it is protected: every other conclusion flows from it; if it is wrong, everything downstream of it is wrong.
  - Who governs it: the owning context (Delivery Observation for Observation Evidence, Reverse DNS Observation, SMTP Greeting Observation, DNS Health Observation, Reputation Observation, Blacklist Observation, DMARC ingest evidence, and health; Portfolio and Client Management for the registry).
  - Business expectation: it is never changed by a non-owner, and its integrity is the integrity of everything that follows. Observation Evidence, Reverse DNS Observation, SMTP Greeting Observation, DNS Health Observation, Reputation Observation, Blacklist Observation, and DMARC Aggregate Evidence are append-only.
- **Derived.** Derived information is the result of the authoritative.
  - Why it is protected: it must not be mistaken for ground truth.
  - Who governs it: the context that derives it (Impact and Prioritization, Reporting, Readiness).
  - Business expectation: it always carries its source and is never presented as independent of it.
- **Historical.** History is the record of what was.
  - Why it is protected: it is the proof of the past and the basis of the trend.
  - Who governs it: the context that accumulated it.
  - Business expectation: it is retained while it has meaning and is never altered to suit the present.
- **Operational.** Working information is the responsibility of the present moment.
  - Why it is protected: it is the live state of the business.
  - Who governs it: the operating context.
  - Business expectation: it is current, and it is revealed only to the operation it serves.
- **Knowledge.** The learned knowledge of the product is its accumulated understanding.
  - Why it is protected: it is the improvement that makes the product more accurate over time.
  - Who governs it: the learning context (Feedback and Learning).
  - Business expectation: it grows from real feedback and history and is never built by misusing real evidence.

In every case, the protection is a behavior of the business — keeping what is true, current, and private — not a property of storage.

## 7. AI Security Philosophy

The same security that governs information governs AI. AI is bounded by it, expressed as what it respects and what it never does.

**AI respects**
- **Evidence.** AI reasons within the evidence it is given and never reasons past it to an invented fact.
- **Ownership.** AI does not claim ownership of an explanation, a report, or a fact; the owner holds the meaning and AI assists.
- **Consent.** AI proposes or reasons only within the customer's Consent and never uses knowledge or shares a conclusion without it.

**AI never**
- **Invent facts.** AI never presents a delivery state, a cause, a rank, or a forecast without ground.
- **Escalate authority.** AI never raises its own authority to decide; it proposes and explains.
- **Cross ownership boundaries.** AI never acts on a domain or any part it may not touch.
- **Act autonomously.** AI never takes an action on a live system on its own; only a consented, human-owned action crosses.

Behind every AI output stands an owner and a Consent; without them, the output is not the business's voice. This is the security of AI — a matter of the boundary of trust, not of technology.

## 8. Security Failure Philosophy

When trust fails, the business behaves in a known way. No recovery mechanism is described; only the business behavior.

- **Consent revoked.** If the customer withdraws a Consent, the action stops and does not continue.
- **Evidence compromised.** If the evidence a conclusion rests on is discovered to be wrong, the conclusion does not stand. It is marked or withdrawn, and the customer is told. The inaccuracy is not presented as true.
- **Ownership disputed.** If ownership of a domain, a report, or an account is disputed, the business holds the disputed item and does not change it until ownership is settled; it does not rule for either claimant.
- **Trust reduced.** If trust is reduced — through a failure, a doubt, or a disputed evidence — the business says so plainly. It does not hide or excuse; trust itself is what the business protects.
- **AI unavailable.** If the AI is not present, the truth still stands; only the translation and the order are reduced, and the customer is told.

The failure of security is answered by honesty and by restraint. A compromised item is not used; a disputed item is held; a revoked consent stops the action.

## 9. Evolution Principles

Security is not static; it evolves deliberately, never by drift.

- **Change with the business.** A security principle or a trust boundary changes only when the business responsibility it protects changes, and only through the governance process of this document.
- **A change is a governed act.** Adding, amending, or retiring a principle or a boundary is a written proposal that names an owner, a traceable basis, and a review trigger, and is approved by the founders.
- **The map is kept current.** The trust boundaries remain consistent with the domain model, the information architecture, and the data model; a change to those sources is reflected here before it is relied upon.
- **Consistency with the Constitution.** Every change is checked against the Constitution; a principle that would contradict it is not adopted.
- **The trust lives in behavior.** The customer's trust rests on business behavior, not on a technology; a new technology does not change the business's security, but a change in behavior does.
- **The principle precedes the tool.** Trust, ownership, and consent are business matters first and technology matters later; no tool is adopted at the cost of a principle.

The security of the business evolves deliberately, governed by the same discipline as the Constitution, and never at the pace of a technology's novelty.

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
- [07_DATA_MODEL.md](07_DATA_MODEL.md)
- [08_API_PHILOSOPHY.md](08_API_PHILOSOPHY.md)

### Feeds into

As a predecessor for the future document `10_DEPLOYMENT_ARCHITECTURE.md` and for the security specifications.

## 11. Governance

Every security principle here is bound by four fixed commitments, in addition to the processes below.

- **Owned.** Every security principle has an owner who is accountable for keeping it true.
- **Traceable.** Every principle must remain traceable to its business source and a customer problem; a principle that cannot be traced is removed.
- **Review trigger.** Every principle requires a review trigger — the event that causes it to be re-examined — and it is re-examined whenever that trigger is met.
- **Constitutional.** Every principle must remain consistent with the Project Constitution; a principle that would contradict it is not adopted.

- **Status.** Draft until the founders review; only the founders freeze this document after approval.
- **Changes to the boundaries.** Changing a trust boundary, an ownership rule, or a consent gate is a written proposal and a review by the founders or the chief architect.
- **Consistency.** The security of the business is bound by the Glossary and the ubiquitous language; a term used against them is out of compliance.
- **Not a technology decision.** Authentication, authorization, encryption, infrastructure security, and network security are decided elsewhere, not here.

---

*This document defines the business security of MailSent, in the language of trust and ownership. It proposes no encryption, no authentication, and no firewall — it states the boundaries of the business that the technology will later hold.*