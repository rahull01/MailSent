# MailSent — Data Model (Conceptual)

> The conceptual business data model of MailSent.
>
> This document models the business data of the product: the aggregates, the entities, the value objects, and how ownership, identity, lineage, and the invariants hold. It is **not** a database model. An aggregate is not a table, an entity is not a row, and a value object is not a primitive. Nothing here presumes a storage technology.
>
> It is subordinate to the [Project Constitution](../00_Foundation/PROJECT_CONSTITUTION.md), the [Master Workflow](../00_Foundation/MAILSENT_MASTER_WORKFLOW.md), and the [Glossary](../00_Foundation/GLOSSARY.md), and is consistent with the [Domain Model](01_DOMAIN_MODEL.md), the [Ubiquitous Language](02_UBIQUITOUS_LANGUAGE.md), the [Bounded Contexts](03_BOUNDED_CONTEXTS.md), the [Information Architecture](04_INFORMATION_ARCHITECTURE.md), the [System Architecture](05_SYSTEM_ARCHITECTURE.md), and the [AI Architecture](06_AI_ARCHITECTURE.md). Terminology uses only the Glossary; every aggregate traces through the domain model and a context to a requirement and a customer problem.

---

**Document governance**

- **Status:** Draft
- **Version:** 1.0
- **Owner:** Founders
- **Review Status:** Pending Founder Review

Only the founders may freeze this document after review.

**Next review trigger:**
- Product philosophy changes
- The domain model changes
- The data boundaries change

---

## 1. Purpose

The business data model is the agreed shape of the product's information, seen as a set of owning wholes. It answers, for each whole: what is it, what does it own, what does it protect, and what keeps it true. It is the model the software will later persist and serve, and it is drawn purely from the business — not from any storage engine.

The model exists so that the business's data is owned, consistent, and traceable: every piece of data belongs to a whole, every whole protects a rule, and every conclusion can be traced to the evidence that produced it.

## 2. Data Modeling Principles

- **One owner per whole.** An aggregate owns its entities; no entity is shared between two owning wholes.
- **Truth at the root.** The value a whole protects is stated at its root, and the whole holds the business rules that keep that value true.
- **Immutable values.** A value object is a fact that does not change; a changed fact is a new value, not an edited one.
- **Identity is stable.** An entity keeps the same identity through its life; identity never changes, and no two entities share one.
- **Everything has a lineage.** Every assessment and conclusion traces to evidence; nothing stands without a source.
- **No orphans.** Every entity and every reference belongs to an aggregate and a context; nothing floats.
- **The Glossary is the language.** Terms are used as the Glossary defines them, never redefined here.

## 3. Core Aggregates

For each aggregate: Purpose, Aggregate Root, Owns, Protects, Lifecycle, Business Invariants.

### Organization

- Purpose: To hold a business entity, its users, and its owned domains as one consistent whole.
- Aggregate Root: Organization.
- Owns: its Users, its Domains, and in an agency its Clients and the Portfolio.
- Protects: the identity and the separation of the organization (INV-09) and the Consent it gives (INV-02).
- Lifecycle: an Organization is formed, grows by adding domains and users, and persists; it ends when the customer closes it.
- Consistency Boundary: the users, domains, and Consent of an organization hold as one unit — a change to ownership or consent is whole, never partial, and the organization never appears half-formed.
- Business Invariants: INV-01 (truth in what it holds), INV-02 (consent), INV-06 (confidence and trust), INV-09 (isolation), INV-10 (identity), INV-13 (ownership).

### Domain Health

- Purpose: To hold the truth of a single domain's delivery.
- Aggregate Root: Domain.
- Owns: the Health Signal and the Health Trend of that domain. **Clarified (HEALTH-001 freeze):** Issues remain Diagnosis (DIAG-004); HEALTH-001 must **not** persist Issues. Observation Evidence, Reverse DNS Observation, SMTP Greeting Observation, DNS Health Observation, Reputation Observation, Blacklist Observation, DMARC Report Receipt, Parsed DMARC Report, and DMARC Aggregate Evidence are not owned by this aggregate; Delivery Observation owns them. The Domain and its Organization provide identity and tenant isolation only.
- Protects: the truth of the health (INV-01) and the single-health rule (INV-05).
- Lifecycle: a Domain is added to observation, receives its first health, is watched and improved, and is retired when it leaves observation.
- Consistency Boundary: the Health Signal and the Health Trend of a domain are always consistent with the observed truth — a domain is never both healthy and at risk in one observation (INV-05); **unknown** is the indication when snapshots exist but we cannot truthfully choose healthy or at-risk (INV-01). Missing a required snapshot is persist-nothing, not unknown. Issues remain Diagnosis and are not stored as Health Signal.
- Business Invariants: INV-01, INV-04, INV-05, INV-10, INV-11, INV-14.

