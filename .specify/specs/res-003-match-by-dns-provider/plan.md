# Implementation Plan: RES-003 — Match by DNS Provider

## 1. Architecture & Boundaries
- **Bounded Context**: Guidance & Guided Resolution (`com.mailsent.resolution`).
- **Domain Layer**:
  - `DnsProvider` enum (`CLOUDFLARE`, `ROUTE53`, `GODADDY`, `GOOGLE_CLOUD_DNS`, `NAMECHEAP`, `GENERIC_DNS`).
  - Enhanced `ResolutionAction.java` domain model with `dnsProvider`, `issueId`, exact record details, and lineage.
- **Application Layer**:
  - `ResolutionService.java` enhanced with `generateActionsForIssue` enforcing `INV-09` tenant boundaries and mapping `RootCause` $\rightarrow$ provider-tailored steps.

---

## 2. Package Layout

```
backend/src/main/java/com/mailsent/resolution/
├── domain/
│   ├── DnsProvider.java        <-- [NEW ENUM & DETECTOR]
│   └── ResolutionAction.java   <-- [ENHANCED WITH PROVIDER CONTEXT]
└── application/
    └── ResolutionService.java  <-- [ENHANCED WITH ISSUE & PROVIDER SUPPORT]
```

---

## 3. Testing & Verification Strategy

### Unit Tests:
- `DnsProviderTest.java`: Verify nameserver matching (e.g. `*.ns.cloudflare.com` $\rightarrow$ `CLOUDFLARE`, `awsdns-*` $\rightarrow$ `ROUTE53`, unknown $\rightarrow$ `GENERIC_DNS`).
- `ResolutionActionTest.java`: Verify provider-tailored action generation with exact record values and lineage citing.
- `ResolutionServiceTest.java`: Verify tenant isolation, domain validation, issue resolution, and backward compatibility.
- Hermetic and live PostgreSQL platform regression verification.
