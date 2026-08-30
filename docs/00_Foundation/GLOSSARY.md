# MailSent — Glossary

> The single authoritative vocabulary of MailSent.
>
> This document assigns one official meaning to every important word the company uses, so that engineers, product managers, AI agents, and every future document share the same language. It prevents terminology drift.
>
> It is subordinate to the [Project Constitution](PROJECT_CONSTITUTION.md) and the [Master Workflow](MAILSENT_MASTER_WORKFLOW.md), and is consistent with every document in `docs/01_Product/` and `docs/02_Product_Design/`. Every definition is grounded in those documents; nothing here is invented.

---

**Document governance**

- **Status:** Draft
- **Version:** 1.0
- **Owner:** Founders
- **Review Status:** Pending Founder Review

After founder approval, update to **Status: Approved** and **Review Status: 🔒 Frozen**. Only the founders may freeze this document.

**Next review trigger:**
- Product philosophy changes
- Terminology changes
- Category changes

---

## 1. Purpose

The purpose of this glossary is to give every term one meaning and to bind every future document to it. Language is how a company stays coherent: when two documents use the same word for different things, or different words for the same thing, the product and the customer's understanding both suffer.

This glossary is the arbiter. When a future document needs a term, it uses the definition here. When a term is missing, it is added here before it is used. When two sources conflict over a term, that conflict is recorded here and left for the founders to decide — never silently resolved.

## 2. Usage Rules

- **One term per concept.** Never use two words for the same thing, and never use one word for two things.
- **Define on first use.** A new document introduces a term by naming it and, if the reader may not know it, linking to this glossary.
- **Use the exact term.** Keep the Glossary's spelling and capitalization; do not invent synonyms.
- **Prefer the product-level term.** In product and requirements documents, use the authoritative term over a more execution-specific one (see the conflicts recorded in Section 10).
- **Do not invent.** If a new term is needed, propose it, add it here, and get it reviewed before using it widely.
- **Report, do not resolve.** When a document uses a term against this glossary, flag it; do not quietly pick a meaning.
- **The Glossary is the source of truth for wording**, not for strategy; strategy lives in the Constitution and the workflow.

## 3. Core Business Terms

**Confidence**
- Definition: The certainty a customer holds that their email reaches the inbox, that infrastructure stays healthy, that problems surface early, and that teams know what to fix.
- Purpose: It is the outcome the company sells; it is the apex of the value hierarchy.
- Not: A feature, a report, or a check.
- Related: Reliability, Trust, Value proposition.

**Reliability**
- Definition: The standard the product itself holds — that its own intelligence is dependable, truthful, observable, and secure.
- Purpose: It is what earns the confidence we sell; it is an internal quality bar, not the thing sold.
- Not: The product's value promise. (The Constitution separates it from confidence deliberately.)
- Related: Confidence, Non-functional requirements.

**Trust**
- Definition: The confidence a business's own customers, clients, and stakeholders place in it, which a missed message can break.
- Purpose: It is the deeper currency, especially for agencies; losing it is the most expensive outcome.
- Not: Confidence (which is what the customer holds about delivery); trust is what their stakeholders hold about them.
- Related: Confidence.

**The Golden Rule**
- Definition: The first governing rule of the company — no feature is approved because it is technically impressive; every feature must solve a validated customer problem and create recurring value.
- Purpose: It is the supreme test every product decision must pass.
- Not: A feature formula (which is execution detail in the workflow).
- Related: Validated customer problem, Recurring value.

**Recurring value**
- Definition: Value a customer receives month after month, such that they would keep paying for it at month twelve.
- Purpose: It distinguishes a platform from a one-time trick, and it is a test in the Golden Rule.
- Not: A single result or a one-off gain.

**Validated customer problem**
- Definition: A real problem a real customer has, confirmed by evidence, not imagined or assumed.
- Purpose: It is the first test of any feature; nothing is built without it.
- Related: The Golden Rule.

**The Kill List**
- Definition: The living record of rejected features, `REJECTED_FEATURES.md`, listing each rejection, its reason, its date, and any condition that would justify revisiting it.
- Purpose: It turns "no" into a discipline and a shared record.
- Not: A list of things we might build later.

## 4. Customer Terms

**Customer**
- Definition: The business or agency that uses MailSent and whose revenue and trust are at stake in delivery.
- Purpose: The focus of every decision; the reason the product exists.
- Not: The end user who receives email (their customer).

**Business decision-maker**
- Definition: A founder, operator, or executive who owns revenue and trust but is not technical.
- Purpose: A primary user type; the product must speak to them in business terms.
- Related: Business language.

**Agency / MSP**
- Definition: A company (digital marketing agency, email marketing agency, or managed service provider) that manages email delivery across many client domains.
- Purpose: A primary customer; the product is proven for them first.
- Related: Portfolio.

**Portfolio**
- Definition: The set of client domains and organizations an agency or multi-domain business is accountable for.
- Purpose: The scale at which the product's value compounds; agencies manage many at once.
- Not: A single domain.

**Business language**
- Definition: Plain, consequence-first expression of a delivery issue — what happened, what it costs, what to do — before any technical mechanism.
- Purpose: The signature of the category; it is how a non-technical person can act.
- Not: Jargon, or leading with a technical term.
- Related: Business decision-maker.

**The black box**
- Definition: The condition of email delivery being invisible to the business that depends on it.
- Purpose: It is the problem the company refuses to accept and the thing the product opens.
- Not: A product state.

**Early detection**
- Definition: Surfacing a problem while there is still time to act, before a customer notices.
- Purpose: A primary user outcome and a core value; early is worth more than correct-but-late.

**Primary / Secondary customer (P0 / P1)**
- Definition: P0 — digital marketing agencies, email marketing agencies, MSPs, and multi-domain SaaS companies, served first. P1 — e-commerce, B2B companies, and startups, served after P0 is solid.
- Purpose: It fixes who the product is built for, in order.
- Not: Invented or additional segments.

## 5. Product Terms

**AI Email Infrastructure Intelligence Platform**
- Definition: The identity of MailSent — software that reasons across everything that determines inbox delivery, explains it in business language, and turns it into a decision the customer can act on.
- Purpose: It is the final identity; the product is never defined otherwise.

**Email infrastructure intelligence (the category)**
- Definition: The software category of reasoning across the whole delivery picture, rather than checking one record or reporting raw state.
- Purpose: It is the category MailSent belongs to and the category MailSent defines.
- Related: Intelligence.

**Delivery / deliverability**
- Definition: Whether email reaches the intended inbox; the health of that condition.
- Purpose: It is what the product protects and what confidence is about.
- Not: Sending the message.

**Email infrastructure**
- Definition: The configuration and signals that determine delivery — authentication, alignment, records, and reputation.
- Purpose: It is what the product observes and reasons across.
- Not: The sending platform.