### Issue

- Purpose: To hold one delivery problem and its full journey to resolution.
- Aggregate Root: Issue.
- Owns: the Root cause, the Business impact, the Priority, the Guidance, and the Fix verification.
- Protects: the closure rule (INV-03) and the grounding of the cause (INV-04).
- Lifecycle: an Issue is discovered, diagnosed, explained, prioritized, guided, verified, and closed — or reopened if it recurs.
- Consistency Boundary: an Issue's cause, impact, priority, guidance, and verification form one coherent resolution — an Issue is never resolved while unverified, and its parts never contradict each other.
- Business Invariants: INV-03, INV-04, INV-10, INV-11, INV-12, INV-14.

### Client Relationship

- Purpose: To hold the relationship between an agency and a client, and the Evidence / proof of health that sustains its trust.
- Aggregate Root: Client.
- Owns: the client's domains, the Reports and Proof Evidence produced for it, and the state of the relationship. This is customer-facing Report / Proof Evidence. It is not Observation Evidence; Client Relationship does not own or mutate the raw observation snapshot.
- Protects: the trust of the client through grounded evidence (INV-01, INV-06), never assertion.
- Lifecycle: a Client relationship is formed when an agency takes it on, is sustained through delivery health, and ends when the relationship ends.
- Consistency Boundary: the Reports and Proof Evidence of a client always reflect that client's real health — the proof produced for a client never exceeds what is grounded for that client, keeping the relationship's trust consistent. That proof ownership is not ownership of Observation Evidence.
- Business Invariants: INV-01, INV-06, INV-09, INV-10, INV-13, INV-14.

### Aggregate Classification

The strategic value of each aggregate, in one sentence each.

| Aggregate | Classification | Why |
| --- | --- | --- |
| Domain Health | Core Domain | It holds the truth of delivery, which is the heart of the business. |
| Issue | Core Domain | It carries the diagnosis and the resolution, the differentiation of the product. |
| Organization | Supporting Domain | It holds identity and ownership, enabling the core without being the moat. |
| Client Relationship | Supporting Domain | It holds the trust evidence for agencies, a support to the core. |

The classification says where value is concentrated: the Core is the reason the business exists; the Supporting makes the core usable and complete.

## 4. Business Entities

For each entity: Identity, Responsibility, Lifecycle. No fields.

**Organization**
- Identity: its own, stable across its life.
- Responsibility: to own the domains and users of one business.
- Lifecycle: formed, grown, persisted, closed.

**Client**
- Identity: its own, as an organization served by an agency.
- Responsibility: to be the party whose delivery and trust the agency protects, and who receives proof.
- Lifecycle: formed into the relationship, sustained, ended.

**Domain**
- Identity: its own, as the owned identity whose delivery is observed.
- Responsibility: to be the unit of observation, diagnosis, and reporting.
- Lifecycle: added to observation, watched, retired.

**User**
- Identity: its own, as a person acting within an organization.
- Responsibility: to act on health, decide, and own the outcome of any conclusion acted on.
- Lifecycle: joined to the organization, active, removed.

**Issue**
- Identity: its own, as one delivery problem.
- Responsibility: to carry the problem from discovery to a verified resolution.
- Lifecycle: discovered, diagnosed, resolved, closed or reopened.

**Report**
- Identity: its own, as one produced account of health.
- Responsibility: to state the proof of health to a client, auditor, or board, grounded in the health it reflects.
- Lifecycle: produced, shared, kept as history.

## 5. Value Objects

For each: Meaning, Why immutable, Usage. Values reference the Glossary and are not redefined.

