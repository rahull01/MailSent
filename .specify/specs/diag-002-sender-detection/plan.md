# Implementation Plan: DIAG-002 — Sender Detection (Recognized Providers)

## 1. Architecture & Boundaries
- **Bounded Context**: Diagnosis Engine (`com.mailsent.diagnosis`).
- **Class Under Analysis**: `SenderDetectionService` (Application Service) and `Sender` (Domain Value Object).
- **Persistence Boundary**: None. Sender detection is an in-memory analysis algorithm operating on immutable `DmarcAggregateEvidence` value snapshots.

---

## 2. Technical Design

### Package Layout
```
backend/src/main/java/com/mailsent/diagnosis/
├── domain/
│   └── Sender.java (Record)
└── application/
    └── SenderDetectionService.java (Spring @Service)
```

### Key Detection Logic Flow
1. Accept `DmarcAggregateEvidence evidence`.
2. Return `Optional.empty()` if evidence is null.
3. Iterate over `evidence.dkimDomains()`:
   - Normalize string to lowercase.
   - Check matching suffixes for Google (`google.com`), Microsoft (`outlook.com`, `onmicrosoft.com`, `microsoft.com`), Salesforce (`salesforce.com`, `sfdc.net`), Mailchimp (`rsgsv.net`, `mcsv.net`, `mailchimp.com`).
   - If matched, return `Optional.of(Sender)`.
4. If no DKIM match, check `evidence.envelopeFrom()`:
   - Normalize string to lowercase.
   - Check substring matching for recognized provider patterns.
   - If matched, return `Optional.of(Sender)`.
5. Return `Optional.empty()` if neither source matches.

---

## 3. Verification Strategy
- **Unit Tests (`SenderDetectionServiceTest`)**:
  - Test all 4 catalog providers via DKIM domain suffix.
  - Test all 4 catalog providers via Envelope From substring.
  - Test case-insensitivity (e.g. `GOOGLE.COM`, `BoUnCeS.SaLeSfOrCe.CoM`).
  - Test multiple DKIM domains where first or second matches.
  - Test DKIM precedence over conflicting Envelope From.
  - Test unrecognized domains return `Optional.empty()`.
  - Test null/empty evidence returns `Optional.empty()`.