**Inbox**
- Definition: The destination of a delivered message, as accepted by the recipient's mailbox provider.
- Purpose: Reaching it is the definition of successful delivery.

**Root cause**
- Definition: The underlying cause of a delivery failure, not its symptom.
- Purpose: Diagnosis aims at the root cause; a statement that names a symptom is a failure. Root cause is owned by Diagnosis (DIAG-003). Alignment is not a Root cause and not an Issue.
- Related: Diagnosis, Issue, Alignment.

**Issue**
- Definition: A delivery problem detected on a domain, with a root cause (Domain Model).
- Purpose: It is the unit of diagnosis and resolution — the thing a customer understands, prioritizes, and fixes. Diagnosis owns Issue and Root cause (DIAG-003/004). Alignment is not an Issue. DIAG-001 does not own Issue lifecycle.
- Not: Alignment; a raw technical finding without meaning; Health Signal.
- Related: Root cause, Diagnosis, Alignment.

**Diagnosis**
- Definition: The bounded-context capability that understands why delivery is at risk. Diagnosis owns Issue and Root cause.
- Purpose: To state a real root cause, not a symptom. Sender detection (DIAG-002) and root cause / Issue (DIAG-003) remain Alignment Intelligence. DIAG-001 is Analyzer-class Alignment derivation (SPF/DKIM/DMARC parsing + alignment), not sender detection and not root cause.
- Not: Health Signal (produced by HEALTH-001); Trust Score; fabrication of health so that diagnosis can proceed; Alignment as an Issue or a persisted table.
- Related: Issue, Root cause, Alignment, Health Signal.

**Alignment**
- Definition: The consistency between who authenticates a message and who the domain says is sending (Domain Model).
- Purpose: To determine whether a provider accepts a message as legitimate. DIAG-001 is Analyzer-class derivation of this value (Constitution Analyzer: SPF/DKIM/DMARC parsing + alignment). Real Alignment requires DMARC Aggregate Evidence from INT-003–006; DIAG-001 cannot compute real SPF/DKIM envelope-vs-header alignment until those facts exist. It does not infer DKIM, DMARC, or alignment from Phase 08 DNS TXT. Observation Evidence stays frozen to MX/TXT/SPF/observedAt/identity; do not add DKIM/DMARC/alignment fields to OE. Alignment is a derived/value concept; no Alignment persistence or table is authorized. DATA Alignment remains a value object with meaning, not a field list. DIAG-001 does not create Health Signal or Trust Score. Alignment is not an Issue. Diagnosis consumes DMARC Aggregate Evidence; it does not own ingest.
- Not: Sender detection; Root cause; Issue; Health Signal; Trust Score; an Observation Evidence field; a persisted Alignment aggregate; DMARC Report Receipt; Parsed DMARC Report; DMARC Aggregate Evidence.
- Related: Sender, Domain, Diagnosis, Observation Evidence, Health Signal, DMARC Aggregate Evidence.

**Sender**
- Definition: A recognized party (for example an email service provider) that sends on behalf of a domain (Domain Model).
- Purpose: To be identified and its authentication checked, because alignment depends on who is sending. Sender detection is DIAG-002 (Alignment Intelligence), not DIAG-001.
- Not: The domain itself; Alignment; an Issue.
- Related: Domain, Alignment, Diagnosis.

**Prioritization**
- Definition: Ordering issues by the risk they pose to the business, so the customer knows what to do first.
- Purpose: It turns a wall of equal findings into a decision.
- Not: Ordering by technical difficulty or recency.

**Health Signal**
- Definition: The unified, legible indication of a domain's delivery health. This is the canonical product term. Unified indication vocabulary (HEALTH-001 freeze): **healthy** | **at-risk** | **unknown** (INV-05 + INV-01). **unknown** means we do not know — not a MAP color.
- Purpose: It is how a customer can answer "is my email working?" HEALTH-001 (Phase 10 / Delivery Observation) is the authoritative source of health state. Envelope/lineage is unchanged. **Interpretation (2026-08-15):** when required snapshots exist, indication is assigned from cited DMARC Aggregate Evidence tokens only — **at-risk** if any cited row has `disposition` quarantine/reject or `spf_result`/`dkim_result`/`spf_aligned`/`dkim_aligned` equal to `fail`; **healthy** only if every cited row has non-null `disposition=none` and SPF+DKIM result+alignment all `pass`; **unknown** if snapshots exist but required judgment fields are null or tokens are not the healthy set and no at-risk token fired (e.g. DKIM `none`). Missing a required snapshot: persist nothing (not unknown). Observation Evidence and Reputation Observation are lineage-required presence, not voters (V9 has no grade field; opaque payload is not parsed). No scoring model, 0–100, weights, G/Y/R, or Trust Score. MAP-002 may later consume this signal; it does not create Health Signal. Diagnosis must not fabricate Health Signal. DIAG-001 Alignment derivation does not create Health Signal.
- Not: Trust Score, which is a deprecated alias (workflow 0–100 and weights are deprecated product language, not HEALTH-001); not MAP-001 observation availability; not a color source invented from INF-001–008; not DIAG-001 Alignment; not an Alignment table; not Issues; not Health Trend (HEALTH-002); not a second Health Signal per component.
- Related: Health Trend, Trust Score (deprecated), MAP-002 (Color-coded view), Observation Evidence, DMARC Aggregate Evidence, Reputation Observation. MAP-003 does not own or compute this signal. MAP-004 does not calculate or create this signal.

**Health Trend**
- Definition: The time-based representation of Health Signal, showing change (improving, stable, declining) over time.
- Purpose: It expresses how health moves across time.
- Not: The whole Health Signal; it is only the time dimension.
- Related: Health Signal.

**Trust Score (deprecated)**
- Definition: A legacy alias for Health Signal, retained only where historical references require it.
- Purpose: It allows reading of older documents; it is not used in new writing.
- Not: The canonical term.
- Related: Health Signal.

**Capability**
- Definition: What the product enables for a user, grouped by purpose (for example, diagnosis, prioritization, monitoring).
- Purpose: It is the unit used in requirements, chosen to stay independent of implementation.
- Not: A Module, which is a logical grouping containing one or more capabilities.
- Related: Module.

**Module**
- Definition: A logical product grouping or workspace that contains one or more capabilities.
- Purpose: It is the execution-level view of the product's organization.
- Not: A Capability, which is what the product enables.
- Related: Capability.

**Guidance / Guided fix**
- Definition: Step-by-step, prioritized direction a user can act on to resolve an issue.
- Purpose: It moves the customer from knowing to acting.
- Related: Prioritization, Verification.

**Continuous monitoring**
- Definition: Observing delivery health over time, so health is a maintained state rather than a single check.
- Purpose: It is how a fix is verified and how problems are caught early.

**Alert**
- Definition: A notification of a change or failure, stated in business language with severity and whether to act.
- Purpose: It is how the product keeps a customer ahead of a problem.
- Not: Raw technical output.