**Health Signal**
- Meaning: the unified, legible indication of a domain's delivery health (Glossary). Vocabulary: **healthy** | **at-risk** | **unknown**. HEALTH-001 interpretation freeze assigns those values from cited V15 tokens (`disposition`, `spf_result`, `dkim_result`, `spf_aligned`, `dkim_aligned`) when required snapshots exist; missing a required snapshot persists nothing. Components are **cited evidence refs**, not separately persisted health scores. No Trust Score, 0–100, or G/Y/R.
- Why immutable: a health indication is a snapshot of one observation; it never mutates; later observations are **append-only** new snapshots. Current signal = latest by time (INV-05 one at a time).
- Usage: the ground truth Delivery Observation reports and all others read. Physical table Flyway **V16** `health_signal`: `indication` TEXT (`healthy` | `at-risk` | `unknown`) plus lineage id lists; **no reason column** — do not add one. Identity: organizationId, domainId, domainName from registered Domain. Not Trust Score. Not Issues. Not Alignment. Not DIAG-001 memory.

**Health Trend**
- Meaning: the time-based representation of a Health Signal (Glossary). HEALTH-002; derived from Health Signal snapshots. Not required to complete HEALTH-001.
- Why immutable: it is a view of past snapshots; it is recomputed, never changed.
- Usage: to show improvement, stability, or decline.

**Root cause**
- Meaning: the underlying cause of an issue, not its symptom (Glossary).
- Why immutable: once confirmed, the cause is a settled fact; a changed cause is a new diagnosis.
- Usage: the target of diagnosis and the basis of explanation.

**Business impact**
- Meaning: what an issue threatens — revenue, trust, or a relationship — in business terms (Glossary).
- Why immutable: it states the consequence at a point in time; a changed consequence is a new assessment.
- Usage: the basis of priority.

**Priority**
- Meaning: the order in which issues are addressed, by business risk (Glossary).
- Why immutable: it is the decided order at a moment; a re-order is a new priority.
- Usage: tells the customer what to do first.

**Alignment**
- Meaning: the consistency between who authenticates a message and who the domain says is sending (Glossary).
- Why immutable: it is a factual relation at an observation; it changes only with the observed state.
- Usage: Analyzer-class derivation (DIAG-001) from DMARC Aggregate Evidence when INT-003–006 facts exist. Not an Issue. Not persisted: no Alignment table or field list is authorized. Not inferred from Observation Evidence DNS TXT. Not Health Signal and not Trust Score. Diagnosis does not persist ingest. HEALTH-001 does **not** persist Alignment and does **not** treat DIAG-001 as a stored input; H2 alignment facts are cited from DMARC Aggregate Evidence.

**Readiness**
- Meaning: the assessment of a domain against the requirements of the mailbox providers (Glossary).
- Why immutable: it is a verdict against a standard at a moment; a new assessment is a new Readiness.
- Usage: what the customer shows to an auditor.

**Alert**
- Meaning: a notification of a change or failure in business language (Glossary).
- Why immutable: an alert states an event; it is not edited after it is sent.
- Usage: what draws the customer's attention.

**Consent**
- Meaning: explicit, informed agreement before any action on live infrastructure (Glossary).
- Why immutable: a grant is a fact; it is given or withdrawn, not edited.
- Usage: the gate for any action (INV-02).
- Fields (AUTH-005 CTO Decision): `id`, `organization_id`, `domain_id` (the target), `granted_by_user_id` (the authorizing user), `action_type`, `status` (`GRANTED`, `REVOKED`), `granted_at`, `revoked_at`.
- Action Types: V1 supports `OBSERVE_DOMAIN_INFRASTRUCTURE` representing authorization to observe the customer's delivery infrastructure (DNS, SMTP probing).

**Role**
- Meaning: a set of what a user may see and do within an organization (Glossary).
- Why immutable: a role is a defined right; a changed right is a new role.
- Usage: the bound of a user's access, conceptually.

**Fix verification**
- Meaning: the confirmation that a resolution held and the issue did not return (Glossary).
- Why immutable: it is a verdict on a fix at a moment; a recurrence is a new event.
- Usage: the close of an Issue.

**Business explanation**
- Meaning: the translation of a diagnosis into the language of the business (Glossary).
- Why immutable: it is a produced account; a better account is a new explanation.
- Usage: what the customer reads and acts on.

**Reverse DNS Observation**
- Meaning: the immutable raw reverse-DNS snapshot for an IP of an observed MX host (Glossary).
- Why immutable: it is a fact actually returned; a later read is a new snapshot, never an overwrite.
- Usage: later interpretation may consume it; it is not Health Signal and not Observation Evidence.

