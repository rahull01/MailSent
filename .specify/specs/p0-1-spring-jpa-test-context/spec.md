# Specification: P0-1 — Spring Boot Test Context, Multi-Tenant Monitoring Hardening & Persistence Alignment

## 1. Context & Problem Statement
Running the full backend test suite (`mvn test`) produced 37 errors across 981 tests due to `NoSuchBeanDefinitionException: No bean named 'entityManagerFactory' available`.
This failure prevents CI execution and breaks MockMvc `@SpringBootTest` tests that run under the hermetic `test` profile (`application-test.yml`), which deliberately excludes `DataSourceAutoConfiguration` and `HibernateJpaAutoConfiguration`.

Forensic investigation revealed two additional critical issues:
1. **INV-09 Multi-Tenant Gap in Monitoring:** Domain aggregates `Incident` and `HealthChangeEvent`, along with their JPA entities and repository ports, operate exclusively on `domain_id` without an `organization_id` tenant boundary.
2. **Missing Flyway DDL for Monitoring JPA Entities:** `IncidentEntity` and `HealthChangeEventEntity` exist in production code but lack matching tables in Flyway migrations `V1`–`V19`, which will cause immediate application crash under `ddl-auto: validate` in production.

## 2. Invariants & Governing Rules
- **Rule N5 (Mandatory Automated Tests):** Complete regression suite must execute cleanly in CI without requiring external infrastructure for unit and slice tests.
- **INV-09 & INV-13 (Multi-Tenant Isolation & Ownership):** All domain aggregates, events, persistence tables, and queries must partition strictly by `organization_id`.
- **Hermetic Test Isolation (Phase 01 Contract):** The default `test` profile MUST NOT require a PostgreSQL or Redis instance. All repository dependencies under `test` must be satisfied by hermetic, thread-safe in-memory beans.
- **Production Persistence Integrity (Constitution §19 / §20):** Under non-test profiles (`local`, `prod`, `it`), Spring Data JPA, PostgreSQL, Flyway, and transaction boundaries must activate seamlessly and validate schemas without error.

## 3. Root Cause Analysis
1. `@EnableJpaRepositories(basePackages = "com.mailsent")` and `@EntityScan(basePackages = "com.mailsent")` were placed unconditionally on `MailSentApplication.java`.
2. When Spring Boot initializes under `@ActiveProfiles("test")`, `application-test.yml` excludes `DataSourceAutoConfiguration` and `HibernateJpaAutoConfiguration`.
3. Spring Data JPA repository interfaces discovered by the top-level `@EnableJpaRepositories` attempt to resolve the missing `EntityManagerFactory`, crashing the `ApplicationContext`.
4. Newly added repository adapters (`JpaIssueRepositoryAdapter`, `JpaIncidentRepositoryAdapter`, `JpaHealthChangeEventRepositoryAdapter`) lacked `@Profile("!test")` annotations and lacked corresponding in-memory test bean counterparts.
5. Monitoring models and persistence classes were authored without `organization_id` foreign keys and columns, violating `INV-09`.

## 4. Architectural Boundaries & Frozen Contracts

### A. Runtime / Production Architecture (`SPRING_PROFILES_ACTIVE=prod|local|it`)
- Spring Data JPA autoconfiguration activates.
- Flyway executes schema migrations `V1` through `V20` (where `V20` creates `incidents` and `health_change_events` with FKs to `organizations` and `domains`).
- JPA entities are scanned from `com.mailsent`.
- All `Jpa*RepositoryAdapter` classes (marked with `@Repository` and `@Profile("!test")`) bind to their respective `JpaRepository` interfaces and provide transactional database persistence.

### B. Hermetic Test Architecture (`SPRING_PROFILES_ACTIVE=test`)
- `DataSourceAutoConfiguration` and `HibernateJpaAutoConfiguration` remain excluded.
- JPA repositories and adapters are NOT instantiated.
- Every domain repository port is backed by a registered in-memory repository bean (e.g. `InMemoryUserRepository`, `InMemoryDomainRepository`, `InMemoryObservationEvidenceRepository`, `InMemoryIssueRepository`, `InMemoryIncidentRepository`, `InMemoryHealthChangeEventRepository`).
- Application context loads in <1s with 0 external network/database dependencies.

### C. Multi-Tenant Monitoring Contracts (INV-09)
- `Incident`: `(id, organizationId, domainId, openedAt, resolvedAt)`.
- `HealthChangeEvent`: `(id, organizationId, domainId, previousIndication, currentIndication, detectedAt)`.
- `IncidentRepository`: `findActiveByOrganizationIdAndDomainId(String organizationId, String domainId)`.
- `HealthChangeEventRepository`: `findByOrganizationIdAndDomainId(String organizationId, String domainId)`.

## 5. Explicit Acceptance Criteria
1. `MailSentApplication.java` has no top-level `@EnableJpaRepositories` or `@EntityScan` annotations.
2. `Incident` and `HealthChangeEvent` domain models, JPA entities, and repository interfaces enforce `organizationId`.
3. `V20__create_monitoring_tables.sql` creates `incidents` and `health_change_events` tables with foreign keys and tenant-scoped indexes.
4. All JPA repository adapters carry `@Profile("!test")`.
5. Thread-safe, tenant-aware in-memory implementations exist for `IssueRepository`, `IncidentRepository`, and `HealthChangeEventRepository` and are registered as beans under `@Profile("test")`.
6. `mvn test` runs with **0 errors and 0 failures** across all test classes (981+ tests).
7. MockMvc `@SpringBootTest` tests (`SignInControllerIntegrationTest`, `SignUpControllerIntegrationTest`, `DmarcReportReceiveControllerTest`, `ActuatorExposureTest`) load cleanly and pass.
8. Real-PostgreSQL IT tests remain fully functional under `@ActiveProfiles("it")` with Flyway migrations `V1`–`V20`.
