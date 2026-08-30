# MailSent — Ubiquitous Language

> The official language of MailSent — how humans, documentation, product, engineering, APIs, events, AI, and the UI all speak about the business, in the same words.
>
> This is **not** a glossary. Definitions live only in the [Glossary](../00_Foundation/GLOSSARY.md) and the [Domain Model](01_DOMAIN_MODEL.md); this document governs how those terms are used consistently across every surface. It is subordinate to the [Project Constitution](../00_Foundation/PROJECT_CONSTITUTION.md), the [Master Workflow](../00_Foundation/MAILSENT_MASTER_WORKFLOW.md), and the [Glossary](../00_Foundation/GLOSSARY.md), and is consistent with the [Domain Model](01_DOMAIN_MODEL.md) and every product and product-design document.

---

**Document governance**

- **Status:** Draft
- **Version:** 1.0
- **Owner:** Founders
- **Review Status:** Pending Founder Review

Only the founders may freeze this document after review.

**Next review trigger:**
- Product philosophy changes
- Terminology changes
- The naming conventions change

---

## 1. Purpose

Language is the most load-bearing material a company builds with. When every engineer, product manager, designer, and AI agent uses the same word for the same business thing, the documentation stays consistent, the product is coherent, and the software is faithful to the business. When they do not, a single concept fractures into many names and the business's meaning is lost.

This document is the rulebook for that consistency. It does not define the terms — the Glossary does. It defines how the terms are chosen, used, and communicated: which word is preferred, which is avoided, when a synonym is allowed, what language is forbidden, how events and code and APIs inherit the business names, and how the product speaks to its customers and through its AI.

## 2. Ubiquitous Language Principles

- **One concept, one name.** Every business concept has exactly one canonical name. It is used everywhere — in documentation, code, APIs, the UI, and in conversation.
- **The Glossary is the authority.** A term's definition is in the Glossary; this document and every surface use that meaning.
- **Names are business, not technology.** The name of a thing is the business's name for it, never the name of a table, a class, or a service.
- **Names carry the meaning.** A reader of any document, or a user of any interface, should be able to infer the business concept from the name alone.
- **Consistency beats cleverness.** A slightly plain but consistent name wins over a clever name that means different things in different places.
- **Terminology is traceable.** Every canonical term must exist in the Glossary or the Domain Model; nothing is invented.
- **Language is a commitment.** A word chosen here is a promise across the company; changing it is a governance act, not a casual edit.

## 3. Canonical Vocabulary

The preferred terms of the business, the terms to avoid, and the reason. Every preferred term exists in the Glossary or the Domain Model.

