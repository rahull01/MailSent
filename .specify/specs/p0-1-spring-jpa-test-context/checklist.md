# Checklist: P0-1 — Spring Boot Test Context, Multi-Tenant Monitoring Hardening & Persistence Alignment

- [ ] Architecture review complete against PROJECT_CONSTITUTION.md, 01_DOMAIN_MODEL.md, 03_BOUNDED_CONTEXTS.md, and 07_DATA_MODEL.md.
- [ ] No changes made to frozen database migrations (V1-V18).
- [ ] `V19__create_issues.sql` is present and clean.
- [ ] `V20__create_monitoring_tables.sql` is created with strict foreign keys to `organizations(id)` and `domains(id)`, tenant-scoped composite indexes, and partial unique active incident index.
- [ ] `Incident` and `HealthChangeEvent` domain entities enforce `organizationId` invariant (`INV-09`, `INV-13`).
- [ ] `IncidentRepository` and `HealthChangeEventRepository` methods enforce `organizationId` scoping.
- [ ] `IncidentEntity` and `HealthChangeEventEntity` contain `@Column(name = "organization_id")`.
- [ ] All JPA repository adapters carry `@Repository @Profile("!test")`.
- [ ] In-memory test repositories mirror concurrent thread-safety and unique constraint semantics under `@Profile("test")`.
- [ ] `MailSentApplication.java` has no top-level `@EnableJpaRepositories` / `@EntityScan`.
- [ ] Full regression run (`mvn test`) passes with 0 errors and 0 failures across 981+ tests.
- [ ] Real PostgreSQL integration verification (`mvn verify -Dmailsent.it.postgres=true`) applies migrations V1–V20 and satisfies `ddl-auto: validate`.
