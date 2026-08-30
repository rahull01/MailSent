# Feature Specification: DIAG-004 — Issue Entity Lifecycle (Hardened Persistence Contract)

## 1. Context & Business Value
In the **Diagnosis Bounded Context** (`com.mailsent.diagnosis`), **Issue Entity Lifecycle** (`DIAG-004` / `DATA Issue`) defines the persisted business aggregate representing a diagnosed delivery failure incident on a registered domain.

An **`Issue`** represents a concrete, actionable delivery problem that has been detected on a domain with an evidence-grounded `RootCause` (`DIAG-003`) and optional recognized `Sender` (`DIAG-002`), triggered exclusively by an `AT_RISK` `HealthSignal` (`HEALTH-001`).

---

## 2. Core Non-Negotiable Invariants

- **`N2` Never Fabricate Truth**: An `Issue` is created ONLY when a real `HealthSignal` on the domain is evaluated as `AT_RISK` and a concrete `RootCause` is identified. `HEALTHY` and `UNKNOWN` health states NEVER create new issues.
- **`INV-09` Multi-Tenant Isolation**: Every `Issue` is strictly partitioned by `organization_id` and `domain_id`. Database foreign keys enforce that `organization_id` references `organizations(id)` and `domain_id` references `domains(id)`. Cross-tenant querying, modification, or exposure is strictly prohibited.
- **`INV-11` Deterministic Lineage**: Every persisted `Issue` references its immutable creation lineage: `origin_health_signal_id`, `root_cause`, optional `provider_id`/`provider_name`, and JSON array of initial cited `origin_dmarc_aggregate_evidence_ids`. On repeat observations, `latest_health_signal_id` and `latest_dmarc_aggregate_evidence_ids` track the ongoing state without mutating origin lineage.
- **`INV-14` Referential Integrity**: Database foreign key constraints guarantee that orphan issues cannot exist and issues cannot reference cross-tenant or unowned domains.
- **Persistence Guarantee**: Issues persist across application restarts in PostgreSQL table `issues` (Flyway `V19`).

---

## 3. Issue Lifecycle State Machine & Semantics

An `Issue` aggregate transitions through explicit, well-defined lifecycle states:

```
                  ┌──────────────┐
                  │              │
                  │  DISCOVERED  │ ◄─────────────┐
                  │              │               │
                  └──────┬───────┘               │
                         │                       │
                         ▼                       │
                  ┌──────────────┐               │
                  │              │               │
                  │  DIAGNOSED   │               │ Reopen (on recurring
                  │              │               │ AT_RISK signal)
                  └──────┬───────┘               │
                         │                       │
                         ▼                       │
                  ┌──────────────┐               │
                  │              │               │
                  │   RESOLVED   │ ──────────────┘
                  │              │
                  └──────┬───────┘
                         │
                         ▼
                  ┌──────────────┐
                  │              │
                  │    CLOSED    │
                  │              │
                  └──────────────┘
```

### Exact State Semantics:
1. **`DISCOVERED` (Active Issue)**:
   - Initial state created when an `AT_RISK` health signal occurs with a diagnosed `RootCause`.
   - Initial timestamps: `discovered_at = NOW()`, `updated_at = NOW()`.
   - Meaning: Problem is detected by the platform but awaiting full diagnostic analysis/triage.
2. **`DIAGNOSED` (Active Issue)**:
   - Full diagnostic facts, sender attribution, and root cause have been attached and verified.
   - Transition: `DISCOVERED` $\rightarrow$ `DIAGNOSED`.
   - Meaning: Ready for user remediation or AI guidance.
3. **`RESOLVED` (Awaiting Verification)**:
   - Remediation fix has been applied by the operator or system (e.g. DNS record updated), but a subsequent healthy observation has NOT yet confirmed it on live infrastructure.
   - Sets `resolved_at = NOW()`.
   - Transition: `DISCOVERED` or `DIAGNOSED` $\rightarrow$ `RESOLVED`.
   - Meaning: Remediated by user, awaiting verification. **Still occupies active unique index** so repeated failure signals during verification will reopen the issue rather than creating duplicate issues.
4. **`CLOSED` (Terminal Historical Record)**:
   - Verified closure when a subsequent `HealthSignal` for the domain evaluates to `HEALTHY`.
   - Sets `closed_at = NOW()`.
   - Transition: `RESOLVED` (or active states upon verified healthy signal) $\rightarrow$ `CLOSED`.
   - Meaning: Verified as resolved. Terminal historical record. **Leaves active unique index**.
5. **Reopen vs New Incident Decision (`REOPEN` / `NEW INCIDENT`)**:
   - **`RESOLVED` + Same Failure Signal**: Transitions back to `DISCOVERED` (`resolved_at = null`, `updated_at = NOW()`). (Verification failed).
   - **`CLOSED` + Same Failure Signal**: Spawns a **NEW** `Issue` aggregate representing a separate historical incident with its own fresh `id` and `discovered_at`. This preserves clean historical records and auditability.