| Preferred Term | Avoid | Reason |
| --- | --- | --- |
| Health Signal | Trust Score | Trust Score is a deprecated alias; Health Signal is the canonical measure. |
| Health Trend | Health Signal to mean the trend | Health Trend is only the time-based representation. |
| Capability | Module to mean what is enabled | A Capability is what the product enables; a Module is a grouping. |
| Module | Capability to mean a grouping | A Module is a logical grouping that contains Capabilities. |
| Confidence | Reliability to mean the value sold | Reliability is the product's internal standard; Confidence is the outcome sold. |
| Trust | Confidence interchangeably | Trust is held by a customer's clients and end customers; Confidence is held by the customer. |
| Customer | End user to mean the business | Customer is the business; the end user receives the email. |
| Client | Customer in an agency context | A Client is an organization an agency serves. |
| Issue | Finding or defect to mean a delivery problem | An Issue is a delivery problem with meaning, a root cause, and a resolution path. |
| Root cause | Symptom | Diagnosis names the cause, not the symptom. |
| Business impact | Technical severity to mean the consequence | Impact is the business consequence; it is not a technical severity. |
| Priority | Urgency as a risk order | Priority is by business risk, not by recency. |
| Consent | Assumed permission | Consent is explicit and informed; it is never assumed. |
| Domain | Sender to mean the owned identity | A Domain is the owned identity; a Sender is a party that sends for it. |
| Observation Evidence | Evidence or proof to mean the raw observation | Observation Evidence is the immutable factual snapshot of an infrastructure observation; it is not customer-facing proof. |
| Evidence / proof of health (Report / Proof Evidence) | Observation Evidence to mean a report | Report / Proof Evidence is later customer-facing proof owned by Reporting; it is not the raw observed facts. |
| Reputation Observation | Reputation or Health Signal to mean the raw provider snapshot | Reputation Observation is the persisted raw Postmaster/SNDS fact; it is not Health Signal and not a Reputation aggregate. |
| Reverse DNS Observation | FCrDNS, Health Signal, or Observation Evidence to mean the PTR snapshot | Reverse DNS Observation (ReverseDnsObservation) is the persisted raw reverse-DNS fact (MX→A/AAAA→PTR); it is not FCrDNS, not Health Signal, and not Observation Evidence. |
| SMTP Greeting Observation | HELO/EHLO validation, Health Signal, or Observation Evidence to mean the SMTP banner snapshot | SMTP Greeting Observation (SmtpGreetingObservation) is the persisted raw SMTP greeting and EHLO fact (MX→A/AAAA→SMTP); it is not validation, not Health Signal, and not Observation Evidence. |
| DNS Health Observation | Health Signal, Trust Score, or Observation Evidence to mean TTL/DNSSEC/propagation | DNS Health Observation (DnsHealthObservation) is the persisted raw TTL, DNSSEC-presence, and multi-resolver fact; it is not Health Signal, not Trust Score, not HEALTH-001, and not Observation Evidence. |
| Blacklist Observation | Health Signal, listed/not-listed, or Reputation Observation to mean the raw blacklist snapshot | Blacklist Observation (BlacklistObservation) is the persisted raw Spamhaus/Barracuda/SURBL/URIBL fact; it is not Health Signal and not a listed conclusion. |
| Infrastructure Map | Health Signal, Map aggregate, or Observation Evidence to mean the topology | Infrastructure Map (MAP-001) is a topology read-model of Domain→DNS→SPF→DKIM→DMARC→Senders→Inbox; it is not Health, not a persisted aggregate, and not MAP-002–004. MAP-001 remains topology/read-model only. |
| MAP-002 (Color-coded view) | Mapping MAP-001 OBSERVED/NOT_OBSERVED/UNAVAILABLE, INF-001–008 observations, or Trust Score to G/Y/R | MAP-002 G/Y/R represents HEALTH, not observation availability. HEALTH-001 is the authoritative health state. MAP-002 consumes existing Health Signal; it does not create it. |
| MAP-003 (Node detail panel) | Independent health computation, G/Y/R, Trust Score, diagnosis, recommendation, compliance, or fabricated node details | MAP-003 is WF-5 click-to-detail on a MAP-001 node: identity, type, observation availability, and real facts/evidence refs when they exist. It does not calculate, create, infer, or own Health Signal. Health meaning and G/Y/R stay with MAP-002 consuming HEALTH-001. |
| MAP-004 (PNG/SVG export) | Phase 16 proof, EVID-003, Phase 15 PDF/email reporting, FUN-R2, Health Signal, or G/Y/R from raw INF / OBSERVED / Trust Score | MAP-004 is export of the Infrastructure Map visualization (PNG and SVG both frozen). Structure is MAP-001 topology. Colors, if any, come only from MAP-002 consuming HEALTH-001. It does not calculate or create Health Signal. |
| Alignment | Issue, Root cause, Health Signal, Observation Evidence field, or persisted Alignment table | Alignment is a derived value (DIAG-001 Analyzer-class) from DMARC evidence when INT-003–006 facts exist. It is not an Issue, not sender detection, not root cause, and not Health Signal. |
| Diagnosis | Health Signal, or Alignment as an Issue | Diagnosis owns Issue and Root cause. It must not fabricate Health Signal. HEALTH-001 produces Health Signal. DIAG-001 Alignment derivation is not Issue lifecycle. |

## 4. Synonym Rules

**Synonyms are prohibited when** they give two names to one business concept. A single concept — a Domain, a Health Signal, an Issue — must never be referred to by two different canonical words, because doing so fractures the meaning. In particular, product names and code names must not diverge from the Glossary.

