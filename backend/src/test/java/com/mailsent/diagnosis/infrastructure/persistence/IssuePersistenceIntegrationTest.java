package com.mailsent.diagnosis.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mailsent.diagnosis.domain.Issue;
import com.mailsent.diagnosis.domain.IssueStatus;
import com.mailsent.diagnosis.domain.RootCause;
import com.mailsent.diagnosis.domain.Sender;
import com.mailsent.identity.authentication.infrastructure.persistence.UserJpaEntity;
import com.mailsent.identity.consent.infrastructure.persistence.ConsentJpaEntity;
import com.mailsent.portfolio.domain.infrastructure.persistence.DomainJpaEntity;
import com.mailsent.portfolio.observation.infrastructure.persistence.DmarcAggregateEvidenceJpaEntity;
import com.mailsent.portfolio.observation.infrastructure.persistence.DmarcReportReceiptJpaEntity;
import com.mailsent.portfolio.observation.infrastructure.persistence.HealthSignalJpaEntity;
import com.mailsent.portfolio.observation.infrastructure.persistence.ObservationEvidenceJpaEntity;
import com.mailsent.portfolio.observation.infrastructure.persistence.ReputationObservationJpaEntity;
import com.mailsent.portfolio.organization.infrastructure.persistence.OrganizationJpaEntity;

