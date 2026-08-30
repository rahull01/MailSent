package com.mailsent.identity.consent.interfaces;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mailsent.common.error.GlobalExceptionHandler;
import com.mailsent.common.security.SessionAuthenticationFilter;
import com.mailsent.identity.authentication.domain.Email;
import com.mailsent.identity.authentication.domain.Role;
import com.mailsent.identity.authentication.domain.Session;
import com.mailsent.identity.authentication.domain.SessionToken;
import com.mailsent.identity.authentication.domain.User;
import com.mailsent.identity.authentication.infrastructure.InMemorySessionRepository;
import com.mailsent.identity.authentication.infrastructure.InMemoryUserRepository;
import com.mailsent.identity.authentication.infrastructure.SecureSessionTokenFactory;
import com.mailsent.identity.consent.application.ConsentService;
import com.mailsent.identity.consent.domain.ActionType;
import com.mailsent.identity.consent.domain.Consent;
import com.mailsent.identity.consent.infrastructure.persistence.InMemoryConsentRepository;
import com.mailsent.portfolio.domain.application.DomainRepository;
import com.mailsent.portfolio.domain.domain.Domain;
import com.mailsent.portfolio.domain.infrastructure.InMemoryDomainRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ConsentControllerSecurityTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private InMemorySessionRepository sessionRepository;
    private InMemoryUserRepository userRepository;
    private InMemoryConsentRepository consentRepository;
    private DomainRepository domainRepository;
    private SecureSessionTokenFactory tokenFactory;
    private Clock fixedClock;
    private Instant now;

    @BeforeEach
    void setUp() {
        sessionRepository = new InMemorySessionRepository();
        userRepository = new InMemoryUserRepository();
        consentRepository = new InMemoryConsentRepository();
        domainRepository = new InMemoryDomainRepository();
        tokenFactory = new SecureSessionTokenFactory();
        objectMapper = new ObjectMapper().findAndRegisterModules();
        now = Instant.parse("2026-08-20T12:00:00Z");
        fixedClock = Clock.fixed(now, ZoneOffset.UTC);

        ConsentService consentService = new ConsentService(consentRepository, domainRepository, userRepository);
        ConsentController consentController = new ConsentController(consentService);

        SessionAuthenticationFilter filter = new SessionAuthenticationFilter(
            sessionRepository,
            userRepository,
            Optional.of(fixedClock),
            objectMapper
        );

        mockMvc = MockMvcBuilders.standaloneSetup(consentController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .addFilters(filter)
            .build();
    }

    private SessionToken createAuthenticatedSession(String userId, String email, String orgId, Role role) {
        User user = User.restore(userId, new Email(email), "hashed_pw", role, now, orgId);
        userRepository.save(user);

        SessionToken token = tokenFactory.randomToken();
        Session session = Session.issue(user.id(), token, now, Duration.ofHours(24));
        sessionRepository.save(session);
        return token;
    }

    @Test
    void grantConsent_Unauthenticated_Returns401Unauthorized() throws Exception {
        GrantConsentRequest request = new GrantConsentRequest("dom-1", ActionType.OBSERVE_DOMAIN_INFRASTRUCTURE);

        mockMvc.perform(post("/api/v1/consents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("AUTH_REQUIRED"));
    }

    @Test
    void grantConsent_IDOR_AttackerCannotGrantConsentForOtherOrganization() throws Exception {
        // Domain belongs to Org 1
        Domain domainOrg1 = Domain.restore("dom-1", "org-1", "example.com", now);
        domainRepository.save(domainOrg1);

        // Attacker is authenticated in Org 2 with OWNER role
        SessionToken attackerToken = createAuthenticatedSession("attacker-1", "attacker@org2.com", "org-2", Role.OWNER);

        GrantConsentRequest request = new GrantConsentRequest("dom-1", ActionType.OBSERVE_DOMAIN_INFRASTRUCTURE);

        mockMvc.perform(post("/api/v1/consents")
                .header("Authorization", "Bearer " + attackerToken.rawToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Domain does not belong to the organization"));
    }

    @Test
    void grantConsent_ViewerRole_Returns403Forbidden() throws Exception {
        Domain domain = Domain.restore("dom-1", "org-1", "example.com", now);
        domainRepository.save(domain);

        // User belongs to Org 1 but has VIEWER role
        SessionToken viewerToken = createAuthenticatedSession("viewer-1", "viewer@org1.com", "org-1", Role.VIEWER);

        GrantConsentRequest request = new GrantConsentRequest("dom-1", ActionType.OBSERVE_DOMAIN_INFRASTRUCTURE);

        mockMvc.perform(post("/api/v1/consents")
                .header("Authorization", "Bearer " + viewerToken.rawToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value("INSUFFICIENT_PERMISSIONS"));
    }

    @Test
    void grantConsent_OwnerOrAnalystRole_Returns201CreatedWithConsentResponse() throws Exception {
        Domain domain = Domain.restore("dom-1", "org-1", "example.com", now);
        domainRepository.save(domain);

        SessionToken ownerToken = createAuthenticatedSession("owner-1", "owner@org1.com", "org-1", Role.OWNER);

        GrantConsentRequest request = new GrantConsentRequest("dom-1", ActionType.OBSERVE_DOMAIN_INFRASTRUCTURE);

        mockMvc.perform(post("/api/v1/consents")
                .header("Authorization", "Bearer " + ownerToken.rawToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.organizationId").value("org-1"))
            .andExpect(jsonPath("$.domainId").value("dom-1"))
            .andExpect(jsonPath("$.grantedByUserId").value("owner-1"))
            .andExpect(jsonPath("$.actionType").value("OBSERVE_DOMAIN_INFRASTRUCTURE"))
            .andExpect(jsonPath("$.status").value("GRANTED"));
    }

    @Test
    void revokeConsent_Unauthenticated_Returns401Unauthorized() throws Exception {
        mockMvc.perform(delete("/api/v1/consents/consent-1"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("AUTH_REQUIRED"));
    }

    @Test
    void revokeConsent_IDOR_AttackerCannotRevokeOtherOrganizationConsent() throws Exception {
        // Active consent belongs to Org 1
        Consent consentOrg1 = Consent.grant("org-1", "dom-1", "user-1", ActionType.OBSERVE_DOMAIN_INFRASTRUCTURE);
        consentRepository.save(consentOrg1);

        // Attacker is in Org 2 with OWNER role
        SessionToken attackerToken = createAuthenticatedSession("attacker-2", "attacker@org2.com", "org-2", Role.OWNER);

        mockMvc.perform(delete("/api/v1/consents/" + consentOrg1.getId())
                .header("Authorization", "Bearer " + attackerToken.rawToken()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Consent does not belong to the organization"));
    }

    @Test
    void revokeConsent_OwnerOrAnalystRole_Returns200Ok() throws Exception {
        Consent consent = Consent.grant("org-1", "dom-1", "owner-1", ActionType.OBSERVE_DOMAIN_INFRASTRUCTURE);
        consentRepository.save(consent);

        SessionToken ownerToken = createAuthenticatedSession("owner-1", "owner@org1.com", "org-1", Role.OWNER);

        mockMvc.perform(delete("/api/v1/consents/" + consent.getId())
                .header("Authorization", "Bearer " + ownerToken.rawToken()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("REVOKED"))
            .andExpect(jsonPath("$.organizationId").value("org-1"));
    }

    @Test
    void grantConsent_DuplicateActiveConsent_Returns409Conflict() throws Exception {
        Domain domain = Domain.restore("dom-1", "org-1", "example.com", now);
        domainRepository.save(domain);

        Consent existing = Consent.grant("org-1", "dom-1", "owner-1", ActionType.OBSERVE_DOMAIN_INFRASTRUCTURE);
        consentRepository.save(existing);

        SessionToken ownerToken = createAuthenticatedSession("owner-1", "owner@org1.com", "org-1", Role.OWNER);

        GrantConsentRequest request = new GrantConsentRequest("dom-1", ActionType.OBSERVE_DOMAIN_INFRASTRUCTURE);

        mockMvc.perform(post("/api/v1/consents")
                .header("Authorization", "Bearer " + ownerToken.rawToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("CONSENT_ALREADY_EXISTS"));
    }
}