**Synonyms are allowed in two narrow cases:**

- **Framing for a reader, not a change of meaning.** A document may use a plain restatement to help a reader (for example, "the delivery problem (an Issue)") as long as it is a clarification, not a substitute name.
- **Customer-facing plain language, by design.** In customer-facing communication, the product may use a familiar phrase instead of a technical canonical term, but only where the meaning is unchanged and the reader is served (see Section 10). This is a deliberate translation, not a synonym for the internal language.

**The rule:** two names for one concept are forbidden; a single plain rendering of a concept for a reader is permitted, provided it never replaces the canonical term in code, APIs, events, or documentation.

## 5. Forbidden Language

These words must never appear in product, engineering, or customer communication, because each misrepresents the business. The reasons follow the Glossary and the Product Positioning.

- **DMARC checker.** It reduces the product to a single-record check, which is the opposite of its nature as a reasoning layer.
- **Warmup / warming.** It is reputation manipulation — a shortcut, not confidence — and it is not what the product does.
- **Marketing automation platform.** It implies the product sends campaigns, which it does not.
- **Communication platform.** It implies the product owns the channel, which it does not.
- **AI-washing / fake AI.** It labels something as AI that is not genuine, grounded intelligence — a lie about the product.
- **Fabricated rank / forecast.** A rank or forecast that cannot be grounded in real data is a lie; the product presents only grounded results.
- **Guaranteed deliverability / 100% inbox.** An absolute promise overclaims what is real and contradicts the truth principle.
- **Lock-in / switching cost.** These are never used as a reason a customer should stay; value is the only reason.

Forbidden language is a quality of the company, not a style preference. A word in this list appearing anywhere is a defect to correct.

## 6. Naming Conventions

Naming philosophy for each kind of thing the business and its software will name. No code is written; this is how a name is chosen.

- **Business Concepts.** Name a concept by the business noun from the Glossary — Domain, Organization, Client, Issue, Confidence. The name is singular, stable, and means one thing.
- **Capabilities.** Name a capability as a statement of what the product enables, in business terms — for example, diagnosis, prioritization, monitoring. The name says the outcome, not the mechanism.
- **Events.** Name an event as a past-tense business fact (see Section 7) — what happened in the business, never what happened in the system.
- **Commands.** Name a command as an imperative business intention — what a user asks the product to do — using the business verb, not a technical verb.
- **Policies.** Name a policy by the rule it governs — for example, a consent policy or an alert policy — so the name states the behavior that is bound.
- **Value Objects.** Name a value object by the characteristic it describes — for example, a Health Trend or a Priority — not by the thing that carries it.
- **Interfaces.** Name an interface by the business concept it exposes, so the contract reads as the business, not as plumbing.
- **Specifications.** Name a specification by the criterion it defines — for example, readiness against a mailbox provider's requirements — so the name states what is being tested.
- **Repositories.** Name a repository by the business concept it holds, conceptually, never by its technology or its storage.
- **Services.** Name a service by the business capability it provides, never by its technology; the name is the business's word for the work.

Across all of these, one rule governs: **the name is the business's name.** If a name does not read as the business, it has not been named.

## 7. Event Naming Philosophy

Events are named as things that happen in the business, in the past tense, and are limited to the business events in the Domain Model.

- **A name states a business fact.** An event is "a domain was added to observation," "a fix was verified," "an issue was discovered" — a statement of what happened in the business.
- **A name is past tense and complete.** The name tells the full fact, not a partial hint.
- **Only business events are named.** Events are changes in delivery health, trust, or a relationship — never infrastructure events such as a job ran or a message was consumed.
- **A name inherits the canonical term.** An event about a Health Signal uses Health Signal; an event about a Client uses Client.

The purpose is that the event stream, read by any person or AI, tells the story of the business — not the story of the machinery.

## 8. API Naming Philosophy

The API is the business speaking to software, and it must inherit the business's names.