**SMTP Greeting Observation**
- Meaning: the immutable raw SMTP greeting/banner and EHLO snapshot for a resolved IP of an observed MX host (Glossary).
- Why immutable: it is a fact actually returned; a later read is a new snapshot, never an overwrite.
- Usage: later interpretation may consume it; it is not Health Signal, not Observation Evidence, and not Reverse DNS Observation.

**DMARC Report Receipt**
- Meaning: the received inbound DMARC aggregate report — raw payload as received (Glossary).
- Why immutable: it is the bytes actually received; a later receipt is a new receipt.
- Usage: lineage and re-parse. Not Observation Evidence. Not Alignment. Conceptual persistence only; no physical table name.

**Parsed DMARC Report**
- Meaning: raw facts extracted from a valid RFC 7489 aggregate XML report (Glossary).
- Why immutable: it records what was present; absent fields stay absent.
- Usage: input to aggregation. Not Alignment. Not Observation Evidence. `report_id` is required to accept as parsed.

**DMARC Aggregate Evidence**
- Meaning: the append-only aggregated RUA evidence snapshot for a registered Domain (Glossary).
- Why immutable: a later ingest is a new snapshot; duplicate report_id for the same Domain is not a second snapshot.
- Usage: DIAG-001 input and HEALTH-001 H2 alignment facts (INV-11 lineage). Authoritative persisted source for HEALTH-001 alignment **and** the only HEALTH-001 indication voter (`spf_result`, `dkim_result`, `spf_aligned`, `dkim_aligned`, `disposition` as stored by INT-004). Not DIAG-001 Alignment VO, not an Alignment table, not Observation Evidence. Physical table Flyway V15 `dmarc_aggregate_evidence`.

## 6. Aggregate Relationships

The ownership between the wholes, in business terms.

- **Organization owns Domain and User.** An Organization owns the domains it protects and the users who act for it. A Domain belongs to exactly one Organization. User → Organization is a single, mandatory relationship after signup (one User to exactly one Organization); both are persisted together as one consistent unit.
- **Agency owns Client, which owns Domain.** An Agency (an Organization) owns the Clients it serves; each Client owns its own Domains; together they form the Portfolio. A Client's domain is owned by the Client, and the relationship by the Agency.
- **Domain (Domain Health) owns its Health Signal and Health Trend; Issues remain Diagnosis.** Delivery Observation owns Health Signal snapshots (HEALTH-001). Diagnosis owns Issue and Root cause (DIAG-004). HEALTH-001 must not persist Issues. Domain and Organization identify and isolate Observation Evidence, Reverse DNS Observation, SMTP Greeting Observation, DNS Health Observation, Reputation Observation, Blacklist Observation, and DMARC ingest evidence (tenant and domain identity); they are not the bounded-context owner of those snapshots. Delivery Observation owns Observation Evidence, Reverse DNS Observation, SMTP Greeting Observation, DNS Health Observation, Reputation Observation, Blacklist Observation, DMARC Report Receipt, Parsed DMARC Report, and DMARC Aggregate Evidence.
- **Issue owns its diagnosis and resolution.** An Issue owns its Root cause, its Business impact, its Priority, its Guidance, and its Fix verification.
- **Reporting draws on Domain Health and Diagnosis.** A Report is produced for a Client from the health and the diagnosis of its domains; the Report belongs to the Client Relationship as Report / Proof Evidence. That association is not ownership of Observation Evidence.

The rule is single ownership: every entity is owned by exactly one aggregate, and no aggregate reaches into another to change what it owns.

## Aggregate Interaction Rules

- **Aggregates never modify other aggregates.** An aggregate changes its own state only. It cannot reach into another aggregate and alter its data; to influence another, it acts through a business interaction that the other aggregate owns.
- **Ownership never crosses aggregate boundaries.** An entity is owned by one aggregate and stays there. Crossing a boundary to own or change another's entity is a violation.
- **Coordination happens through business interactions, not shared ownership.** Aggregates cooperate by passing the business knowledge (value objects) that one produces and another consumes — a Health Signal passed to Diagnosis **when it can state healthy vs at-risk** (Issue/root-cause), a Priority to Guidance. Alignment (DIAG-001) is derived from DMARC Aggregate Evidence, not passed as Health Signal, and is not a HEALTH-001 stored input. They do not coordinate by sharing a piece of owned data. Diagnosis must not fabricate Health Signal and must not persist ingest.
- **Technology independence is preserved.** The interaction rules are principles of the business, stated without any mechanism. Whether the coordination is realized by a call, a message, or any other means is a later decision, not made here.

