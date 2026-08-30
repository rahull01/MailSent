# Checklist: HEALTH-002 — Health Trend Series

## Definition of Done (Constitution §19 & Task Criteria)

- [ ] **1. Domain Model & Invariants**
  - [x] Canonical trend directions frozen: `IMPROVING`, `STABLE`, `DECLINING`, `INSUFFICIENT_DATA`.
  - [x] Zero numeric scoring / Zero Trust Score (strictly operates on discrete `HealthIndication` transitions).
  - [x] `N2` Never Fabricate Truth: Trend derived strictly from historical `HealthSignal` snapshots.
  - [x] `INV-09` Multi-Tenant Isolation: Queries strictly partitioned by `organizationId` and `domainId`.
  - [x] `INV-11` Deterministic Lineage: Output cites the exact sequence of `evaluatedHealthSignalIds`.

- [ ] **2. Algorithm & Transition Rules**
  - [x] Minimum 2 distinct snapshots required for directional trend.
  - [x] `[AT_RISK, HEALTHY]` $\rightarrow$ `IMPROVING`.
  - [x] `[HEALTHY, AT_RISK]` $\rightarrow$ `DECLINING`.
  - [x] `[HEALTHY, HEALTHY]` or `[AT_RISK, AT_RISK]` $\rightarrow$ `STABLE`.
  - [x] Unresolved `UNKNOWN` or insufficient historical snapshots $\rightarrow$ `INSUFFICIENT_DATA`.

- [ ] **3. Architectural & Bounded Context Integrity**
  - [x] Sits strictly within `Delivery Observation` Bounded Context (`com.mailsent.portfolio.observation`).
  - [x] Does not create unnecessary tables or Flyway migrations (derived on-the-fly from `health_signal` table).
  - [x] Does not mutate existing `HEALTH-001` or `DIAG-001..004` code.