- **Names come from the vocabulary.** Operations are named by the business concept and intention — the customer's verb and noun — so an API reads as the business it serves.
- **No technical jargon.** A name never reveals a single-check concept or an implementation detail; it states the business outcome.
- **No invented abstraction.** An API name is a business name, never a layer or a technology name.
- **Consistency with the UI and events.** The word a customer sees, the word a developer calls, and the word an event uses are the same word.
- **Names state the business action**, not the mechanism that performs it.

An API that needs its own vocabulary has failed the ubiquitous language; it should inherit it.

## 9. Code Naming Philosophy

Code is written in the business's language, so that reading code is reading the business.

- **Identifiers mirror the concepts.** A name in code is the Glossary term for the business thing — a Domain is a Domain, an Issue is an Issue.
- **Capabilities name the code.** The boundaries of the code are the business capabilities, not technical layers; a service or module carries the capability's name.
- **No tech-named abstractions.** Names are never about tables, queues, or frameworks; they are about the business.
- **A name must read as the business.** If a reader must translate a code name back into business terms, the name is wrong and must be changed.
- **Consistency is a contract.** The same business word is used in every file that touches that concept; no local alias is created.

The principle is simple: the codebase is a faithful, readable embodiment of the business, because its names are the business's names.

## 10. Customer-Facing Language

MailSent speaks to its customers the way the business promises to: business language first, technical detail second, and truth always.

- **Lead with the consequence.** A customer is told what happened and what it costs before any mechanism. The first sentence is the business meaning.
- **Answer the five questions.** For any state, the product answers: what happened, why, what it costs, what to do, and what happens if it is ignored.
- **Never lead with jargon.** A protocol name or a technical term is a layer deeper, available but never first.
- **State health plainly.** Health Signal, risk, and urgency are expressed in clear business terms a non-technical person reads correctly.
- **Use the customer's words.** Customer, Client, Domain, Issue, Confidence, Trust — the Glossary's terms, which are already business terms.
- **Never overclaim.** The product promises confidence and truth, not a guarantee it cannot keep; forbidden language (Section 5) never appears.
- **Honor the customer's intelligence.** The customer is an expert in their business; the product speaks to that expertise in their language.

Customer-facing language is the product's promise made audible. It is business language because the customer is a business person.

## 11. AI Communication Language

AI is a core speaker of the ubiquitous language, and it is bound by rules that keep its speech truthful and useful.

- **How AI explains.** AI explains in business language, moving from cause to impact to action — what happened, why it matters, and what to do — and offers the technical mechanism one layer deeper only on request.
- **How AI avoids certainty.** AI never states an ungrounded conclusion as fact. If it cannot ground a claim, it does not make the claim as certain.
- **How AI expresses confidence.** When AI makes a recommendation or gives guidance, it states its confidence in a way a customer can weigh, so the customer knows how much to rely on it.
- **How AI communicates uncertainty.** When AI is not sure, it says it is not sure. It distinguishes what it knows from what it does not, and it is clear about the limits of its own conclusion.
- **How AI stays accountable.** AI explains, prioritizes, guides, and assists; it does not decide. A human owns the outcome of anything the AI tells a customer.

AI speech is the same language as the rest of the product, held to the same truth bar, because a confidence platform cannot afford an AI that speaks with false certainty.

## 12. Governance

- **The Glossary is the authority.** This document governs usage; the Glossary owns definitions. A change to a term begins in the Glossary.
- **Changes are recorded.** A change to a naming convention or the canonical vocabulary is written down with its reason and reviewed before adoption.
- **Traceability.** Every canonical term must exist in the Glossary or the Domain Model; a term that cannot be traced is not used.
- **Consistency is enforced across surfaces.** A name must be the same in documentation, code, APIs, events, the UI, and AI output; drift is a defect to correct.
- **Forbidden language is removed.** A forbidden word appearing anywhere is corrected, not excused.
- **Status.** Draft until the founders review; only the founders freeze this document after approval.

---

*This document governs the language of MailSent. It defines no software and redefines no term; it ensures that every surface — documentation, product, engineering, APIs, events, AI, and the UI — speaks about the business in the same words.*
