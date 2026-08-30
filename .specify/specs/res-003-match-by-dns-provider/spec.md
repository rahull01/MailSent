# Feature Specification: RES-003 — Match by DNS Provider (Tailored Guidance)

## 1. Context & Business Value
MailSent is an AI Email Infrastructure Intelligence Platform. In the **Guidance & Guided Resolution Bounded Context** (`com.mailsent.resolution`), **RES-003** (`FUN-G3`) ensures that step-by-step resolution guidance provided to customers matches their real DNS infrastructure and hosting environment (e.g. Cloudflare, AWS Route 53, GoDaddy, Google Cloud DNS, Namecheap, or Generic DNS).

Rather than giving vague, abstract DNS instructions that confuse business owners and IT staff, MailSent generates provider-tailored guidance (e.g., Cloudflare-specific instructions warning about the "Orange Cloud / Proxy" toggle for SPF/DKIM TXT records, or Route 53-specific zone navigation steps) with exact record names, types, and values.

---

## 2. Core Non-Negotiable Invariants

- **`N2` Never Fabricate Truth**: DNS instructions and exact values are derived deterministically from the real diagnosed `Issue`, `RootCause`, `Sender`, and registered domain facts. Zero synthetic advice or hallucinated configuration syntax.
- **`INV-09` Multi-Tenant Isolation**: Resolution generation is strictly scoped to the domain's owning `organization_id` and registered `domain_id`.
- **`INV-11` Deterministic Lineage**: Every generated `ResolutionAction` cites the originating `Issue` ID, `RootCause`, and source `HealthSignal` ID.
- **Provider Detection & Matching**:
  - If a specific `DnsProvider` (e.g. `CLOUDFLARE`, `ROUTE53`, `GODADDY`, `GOOGLE_CLOUD_DNS`, `NAMECHEAP`) is identified from observation facts (NS records) or user selection, tailored steps are provided.
  - Defaults cleanly to `GENERIC_DNS` if the DNS provider is unrecognized, ensuring complete guidance is always provided.
- **Zero Database Persistence Alteration**: Resolution action generation is a stateless derived projection. Zero database tables or Flyway migrations.

---

## 3. Domain Model: DnsProvider & ResolutionAction

### Package: `com.mailsent.resolution.domain`

```java
package com.mailsent.resolution.domain;

public enum DnsProvider {
    CLOUDFLARE,
    ROUTE53,
    GODADDY,
    GOOGLE_CLOUD_DNS,
    NAMECHEAP,
    GENERIC_DNS;

    public static DnsProvider fromNameservers(List<String> nameservers);
}
```

```java
package com.mailsent.resolution.domain;

import java.util.List;
import java.util.Objects;

public final class ResolutionAction {

    private final String id;
    private final String issueId; // Lineage to Issue (INV-11)
    private final String title;
    private final String description;
    private final DnsProvider dnsProvider;
    private final List<String> exactSteps;
    private final String recordType; // e.g. "TXT", "CNAME"
    private final String recordName; // e.g. "_dmarc.example.com"
    private final String recordValue; // exact DNS value
    private final int priority;

    // Factory methods for generating provider-tailored actions
}
```

---

## 4. Provider Tailoring Matrix

| Root Cause / Issue | `DnsProvider.CLOUDFLARE` Specifics | `DnsProvider.ROUTE53` Specifics | `DnsProvider.GENERIC_DNS` |
| :--- | :--- | :--- | :--- |
| **`DMARC_REJECT` / `DMARC_QUARANTINE`** | "Add TXT record `_dmarc`. Note: Ensure Proxy status is DNS Only (grey cloud)." | "In Route 53 Hosted Zone, Create Record -> Simple Routing -> TXT `_dmarc`." | "In your DNS management console, create a TXT record for `_dmarc`." |
| **`SPF_ALIGNMENT_FAILURE` / `SPF_AUTHENTICATION_FAILURE`** | "Edit existing SPF TXT record at root `@`. Include sender mechanism. Ensure TTL Auto." | "Edit TXT record for apex domain. Wrap multiple strings in double quotes if length > 255." | "Update your root SPF TXT record (`v=spf1 ...`)." |
| **`DKIM_ALIGNMENT_FAILURE` / `DKIM_AUTHENTICATION_FAILURE`** | "Add CNAME or TXT record for selector. Turn off Cloudflare Proxy (DNS Only)." | "Create CNAME / TXT record for DKIM selector." | "Publish DKIM selector record in your DNS zone." |

---

## 5. Application Service Boundary

### Package: `com.mailsent.resolution.application`

```java
@Service
public class ResolutionService {

    private final DomainRepository domainRepository;
    private final IssueRepository issueRepository;

    public List<ResolutionAction> generateActionsForIssue(String organizationId, String issueId, DnsProvider providerOverride);
    public List<ResolutionAction> generateActions(HealthSignal signal); // Backward compatibility
}
```
