package com.mailsent.portfolio.domain.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.mailsent.identity.team.application.AuthorizationService;
import com.mailsent.portfolio.domain.application.DomainImportService;
import com.mailsent.portfolio.domain.application.DomainQueryService;
import com.mailsent.portfolio.domain.application.DomainRepository;
import com.mailsent.portfolio.domain.application.DomainService;
import com.mailsent.portfolio.domain.infrastructure.InMemoryDomainRepository;
import com.mailsent.portfolio.organization.application.OrganizationRepository;

/**
 * Configuration and dependency wiring for the sending-domain module (ORG-003, DOM-001, DOM-002, DOM-003).
 *
 * <p>There is exactly one {@link DomainRepository} bean per profile: the PostgreSQL
 * adapter ({@code JpaDomainRepositoryAdapter}) is component-scanned under {@code !test};
 * the hermetic {@link InMemoryDomainRepository} is declared here under the {@code test}
 * profile. The {@link DomainService}, {@link DomainImportService}, and {@link DomainQueryService}
 * complete the wiring.</p>
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

    @Bean
    DomainImportService domainImportService(DomainService domains, OrganizationRepository organizations,
                                            com.mailsent.identity.team.application.AuthorizationService authorizationService) {
        return new DomainImportService(domains, organizations, authorizationService);
    }

    @Bean
    DomainQueryService domainQueryService(DomainRepository repository,
                                          OrganizationRepository organizations,
                                          AuthorizationService authorizationService) {
        return new DomainQueryService(repository, organizations, authorizationService);
    }
}