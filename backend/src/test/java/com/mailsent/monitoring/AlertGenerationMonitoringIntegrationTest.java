package com.mailsent.monitoring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import com.mailsent.alerts.application.AlertGenerationService;
import com.mailsent.alerts.domain.Alert;
import com.mailsent.alerts.domain.AlertSeverity;
import com.mailsent.identity.authentication.application.UserRepository;
import com.mailsent.identity.authentication.infrastructure.persistence.JpaUserRepository;
import com.mailsent.identity.authentication.infrastructure.persistence.JpaUserRepositoryAdapter;
import com.mailsent.identity.authentication.infrastructure.persistence.UserJpaEntity;
import com.mailsent.identity.consent.application.ConsentRepository;
import com.mailsent.identity.consent.application.ConsentService;
import com.mailsent.identity.consent.domain.ActionType;
import com.mailsent.identity.consent.domain.Consent;
import com.mailsent.identity.consent.infrastructure.persistence.ConsentJpaEntity;
import com.mailsent.identity.consent.infrastructure.persistence.JpaConsentRepository;
import com.mailsent.identity.consent.infrastructure.persistence.JpaConsentRepositoryAdapter;
import com.mailsent.monitoring.application.HealthChangeDetector;
import com.mailsent.monitoring.application.HealthChangeEventRepository;
import com.mailsent.monitoring.application.IncidentService;
import com.mailsent.monitoring.domain.HealthChangeEvent;
import com.mailsent.monitoring.infrastructure.persistence.HealthChangeEventEntity;
import com.mailsent.monitoring.infrastructure.persistence.IncidentEntity;
import com.mailsent.monitoring.infrastructure.persistence.JpaHealthChangeEventRepositoryAdapter;
import com.mailsent.monitoring.infrastructure.persistence.JpaIncidentRepositoryAdapter;
import com.mailsent.monitoring.infrastructure.persistence.SpringDataHealthChangeEventRepository;
import com.mailsent.monitoring.infrastructure.persistence.SpringDataIncidentRepository;
import com.mailsent.portfolio.domain.application.DomainRepository;
import com.mailsent.portfolio.domain.application.DomainService;
import com.mailsent.portfolio.domain.domain.Domain;
import com.mailsent.portfolio.domain.infrastructure.persistence.DomainJpaEntity;
import com.mailsent.portfolio.domain.infrastructure.persistence.JpaDomainRepository;
import com.mailsent.portfolio.domain.infrastructure.persistence.JpaDomainRepositoryAdapter;
import com.mailsent.portfolio.observation.application.DmarcAggregateEvidenceRepository;
import com.mailsent.portfolio.observation.application.DmarcAggregateEvidenceService;
import com.mailsent.portfolio.observation.application.DmarcReportParseService;
import com.mailsent.portfolio.observation.application.DmarcReportReceiptRepository;
import com.mailsent.portfolio.observation.application.DmarcReportReceiveService;
import com.mailsent.portfolio.observation.application.DnsObservationService;
import com.mailsent.portfolio.observation.application.DnsResolver;
import com.mailsent.portfolio.observation.application.GooglePostmasterPort;
import com.mailsent.portfolio.observation.application.HealthSignalRepository;
import com.mailsent.portfolio.observation.application.HealthSignalService;
import com.mailsent.portfolio.observation.application.MicrosoftSndsPort;
import com.mailsent.portfolio.observation.application.ObservationEvidenceRepository;
import com.mailsent.portfolio.observation.application.ObservationEvidenceService;
import com.mailsent.portfolio.observation.application.ReputationObservationRepository;
import com.mailsent.portfolio.observation.application.ReputationObservationService;
import com.mailsent.portfolio.observation.application.ReputationProviderObservation;
import com.mailsent.portfolio.observation.domain.DnsObservation;
import com.mailsent.portfolio.observation.domain.HealthIndication;
import com.mailsent.portfolio.observation.domain.HealthSignal;
import com.mailsent.portfolio.observation.domain.MxRecord;
import com.mailsent.portfolio.observation.infrastructure.Rfc7489DmarcAggregateXmlParser;
import com.mailsent.portfolio.observation.infrastructure.persistence.DmarcAggregateEvidenceJpaEntity;
import com.mailsent.portfolio.observation.infrastructure.persistence.DmarcReportReceiptJpaEntity;
import com.mailsent.portfolio.observation.infrastructure.persistence.HealthSignalJpaEntity;
import com.mailsent.portfolio.observation.infrastructure.persistence.JpaDmarcAggregateEvidenceRepository;
import com.mailsent.portfolio.observation.infrastructure.persistence.JpaDmarcAggregateEvidenceRepositoryAdapter;
import com.mailsent.portfolio.observation.infrastructure.persistence.JpaDmarcReportReceiptRepository;
import com.mailsent.portfolio.observation.infrastructure.persistence.JpaDmarcReportReceiptRepositoryAdapter;
import com.mailsent.portfolio.observation.infrastructure.persistence.JpaHealthSignalRepository;
import com.mailsent.portfolio.observation.infrastructure.persistence.JpaHealthSignalRepositoryAdapter;
import com.mailsent.portfolio.observation.infrastructure.persistence.JpaObservationEvidenceRepository;
import com.mailsent.portfolio.observation.infrastructure.persistence.JpaObservationEvidenceRepositoryAdapter;
import com.mailsent.portfolio.observation.infrastructure.persistence.JpaReputationObservationRepository;
import com.mailsent.portfolio.observation.infrastructure.persistence.JpaReputationObservationRepositoryAdapter;
import com.mailsent.portfolio.observation.infrastructure.persistence.ObservationEvidenceJpaEntity;
import com.mailsent.portfolio.observation.infrastructure.persistence.ReputationObservationJpaEntity;
import com.mailsent.portfolio.organization.application.OrganizationRepository;
import com.mailsent.portfolio.organization.application.OrganizationService;
import com.mailsent.portfolio.organization.infrastructure.persistence.JpaOrganizationRepository;
import com.mailsent.portfolio.organization.infrastructure.persistence.JpaOrganizationRepositoryAdapter;
import com.mailsent.portfolio.organization.infrastructure.persistence.OrganizationJpaEntity;

