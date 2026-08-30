# Implementation Plan: COMP-003 — Compliance Statement / PDF Report Generation

## 1. Architecture & Boundaries
- **Bounded Context**: Readiness & Compliance (`com.mailsent.compliance`).
- **Domain Layer**:
  - `ComplianceStatement.java` domain model containing executive summary, provider matrix, requirement breakdown, and evidence lineage.
- **Application Layer**:
  - `ComplianceService.java` application service enforcing `INV-09` tenant boundaries and compiling grounded statements.

---

## 2. Package Layout

```
backend/src/main/java/com/mailsent/compliance/
├── domain/
│   ├── MailboxProvider.java
│   ├── ReadinessLevel.java
│   ├── RequirementStatus.java
│   ├── ProviderRequirement.java
│   ├── ProviderRequirementChecklist.java
│   ├── ReadinessBadge.java
│   └── ComplianceStatement.java   <-- [NEW DOMAIN ENTITY]
└── application/
    └── ComplianceService.java     <-- [NEW APPLICATION SERVICE]
```

---

## 3. Testing & Verification Strategy

### Unit Tests:
- `ComplianceStatementTest.java`: Verify statement generation for `COMPLIANT`, `NEEDS_ATTENTION`, and `NON_COMPLIANT` states, verifying evidence lineage citing.
- `ComplianceServiceTest.java`: Verify tenant isolation, domain validation, and statement compilation.
- Hermetic and live PostgreSQL platform regression verification.
