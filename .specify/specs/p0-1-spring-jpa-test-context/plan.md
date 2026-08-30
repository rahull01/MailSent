# Implementation Plan: P0-1 — Spring Boot Test Context, Multi-Tenant Monitoring Hardening & Persistence Alignment

## 1. Objectives
Eliminate ApplicationContext loading crashes in hermetic tests, enforce `INV-09` multi-tenant isolation in the `monitoring` bounded context, author `V20` persistence schema, and guarantee 100% production persistence integrity under `ddl-auto: validate`.

## 2. Technical Strategy

### Step 1: Multi-Tenant Domain Hardening (INV-09)
- Add `organizationId` parameter to `Incident.java` and `HealthChangeEvent.java` constructors and factories (`Incident.open`, `HealthChangeEvent.detect`).
- Update `IncidentRepository.java` to declare `findActiveByOrganizationIdAndDomainId(String organizationId, String domainId)` and `findById(String organizationId, String incidentId)`.
- Update `HealthChangeEventRepository.java` to declare `findByOrganizationIdAndDomainId(String organizationId, String domainId)`.
- Update `IncidentService.java` and `HealthMonitoringScheduler.java` to extract `organizationId` from `HealthSignal` / `Domain` and pass it down.

### Step 2: Persistence & Schema Migration (V20)
- Add `@Column(name = "organization_id", length = 36, nullable = false)` to `IncidentEntity.java` and `HealthChangeEventEntity.java`.
- Update `SpringDataIncidentRepository.java` and `SpringDataHealthChangeEventRepository.java` with tenant-scoped query methods.
- Update `JpaIncidentRepositoryAdapter.java` and `JpaHealthChangeEventRepositoryAdapter.java` with `@Repository @Profile("!test")` and tenant mappings.
- Author `backend/src/main/resources/db/migration/V20__create_monitoring_tables.sql` defining `incidents` and `health_change_events` with FKs to `organizations(id)` and `domains(id)`, composite indexes, and partial unique active incident index.

### Step 3: Production JPA Adapter Profile Guarding
- Add `@Repository @Profile("!test")` to `JpaIssueRepositoryAdapter.java`.

### Step 4: Hermetic In-Memory Test Doubles & Profile Configuration
- Create thread-safe `InMemoryIssueRepository.java`, `InMemoryIncidentRepository.java`, and `InMemoryHealthChangeEventRepository.java`.
- Create `IssueConfig.java` registering `InMemoryIssueRepository` under `@Profile("test")`.
- Create `MonitoringConfig.java` registering `InMemoryIncidentRepository` and `InMemoryHealthChangeEventRepository` under `@Profile("test")`.

### Step 5: Clean Application Entry Point
- Remove `@EnableJpaRepositories` and `@EntityScan` from `MailSentApplication.java`.

### Step 6: Verification
- Run `mvn test` (hermetic suite) -> verify 0 errors, 0 failures across 981+ tests.
- Run `mvn test -Dtest=IncidentServiceTest,IncidentTest,JpaIncidentRepositoryAdapterTest` -> verify monitoring unit and repository tests.
- Run `mvn verify -Dmailsent.it.postgres=true` (when PostgreSQL is active) -> verify Flyway V1-V20 and `ddl-auto: validate`.
