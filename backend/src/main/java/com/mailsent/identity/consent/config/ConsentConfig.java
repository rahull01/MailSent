package com.mailsent.identity.consent.config;

import com.mailsent.identity.consent.application.ConsentRepository;
import com.mailsent.identity.consent.application.ConsentService;
import com.mailsent.portfolio.domain.application.DomainRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConsentConfig {

    @Bean
    public ConsentService consentService(ConsentRepository consentRepository, DomainRepository domainRepository,
                                         com.mailsent.identity.authentication.application.UserRepository userRepository) {
        return new ConsentService(consentRepository, domainRepository, userRepository);
    }
}
