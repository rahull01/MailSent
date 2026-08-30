# Feature Specification: EXPL-003 — Simpler Toggle (Business vs Technical View)

## 1. Context & Business Value
MailSent is an AI Email Infrastructure Intelligence Platform. In the **Business Explanation Bounded Context** (`com.mailsent.explanation`), **EXPL-003** (`FUN-E4`) allows non-technical business stakeholders (founders, domain owners, marketers) and technical operators (engineers, IT administrators, deliverability specialists) to view the identical underlying delivery health and diagnostic state through tailored levels of detail:

1. **`BUSINESS_SUMMARY`**: High-level, plain-language business impact focusing on *what happened* and *why it matters* (threat to revenue, trust, customer communication) without exposing raw DNS / RFC jargon.
2. **`TECHNICAL_DEEP_DIVE`**: Detailed diagnostic and mechanism view explaining the exact technical breakdown (specific DNS records, SPF/DKIM authentication mechanisms, DMARC alignment errors, sender mismatch) for operators taking direct technical action.

---

## 2. Core Invariants & Architectural Rules

- **`N2` Never Fabricate Truth**: Both view modes project from the **identical underlying evidence facts**. The toggle changes presentation abstraction, never the underlying truth.
- **`INV-09` Multi-Tenant Isolation**: Querying explanations and view modes is strictly validated against the domain's owning `organization_id`. Mismatched requests reject with `IllegalArgumentException`.
- **`INV-11` Deterministic Lineage**: Every explanation view retains an immutable `Grounding` value object citing the exact source `HealthSignal` ID, `ObservationEvidence` ID, `DmarcAggregateEvidence` IDs, and `ReputationObservation` IDs.
- **Dual Origin Support**:
  - Explanations can be derived from a **`HealthSignal`** snapshot (overall domain health state).
  - Explanations can be derived from an **`Issue`** aggregate root (specific diagnosed root cause).
- **Zero AI-Washing / Zero Hallucination**: Rule-based, deterministic explanation templates anchored to validated evidence facts.
- **Zero Persistence Alteration**: Explanations remain value projections calculated on-the-fly. Zero database migrations.

---

## 3. Domain Model: ExplanationMode & ExplanationView

### Package: `com.mailsent.explanation.domain`

```java
package com.mailsent.explanation.domain;

public enum ExplanationMode {
    BUSINESS_SUMMARY,
    TECHNICAL_DEEP_DIVE
}
```

```java
package com.mailsent.explanation.domain;

import java.util.Objects;

public final class ExplanationView {

    private final String explanationId;
    private final String organizationId;
    private final String domainId;
    private final String domainName;
    private final ExplanationMode mode;
    private final String headline;
    private final String summary;
    private final String details;
    private final Grounding grounding;

    // Factory methods for rendering BUSINESS_SUMMARY and TECHNICAL_DEEP_DIVE
}
```

---

## 4. Mode Rendering Matrix

| Source State | `BUSINESS_SUMMARY` Mode | `TECHNICAL_DEEP_DIVE` Mode |
| :--- | :--- | :--- |
| **`HEALTHY`** | **Headline**: "Email delivery is protected."<br>**Summary**: "Your emails are fully authenticated and trusted by mailbox providers." | **Headline**: "SPF/DKIM Authenticated & DMARC Aligned."<br>**Summary**: "All required DNS records are published and passing alignment."<br>**Details**: "DMARC policy active; DKIM/SPF passing RFC 7489 alignment." |
| **`AT_RISK`** | **Headline**: "Email delivery is at risk."<br>**Summary**: "Emails sent from this domain may be quarantined or rejected." | **Headline**: "Authentication / Alignment Failure Detected."<br>**Summary**: "One or more DNS records are misconfigured or failing alignment."<br>**Details**: "SPF/DKIM failure or DMARC quarantine/reject policy triggered." |
| **`UNKNOWN`** | **Headline**: "Awaiting observation data."<br>**Summary**: "Insufficient recent data to determine email delivery state." | **Headline**: "No Recent Evidence Snapshots."<br>**Summary**: "Missing DMARC aggregate reports or observation evidence."<br>**Details**: "Awaiting fresh DNS observation or aggregate feedback." |

---

## 5. Application Service Boundary

### Package: `com.mailsent.explanation.application`

```java
@Service
public class ExplanationService {

    private final HealthSignalService healthSignalService;
    private final DomainRepository domainRepository;

    public Optional<Explanation> explainLatest(String domainId); // Backward compatible
    public Optional<ExplanationView> explainLatestWithMode(String organizationId, String domainId, ExplanationMode mode);
    public ExplanationView renderView(Explanation explanation, ExplanationMode mode);
}
```
