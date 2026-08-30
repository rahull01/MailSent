# Implementation Plan: HEALTH-002 — Health Trend Series

## 1. Architecture & Boundaries
- **Bounded Context**: Delivery Observation (`com.mailsent.portfolio.observation`).
- **Domain Layer**:
  - `TrendDirection` enum (`IMPROVING`, `STABLE`, `DECLINING`, `INSUFFICIENT_DATA`).
  - `HealthTrend` value projection class.
- **Application Layer**:
  - `HealthTrendService` query service.
- **Persistence & Migration Impact**:
  - **Zero new tables, Zero Flyway migrations**. Health Trend is derived on-the-fly from the append-only `health_signal` table (`V16`).

---

## 2. Package Layout

```
backend/src/main/java/com/mailsent/portfolio/observation/
├── domain/
│   ├── HealthIndication.java
│   ├── HealthSignal.java
│   ├── TrendDirection.java <-- [NEW ENUM]
│   └── HealthTrend.java     <-- [NEW DOMAIN PROJECTION]
└── application/
    ├── HealthSignalService.java
    ├── HealthSignalRepository.java
    └── HealthTrendService.java  <-- [NEW APPLICATION SERVICE]
```

---

## 3. Testing & Verification Matrix

### Unit Tests (`HealthTrendTest`, `HealthTrendServiceTest`):
1. **Trend Transitions**:
   - `[AT_RISK, HEALTHY]` $\rightarrow$ `IMPROVING`.
   - `[HEALTHY, AT_RISK]` $\rightarrow$ `DECLINING`.
   - `[HEALTHY, HEALTHY, HEALTHY]` $\rightarrow$ `STABLE`.
   - `[AT_RISK, AT_RISK]` $\rightarrow$ `STABLE`.
2. **Insufficient Data & Unknown Handling**:
   - `[HEALTHY]` (single snapshot) $\rightarrow$ `INSUFFICIENT_DATA`.
   - `[UNKNOWN, UNKNOWN]` $\rightarrow$ `INSUFFICIENT_DATA`.
   - `[HEALTHY, UNKNOWN]` (latest is unknown) $\rightarrow$ `INSUFFICIENT_DATA`.
   - `[]` (empty snapshot series) $\rightarrow$ `INSUFFICIENT_DATA`.
3. **Multi-Tenant Isolation**:
   - Negative test: Querying trend with mismatching `organizationId` throws `IllegalArgumentException`.
   - Negative test: Querying trend for non-existent domain throws `DomainNotRegisteredException`.
4. **Deterministic Lineage (`INV-11`)**:
   - Output `evaluatedHealthSignalIds` matches evaluated snapshots in exact order.

### Platform Regression Tests:
- Run full 220+ test hermetic regression suite and live PostgreSQL suite.
