package com.mailsent.portfolio.domain.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.mailsent.portfolio.domain.application.DomainRepository;
import com.mailsent.portfolio.domain.application.DomainService;
import com.mailsent.portfolio.domain.infrastructure.InMemoryDomainRepository;
import com.mailsent.portfolio.organization.application.OrganizationRepository;

/**
 * Configuration and dependency wiring for the sending-domain module (ORG-003).
 *
 * <p>There is exactly one {@link DomainRepository} bean per profile: the PostgreSQL
 * adapter ({@code JpaDomainRepositoryAdapter}) is component-scanned under {@code !test};
 * the hermetic {@link InMemoryDomainRepository} is declared here under the {@code test}
 * profile. The {@link DomainService} completes the wiring, taking the shared
 * {@link OrganizationRepository} (to verify the owning organization exists) and the
 * shared {@link Clock}.</p>
 *
 * <p>ORG-003 is domain/application/persistence only — the frozen documentation defines no
 * domain API contract, so no controller is registered.</p>
 */
@Configuration
public class DomainConfig {

    @Bean
    @Profile("test")
    DomainRepository inMemoryDomainRepository() {
        return new InMemoryDomainRepository();
    }

    @Bean
    DomainService domainService(DomainRepository repository, OrganizationRepository organizations, Clock clock) {
        return new DomainService(repository, organizations, clock);
    }
}