**Readiness / compliance**
- Definition: Assessment of a domain against the standards set by mailbox providers that decide inbox entry.
- Purpose: It lets a customer demonstrate to an auditor or partner that delivery is acceptable.

**Onboarding**
- Definition: The path from a customer's first use to a working, monitored state, including any configuration needed to begin health observation.
- Purpose: It is how a customer reaches their first answer.

## 6. AI Terms

**AI (in the product)**
- Definition: The capability that reasons across many signals and renders them in business terms; the enabler of the category.
- Purpose: It is why the reasoning at the required scale is possible.
- Not: A marketing label.

**AI explains / prioritizes / guides / assists**
- Definition: The four allowed behaviors of AI in the product.
- Purpose: They bound what AI may do — never decide, never act alone, never act on live systems without consent.
- Related: Human accountability.

**Explainability**
- Definition: The property that a conclusion's underlying evidence and reasoning are apparent when asked "why."
- Purpose: It keeps every conclusion verifiable.

**Grounded / grounding**
- Definition: The requirement that any statement or answer trace to real data; a guess presented as fact is a violation.
- Purpose: It is the condition that keeps an AI-powered confidence product honest.
- Related: AI confidence, Truth.

**AI confidence (indication)**
- Definition: AI's stated certainty accompanying a recommendation or guidance.
- Purpose: It lets a user weigh how much to rely on the recommendation.

**Transparency**
- Definition: The property that AI's role is clear to the user and that the basis for an output can be seen.
- Purpose: It prevents the product from hiding that a model produced a result.

**Human accountability**
- Definition: The rule that a human owns the outcome of anything that affects what a customer believes or decides.
- Purpose: It keeps AI as an assistant, not an authority.

## 7. Trust & Evidence Terms

**Truth / truthful**
- Definition: The property that every reported state traces to a real, observed event, and nothing is fabricated.
- Purpose: It is the floor under all trust and a non-negotiable.
- Related: Grounded.

**Observable / inspectable**
- Definition: The property that every state and failure can be seen, logged, and traced.
- Purpose: It is what makes the product trustworthy.

**Auditability / audit trail**
- Definition: The record that lets any reported conclusion be traced back to the data it was drawn from, and what was reported and when.
- Purpose: It lets a customer verify the product rather than trust it blindly.

**Verification (of a fix)**
- Definition: Confirming that a resolution held and the problem did not silently return.
- Purpose: It closes the loop; an unverified fix is a guess.

**Observation Evidence**
- Definition: An immutable, persisted snapshot of factual data obtained from a real infrastructure observation.
- Purpose: It is the authoritative raw record of facts actually returned by the observation. For current Phase 08 DNS, it is frozen to MX, TXT, SPF, observedAt, and identity. It is append-only: a later observation creates a new snapshot. Later interpretation, diagnosis, health, and reporting consume it; they do not rewrite it. Reverse DNS / PTR is not part of Observation Evidence. SMTP greeting / EHLO is not part of Observation Evidence. DKIM, DMARC, and alignment are not Observation Evidence fields and must not be inferred from Phase 08 DNS TXT. DMARC Report Receipt, Parsed DMARC Report, and DMARC Aggregate Evidence are not Observation Evidence.
- Not: Health Signal, Diagnosis, Finding, Assessment, Recommendation, AI conclusion, Report, compliance conclusion, inferred health state, fabricated validity state, Reverse DNS Observation, SMTP Greeting Observation, DNS Health Observation, DMARC Report Receipt, Parsed DMARC Report, DMARC Aggregate Evidence, or Evidence / proof of health.
- Related: Evidence / proof of health, Health Signal, Reputation Observation, Reverse DNS Observation, SMTP Greeting Observation, DNS Health Observation, DMARC Aggregate Evidence.

**DMARC Report Receipt**
- Definition: The received inbound DMARC aggregate report — the raw payload/attachment as received.
- Purpose: To hold the bytes actually received for a registered Domain so lineage (INV-11) and re-parse are possible. Owned by Delivery Observation. Accepted only when the RUA uuid matches a registered Domain. Unknown uuid is discarded and is not persisted.
- Not: Observation Evidence; Alignment; Parsed DMARC Report; DMARC Aggregate Evidence; a forensic (RUF) report; a public API resource.
- Related: Parsed DMARC Report, DMARC Aggregate Evidence, Domain.

**Parsed DMARC Report**
- Definition: Facts extracted from a valid DMARC aggregate XML report (RFC 7489 aggregate). Zip/gz wrappers are transport compression of that XML, not a second schema.
- Purpose: To record raw parsed facts (ReportMetadata, Records, PolicyEvaluated as listed in the Master Workflow), not derived Alignment. `report_id` is required to accept as parsed. Absent fields stay absent. Owned by Delivery Observation.
- Not: Alignment; Observation Evidence; Health Signal; a fabricated field matrix; forensic (RUF) facts.
- Related: DMARC Report Receipt, DMARC Aggregate Evidence, Alignment.

**DMARC Aggregate Evidence**
- Definition: The persisted (or persistable) aggregated evidence snapshot of RUA facts for a registered Domain, used for lineage (INV-11), as DIAG-001 input, and as HEALTH-001’s authoritative persisted source for H2 alignment facts.
- Purpose: Append-only per-sender (`source_ip`), per-domain (registered Domain), per-day (UTC date of `date_range.begin`) snapshot owned by Delivery Observation. Duplicate `report_id` for the same Domain is idempotent. DIAG-001 consumes it; HEALTH-001 cites it for alignment of authentication signals. Diagnosis does not persist ingest. HEALTH-001 does not persist Alignment.
- Not: An Alignment table; Observation Evidence; Health Signal; a rolling mutable cube; forensic (RUF) evidence.
- Related: DMARC Report Receipt, Parsed DMARC Report, Alignment, Observation Evidence.

**Reputation**
- Definition: External signals about a domain's sending reputation, as observed where possible.
- Purpose: To be one later input to health, reflecting how providers perceive the domain. It is not a persisted snapshot and not a score invented by MailSent.
- Not: Reputation Observation (the raw persisted fact), Health Signal, Trust Score, warmup, or a fabricated grade.
- Related: Reputation Observation, Health Signal.

**Reputation Observation**
- Definition: An immutable, persisted snapshot of one raw external reputation observation for a registered domain, from Google Postmaster or Microsoft SNDS.
- Purpose: It records only facts actually returned by a provider (source identity, observation instant, and opaque raw payload). It is append-only: a later observation creates a new snapshot. If a provider is unavailable or returns nothing, nothing is persisted for that provider. HEALTH-001 V1 **requires** at least one persisted row as **lineage-required presence** (H2/H3 intake). V9 has no grade/score field; opaque `payload` is **not** an indication voter. Production Unavailable adapters persist no row ⇒ HEALTH-001 persist-nothing. Reputation Observation does not create Health Signal.
- Not: A Reputation aggregate, Health Signal, Trust Score, diagnosis, Finding, Assessment, recommendation, compliance conclusion, blacklist result (INF-008), or Evidence / proof of health. It does not store scores, grades, risk, validity, or health.
- Related: Reputation, Observation Evidence, Reverse DNS Observation, SMTP Greeting Observation, DNS Health Observation, Blacklist Observation, Health Signal.

