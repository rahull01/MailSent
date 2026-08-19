# Implementation Plan: DIAG-003 — Root Cause Determination

## 1. Architecture & Boundaries
- **Bounded Context**: Diagnosis Engine (`com.mailsent.diagnosis`).
- **Core Application Service**: `RootCauseDeterminationService` (`com.mailsent.diagnosis.application`).
- **Domain Enum**: `RootCause` (`com.mailsent.diagnosis.domain`).
- **Consumes**:
  - `HealthSignal` (from `com.mailsent.portfolio.observation.domain`)
  - `DmarcAggregateEvidenceRepository` & `DmarcAggregateEvidence` (from `com.mailsent.portfolio.observation`)

---

## 2. Technical Design & Algorithm Flow

1. Receive `HealthSignal healthSignal`.
2. Guard: If `healthSignal == null`, throw `IllegalArgumentException`.
3. Guard: If `healthSignal.indication() != HealthIndication.AT_RISK`, return `Optional.empty()`.
4. Hydrate cited evidence via `healthSignal.dmarcAggregateEvidenceIds()` through `DmarcAggregateEvidenceRepository`.
5. Evaluate cited evidence rows against the precedence hierarchy:
   - Check `disposition`: `reject` $\rightarrow$ `DMARC_REJECT`, `quarantine` $\rightarrow$ `DMARC_QUARANTINE`.
   - Check `spfAligned`: `fail` $\rightarrow$ `SPF_ALIGNMENT_FAILURE`.
   - Check `dkimAligned`: `fail` $\rightarrow$ `DKIM_ALIGNMENT_FAILURE`.
   - Check `spfResult`: `fail` $\rightarrow$ `SPF_AUTHENTICATION_FAILURE`.
   - Check `dkimResult`: `fail` $\rightarrow$ `DKIM_AUTHENTICATION_FAILURE`.
6. If no specific failure token matched across cited rows, return `Optional.of(RootCause.UNKNOWN_ISSUE)`.

---

## 3. Verification Strategy
- **Unit Testing (`RootCauseDeterminationServiceTest`)**:
  - Test null input rejection.
  - Test non-at-risk signals (`HEALTHY`, `UNKNOWN`) return empty.
  - Test each root cause classification:
    - `DMARC_REJECT`
    - `DMARC_QUARANTINE`
    - `SPF_ALIGNMENT_FAILURE`
    - `DKIM_ALIGNMENT_FAILURE`
    - `SPF_AUTHENTICATION_FAILURE`
    - `DKIM_AUTHENTICATION_FAILURE`
    - `UNKNOWN_ISSUE`
  - Test multi-failure precedence (e.g. `reject` disposition taking precedence over `spfAligned=fail`).
  - Test missing evidence resolution (empty repository lookup).