/**
 * PostgreSQL Integration Test for Issue persistence (Flyway V19 / DIAG-004).
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("it")
@ContextConfiguration(classes = IssuePersistenceIntegrationTest.TestConfig.class)
@EnabledIfSystemProperty(named = "mailsent.it.postgres", matches = "true")
class IssuePersistenceIntegrationTest {

    @Configuration
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
            IssueJpaEntity.class
    })
    @EnableJpaRepositories(basePackageClasses = {
            SpringDataIssueRepository.class
    })
    @Import({
            JpaIssueRepositoryAdapter.class,
            ObjectMapper.class
    })
    static class TestConfig {}

    @Autowired
    private JpaIssueRepositoryAdapter adapter;

    @Autowired
    private TestEntityManager entityManager;

    private String orgId;
    private String domainId;

    @BeforeEach
    void setUp() {
        orgId = "org-" + System.currentTimeMillis();
        domainId = "dom-" + System.currentTimeMillis();

        OrganizationJpaEntity org = new OrganizationJpaEntity(orgId, "Acme Corp", Instant.now(), com.mailsent.portfolio.organization.domain.Organization.Status.ACTIVE, null);
        entityManager.persistAndFlush(org);

        DomainJpaEntity domain = new DomainJpaEntity(domainId, orgId, "test-" + System.currentTimeMillis() + ".com", Instant.now());
        entityManager.persistAndFlush(domain);
    }

    @Test
    void shouldPersistAndReloadIssueWithFullLineage() {
        Instant now = Instant.now();
        Issue issue = Issue.discover(
                orgId, domainId, "test.com",
                RootCause.DMARC_REJECT,
                new Sender("google_workspace", "Google Workspace"),
                "hs-1", List.of("ev-1", "ev-2"),
                now
        );

        Issue saved = adapter.save(issue);
        assertThat(saved.id()).isEqualTo(issue.id());

        Optional<Issue> reloaded = adapter.findById(saved.id());
        assertThat(reloaded).isPresent();
        Issue entity = reloaded.get();
        assertThat(entity.organizationId()).isEqualTo(orgId);
        assertThat(entity.domainId()).isEqualTo(domainId);
        assertThat(entity.rootCause()).isEqualTo(RootCause.DMARC_REJECT);
        assertThat(entity.sender()).isEqualTo(new Sender("google_workspace", "Google Workspace"));
        assertThat(entity.status()).isEqualTo(IssueStatus.DISCOVERED);
        assertThat(entity.originHealthSignalId()).isEqualTo("hs-1");
        assertThat(entity.originDmarcAggregateEvidenceIds()).containsExactly("ev-1", "ev-2");
        assertThat(entity.latestHealthSignalId()).isEqualTo("hs-1");
        assertThat(entity.latestDmarcAggregateEvidenceIds()).containsExactly("ev-1", "ev-2");
    }

    @Test
    void shouldEnforcePartialUniqueConstraintOnActiveIssues() {
        Instant now = Instant.now();
        Issue issue1 = Issue.discover(
                orgId, domainId, "test.com",
                RootCause.DMARC_REJECT,
                new Sender("google_workspace", "Google Workspace"),
                "hs-1", List.of("ev-1"),
                now
        );
        adapter.save(issue1);

        Issue duplicateActive = Issue.discover(
                orgId, domainId, "test.com",
                RootCause.DMARC_REJECT,
                new Sender("google_workspace", "Google Workspace"),
                "hs-2", List.of("ev-2"),
                now
        );

        // Attempting to save a duplicate active issue must violate the partial unique constraint uk_issues_active_fingerprint
        assertThatThrownBy(() -> {
            adapter.save(duplicateActive);
            entityManager.flush();
        }).satisfies(thrown -> {
            assertThat(thrown).isInstanceOfAny(
                    org.springframework.dao.DataIntegrityViolationException.class,
                    org.hibernate.exception.ConstraintViolationException.class,
                    jakarta.persistence.PersistenceException.class
            );
        });
    }

    @Test
    void shouldAllowNewIssueWhenPreviousIsClosed() {
        Instant now = Instant.now();
        Issue issue1 = Issue.discover(
                orgId, domainId, "test.com",
                RootCause.DMARC_REJECT,
                new Sender("google_workspace", "Google Workspace"),
                "hs-1", List.of("ev-1"),
                now
        );
        issue1.close(now);
        adapter.save(issue1);
        entityManager.flush();

        // Previous is CLOSED, so creating a new active issue with the same fingerprint is permitted
        Issue newIncident = Issue.discover(
                orgId, domainId, "test.com",
                RootCause.DMARC_REJECT,
                new Sender("google_workspace", "Google Workspace"),
                "hs-2", List.of("ev-2"),
                now
        );

        Issue savedNew = adapter.save(newIncident);
        entityManager.flush();
        assertThat(savedNew.id()).isNotEqualTo(issue1.id());
    }

    @Test
    void shouldIsolateIssuesByOrganization() {
        String org2 = "org-other-" + System.currentTimeMillis();
        OrganizationJpaEntity otherOrg = new OrganizationJpaEntity(org2, "Other Org", Instant.now(), com.mailsent.portfolio.organization.domain.Organization.Status.ACTIVE, null);
        entityManager.persistAndFlush(otherOrg);

        String dom2 = "dom-other-" + System.currentTimeMillis();
        DomainJpaEntity otherDomain = new DomainJpaEntity(dom2, org2, "other-" + System.currentTimeMillis() + ".com", Instant.now());
        entityManager.persistAndFlush(otherDomain);

        Issue issue1 = Issue.discover(orgId, domainId, "dom1.com", RootCause.SPF_ALIGNMENT_FAILURE, null, "hs-1", List.of("ev-1"), Instant.now());
        Issue issue2 = Issue.discover(org2, dom2, "dom2.com", RootCause.SPF_ALIGNMENT_FAILURE, null, "hs-2", List.of("ev-2"), Instant.now());

        adapter.save(issue1);
        adapter.save(issue2);
        entityManager.flush();

        List<Issue> org1Issues = adapter.findByOrganizationId(orgId);
        List<Issue> org2Issues = adapter.findByOrganizationId(org2);

        assertThat(org1Issues).hasSize(1).extracting(Issue::id).containsExactly(issue1.id());
        assertThat(org2Issues).hasSize(1).extracting(Issue::id).containsExactly(issue2.id());
    }

    @Test
    void shouldUpdateLatestLineageWhileKeepingOriginLineageUntouched() {
        Instant t0 = Instant.parse("2026-08-19T10:00:00Z");
        Instant t1 = Instant.parse("2026-08-19T11:00:00Z");

        Issue issue = Issue.discover(orgId, domainId, "dom.com", RootCause.DMARC_QUARANTINE, null, "hs-1", List.of("ev-1"), t0);
        Issue saved = adapter.save(issue);
        entityManager.flush();

        saved.recordSubsequentObservation("hs-2", List.of("ev-2", "ev-3"), t1);
        adapter.save(saved);
        entityManager.flush();

        Issue reloaded = adapter.findById(saved.id()).orElseThrow();
        assertThat(reloaded.originHealthSignalId()).isEqualTo("hs-1");
        assertThat(reloaded.originDmarcAggregateEvidenceIds()).containsExactly("ev-1");
        assertThat(reloaded.latestHealthSignalId()).isEqualTo("hs-2");
        assertThat(reloaded.latestDmarcAggregateEvidenceIds()).containsExactly("ev-2", "ev-3");
        assertThat(reloaded.updatedAt()).isEqualTo(t1);
    }
}