**Blacklist Observation**
- Definition: An immutable, persisted snapshot of one raw external blacklist observation for a registered domain, from Spamhaus, Barracuda, SURBL, or URIBL.
- Purpose: It records only facts actually returned by a provider (source identity, observation instant, and opaque raw payload). It is append-only: a later observation creates a new snapshot. If a provider is unavailable or returns nothing, nothing is persisted for that provider. Each of the four providers is queried independently.
- Not: Health Signal, Trust Score, diagnosis, Finding, Assessment, recommendation, compliance conclusion, listed/not-listed conclusion, Reputation Observation, or Evidence / proof of health. It does not store scores, grades, risk, validity, or health. HEALTH-001 may consume it later; Blacklist Observation does not create a Health Signal.
- Related: Observation Evidence, Reputation Observation, Reverse DNS Observation, SMTP Greeting Observation, DNS Health Observation, Health Signal.

**DNS Health Observation**
- Definition: An immutable, persisted snapshot of one raw DNS health observation for a registered Domain: per-record TTL, DNSSEC-related record presence (DNSKEY/DS), and answers from named resolvers (propagation).
- Purpose: It records only facts actually returned — identity from the registered Domain, observedAt, TTL as returned per record, DNSSEC present/absent/unavailable without cryptographic validation, and per-resolver raw answers or unavailability. It is append-only: a later observation creates a new snapshot. It is Phase 08 raw infrastructure observation, owned by Delivery Observation, and a snapshot separate from Observation Evidence. If observation cannot be obtained, nothing is persisted (INV-01). If one named resolver fails, real observations are still persisted; the failed resolver is recorded as unavailable with no fabricated answer. INF-004 scheduling is unchanged; this snapshot is not added to re-observation yet.
- Not: Observation Evidence (TTL/DNSSEC/propagation are not added there). Not Health Signal, not Trust Score, not HEALTH-001, not INF-008 (blacklists), not a health/score/validity/diagnosis conclusion, not "DNSSEC valid", not a propagation percentage, and not Evidence / proof of health. HEALTH-001 may consume it later; DNS Health Observation does not create a Health Signal.
- Related: Observation Evidence, Reverse DNS Observation, SMTP Greeting Observation, Reputation Observation, Health Signal.

**Reverse DNS Observation**
- Definition: An immutable, persisted snapshot of one raw reverse-DNS (PTR) observation for an IP address of an observed MX host of a registered Domain.
- Purpose: It records only facts actually returned: identity from the registered Domain, the source MX hostname, the IP address (IPv4 and/or IPv6 when those addresses actually exist), the PTR values as observed, and when it was observed. The read is MX hostname → A and/or AAAA → PTR. Both address families are observed when addresses are actually available. It is append-only: a later observation creates a new snapshot. It is Phase 08 raw infrastructure observation, owned by Delivery Observation, and is a snapshot separate from Observation Evidence.
- Not: Observation Evidence (PTR is not added there). Not FCrDNS, pass/fail, Health Signal, diagnosis, score, deliverability, Trust Score, DNSSEC/TTL (INF-007), blacklist result (INF-008), or Evidence / proof of health. HEALTH-001 may consume it later; Reverse DNS Observation does not create a Health Signal.
- Related: Observation Evidence, SMTP Greeting Observation, DNS Health Observation, Reputation Observation, Health Signal.

**SMTP Greeting Observation**
- Definition: An immutable, persisted snapshot of one raw SMTP greeting/banner and EHLO capability observation for a resolved IP of an observed MX host of a registered Domain.
- Purpose: It records only facts actually returned: identity from the registered Domain, the source MX hostname taken from Observation Evidence, the resolved IP and address family, when it was observed, the connection/probe outcome, the raw SMTP greeting/banner as returned, and the raw EHLO response/capabilities if the server actually accepted EHLO. The read is MX hostname (from Observation Evidence) → A and/or AAAA → SMTP service. Both address families are observed when addresses are actually available. The SMTP connection is an active probe, permitted only against infrastructure associated with the registered Domain; it does not send mail or mutate remote infrastructure, and it does not probe arbitrary IPs, unrelated hosts, or third-party infrastructure. It is append-only: a later observation creates a new snapshot. Timeout, connection refusal, or DNS failure is observation unavailable: persist nothing for that probe (INV-01), matching Reputation Observation. Do not manufacture a banner or EHLO. A connection/probe outcome records unavailability without fabricated greeting or EHLO text; those texts are stored only when actually observed. It is Phase 08 raw infrastructure observation, owned by Delivery Observation, and a snapshot separate from Observation Evidence and Reverse DNS Observation.
- Not: Observation Evidence (greeting/EHLO is not added there). Not Reverse DNS Observation. Not HELO/EHLO validation, valid/invalid, healthy, deliverability score, open relay finding, security diagnosis, compliance, Health Signal, Trust Score, diagnosis, recommendation, report/proof, mail sending, SMTP authentication, relay testing, or delivery testing. Not INF-007 (DNSSEC/TTL), INF-008 (blacklists), or HEALTH-001. HEALTH-001 may consume it later; SMTP Greeting Observation does not create a Health Signal.
- Related: Observation Evidence, Reverse DNS Observation, DNS Health Observation, Reputation Observation, Health Signal.

**Infrastructure Map**
- Definition: The topology read-model of a registered Domain's email infrastructure along the frozen chain Domain → DNS → SPF → DKIM → DMARC → Senders → Inbox (MAP-001).
- Purpose: It presents which of those nodes are observed, not observed, or unavailable from existing snapshots. Observation Evidence is the primary source for DNS and SPF. MAP-001 remains topology/read-model only. It is not a persisted aggregate, not a table, not Health Signal, and not MAP-002 (color), MAP-003 (detail), or MAP-004 (export).
- Not: An observation aggregate; Health Signal; Trust Score; color, score, health, or severity; MAP-002–004; a graph database; or a public HTTP resource.
- Related: Observation Evidence, Domain, Reputation Observation, DNS Health Observation, Blacklist Observation, Health Signal.

