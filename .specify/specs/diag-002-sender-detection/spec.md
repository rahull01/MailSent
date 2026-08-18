# Feature Specification: DIAG-002 — Sender Detection (Recognized Providers)

## 1. Context & Business Value
MailSent provides AI Email Infrastructure Intelligence. In the **Diagnosis Bounded Context** (`com.mailsent.diagnosis`), **Sender Detection** (`DIAG-002` / `FUN-X2`) identifies known third-party email sending service providers (e.g., Google Workspace, Microsoft 365, Salesforce, Mailchimp) operating on behalf of a registered domain based on factual evidence in DMARC aggregate reports (`DmarcAggregateEvidence` V15).

Sender detection enables downstream **Root Cause Determination** (`DIAG-003`) to pinpoint exactly which sender service is misaligned or failing authentication, without guessing.

---

## 2. Core Non-Negotiable Invariants

- **`N2` Never Fabricate Truth**: If the sending infrastructure does not match a verified pattern in the recognized provider catalog, the service MUST return `Optional.empty()` (unknown). It must NEVER guess or infer a provider based on arbitrary heuristics.
- **`INV-09` Multi-Tenant Isolation**: Sender detection operates exclusively within the evidence context of the caller's registered domain and organization.
- **`INV-11` Deterministic Lineage**: Sender detection derives exclusively from cited snapshot fields (`dkimDomains`, `envelopeFrom`).
- **Pure In-Memory Analyzer**: `Sender` detection does not persist data, does not create database tables, and does not require Flyway migrations (`V19+`).
- **No Health Signal / Scoring**: Sender detection does not calculate health states (`HEALTHY`/`AT_RISK`), trust scores, or risk grades.

---

## 3. Recognized Provider Catalog

| Provider ID (`providerId`) | Canonical Name (`providerName`) | DKIM Domain Suffixes | Envelope From Match Patterns |
| :--- | :--- | :--- | :--- |
| `google_workspace` | Google Workspace | `google.com` | Contains `google.com` |
| `microsoft_365` | Microsoft 365 | `outlook.com`, `onmicrosoft.com`, `microsoft.com` | Contains `outlook.com`, `onmicrosoft.com` |
| `salesforce` | Salesforce | `salesforce.com`, `sfdc.net` | Contains `salesforce.com`, `sfdc.net` |
| `mailchimp` | Mailchimp | `rsgsv.net`, `mcsv.net`, `mailchimp.com` | Contains `rsgsv.net`, `mcsv.net`, `mailchimp.com` |

---

## 4. Detection & Precedence Rules

1. **Null / Missing Evidence**: If `evidence == null` or contains empty/blank fields, returns `Optional.empty()`.
2. **Case-Insensitive Matching**: All DKIM domains and `envelope_from` strings are normalized to lowercase prior to pattern matching.
3. **Evaluation Precedence**:
   - **Primary Source (DKIM Domains)**: Evaluates `dkimDomains` first in iteration order. The first recognized DKIM suffix match determines the `Sender`.
   - **Secondary Source (Envelope From)**: If no DKIM domain matches a recognized provider, evaluates `envelopeFrom`.
   - **Fallback (Unknown)**: If neither DKIM nor Envelope From matches the catalog, returns `Optional.empty()`.

---

## 5. Domain Models

```java
public record Sender(String providerId, String providerName) {
    public Sender {
        if (providerId == null || providerId.isBlank()) {
            throw new IllegalArgumentException("providerId cannot be null or blank");
        }
        if (providerName == null || providerName.isBlank()) {
            throw new IllegalArgumentException("providerName cannot be null or blank");
        }
    }
}
```

---

## 6. Downstream & Inter-Context Contracts
- **DIAG-001 (Alignment)**: Operates in parallel on `DmarcAggregateEvidence` to derive alignment facts.
- **DIAG-003 (Root Cause Determination)**: Consumes detected `Sender` to build grounded `RootCause` diagnoses (e.g. `SALESFORCE_SPF_MISALIGNED`).
- **HEALTH-001**: Independent of sender detection; `HEALTH-001` unifies health indications across the domain without requiring sender identification.
