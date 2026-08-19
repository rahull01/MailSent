# Feature Specification: DIAG-003 — Root Cause Determination

## 1. Context & Business Value
MailSent is an AI Email Infrastructure Intelligence platform. In the **Diagnosis Bounded Context** (`com.mailsent.diagnosis`), **Root Cause Determination** (`DIAG-003` / `FUN-X3` / `INV-11`) analyzes `HealthSignal` instances whose unified indication is `AT_RISK` and traverses their cited `DmarcAggregateEvidence` (`V15`) lineage to establish deterministic, evidence-grounded root causes.

Without guessing or hallucinating (`N2`), DIAG-003 identifies the exact operational breakdown (e.g. policy reject enforcement, policy quarantine enforcement, alignment failures, or authentication failures).

---

## 2. Core Non-Negotiable Invariants

- **`N2` Never Fabricate Truth**: Root cause determination only executes on `HealthSignal` instances with `indication == HealthIndication.AT_RISK`. For non-at-risk signals (`HEALTHY` or `UNKNOWN`), it returns `Optional.empty()`. It never invents root causes when evidence is absent or compliant.
- **`INV-09` Multi-Tenant Isolation**: Operates exclusively over the evidence cited in the domain's own `HealthSignal` lineage.
- **`INV-11` Deterministic Lineage**: Every determined `RootCause` maps directly to verified tokens on cited `DmarcAggregateEvidence` snapshots.
- **Pure In-Memory Analyzer**: Root cause determination is an application service (`RootCauseDeterminationService`); it does not persist data to a database table and requires no Flyway migrations (`V19+`).
- **Separation of Concerns**: `HEALTH-001` (Delivery Observation) owns Health Signal unification. `DIAG-003` (Diagnosis) owns root cause attribution.

---

## 3. Root Cause Taxonomy

| Root Cause Enum (`RootCause`) | Description | Trigger Condition on Cited Evidence |
| :--- | :--- | :--- |
| `DMARC_REJECT` | Policy evaluated disposition is reject | `disposition == "reject"` (case-insensitive) |
| `DMARC_QUARANTINE` | Policy evaluated disposition is quarantine | `disposition == "quarantine"` (case-insensitive) |
| `SPF_ALIGNMENT_FAILURE` | SPF identifier alignment failed | `spfAligned == "fail"` (case-insensitive) |
| `DKIM_ALIGNMENT_FAILURE` | DKIM identifier alignment failed | `dkimAligned == "fail"` (case-insensitive) |
| `SPF_AUTHENTICATION_FAILURE` | SPF mechanism evaluation failed | `spfResult == "fail"` (case-insensitive) |
| `DKIM_AUTHENTICATION_FAILURE` | DKIM cryptographic signature verification failed | `dkimResult == "fail"` (case-insensitive) |
| `UNKNOWN_ISSUE` | Signal is at-risk, but specific failure token is unclassified | At-risk signal where specific tokens are unrecognized |

---

## 4. Deterministic Precedence Rules
When cited `DmarcAggregateEvidence` rows contain multiple failure conditions, evaluation follows strict deterministic precedence:
1. **Policy Disposition Enforcements**:
   - `DMARC_REJECT`
   - `DMARC_QUARANTINE`
2. **Identifier Alignment Failures**:
   - `SPF_ALIGNMENT_FAILURE`
   - `DKIM_ALIGNMENT_FAILURE`
3. **Authentication Mechanism Failures**:
   - `SPF_AUTHENTICATION_FAILURE`
   - `DKIM_AUTHENTICATION_FAILURE`
4. **Fallback**:
   - `UNKNOWN_ISSUE` (if `AT_RISK` but no specific token matched)
   - `Optional.empty()` (if `HealthSignal` is `HEALTHY` or `UNKNOWN`)

---

## 5. Domain & Application Models

```java
package com.mailsent.diagnosis.domain;

public enum RootCause {
    DMARC_REJECT,
    DMARC_QUARANTINE,
    SPF_ALIGNMENT_FAILURE,
    DKIM_ALIGNMENT_FAILURE,
    SPF_AUTHENTICATION_FAILURE,
    DKIM_AUTHENTICATION_FAILURE,
    UNKNOWN_ISSUE
}
```

```java
package com.mailsent.diagnosis.application;

public class RootCauseDeterminationService {
    public Optional<RootCause> determineRootCause(HealthSignal healthSignal);
}
```