---

## 4. Immutable vs Mutable Field Split

| Category | Fields | Mutability Rule |
| :--- | :--- | :--- |
| **Immutable Snapshot (Origin Lineage)** | `id`, `organization_id`, `domain_id`, `domain_name`, `root_cause`, `provider_id`, `provider_name`, `origin_health_signal_id`, `origin_dmarc_aggregate_evidence_ids`, `discovered_at` | Set strictly at discovery; **NEVER mutated**. |
| **Mutable Lifecycle State** | `status`, `latest_health_signal_id`, `latest_dmarc_aggregate_evidence_ids`, `resolved_at`, `closed_at`, `updated_at` | Mutated only via explicit aggregate business methods. |

---

## 5. Idempotency Fingerprint & Concurrency Strategy

- **Active Issue Fingerprint**:
  An active issue is uniquely identified by the tuple:
  `(organization_id, domain_id, root_cause, COALESCE(provider_id, ''))` where `status IN ('DISCOVERED', 'DIAGNOSED', 'RESOLVED')`.
- **Repeated `AT_RISK` Observations**:
  If a new `AT_RISK` signal arrives for an active issue with matching fingerprint:
  - **No duplicate issue is created**.
  - The existing active `Issue` updates `latest_health_signal_id = newSignal.id()`, `latest_dmarc_aggregate_evidence_ids = newEvidenceIds`, and `updated_at = NOW()`.
- **Concurrency & Race Condition Safety**:
  - Encapsulated in `@Transactional(isolation = READ_COMMITTED)`.
  - Backed by PostgreSQL partial unique index `uk_issues_active_fingerprint`.
  - Concurrent inserts of identical failure signals catch `DataIntegrityViolationException` and load the existing active issue.

---

## 6. Evidence Storage & Lineage Model
- `origin_dmarc_aggregate_evidence_ids` and `latest_dmarc_aggregate_evidence_ids` are stored as **PostgreSQL `TEXT` containing serialized JSON arrays of UUID strings** (matching the established MailSent architectural pattern in `V8__create_observation_evidence.sql` and `V16__create_health_signal.sql`).
- Format: `["ev-1", "ev-2"]`.
- Validation: Sorted, de-duplicated, non-empty list of valid evidence identifiers.

---

## 7. Auditability & Organization State Changes
- **Lifecycle Auditability**: The aggregate's explicit timestamps (`discovered_at`, `resolved_at`, `closed_at`, `updated_at`) and origin/latest lineage IDs fully satisfy the V1 requirements.
- **Domain / Organization Deletion / Status Change**:
  - Foreign keys `fk_issues_organization` and `fk_issues_domain` enforce strict referential integrity.
  - If a domain or organization is closed (`status = CLOSED`), issues remain persisted as immutable historical audit records.

---

## 8. Proposed Flyway V19 Schema Specification

```sql
-- =============================================================================
-- MailSent — Flyway migration V19: issues
-- DIAG-004 (Issue Entity Lifecycle) production schema.
-- =============================================================================

CREATE TABLE IF NOT EXISTS issues (
    id                                      VARCHAR(36)  NOT NULL,
    organization_id                         VARCHAR(36)  NOT NULL,
    domain_id                               VARCHAR(36)  NOT NULL,
    domain_name                             VARCHAR(253) NOT NULL,
    root_cause                              VARCHAR(64)  NOT NULL,
    provider_id                             VARCHAR(64),
    provider_name                           VARCHAR(128),
    status                                  VARCHAR(32)  NOT NULL,
    origin_health_signal_id                 VARCHAR(36)  NOT NULL,
    origin_dmarc_aggregate_evidence_ids     TEXT         NOT NULL,
    latest_health_signal_id                 VARCHAR(36)  NOT NULL,
    latest_dmarc_aggregate_evidence_ids     TEXT         NOT NULL,
    discovered_at                           TIMESTAMP WITH TIME ZONE NOT NULL,
    resolved_at                             TIMESTAMP WITH TIME ZONE,
    closed_at                               TIMESTAMP WITH TIME ZONE,
    updated_at                              TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_issues PRIMARY KEY (id),
    CONSTRAINT fk_issues_organization
        FOREIGN KEY (organization_id) REFERENCES organizations (id),
    CONSTRAINT fk_issues_domain
        FOREIGN KEY (domain_id) REFERENCES domains (id)
);

CREATE INDEX idx_issues_organization_id ON issues (organization_id);
CREATE INDEX idx_issues_domain_id ON issues (domain_id);
CREATE INDEX idx_issues_status ON issues (organization_id, status);

CREATE UNIQUE INDEX uk_issues_active_fingerprint 
ON issues (organization_id, domain_id, root_cause, COALESCE(provider_id, '')) 
WHERE status IN ('DISCOVERED', 'DIAGNOSED', 'RESOLVED');
```