## 7. Business Invariants

The invariants the data model upholds. INV-01 through INV-09 are the permanent references established in the Domain Model; they are restated here because the data model must honor them. INV-10 onward are the data-model rules.

- **INV-01 — Truth.** Reported health and every reported state trace to a real observation; no fabricated state.
- **INV-02 — Consent.** No action on live infrastructure without explicit, informed consent.
- **INV-03 — Closure.** An Issue is resolved only when its fix is verified.
- **INV-04 — Grounding.** Every explanation, impact, and priority is grounded in the real diagnosis.
- **INV-05 — Single health.** A Domain has one current Health Signal at a time (latest append-only snapshot), never both healthy and at risk in the same observation. **unknown** is a valid indication when we cannot truthfully choose (INV-01).
- **INV-06 — Confidence and trust.** The business sells Confidence and protects the Trust of clients and customers.
- **INV-07 — Human accountability.** A human owns the outcome of any conclusion.
- **INV-08 — Identity.** The product is never a sender, a single-check tool, a reputation game, or a communication platform.
- **INV-09 — Isolation.** One organization's health and data never mix with another's.
- **INV-10 — Identity.** Every entity has one stable identity; no two entities share one.
- **INV-11 — Lineage.** Every Assessment, Health Signal, and Explanation traces to the actual evidence source. HEALTH-001 persisted Health Signal must cite: organizationId, domainId, domainName; observationEvidenceId (DNS/SPF facts); dmarcAggregateEvidence ids actually used (alignment identities — INT-003–006); reputationObservation ids actually used; observedAt of the Health Signal. Must **not** cite Observation Evidence as the source of alignment identities. Must **not** cite an Alignment table (none). Must **not** cite DIAG-001 memory. Alignment (when DIAG-001 exists) lineages to DMARC ingest evidence, not to OE. A conclusion without lineage to its actual snapshot/source does not exist. This wording invents no physical table schema.
- **INV-12 — Immutability.** A value object never changes; a changed value is a new value. Observation Evidence, Reverse DNS Observation, SMTP Greeting Observation, DNS Health Observation, Reputation Observation, Blacklist Observation, DMARC Report Receipt, DMARC Aggregate Evidence, and Health Signal snapshots are append-only: existing snapshots are never overwritten. Re-ingest of the same report_id for the same Domain is idempotent (no second snapshot). HEALTH-001 persists nothing new when all cited source snapshot ids equal the latest Health Signal.
- **INV-13 — Ownership.** Every entity is owned by exactly one aggregate root; there is no shared-ownership entity.
- **INV-14 — No orphans.** Every reference points to an existing, owned entity; nothing references what is not there.

## 8. Evidence Lineage

The chain by which the product's data becomes a customer's decision, and the business meaning of each step.

Lineage: Infrastructure → Observation → Observation Evidence / Reverse DNS Observation / SMTP Greeting Observation / DNS Health Observation / Reputation Observation / Blacklist Observation; RUA ingest → DMARC Report Receipt → Parsed DMARC Report → DMARC Aggregate Evidence → later interpretation / diagnosis / health → Reporting / customer-facing proof.

