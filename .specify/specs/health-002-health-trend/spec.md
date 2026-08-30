# Feature Specification: HEALTH-002 — Health Trend Series

## 1. Context & Business Value
MailSent is an AI Email Infrastructure Intelligence Platform. In the **Delivery Observation Bounded Context** (`com.mailsent.portfolio.observation`), **Health Trend** (`HEALTH-002` / `FUN-H4`) provides customers and operators with a legible historical trajectory of a registered domain's delivery infrastructure health.

While `HEALTH-001` provides a single unified point-in-time `HealthSignal` snapshot (`HEALTHY`, `AT_RISK`, `UNKNOWN`), `HEALTH-002` evaluates the sequential history of append-only `HealthSignal` snapshots across a specified time window (e.g. default 30-day window or explicit snapshot series) to determine the deterministic trend:
- **`IMPROVING`**: Health has transitioned positively (e.g. from `AT_RISK` to `HEALTHY`).
- **`STABLE`**: Health has remained consistent across evaluated snapshots (e.g. consecutively `HEALTHY` or consecutively `AT_RISK`).
- **`DECLINING`**: Health has transitioned negatively (e.g. from `HEALTHY` to `AT_RISK`).
- **`INSUFFICIENT_DATA`**: Fewer than 2 distinct observation snapshots exist in the evaluation window, or the evaluated series contains exclusively `UNKNOWN` signals.

---

## 2. Core Non-Negotiable Invariants

- **`N2` Never Fabricate Truth**: Health Trend is calculated strictly from real, persisted, append-only `HealthSignal` snapshots. It never guesses, interpolates synthetic datapoints, or fabricates ranking/scoring.
- **`INV-09` Multi-Tenant Isolation**: All trend queries are strictly scoped to the domain's owning `organization_id` and registered `domain_id`. Cross-tenant querying is strictly prohibited.
- **`INV-11` Deterministic Lineage**: Every `HealthTrend` evaluation output cites the exact sequence of `HealthSignal` IDs evaluated in chronological order.
- **No Numeric Scoring / No Trust Score**: `HealthTrend` does NOT compute a 0–100 score curve, weighted averages, or color gradients. It operates strictly on discrete transitions of canonical `HealthIndication` values (`HEALTHY`, `AT_RISK`, `UNKNOWN`).
- **Persistence Boundary**: `HealthTrend` is a **derived value projection** calculated on-the-fly from the append-only `health_signal` table (`V16`). **No new database table or Flyway migration is created**.

---

## 3. Domain Model: HealthTrend & TrendDirection

### Package: `com.mailsent.portfolio.observation.domain`

```java
package com.mailsent.portfolio.observation.domain;

public enum TrendDirection {
    IMPROVING,
    STABLE,
    DECLINING,
    INSUFFICIENT_DATA
}
```

```java
package com.mailsent.portfolio.observation.domain;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public final class HealthTrend {

    private final String organizationId;
    private final String domainId;
    private final String domainName;
    private final TrendDirection direction;
    private final HealthIndication currentIndication;
    private final HealthIndication previousIndication; // nullable if INSUFFICIENT_DATA
    private final List<String> evaluatedHealthSignalIds;
    private final Instant evaluatedAt;

    // Factory methods:
    public static HealthTrend calculate(String organizationId, String domainId, String domainName,
                                        List<HealthSignal> historicalSignals, Instant evaluatedAt);
}
```

---

## 4. Deterministic Trend Calculation Algorithm

Let $S = [s_1, s_2, \dots, s_n]$ be the list of historical `HealthSignal` snapshots for the domain within the requested time window, sorted in **ascending chronological order** by `observedAt` (where $s_1$ is oldest and $s_n$ is the latest/current signal).

### Step 1: Minimum Data Gating
- If $n < 2$: Return `INSUFFICIENT_DATA` (`currentIndication = s_n.indication()`, `previousIndication = null`, `evaluatedHealthSignalIds = cited IDs`).

### Step 2: Evaluation of Consecutive Indicative Signals
Filter the sequence to find the latest valid baseline transition by comparing the current latest signal $s_n$ with the prior baseline signal $s_{baseline}$ (the most recent preceding signal with a definitive non-identical indication or definitive baseline):

| Current Signal ($s_n$) | Prior Baseline ($s_{baseline}$) | Trend Direction | Meaning |
| :--- | :--- | :--- | :--- |
| **`HEALTHY`** | **`AT_RISK`** | **`IMPROVING`** | Domain was at risk and has transitioned to healthy. |
| **`HEALTHY`** | **`HEALTHY`** | **`STABLE`** | Domain remains consistently healthy. |
| **`AT_RISK`** | **`HEALTHY`** | **`DECLINING`** | Domain was healthy and has degraded to at-risk. |
| **`AT_RISK`** | **`AT_RISK`** | **`STABLE`** | Domain remains consistently at risk. |
| **`UNKNOWN`** | Any | **`INSUFFICIENT_DATA`** | Current state is unknown; cannot assert a trajectory. |
| Any | **`UNKNOWN`** | **`INSUFFICIENT_DATA`** | If all preceding historical signals are unknown, baseline is insufficient. |

### Step 3: Unchanged / Repeated Snapshots
- If all snapshots in the window are `HEALTHY`: Trend is **`STABLE`**.
- If all snapshots in the window are `AT_RISK`: Trend is **`STABLE`**.
- If all snapshots in the window are `UNKNOWN`: Trend is **`INSUFFICIENT_DATA`**.

---

## 5. Application Service & Query Interface

### Package: `com.mailsent.portfolio.observation.application`

```java
@Service
public class HealthTrendService {

    private final HealthSignalRepository healthSignalRepository;
    private final DomainRepository domainRepository;
    private final Clock clock;

    public HealthTrendService(HealthSignalRepository healthSignalRepository,
                              DomainRepository domainRepository,
                              Clock clock) { ... }

    @Transactional(readOnly = true)
    public HealthTrend getTrendForDomain(String organizationId, String domainId, Instant since) {
        // 1. Verify tenant domain ownership (INV-09)
        // 2. Fetch historical HealthSignal snapshots for domainId ordered by observedAt ASC
        // 3. Filter by 'since' timestamp if provided (default 30 days)
        // 4. Calculate HealthTrend projection
        // 5. Return deterministic HealthTrend with evaluated snapshot IDs (INV-11)
    }
}
```

---

## 6. Tenant Isolation & Security Boundary (`INV-09`)
- `getTrendForDomain(organizationId, domainId, since)` validates that `domain.organizationId().equals(organizationId)`.
- If the domain does not exist or belongs to another organization, throws `DomainNotRegisteredException` or `IllegalArgumentException("Tenant boundary violation")`.
- Repository query `findByDomainId(domainId)` is filtered against the verified domain's ownership.