**MAP-002 (Color-coded view)**
- Definition: A later color-coded view of HEALTH on the Infrastructure Map topology. Green/Yellow/Red represents HEALTH, not observation availability.
- Purpose: When HEALTH-001 exists, MAP-002 consumes the existing real Health Signal; it does not calculate or create Health Signal. It depends on MAP-001 topology plus HEALTH-001 Health Signal **and** a later MAP-002 freeze of how unknown/healthy/at-risk maps to colors. HEALTH-001 does not freeze G/Y/R. An **unknown** envelope is not a color source. HEALTH-001 (Phase 10) is the authoritative source of health state. MAP-001 OBSERVED / NOT_OBSERVED / UNAVAILABLE must never map directly to G/Y/R. Raw INF-001–008 observations must never be converted into MAP-002 colors. Trust Score is not a color source. No Health Signal implementation in Phase 08. This freeze implements nothing: no table, Flyway, backend health-calculation, HTTP API, or frontend. MAP-001 is unchanged. Per-node health semantics, thresholds, scores, and missing-node behavior are not defined here.
- Not: Observation availability; a mapping of MAP-001 OBSERVED/NOT_OBSERVED/UNAVAILABLE to color; a conversion of INF-001–008 raw observations into color; Trust Score; a Health Signal implementation; HEALTH-001; Phase 08 work.
- Related: Infrastructure Map, Health Signal.

**MAP-003 (Node detail panel)**
- Definition: A later panel opened by selecting or clicking a MAP-001 node. It may show that node's identity, type, observation availability/status, and real raw observation facts or evidence references when those facts exist.
- Purpose: Preserve WF-5 click-to-detail. Contents are raw node metadata and observations, not independent health computation. MAP-003 must not calculate, create, infer, or own Health Signal. Health meaning and G/Y/R are owned by MAP-002 consuming HEALTH-001. OBSERVED / NOT_OBSERVED / UNAVAILABLE must never be translated into health, severity, warning, critical, or color. No Trust Score, deprecated scoring, diagnosis, recommendation, compliance, or fabricated explanation. NOT_OBSERVED nodes must not receive fabricated details. DKIM, DMARC, Senders, and Inbox currently have no observed facts and must not display invented details. Evidence references must not automatically become arbitrary raw-payload dumps; a detailed evidence-access/API contract is frozen separately before implementation. This freeze implements nothing: no table, Flyway, scheduler, independent backend health service, HTTP API, or frontend. Must not modify INF-001–008, MAP-001, MAP-002, V1–V11, or HEALTH-001. MAP-003 remains ⬜.
- Not: Health Signal; MAP-002 G/Y/R; a translation of observation availability into health, severity, warning, critical, or color; Trust Score; diagnosis; recommendation; compliance; fabricated explanation; fabricated details for NOT_OBSERVED nodes or for DKIM, DMARC, Senders, or Inbox; an automatic raw-payload dump; a new persistence, scheduler, or health service; Phase 08 implementation.
- Related: Infrastructure Map, MAP-002 (Color-coded view), Observation Evidence, Health Signal.

**MAP-004 (PNG/SVG export)**
- Definition: A later export of the Infrastructure Map visualization. PNG and SVG are both frozen output formats.
- Purpose: To export the MAP-001 topology as a visualization. MAP-004 is not Phase 16 proof of health and does not replace EVID-003. It is not Phase 15 PDF/email reporting and must not merge into FUN-R2. Keep it separate from Phase 15/16 reporting and proof. Exported structure is based on MAP-001 topology. If health colors eventually appear in the export, they come only from MAP-002 consuming real HEALTH-001 Health Signal. Never derive G/Y/R from raw INF-001–008 observations, OBSERVED / NOT_OBSERVED / UNAVAILABLE status, Trust Score, or deprecated scoring. NOT_OBSERVED nodes must not receive fabricated health, facts, or details. MAP-004 must not calculate or create Health Signal. This freeze implements nothing: no table, Flyway, scheduler, graph database, or persistence model; no invented HTTP/API contract; no selected or added rendering/export library; no frontend or backend export code. Must not modify INF-001–008, MAP-001, MAP-002, MAP-003, V1–V11, or HEALTH-001. MAP-004 remains ⬜.
- Not: Phase 16 proof; EVID-003; Phase 15 PDF/email reporting; FUN-R2; Health Signal; a G/Y/R derivation from raw INF observations, observation availability, Trust Score, or deprecated scoring; fabricated health/facts/details for NOT_OBSERVED nodes; a new table, Flyway, scheduler, graph store, or persistence model; an HTTP/API contract; a rendering/export library choice; frontend or backend export implementation.
- Related: Infrastructure Map, MAP-002 (Color-coded view), Health Signal, Evidence / proof of health.

**Evidence / proof of health**
- Definition: A grounded demonstration of delivery health a customer can show to a client, auditor, or board.
- Purpose: It lets a customer prove rather than assert. It is later, customer-facing reporting — not the raw observation snapshot.
- Not: Observation Evidence, which is the immutable factual snapshot of an infrastructure observation.
- Related: Reporting, Observation Evidence.

**Silent failure**
- Definition: A delivery failure that produces no immediate signal and is discovered, if at all, only after the cost is incurred.
- Purpose: It is the central problem the product exists to end.

**Silent data loss**
- Definition: A loss of reception, storage, or processing that is not detected or surfaced.
- Purpose: It is prohibited; the product must detect and surface any such loss.
- Related: Reliability.

**Consent**
- Definition: Explicit, informed agreement by the customer before any action is taken on their live infrastructure.
- Purpose: It is the required gate for any real-world action; the product never acts on live systems without it.
- Related: Trust and safety.

## 8. Terms We Never Use

These terms are prohibited because they misrepresent the company, the product, or the promise. Each is avoided deliberately.

- **DMARC checker.** It reduces the product to a single-record check, which is the opposite of reasoning across the whole picture. The product is intelligence, not a check.
- **Warmup / warming.** It is reputation manipulation — a shortcut, not confidence, and it is legally gray. The product protects health; it does not game it.
- **Marketing automation platform.** It implies we send campaigns and manage audiences. We do not send; we protect what a business sends.
- **Communication platform.** It implies we own the channel. We do not; we ensure the channel is trustworthy.
- **AI-washing.** It is labeling something as AI that is not genuine, grounded intelligence — a lie about the product.
- **Fabricated rank / forecast.** A rank or forecast we cannot ground in real data is a lie. We present only what real data supports.
- **Guaranteed deliverability.** An absolute promise of inbox delivery overclaims what is real and contradicts the truth principle. We promise confidence and truth, not a guarantee no one can keep.
- **Lock-in.** We do not hold customers through artificial dependency; value is the only reason to stay.
- **Switching cost.** Same reason — we never rely on the difficulty of leaving as a reason to remain.

## 9. Naming Rules

How future documents should introduce and use terminology:

- **Name the term once and correctly.** When a document first uses an important term, write it exactly as in this glossary and link to it.
- **Use one term per concept.** Do not alternate between synonyms; if the document needs variety, vary the sentence, not the term.
- **Prefer the product-level term.** Where a workflow execution term and a product-level term both exist, use the product-level term in product and requirements documents; see the Canonical Naming Table.
- **Check before inventing.** If a needed term is not in the glossary, add it here and get it reviewed before using it broadly.
- **Flag drift.** If a source uses a term differently, note it here rather than quietly reinterpreting it.
- **Keep definitions concise.** A glossary entry states the meaning and purpose; it does not restate the whole product strategy.