- **Observation → Observation Evidence.** The reading of the delivery infrastructure produces Observation Evidence — an immutable, persisted snapshot of facts actually returned. It is append-only: a later observation (including re-observation) creates a new snapshot; stored observation facts are never overwritten. Observation Evidence is not a physical schema here. Conceptually it is limited to domain identity, organization identity, when it was observed, the observed domain name where required, and the MX, TXT, and SPF actually observed. This DNS shape is frozen: PTR / reverse DNS is not added to Observation Evidence. SMTP greeting / EHLO is not added to Observation Evidence. DKIM, DMARC, and alignment are not added to Observation Evidence and must not be inferred from Phase 08 DNS TXT. It does not persist inferred DKIM or SPF validity, domain health, a deliverability score, risk, diagnosis, compliance, AI interpretation, or recommendations.
- **Observation → Reverse DNS Observation.** A real reverse-DNS read of IPs from a registered domain's observed MX hosts produces Reverse DNS Observation — an immutable, persisted snapshot of the raw reverse-DNS fact. It is not a physical schema here. Conceptually it is limited to identity from the registered Domain, the source MX hostname, the IP address (IPv4 and/or IPv6 when those addresses actually exist), the PTR values as observed, and when it was observed. The read is MX hostname → A and/or AAAA → PTR. It is append-only. It does not persist FCrDNS, pass/fail, health, diagnosis, score, deliverability, or Trust Score. HEALTH-001 may consume it later; this snapshot does not create Health Signal. A future persistence migration will be required; this document does not define tables or columns.
- **Observation → SMTP Greeting Observation.** A real SMTP read of IPs from a registered domain's observed MX hosts in Observation Evidence produces SMTP Greeting Observation — an immutable, persisted snapshot of the raw greeting/banner and EHLO fact. It is not a physical schema here. Conceptually it is limited to organization identity, domain identity, domain name, MX hostname, resolved IP, address family, when it was observed, connection/probe outcome, raw SMTP greeting if actually returned, and raw EHLO response/capabilities if actually observed. The read is MX hostname from Observation Evidence → A and/or AAAA → SMTP service. It does not probe arbitrary IPs, unrelated hosts, or third-party infrastructure. The SMTP connection is an active probe; it does not send mail, authenticate, or test relay or delivery. It is append-only. If the probe is unavailable (timeout, connection refusal, or DNS failure), nothing is persisted for that probe (INV-01), matching Reputation Observation. Do not manufacture a greeting or EHLO. Connection/probe outcome records unavailability without fabricated greeting or EHLO text. It does not persist valid/invalid, healthy, deliverability score, open relay finding, security diagnosis, compliance, or Health Signal. HEALTH-001 may consume it later; this snapshot does not create Health Signal. Flyway V13 persists `smtp_greeting_observation` (TEXT greeting/EHLO, VARCHAR probe_outcome, no UNIQUE on domain_id). Observation Evidence (including V8) and Reverse DNS Observation are not modified.
- **Observation → Reputation Observation.** A real Google Postmaster or Microsoft SNDS read produces Reputation Observation — an immutable, persisted snapshot of the raw provider fact. Flyway V9 `reputation_observation`: source (`POSTMASTER` | `SNDS`), observed_at, opaque payload TEXT. If a provider is unavailable or returns nothing, there is no fact to persist. It does not persist scores, grades, health, risk, diagnosis, findings, recommendations, or compliance. HEALTH-001 requires at least one row as lineage presence; payload is not an indication voter.
- **Observation → Blacklist Observation.** A real Spamhaus, Barracuda, SURBL, or URIBL read produces Blacklist Observation — an immutable, persisted snapshot of the raw provider fact. Conceptually it is limited to domain identity, organization identity, domain name, source identity (SPAMHAUS, BARRACUDA, SURBL, or URIBL), when it was observed, and the opaque raw payload (TEXT in Flyway V11 `blacklist_observation`). Each provider is queried independently. If a provider is unavailable or returns nothing, there is no fact to persist for that provider. It is append-only (no UNIQUE on domain_id). It does not persist listed/not-listed conclusions, scores, grades, health, risk, diagnosis, findings, recommendations, or compliance. HEALTH-001 may consume it later; this snapshot does not create Health Signal. INF-004 is unchanged.
- **Observation → DNS Health Observation.** A real DNS-health read of a registered Domain produces DNS Health Observation — an immutable, persisted snapshot of raw TTL, DNSSEC-related presence, and named-resolver answers. Conceptually it is limited to organization identity, domain identity, domain name, when it was observed, TTL observations, DNSSEC observations, and propagation observations (TEXT JSON in Flyway V10 `dns_health_observation`). It is append-only (no UNIQUE on domain_id). It does not persist health, score, validity, diagnosis, Trust Score, cryptographic DNSSEC validation, or a propagation percentage. HEALTH-001 may consume it later; this snapshot does not create Health Signal. INF-004 is unchanged. Observation Evidence (including V8) is not modified.
- **Observation Evidence → later interpretation.** Later interpretation, diagnosis, and Health Signal consume Observation Evidence and may later consume Reverse DNS Observation, SMTP Greeting Observation, and DNS Health Observation. They do not rewrite those snapshots. HEALTH-001 V1 **required** sources are Observation Evidence, DMARC Aggregate Evidence, and Reputation Observation. HEALTH-001 **must not wait** on Blacklist / Reverse DNS / SMTP Greeting / DNS Health Observation and must not invent from them. Finding and Assessment, where they appear later in interpretation, are not those snapshots. INF-005, INF-006, and INF-007 do not create Health Signal. DIAG-001 must not infer DKIM, DMARC, or alignment from Observation Evidence; those fields are not added to OE. Real Alignment traces to DMARC Aggregate Evidence / Parsed DMARC Report (INT-003–006), not to a fabricated OE field. Alignment is a derived value object; no Alignment persistence or table is authorized. Diagnosis does not persist ingest. HEALTH-001 does not persist Alignment.
- **Observation → Health Signal (HEALTH-001 interpretation freeze).** Explicit in-process derivation produces an append-only Health Signal snapshot (Flyway V16 `health_signal`) when **all** V1 required sources exist for the Domain (latest OE V8, all V15 rows of the latest aggregate `report_id`, at least one V9 Reputation Observation). If any required source is missing, **persist nothing**. Indication: **at-risk** if any cited aggregate row has stored `disposition` quarantine/reject or auth/alignment `fail`; **healthy** only if every cited row has non-null `disposition=none` and SPF+DKIM result+alignment all `pass`; else **unknown** (snapshots exist). OE MX/TXT/SPF and Reputation `payload` do not vote. No reason column. Same cited ids as latest ⇒ persist nothing new. Not Trust Score. Not Issues. Not scheduled. No public HTTP. Current Java always-unknown is insufficient vs this freeze; this document does not implement Java.
- **RUA ingest → DMARC Report Receipt / Parsed DMARC Report / DMARC Aggregate Evidence.** Delivery Observation conceptually persists the received raw aggregate payload as DMARC Report Receipt (registered Domain identity, received-at, raw bytes) and, when parse succeeds, Parsed DMARC Report facts and an append-only DMARC Aggregate Evidence snapshot. These are observation-adjacent snapshots, not Observation Evidence and not an Alignment table. Unknown RUA uuid is discarded (no persist). Missing report_id: persist Receipt if already received for a known Domain; do not produce Aggregate Evidence. Duplicate report_id for the same Domain is idempotent. Day key is UTC date of date_range.begin; sender key is source_ip; domain key is the registered Domain. This freeze invents no physical table names or columns. A future persistence migration is authorized in principle when implementation starts; do not name a Flyway version here. Observation Evidence (including V8) is not modified. HEALTH-001 cites Aggregate Evidence for H2; INT-006 does not trigger HEALTH-001.
- **Infrastructure Map (MAP-001).** A topology read-model of Domain → DNS → SPF → DKIM → DMARC → Senders → Inbox for one registered Domain. It is not an aggregate, not a table, not a Flyway migration, and not a graph store. It projects existing Observation Evidence (DNS/SPF) and leaves DKIM, DMARC, Senders, and Inbox unobserved until those facts exist. It is not Health Signal and not MAP-002–004. INF-001–008 schemas are unchanged. MAP-001 remains topology/read-model only.
- **MAP-002 freeze (Color-coded view).** MAP-002 G/Y/R represents HEALTH, not observation availability. MAP-001 OBSERVED/NOT_OBSERVED/UNAVAILABLE must never map directly to G/Y/R. Raw INF-001–008 observations must never be converted into MAP-002 colors. Trust Score is not a color source. HEALTH-001 (Phase 10) is the authoritative source of health state. MAP-002 **consumes** Health Signal; it does not calculate or create it. HEALTH-001 does **not** freeze G/Y/R. MAP-002 remains blocked on HEALTH-001 producing the canonical signal **and** on a later MAP-002 freeze of how unknown/healthy/at-risk maps to colors. An **unknown** envelope is not a color source. No Health Signal implementation in Phase 08. This freeze: no table, Flyway, backend health-calculation, HTTP API, or frontend for MAP-002. MAP-001 unchanged. Do not invent per-node health semantics, thresholds, scores, or missing-node behavior.
- **MAP-003 freeze (Node detail panel).** MAP-003 is opened by selecting or clicking a MAP-001 node (WF-5 click-to-detail). The panel may show node identity, type, observation availability/status, and real raw observation facts or evidence references when those facts exist. Contents are raw node metadata and observations, not independent health computation. MAP-003 must not calculate, create, infer, or own Health Signal. Health meaning and G/Y/R are owned by MAP-002 consuming HEALTH-001. OBSERVED/NOT_OBSERVED/UNAVAILABLE must never be translated into health, severity, warning, critical, or color. No Trust Score, deprecated scoring, diagnosis, recommendation, compliance, or fabricated explanation. NOT_OBSERVED nodes must not receive fabricated details. DKIM, DMARC, Senders, and Inbox currently have no observed facts and must not display invented details. Evidence references must not automatically become arbitrary raw-payload dumps; a detailed evidence-access/API contract is frozen separately before implementation. Must not modify INF-001–008, MAP-001, MAP-002, V1–V11, or HEALTH-001. This freeze: no table, Flyway, scheduler, independent backend health service, HTTP API, or frontend. MAP-003 remains ⬜. Depends on MAP-001 topology plus existing raw observations when they exist. Do not invent per-node health.
- **MAP-004 freeze (PNG/SVG export).** MAP-004 is export of the Infrastructure Map visualization. PNG and SVG are both frozen output formats. It is not Phase 16 proof and does not replace EVID-003. It is not Phase 15 PDF/email reporting and must not merge into FUN-R2. Keep it separate from Phase 15/16 reporting and proof. Exported structure is based on MAP-001 topology. If health colors eventually appear in the export, they come from MAP-002 consuming real HEALTH-001 Health Signal. Never derive G/Y/R from raw INF-001–008 observations, OBSERVED status, Trust Score, or deprecated scoring. NOT_OBSERVED nodes must not receive fabricated health, facts, or details. MAP-004 must not calculate or create Health Signal. No new table, Flyway, scheduler, graph database, or persistence model. Do not invent an HTTP/API contract. Do not select or add a rendering/export library in this freeze. No frontend or backend export code. Must not modify INF-001–008, MAP-001, MAP-002, MAP-003, V1–V11, or HEALTH-001. MAP-004 remains ⬜. Depends on MAP-001 topology; any later color depends on MAP-002 consuming HEALTH-001.
- **Health Signal → AI Explanation.** The Health Signal is translated into the language of the business as an AI Explanation.
- **AI Explanation → Recommendation.** From the explanation, a prioritized Recommendation is made, with a reason and a confidence.
- **Recommendation → Customer Decision.** The customer decides, and a human owns the outcome of that decision.
- **Reporting / Proof Evidence.** Customer-facing Report / Proof Evidence is produced later from grounded health. Client Relationship may associate that proof with the client; Reporting and Evidence owns it. Neither owns or mutates Observation Evidence.