/**
 * Real-PostgreSQL integration test for the ALERT-002 generation seam (CTO decision D4).
 *
 * <p>Proves the monitored chain end-to-end on the live stack: a real derived health signal
 * flows through {@link HealthChangeDetector}, which persists the {@code HealthChangeEvent}
 * (V20) and synchronously produces a business-language {@link Alert} via
 * {@link AlertGenerationService}. DNS and reputation providers are stubbed exactly like
 * the HEALTH-001 integration suite; PostgreSQL remains authoritative. Per D3 there is
 * deliberately NO alerts table — nothing about the generated alert is persisted. Gated on
 * {@code mailsent.it.postgres}; idempotent (randomized names/ids).</p>
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("it")
@ContextConfiguration(classes = AlertGenerationMonitoringIntegrationTest.TestConfig.class)
@EnabledIfSystemProperty(named = "mailsent.it.postgres", matches = "true")
class AlertGenerationMonitoringIntegrationTest {

    private static final Instant BASE = Instant.parse("2026-08-26T08:00:00Z");

    @Autowired private OrganizationService organizationService;
    @Autowired private DomainService domainService;
    @Autowired private ObservationEvidenceService evidenceService;
    @Autowired private ReputationObservationService reputationService;
    @Autowired private DmarcReportReceiveService receiveService;
    @Autowired private DmarcReportParseService parseService;
    @Autowired private DmarcAggregateEvidenceService aggregateService;
    @Autowired private HealthSignalService healthSignalService;
    @Autowired private ConsentRepository consentRepository;
    @Autowired private HealthChangeEventRepository eventRepository;
    @Autowired private AlertGenerationService alertGenerationService;
    @Autowired private HealthChangeDetector detector;

    private Domain registeredDomain() {
        String orgId = organizationService.create("AlertIT " + UUID.randomUUID()).id();
        Domain domain = domainService.register(orgId, "it-" + UUID.randomUUID() + ".com");
        consentRepository.save(Consent.grant(
            orgId, domain.id(), "user-" + UUID.randomUUID(),
            ActionType.OBSERVE_DOMAIN_INFRASTRUCTURE));
        return domain;
    }

    private void seedRequiredSources(Domain domain) {
        evidenceService.record(domain.name());
        reputationService.record(domain.name());
        var receipt = receiveService.receive(domain.id(),
                xml(domain.name(), "none", "fail", "pass").getBytes(StandardCharsets.UTF_8))
            .orElseThrow();
        aggregateService.store(parseService.parse(receipt.id()));
    }

    @Test
    void realAtRiskDerivationFlowsThroughDetectorToABusinessLanguageAlert() {
        Domain domain = registeredDomain();
        seedRequiredSources(domain);

        // Real derivation through HEALTH-001 (all required snapshots persisted above):
        HealthSignal signal = healthSignalService.derive(domain.name()).orElseThrow();
        assertEquals(HealthIndication.AT_RISK, signal.indication());

        // The monitored seam under test (D4): first observation of an at-risk domain.
        var eventOpt = detector.detectChange(Optional.empty(), signal, BASE);
        assertTrue(eventOpt.isPresent(), "first AT_RISK observation is a meaningful change");
        var event = eventOpt.get();

        // Event persistence is real (V20), owned by MON-003:
        List<HealthChangeEvent> persisted =
            eventRepository.findByOrganizationIdAndDomainId(domain.organizationId(), domain.id());
        assertTrue(persisted.stream().anyMatch(e -> e.id().equals(event.id())),
            "the monitoring event is durably stored by the existing pipeline");

        // ALERT-002 result: deterministic business-language alert, tenant-verbatim (INV-09).
        Alert alert = alertGenerationService.generate(event).orElseThrow();
        assertEquals(domain.organizationId(), alert.organizationId());
        assertEquals(AlertSeverity.CRITICAL, alert.severity(),
            "first AT_RISK observation maps to CRITICAL per approved policy D1");
        assertNull(alert.incidentId(), "health-change-originated alert has no incident (D2)");
        assertTrue(alert.whatHappened().contains(domain.id()));

        String haystack = (alert.whatHappened() + " " + alert.whyItMatters()).toLowerCase(Locale.ROOT);
        for (String forbidden : new String[]{"spf", "dkim", "dmarc", "blacklist", "dns"}) {
            assertTrue(!haystack.contains(forbidden), "no fabricated technical cause: " + forbidden);
        }
    }

    /** Minimal RUA aggregate payload with a failing DKIM result ⇒ AT_RISK per HEALTH-001. */
    private static String xml(String headerFrom, String disposition, String spf, String dkim) {
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <feedback>
              <report_metadata>
                <org_name>google.com</org_name>
                <report_id>it-alert-%s</report_id>
                <date_range><begin>1609459200</begin><end>1609545599</end></date_range>
              </report_metadata>
              <policy_published><domain>other.example</domain><p>none</p></policy_published>
              <record>
                <row>
                  <source_ip>192.0.2.1</source_ip>
                  <count>1</count>
                  <policy_evaluated><disposition>%s</disposition><dkim>%s</dkim><spf>%s</spf></policy_evaluated>
                </row>
                <identifiers><header_from>%s</header_from></identifiers>
                <auth_results>
                  <dkim><domain>sel.example.com</domain><result>%s</result></dkim>
                  <spf><domain>%s</domain><result>%s</result></spf>
                </auth_results>
              </record>
            </feedback>
            """.formatted(UUID.randomUUID(), disposition, dkim, spf, headerFrom, dkim, headerFrom, spf);
    }

    /** Deterministic stub DNS observer mirroring the HEALTH-001 integration suite. */
    static class SequenceResolver implements DnsResolver {
        private final AtomicInteger seq = new AtomicInteger();

        @Override
        public DnsObservation observe(String domainName) {
            Instant observedAt = BASE.plusSeconds(seq.getAndIncrement());
            return DnsObservation.of(
                domainName,
                List.of(new MxRecord(10, "mail.example.com")),
                List.of("v=spf1 -all"),
                "v=spf1 -all",
                observedAt);
        }
    }

    @Configuration
    @EnableAutoConfiguration(exclude = {RedisAutoConfiguration.class, RedisRepositoriesAutoConfiguration.class})
    @EntityScan(basePackageClasses = {
        OrganizationJpaEntity.class,
        UserJpaEntity.class,
        ConsentJpaEntity.class,
        DomainJpaEntity.class,
        ObservationEvidenceJpaEntity.class,
        ReputationObservationJpaEntity.class,
        DmarcReportReceiptJpaEntity.class,
        DmarcAggregateEvidenceJpaEntity.class,
        HealthSignalJpaEntity.class,
        IncidentEntity.class,
        HealthChangeEventEntity.class
    })
    @EnableJpaRepositories(basePackageClasses = {
        JpaOrganizationRepository.class,
        JpaUserRepository.class,
        JpaConsentRepository.class,
        JpaDomainRepository.class,
        JpaObservationEvidenceRepository.class,
        JpaReputationObservationRepository.class,
        JpaDmarcReportReceiptRepository.class,
        JpaDmarcAggregateEvidenceRepository.class,
        JpaHealthSignalRepository.class,
        SpringDataIncidentRepository.class,
        SpringDataHealthChangeEventRepository.class
    })
    @Import({
        JpaOrganizationRepositoryAdapter.class,
        JpaUserRepositoryAdapter.class,
        JpaConsentRepositoryAdapter.class,
        JpaDomainRepositoryAdapter.class,
        JpaObservationEvidenceRepositoryAdapter.class,
        JpaReputationObservationRepositoryAdapter.class,
        JpaDmarcReportReceiptRepositoryAdapter.class,
        JpaDmarcAggregateEvidenceRepositoryAdapter.class,
        JpaHealthSignalRepositoryAdapter.class,
        JpaIncidentRepositoryAdapter.class,
        JpaHealthChangeEventRepositoryAdapter.class
    })
    static class TestConfig {

        @Bean
        Clock clock() {
            return Clock.systemUTC();
        }

        @Bean
        SequenceResolver sequenceResolver() {
            return new SequenceResolver();
        }

        @Bean
        @Primary
        DnsResolver fakeDnsResolver(SequenceResolver sequence) {
            return sequence;
        }

        @Bean
        @Primary
        GooglePostmasterPort fakeGooglePostmasterPort() {
            return domainName -> Optional.of(
                new ReputationProviderObservation("it-postmaster", BASE));
        }

        @Bean
        @Primary
        MicrosoftSndsPort fakeMicrosoftSndsPort() {
            return domainName -> Optional.empty();
        }

        @Bean
        OrganizationService organizationService(OrganizationRepository repository, Clock clock) {
            return new OrganizationService(repository, clock);
        }

        @Bean
        DomainService domainService(DomainRepository repository, OrganizationRepository organizations, Clock clock) {
            return new DomainService(repository, organizations, clock);
        }

        @Bean
        ConsentService consentService(ConsentRepository consentRepository, DomainRepository domainRepository, UserRepository userRepository) {
            return new ConsentService(consentRepository, domainRepository, userRepository);
        }

        @Bean
        DnsObservationService dnsObservationService(DnsResolver resolver, DomainRepository domains, ConsentService consentService) {
            return new DnsObservationService(resolver, domains, consentService);
        }

        @Bean
        ObservationEvidenceService observationEvidenceService(ObservationEvidenceRepository repository,
                                                              DomainRepository domains,
                                                              DnsObservationService dnsObservationService) {
            return new ObservationEvidenceService(repository, domains, dnsObservationService);
        }

        @Bean
        ReputationObservationService reputationObservationService(ReputationObservationRepository repository,
                                                                  DomainRepository domains,
                                                                  GooglePostmasterPort postmaster,
                                                                  MicrosoftSndsPort snds) {
            return new ReputationObservationService(repository, domains, postmaster, snds);
        }

        @Bean
        Rfc7489DmarcAggregateXmlParser rfc7489DmarcAggregateXmlParser() {
            return new Rfc7489DmarcAggregateXmlParser();
        }

        @Bean
        DmarcReportReceiveService dmarcReportReceiveService(DmarcReportReceiptRepository receipts,
                                                            DomainRepository domains) {
            return new DmarcReportReceiveService(receipts, domains);
        }

        @Bean
        DmarcReportParseService dmarcReportParseService(DmarcReportReceiptRepository receipts,
                                                        Rfc7489DmarcAggregateXmlParser parser) {
            return new DmarcReportParseService(receipts, parser);
        }

        @Bean
        DmarcAggregateEvidenceService dmarcAggregateEvidenceService(DmarcAggregateEvidenceRepository repository) {
            return new DmarcAggregateEvidenceService(repository);
        }

        @Bean
        HealthSignalService healthSignalService(HealthSignalRepository signals,
                                                DomainRepository domains,
                                                ObservationEvidenceRepository observationEvidence,
                                                DmarcAggregateEvidenceRepository aggregateEvidence,
                                                ReputationObservationRepository reputationObservations,
                                                Clock clock) {
            return new HealthSignalService(signals, domains, observationEvidence, aggregateEvidence,
                    reputationObservations, clock);
        }

        @Bean
        AlertGenerationService alertGenerationService() {
            return new AlertGenerationService();
        }

        @Bean
        IncidentService incidentService(com.mailsent.monitoring.application.IncidentRepository incidents) {
            return new IncidentService(incidents);
        }

        @Bean
        HealthChangeDetector healthChangeDetector(HealthChangeEventRepository events,
                                                  IncidentService incidentService,
                                                  AlertGenerationService alertGenerationService) {
            return new HealthChangeDetector(events, incidentService, alertGenerationService);
        }
    }
}