## Canonical Naming Table

The preferred terms of the company and the terms to avoid in new writing. Use the Preferred Term; avoid the Avoid term for the reason given.

| Preferred Term | Avoid | Reason |
| --- | --- | --- |
| Health Signal | Trust Score | Trust Score is a deprecated legacy alias (including workflow 0–100 and weights); Health Signal is the canonical measure. HEALTH-001 is not Trust Score. |
| Health Signal | Health (Trend) as the whole measure | Health Trend is only the time-based representation, not the full indication. |
| Health Trend | Health Signal to mean the trend | Health Signal is the overall indication; use Health Trend for the time series. |
| Capability | Module to mean what is enabled | A Capability is what the product enables; a Module is a grouping. |
| Module | Capability to mean a grouping | A Module is a logical grouping or workspace that contains capabilities. |
| Confidence | Reliability to mean the value sold | Reliability is the product's internal standard; Confidence is the outcome sold. |
| Trust | Confidence interchangeably | Trust is what a customer's stakeholders hold; Confidence is what the customer holds. |
| Observation Evidence | Evidence / proof of health to mean the raw observation | Observation Evidence is the immutable factual snapshot; Evidence / proof of health is later customer-facing proof. |
| Evidence / proof of health | Observation Evidence to mean a customer-facing report | Proof is Reporting's demonstration of health, not the raw observed facts. |
| Reputation Observation | Reputation or Health Signal to mean the raw provider snapshot | Reputation Observation is the persisted raw Postmaster/SNDS fact; Reputation is the later health input; Health Signal is later interpretation. |
| Reverse DNS Observation | FCrDNS, Health Signal, or Observation Evidence to mean the PTR snapshot | Reverse DNS Observation (ReverseDnsObservation) is the persisted raw reverse-DNS fact (MX→A/AAAA→PTR); it is not FCrDNS, not Health Signal, and not Observation Evidence. |
| SMTP Greeting Observation | HELO/EHLO validation, Health Signal, or Observation Evidence to mean the SMTP banner snapshot | SMTP Greeting Observation (SmtpGreetingObservation) is the persisted raw SMTP greeting and EHLO fact (MX→A/AAAA→SMTP); it is not validation, not Health Signal, and not Observation Evidence. |
| DNS Health Observation | Health Signal, Trust Score, or Observation Evidence to mean TTL/DNSSEC/propagation | DNS Health Observation (DnsHealthObservation) is the persisted raw TTL, DNSSEC-presence, and multi-resolver fact; it is not Health Signal, not Trust Score, not HEALTH-001, and not Observation Evidence. |
| Blacklist Observation | Health Signal, listed/not-listed, or Reputation Observation to mean the raw blacklist snapshot | Blacklist Observation (BlacklistObservation) is the persisted raw Spamhaus/Barracuda/SURBL/URIBL fact; it is not Health Signal and not a listed conclusion. |
| Infrastructure Map | Health Signal, Map aggregate, or Observation Evidence to mean the topology | Infrastructure Map (MAP-001) is a topology read-model of Domain→DNS→SPF→DKIM→DMARC→Senders→Inbox; it is not Health, not a persisted aggregate, and not MAP-002–004. |
| MAP-002 (Color-coded view) | Mapping MAP-001 OBSERVED/NOT_OBSERVED/UNAVAILABLE, INF-001–008 observations, or Trust Score to G/Y/R | MAP-002 G/Y/R represents HEALTH from HEALTH-001 Health Signal, not observation availability. It consumes existing Health Signal; it does not create it. |
| MAP-003 (Node detail panel) | Independent health computation, G/Y/R, Trust Score, diagnosis, recommendation, compliance, or fabricated node details | MAP-003 is WF-5 click-to-detail on a MAP-001 node: raw identity, type, observation availability, and real facts/evidence refs when they exist. It does not calculate, create, infer, or own Health Signal. |
| MAP-004 (PNG/SVG export) | Phase 16 proof, EVID-003, Phase 15 PDF/email reporting, FUN-R2, Health Signal, or G/Y/R from raw INF / OBSERVED / Trust Score | MAP-004 is export of the Infrastructure Map visualization (PNG and SVG both frozen). Structure is MAP-001 topology. It does not calculate or create Health Signal. |
| Alignment | Issue, Root cause, Health Signal, Observation Evidence field, or persisted Alignment table | Alignment is a derived value (DIAG-001 Analyzer-class) from DMARC Aggregate Evidence when INT-003–006 facts exist. It is not an Issue, not sender detection, not root cause, not Health Signal, and not an OE field. |
| Diagnosis | Health Signal, Alignment as an Issue, or DMARC ingest ownership | Diagnosis owns Issue and Root cause. It consumes DMARC Aggregate Evidence; it does not own or persist ingest. It must not fabricate Health Signal. HEALTH-001 produces Health Signal. DIAG-001 Alignment derivation is not Issue lifecycle. |
| DMARC Report Receipt | Observation Evidence, Alignment, or Parsed DMARC Report to mean the raw inbound payload | DMARC Report Receipt is the received aggregate report as received. It is not OE and not Alignment. |
| Parsed DMARC Report | Alignment, Observation Evidence, or Health Signal to mean extracted XML facts | Parsed DMARC Report is raw parsed aggregate facts. It is not Alignment and not OE. |
| DMARC Aggregate Evidence | Alignment table, Observation Evidence, or Health Signal to mean the ingest snapshot | DMARC Aggregate Evidence is the append-only aggregated ingest snapshot owned by Delivery Observation. DIAG-001 and HEALTH-001 (H2 alignment facts) consume it. It is not an Alignment table, not OE, and not Health Signal. |

## 10. Governance

This glossary changes deliberately, like the Constitution.

- **Adding a term.** A proposal is written down with the term, its definition, and why it is needed, then reviewed before adoption.
- **Changing a term.** Changes are recorded with what the old meaning was and why it changed.
- **Resolving conflicts.** An open conflict is listed below and resolved only by the founders, with the decision recorded.
- **Freezing.** Only the founders may freeze this document after review.

### Terminology Resolution Record

