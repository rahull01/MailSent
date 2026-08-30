# Tasks: P0-1 — Spring Boot Test Context, Multi-Tenant Monitoring Hardening & Persistence Alignment

- [ ] `T-P01-1`: Modify `Incident.java` and `HealthChangeEvent.java` domain models to enforce `organizationId` parameter in constructors, factories, and getters.
- [ ] `T-P01-2`: Update `IncidentRepository.java` and `HealthChangeEventRepository.java` interface methods with `(organizationId, domainId)`.
- [ ] `T-P01-3`: Update `IncidentService.java` and `HealthMonitoringScheduler.java` to pass `organizationId` down through monitoring workflows.
- [ ] `T-P01-4`: Update `IncidentEntity.java` and `HealthChangeEventEntity.java` with `@Column(name = "organization_id", length = 36, nullable = false)`.
- [ ] `T-P01-5`: Update `SpringDataIncidentRepository.java` and `SpringDataHealthChangeEventRepository.java` with tenant-scoped query methods.
- [ ] `T-P01-6`: Update `JpaIncidentRepositoryAdapter.java` and `JpaHealthChangeEventRepositoryAdapter.java` with `@Repository @Profile("!test")` and tenant mapping.
- [ ] `T-P01-7`: Create `backend/src/main/resources/db/migration/V20__create_monitoring_tables.sql` defining `incidents` and `health_change_events` tables with foreign keys and tenant-scoped indexes.
- [ ] `T-P01-8`: Annotate `JpaIssueRepositoryAdapter.java` with `@Repository @Profile("!test")`.
- [ ] `T-P01-9`: Implement thread-safe `InMemoryIssueRepository.java` in `com.mailsent.diagnosis.infrastructure.persistence`.
- [ ] `T-P01-10`: Implement thread-safe, tenant-aware `InMemoryIncidentRepository.java` and `InMemoryHealthChangeEventRepository.java` in `com.mailsent.monitoring.infrastructure.persistence`.
- [ ] `T-P01-11`: Create `IssueConfig.java` and `MonitoringConfig.java` with `@Profile("test")` in-memory repository beans.
- [ ] `T-P01-12`: Update `IncidentTest.java`, `IncidentServiceTest.java`, and `JpaIncidentRepositoryAdapterTest.java` to reflect `organizationId`.
- [ ] `T-P01-13`: Modify `backend/src/main/java/com/mailsent/MailSentApplication.java` to remove class-level `@EnableJpaRepositories` and `@EntityScan`.
- [ ] `T-P01-14`: Run `mvn test` and verify 0 errors / 0 failures across the entire test suite (981+ tests).
- [ ] `T-P01-15`: Run `mvn verify -Dmailsent.it.postgres=true` (when PostgreSQL is active) and verify Flyway V1-V20 and `ddl-auto: validate`.
