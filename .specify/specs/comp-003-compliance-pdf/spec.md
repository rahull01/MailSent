# Feature Specification: COMP-003 — Compliance Statement / PDF Report Generation (Readiness Output)

## 1. Context & Business Value
MailSent is an AI Email Infrastructure Intelligence Platform. In the **Readiness & Compliance Bounded Context** (`com.mailsent.compliance`), **COMP-003** (`FUN-C3`) enables customers, agency partners, and deliverability operators to produce a formal, verifiable **Compliance & Readiness Statement / Document** (suitable for auditors, enterprise security partners, enterprise clients, or board reviews) that certifies the exact compliance state of a domain against mailbox provider standards (Google, Yahoo, Microsoft, PCI DSS).

While `COMP-001` evaluates the provider-specific requirement checklists and `COMP-002` computes the aggregated `ReadinessBadge`, `COMP-003` compiles these assessments into a formatted, grounded compliance output with deterministic lineage (`INV-11`) and tenant isolation (`INV-09`).

---

## 2. Core Non-Negotiable Invariants

- **`N2` Never Fabricate Truth**: The compliance statement is constructed strictly from real, evaluated `ProviderRequirementChecklist` and `ReadinessBadge` snapshots. It never fabricates compliance pass states or omits known deficiencies.
- **`INV-09` Multi-Tenant Isolation**: Generating a compliance statement is strictly validated against the domain's owning `organization_id`. Mismatched requests reject with `IllegalArgumentException`.
- **`INV-11` Deterministic Lineage**: Every generated `ComplianceStatement` cites the exact `ReadinessBadge` ID, `ProviderRequirementChecklist` IDs, and underlying evidence references evaluated.
- **Document Output Model**:
  - `ComplianceStatement`: Domain entity containing structured compliance sections (Executive Summary, Mailbox Provider Status Matrix, Itemized Requirement Breakdown, and Audit Evidence Lineage).
  - Formatted for clean export as PDF/HTML/Markdown document payload without external heavy rendering dependencies.
- **Zero Database Persistence Alteration**: The compliance statement is a stateless compiled projection. Zero database tables or Flyway migrations.

---

## 3. Domain Model: ComplianceStatement

### Package: `com.mailsent.compliance.domain`

```java
package com.mailsent.compliance.domain;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class ComplianceStatement {

    private final String id;
    private final String organizationId;
    private final String domainId;
    private final String domainName;
    private final ReadinessBadge.BadgeTier overallTier;
    private final int satisfiedCount;
    private final int totalCount;
    private final List<ProviderRequirementChecklist> evaluatedChecklists;
    private final String executiveSummary;
    private final String documentBody;
    private final Instant generatedAt;

    // Factory method:
    public static ComplianceStatement generate(String organizationId, String domainId, String domainName,
                                               ReadinessBadge badge, List<ProviderRequirementChecklist> checklists,
                                               Instant generatedAt);
}
```

---

## 4. Application Service Boundary

### Package: `com.mailsent.compliance.application`

```java
@Service
public class ComplianceService {

    private final DomainRepository domainRepository;

    public ComplianceStatement generateStatement(String organizationId, String domainId,
                                                ReadinessBadge badge, List<ProviderRequirementChecklist> checklists);
}
```
