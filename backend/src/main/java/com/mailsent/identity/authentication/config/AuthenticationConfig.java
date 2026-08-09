package com.mailsent.identity.authentication.config;

import java.time.Clock;
import java.time.Duration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.mailsent.identity.authentication.application.SessionRepository;
import com.mailsent.identity.authentication.application.SignInService;
import com.mailsent.identity.authentication.application.SignUpService;
import com.mailsent.identity.authentication.application.UserRepository;
import com.mailsent.identity.authentication.domain.PasswordHasher;
import com.mailsent.identity.authentication.domain.PasswordPolicy;
import com.mailsent.identity.authentication.domain.Role;
import com.mailsent.identity.authentication.domain.SessionTokenFactory;
import com.mailsent.identity.authentication.infrastructure.InMemorySessionRepository;
import com.mailsent.identity.authentication.infrastructure.InMemoryUserRepository;
import com.mailsent.identity.authentication.infrastructure.Pbkdf2PasswordHasher;
import com.mailsent.identity.authentication.infrastructure.SecureSessionTokenFactory;
import com.mailsent.portfolio.organization.application.OrganizationRepository;

/**
 * Configuration and dependency wiring for the authentication module.
 *
 * <p>Wires the sign-up use case (AUTH-000): the password policy, the PBKDF2 hasher,
 * and the use-case service. There is exactly one {@link UserRepository} bean per
 * profile:</p>
 * <ul>
 *   <li><strong>Production</strong> (any non-test profile) — the PostgreSQL adapter
 *       ({@code JpaUserRepositoryAdapter}, component-scanned under {@code !test}).</li>
 *   <li><strong>Test</strong> — the hermetic {@link InMemoryUserRepository} so the
 *       context loads with no DataSource and tests stay fast and deterministic.</li>
 * </ul>
 *
 * <p>Security policies are externalized via {@link AuthenticationSecurityProperties}
 * (prefix {@code mailsent.auth.authentication}); the defaults are secure and can only
 * be raised, never lowered below the validated floors.</p>
 *
 * <p>The default role assigned at sign up is {@link Role#VIEWER} (least authority);
 * escalation is a governed later concern.</p>
 */
@Configuration
@EnableConfigurationProperties(AuthenticationSecurityProperties.class)
public class AuthenticationConfig {

    @Bean
    PasswordPolicy passwordPolicy(AuthenticationSecurityProperties props) {
        return new PasswordPolicy(props.getPasswordMinLength());
    }

    @Bean
    PasswordHasher passwordHasher(AuthenticationSecurityProperties props) {
        return new Pbkdf2PasswordHasher(props.getPbkdf2Iterations(), props.getPbkdf2SaltBytes());
    }

    @Bean
    @Profile("test")
    UserRepository inMemoryUserRepository() {
        return new InMemoryUserRepository();
    }

    @Bean
    @Profile("test")
    SessionRepository inMemorySessionRepository() {
        return new InMemorySessionRepository();
    }

    @Bean
    SessionTokenFactory sessionTokenFactory() {
        return new SecureSessionTokenFactory();
    }

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    SignUpService signUpService(UserRepository userRepository, OrganizationRepository organizationRepository,
                                PasswordHasher passwordHasher, PasswordPolicy passwordPolicy) {
        return new SignUpService(userRepository, organizationRepository, passwordHasher, passwordPolicy);
    }

    @Bean
    SignInService signInService(UserRepository userRepository, SessionRepository sessionRepository,
                                PasswordHasher passwordHasher, SessionTokenFactory sessionTokenFactory,
                                Clock clock, AuthenticationSecurityProperties props) {
        Duration lifetime = Duration.ofMinutes(props.getSessionLifetimeMinutes());
        return new SignInService(userRepository, sessionRepository, passwordHasher,
            sessionTokenFactory, clock, lifetime);
    }
}