- **Resolved 2026-08-07 by founder decision:** **Health Signal** is the canonical product term; **Health Trend** is its time-based representation; **Trust Score** is deprecated and kept only as a legacy alias for historical references. **Capability** is what the product enables; **Module** is a logical product grouping or workspace that contains one or more capabilities.
- **Resolved 2026-08-14 by CTO decision:** **Observation Evidence** is the immutable, persisted snapshot of facts actually returned by a real infrastructure observation. **Evidence / proof of health** remains the later customer-facing demonstration owned by Reporting. Standalone "Evidence" is avoided where the two could be confused. Observation Evidence is owned by Delivery Observation; the Domain provides identity and tenant isolation (`organization_id` → `domain_id`) and is not the bounded-context owner.
- **Resolved 2026-08-14 by CTO decision:** **Reputation Observation** is the immutable, persisted raw fact of one Google Postmaster or Microsoft SNDS observation for a registered Domain. It is owned by Delivery Observation. It is not the Reputation concept used later as a health input, not a Reputation aggregate, and not Health Signal. Organization and domain identity are derived from the registered Domain; the caller supplies only the domain name. A provider failure or unavailability persists nothing (INV-01).
- **Resolved 2026-08-14 by CTO decision:** **Reverse DNS Observation** (ReverseDnsObservation) is the immutable, persisted raw reverse-DNS snapshot for IPs of observed MX hosts of a registered Domain (MX hostname → A and/or AAAA → PTR). Delivery Observation owns it. Observation Evidence remains frozen DNS-only (MX, TXT, SPF, observedAt, identity); PTR is not added to Observation Evidence. Identity is derived from the registered Domain; the caller supplies only the domain name. No public HTTP API. No independent scheduler. It is not FCrDNS, Health Signal, diagnosis, score, deliverability, Trust Score, INF-007 (DNSSEC/TTL), or INF-008 (blacklists). HEALTH-001 may consume it later; INF-005 does not create Health Signal. A future persistence migration will be required; it is not created in this freeze. INF-001 through INF-004 remain untouched until implementation is authorized.
- **Resolved 2026-08-14 by CTO decision:** **SMTP Greeting Observation** (SmtpGreetingObservation) is the immutable, persisted raw SMTP greeting/banner and EHLO capability snapshot for IPs of observed MX hosts from Observation Evidence of a registered Domain (MX hostname → A and/or AAAA → SMTP service). Delivery Observation owns it. Observation Evidence remains frozen DNS-only; greeting/EHLO is not added there. Reverse DNS Observation is unchanged. Identity is derived from the registered Domain; the caller supplies only the domain name. The SMTP connection is an active probe permitted only against registered-domain infrastructure; no mail send, no mutation, no SMTP auth, no relay or delivery testing, and no probing of arbitrary IPs or third-party hosts. No public HTTP API. No independent scheduler; INF-004 is not modified. It is not HELO/EHLO validation, Health Signal, diagnosis, score, deliverability, open relay, security finding, Trust Score, INF-007, INF-008, or mail sending. HEALTH-001 may consume it later; INF-006 does not create Health Signal. Unavailable (timeout, refusal, DNS failure) persists nothing; do not fabricate banner or EHLO. Connection/probe outcome records unavailability without fabricated greeting/EHLO text. Flyway V13 persists `smtp_greeting_observation`. Do not modify Observation Evidence, V8–V12, or ReverseDnsObservation. INF-001 through INF-005 remain unchanged.
- **Resolved 2026-08-14 by CTO decision:** **DNS Health Observation** (DnsHealthObservation) is the immutable, persisted raw DNS TTL, DNSSEC-related presence, and named-resolver propagation snapshot for a registered Domain. Delivery Observation owns it. Observation Evidence remains frozen DNS-only (MX, TXT, SPF, observedAt, identity); TTL/DNSSEC/propagation are not added to Observation Evidence. Identity is derived from the registered Domain; the caller supplies only the domain name. No public HTTP API. No independent scheduler; INF-004 is not modified. It is not Health Signal, Trust Score, HEALTH-001, INF-008, cryptographic DNSSEC validation, or a health/score/validity conclusion. HEALTH-001 may consume it later; INF-007 does not create Health Signal. Flyway V10 persists `dns_health_observation`. INF-001 through INF-004 remain unchanged.
- **Resolved 2026-08-14 by CTO decision:** **Blacklist Observation** (BlacklistObservation) is the immutable, persisted raw fact of one Spamhaus, Barracuda, SURBL, or URIBL observation for a registered Domain. It is owned by Delivery Observation. It is not Health Signal and not a listed/not-listed conclusion. Organization and domain identity are derived from the registered Domain; the caller supplies only the domain name. Each provider is queried independently. A provider failure or unavailability persists nothing for that provider (INV-01). No public HTTP API. No independent scheduler; INF-004 is not modified. Flyway V11 persists `blacklist_observation`. INF-001 through INF-007 remain unchanged.
- **Resolved 2026-08-14 by CTO decision:** **Infrastructure Map** (MAP-001) is a topology read-model of the frozen chain Domain → DNS → SPF → DKIM → DMARC → Senders → Inbox for a registered Domain. It is not an observation aggregate, not a persisted table or graph, not Health Signal, and not MAP-002–004. Observation Evidence is the primary source for DNS and SPF; DKIM, DMARC, Senders, and Inbox remain unobserved until those facts exist. No public HTTP API. No Flyway. INF-001 through INF-008 remain unchanged.
- **Resolved 2026-08-15 by CTO decision:** **MAP-001** remains topology/read-model only. **MAP-002** G/Y/R represents HEALTH, not observation availability. MAP-001 OBSERVED/NOT_OBSERVED/UNAVAILABLE must never map directly to G/Y/R. Raw INF-001–008 observations must never be converted into MAP-002 colors. Trust Score is not a color source. HEALTH-001 (Phase 10) is the authoritative source of health state. MAP-002 consumes existing real Health Signal when HEALTH-001 exists; it does not calculate or create Health Signal. No Health Signal implementation in Phase 08. This freeze: no table, Flyway, backend health-calculation, HTTP API, or frontend for MAP-002. MAP-001 unchanged. MAP-002 depends on MAP-001 topology + HEALTH-001 Health Signal. Do not invent per-node health semantics, thresholds, scores, or missing-node behavior.
- **Resolved 2026-08-15 by CTO decision:** **MAP-003** (Node detail panel) is opened by selecting or clicking a MAP-001 node (WF-5 click-to-detail). The panel may show node identity, type, observation availability/status, and real raw observation facts or evidence references when those facts exist. Contents are raw node metadata and observations, not independent health computation. MAP-003 must not calculate, create, infer, or own Health Signal. Health meaning and G/Y/R are owned by MAP-002 consuming HEALTH-001. OBSERVED/NOT_OBSERVED/UNAVAILABLE must never be translated into health, severity, warning, critical, or color. No Trust Score, deprecated scoring, diagnosis, recommendation, compliance, or fabricated explanation. NOT_OBSERVED nodes must not receive fabricated details. DKIM, DMARC, Senders, and Inbox currently have no observed facts and must not display invented details. Evidence references must not automatically become arbitrary raw-payload dumps; a detailed evidence-access/API contract is frozen separately before implementation. Must not modify INF-001–008, MAP-001, MAP-002, V1–V11, or HEALTH-001. No new table, Flyway, scheduler, independent backend health service, HTTP API, or frontend in this freeze. MAP-003 remains ⬜. Do not invent per-node health.
- **Resolved 2026-08-15 by CTO decision:** **MAP-004** is export of the Infrastructure Map visualization. PNG and SVG are both frozen output formats. It is not Phase 16 proof and does not replace EVID-003. It is not Phase 15 PDF/email reporting and must not merge into FUN-R2. Keep it separate from Phase 15/16 reporting and proof. Exported structure is based on MAP-001 topology. If health colors eventually appear in the export, they come from MAP-002 consuming real HEALTH-001 Health Signal. Never derive G/Y/R from raw INF-001–008 observations, OBSERVED status, Trust Score, or deprecated scoring. NOT_OBSERVED nodes must not receive fabricated health, facts, or details. MAP-004 must not calculate or create Health Signal. No new table, Flyway, scheduler, graph database, or persistence model. Do not invent an HTTP/API contract. Do not select or add a rendering/export library in this freeze. No frontend or backend export code in this freeze. Must not modify INF-001–008, MAP-001, MAP-002, MAP-003, V1–V11, or HEALTH-001. MAP-004 remains ⬜.
- **Resolved 2026-08-15 by CTO decision:** **DIAG-001** is Analyzer-class Alignment derivation (Constitution Analyzer: SPF/DKIM/DMARC parsing + alignment). It is not sender detection and not root cause (those remain Alignment Intelligence / DIAG-002/003). Real Alignment requires DMARC Aggregate Evidence from INT-003–006; DIAG-001 cannot compute real SPF/DKIM envelope-vs-header alignment until those facts exist in code. Diagnosis is a consumer of ingest, not the owner. DIAG-001 does not infer DKIM/DMARC/alignment from Phase 08 DNS TXT. Observation Evidence stays frozen to MX/TXT/SPF/observedAt/identity; do not add DKIM/DMARC/alignment fields to OE. Alignment is a derived/value concept; no Alignment persistence/table is authorized; DATA Alignment remains a VO with meaning, not a field list. DIAG-001 does not create Health Signal or Trust Score. FUN-X1 belongs primarily to DIAG-003; DIAG-001 traces to Alignment Engine / FUN-H2. Diagnosis owns Issue and Root cause; Alignment is not an Issue; DIAG-001 does not own Issue lifecycle. Lineage points to the actual evidence source (OE for DNS/SPF facts; DMARC Aggregate Evidence / Parsed DMARC Report for alignment identities). Health Signal is produced by HEALTH-001 (Phase 10 / Delivery Observation), not by Diagnosis. Diagnosis consumes Health Signal when it can state healthy vs at-risk for Issue/root-cause; an unknown envelope does not unblock DIAG-003. Alignment (DIAG-001) is derived from DMARC Aggregate Evidence, not from Health Signal. HEALTH-001 does not consume DIAG-001 in-memory Alignment. DIAG-003 remains blocked until Health Signal can state healthy vs at-risk. Do not skip ingest facts (INT-003–006). HEALTH-001 remains ⬜. INF-001 title DKIM is leftover: DKIM observation is not in OE; INF-001 implementation is not reopened. DIAG-001 remains ⬜.
- **Resolved 2026-08-15 by CTO decision:** **INT-003–006** freeze DMARC Report Ingestion as RUA aggregate reports only. RUF/forensic ingest is out of scope. Canonical terms: **DMARC Report Receipt**, **Parsed DMARC Report**, **DMARC Aggregate Evidence**. Delivery Observation owns ingest evidence; Observation Evidence is unchanged; Diagnosis consumes and does not persist ingest. V1 receive is an inbound HTTP webhook on the Spring Boot application (Phase 01: Java 21 / Spring Boot / PostgreSQL). IMAP is deferred. AWS SES → S3 → Lambda is not authorized for V1. `{uuid}` is the registered Domain id. Unknown uuid is discarded. Conceptual persistence is authorized without physical table names. INT-005 includes Store. INT-006 triggers DIAG-001 only, in-process after Store, with report_id+Domain idempotency. No FUN ID. No public report-list API. LAUNCH-002 remains Phase 31. This freeze does not implement Java, SQL, or APIs. INT-003–006 remain ⬜.
- **Resolved 2026-08-15 by CTO decision:** **HEALTH-001** is Delivery Observation’s Health Signal envelope (unified indication + lineage), **not** Trust Score. Diagnosis does not own Health Signal; DIAG-001 does not create it. HEALTH-001 consumes **persisted snapshots only**: Observation Evidence (V8) DNS/SPF facts; DMARC Aggregate Evidence (INT-003–006 Store / V15) for H2 authentication/alignment facts; Reputation Observation (V9). Never `Alignment.lastDerived()` / in-memory DIAG-001. No Alignment table. V1 not required: Blacklist, Reverse DNS, SMTP Greeting, DNS Health Observation, DIAG-001 Alignment VO. Missing any required source: **persist nothing**. Vocabulary: healthy | at-risk | unknown. Append-only snapshots; current = latest by time. HEALTH-002 is trend derived later. Trigger: explicit in-process `record`/`derive` by domainName — not scheduled, not INF-004, not INT-006, no public HTTP. Idempotent: persist nothing new if all cited source snapshot ids equal the latest Health Signal. INV-11 cites OE id, Aggregate Evidence ids actually used, Reputation Observation ids actually used — not OE for alignment identities, not Alignment table, not DIAG-001 memory. HEALTH-001 does not persist Issues. MAP-002 consumes Health Signal; colors not frozen here. HEALTH-001 remains ⬜.
- **Resolved 2026-08-15 by CTO decision:** **HEALTH-001 interpretation** assigns the three indications from **stored V15 tokens** (INT-004: `spf_result`/`dkim_result` = first auth_results results; `spf_aligned`/`dkim_aligned` = policy_evaluated spf/dkim; `disposition` = policy_evaluated disposition; tests store `pass`/`fail`/`none`/`quarantine`; RFC also `reject`). At-risk: any cited latest-report row with disposition quarantine/reject or auth/alignment `fail` (union of failures; known failure wins). Healthy: every cited row non-null disposition `none` and SPF+DKIM result+alignment all `pass`. Else unknown when snapshots exist (null judgment fields; `none`/`neutral`/`softfail`/`temperror`/`permerror` on auth; mixed pass+none). Never persist unknown when a required snapshot is missing. **Reputation Observation is lineage-required presence only, not an indication voter** (V9 fields: source, observedAt, opaque payload; production Unavailable adapters persist no row). **OE does not vote** from MX/TXT/SPF strings. No TTL/staleness. No V16 reason column; explanation is the rule that fired at read time. No 0–100, G/Y/R, Trust Score. Current Java always-unknown is insufficient; schema sufficient. This decision does not implement Java. DIAG-003 remains blocked until Java assigns healthy vs at-risk. HEALTH-001 remains ⬜.
- **Status:** No open terminology conflicts remain.

---

*This glossary is the single source of truth for the company's words. Every future document, engineer, product manager, and AI agent uses the same definitions — and when two sources disagree on a word, this document records it for the founders to settle.*