Each step is a transformation that keeps its source. A customer can therefore walk a decision back to the actual snapshot or source it came from (Observation Evidence for DNS/SPF; DMARC Aggregate Evidence / Parsed DMARC Report for alignment identities). That lineage is what lets the customer trust, and what lets the AI stay honest.

This section is semantic only. It does not introduce a new Evidence aggregate, and it does not define tables or columns.

## 9. Model Evolution

- **Introducing a new aggregate, entity, or value object.** A new business object is introduced only when the business needs it, under the Golden Rule, and when it traces to a concept in the domain model and a context. An object that cannot trace is not introduced.
- **Preventing duplication.** The single-owner rule and the lineage rule prevent duplication: a value or entity that repeats information already owned by another aggregate is a defect, and a new object that would duplicate an existing one is rejected.
- **Preserving the invariants.** A change to the model must keep every invariant true. A change that would break an invariant is not made; the invariants are the floor.
- **Growing slowly.** The model is built from the same boundaries as the domain and the contexts; any change to it goes through the same governance as the domain model.

The data model must not become sprawl. Every object is real, owned, and traced to a business reason.

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

### Feeds into

As a predecessor for the future documents `08_API_PHILOSOPHY.md`, `09_SECURITY_ARCHITECTURE.md`, and `10_DEPLOYMENT_ARCHITECTURE.md`.

## 11. Governance

- **Status.** Draft until the founders review. Only the founders freeze this document after approval.
- **Changes to the model.** Changing an aggregate, an entity, a value object, or an invariant is a written proposal reviewed by the founders or the chief architect.
- **Traceability.** Every business object traces through a context to a concept and a problem; an orphan is removed.
- **Consistency.** The model is subordinate to the Constitution and consistent with the Glossary and the domain model.
- **Not a technology decision.** The database, the storage engine, the ORM, the serialization, and the APIs are decided elsewhere, not here.

---

*This document models the business data of MailSent — its aggregates, its entities, its value objects, and the invariants that hold them true. It proposes no database and no storage; it states the shape of the business's data, which the software will later realize.*