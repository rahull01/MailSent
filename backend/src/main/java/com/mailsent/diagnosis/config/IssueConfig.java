package com.mailsent.diagnosis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.mailsent.diagnosis.domain.IssueRepository;
import com.mailsent.diagnosis.infrastructure.persistence.InMemoryIssueRepository;

/**
 * Spring configuration for DIAG-004 Issue persistence wiring.
 *
 * <p>Registers {@link InMemoryIssueRepository} as the {@link IssueRepository}
 * bean for the {@code test} profile so that Spring test slices and application
 * context tests hydrate cleanly without requiring live PostgreSQL.
 */
@Configuration
public class IssueConfig {

    @Bean
    @Profile("test")
    public IssueRepository inMemoryIssueRepository() {
        return new InMemoryIssueRepository();
    }
}
