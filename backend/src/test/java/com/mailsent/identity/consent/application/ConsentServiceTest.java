package com.mailsent.identity.consent.application;

import com.mailsent.identity.consent.domain.ActionType;
import com.mailsent.identity.consent.domain.Consent;
import com.mailsent.identity.consent.domain.Status;
import com.mailsent.identity.consent.infrastructure.persistence.InMemoryConsentRepository;
import com.mailsent.identity.authentication.application.UserRepository;
import com.mailsent.identity.authentication.domain.User;
import com.mailsent.identity.authentication.domain.Email;
import com.mailsent.portfolio.domain.application.DomainRepository;
import com.mailsent.portfolio.domain.domain.Domain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConsentServiceTest {

    private ConsentRepository consentRepository;
    private DomainRepository domainRepository;
    private UserRepository userRepository;
    private ConsentService consentService;

    @BeforeEach
    void setUp() {
        consentRepository = new InMemoryConsentRepository();
        domainRepository = mock(DomainRepository.class);
        userRepository = mock(UserRepository.class);
        consentService = new ConsentService(consentRepository, domainRepository, userRepository);
    }

    @Test
    void grantConsent_Success() {
        String orgId = "org-1";
        String domainId = "domain-1";
        Domain domain = Domain.restore(domainId, orgId, "example.com", Instant.now());
        when(domainRepository.findById(domainId)).thenReturn(Optional.of(domain));
        User user = User.restore("user-1", new Email("test@test.com"), "hash", com.mailsent.identity.authentication.domain.Role.OWNER, Instant.now(), orgId);
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));

        Consent consent = consentService.grantConsent(orgId, domainId, "user-1", ActionType.OBSERVE_DOMAIN_INFRASTRUCTURE);

        assertNotNull(consent.getId());
        assertEquals(orgId, consent.getOrganizationId());
        assertEquals(domainId, consent.getDomainId());
        assertEquals(Status.GRANTED, consent.getStatus());
    }

    @Test
    void grantConsent_DomainNotFound() {
        when(domainRepository.findById("domain-1")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> 
            consentService.grantConsent("org-1", "domain-1", "user-1", ActionType.OBSERVE_DOMAIN_INFRASTRUCTURE));
    }

    @Test
    void grantConsent_WrongOrganization() {
        Domain domain = Domain.restore("domain-1", "org-2", "example.com", Instant.now());
        when(domainRepository.findById("domain-1")).thenReturn(Optional.of(domain));

        assertThrows(IllegalArgumentException.class, () -> 
            consentService.grantConsent("org-1", "domain-1", "user-1", ActionType.OBSERVE_DOMAIN_INFRASTRUCTURE));
    }
    
    @Test
    void grantConsent_Idor_UserWrongOrganization() {
        String orgId = "org-1";
        String domainId = "domain-1";
        Domain domain = Domain.restore(domainId, orgId, "example.com", Instant.now());
        when(domainRepository.findById(domainId)).thenReturn(Optional.of(domain));
        
        User maliciousUser = User.restore("user-2", new Email("bad@test.com"), "hash", com.mailsent.identity.authentication.domain.Role.OWNER, Instant.now(), "org-2");
        when(userRepository.findById("user-2")).thenReturn(Optional.of(maliciousUser));

        assertThrows(IllegalArgumentException.class, () -> 
            consentService.grantConsent(orgId, domainId, "user-2", ActionType.OBSERVE_DOMAIN_INFRASTRUCTURE));
    }

    @Test
    void requireConsent_MissingConsent() {
        assertThrows(ConsentRequiredException.class, () -> 
            consentService.requireConsent("org-1", "domain-1", ActionType.OBSERVE_DOMAIN_INFRASTRUCTURE));
    }

    @Test
    void requireConsent_GrantedConsentExists() {
        Consent consent = Consent.grant("org-1", "domain-1", "user-1", ActionType.OBSERVE_DOMAIN_INFRASTRUCTURE);
        consentRepository.save(consent);

        assertDoesNotThrow(() -> consentService.requireConsent("org-1", "domain-1", ActionType.OBSERVE_DOMAIN_INFRASTRUCTURE));
    }

    @Test
    void requireConsent_RejectsForeignTenantConsent() {
        // INV-09: a consent granted by org-1 never satisfies org-2's gate on the same domain.
        Consent consent = Consent.grant("org-1", "domain-1", "user-1", ActionType.OBSERVE_DOMAIN_INFRASTRUCTURE);
        consentRepository.save(consent);

        assertThrows(ConsentRequiredException.class, () -> 
            consentService.requireConsent("org-2", "domain-1", ActionType.OBSERVE_DOMAIN_INFRASTRUCTURE));
    }

    @Test
    void revokeConsent_Success() {
        Consent consent = Consent.grant("org-1", "domain-1", "user-1", ActionType.OBSERVE_DOMAIN_INFRASTRUCTURE);
        consentRepository.save(consent);
        
        User user = User.restore("user-2", new Email("test2@test.com"), "hash", com.mailsent.identity.authentication.domain.Role.OWNER, Instant.now(), "org-1");
        when(userRepository.findById("user-2")).thenReturn(Optional.of(user));

        Consent revoked = consentService.revokeConsent(consent.getId(), "org-1", "user-2");
        assertEquals(Status.REVOKED, revoked.getStatus());
        assertNotNull(revoked.getRevokedAt());
    }
    
    @Test
    void revokeConsent_Idor_UserWrongOrganization() {
        Consent consent = Consent.grant("org-1", "domain-1", "user-1", ActionType.OBSERVE_DOMAIN_INFRASTRUCTURE);
        consentRepository.save(consent);
        
        User maliciousUser = User.restore("user-2", new Email("bad@test.com"), "hash", com.mailsent.identity.authentication.domain.Role.OWNER, Instant.now(), "org-2");
        when(userRepository.findById("user-2")).thenReturn(Optional.of(maliciousUser));

        assertThrows(IllegalArgumentException.class, () -> 
            consentService.revokeConsent(consent.getId(), "org-1", "user-2"));
    }
    
    @Test
    void requireConsent_RejectsRevokedConsent() {
        Consent consent = Consent.grant("org-1", "domain-1", "user-1", ActionType.OBSERVE_DOMAIN_INFRASTRUCTURE);
        consent.revoke("user-1", Instant.now());
        consentRepository.save(consent);

        assertThrows(ConsentRequiredException.class, () -> 
            consentService.requireConsent("org-1", "domain-1", ActionType.OBSERVE_DOMAIN_INFRASTRUCTURE));
    }

    @Test
    void grantConsent_ConcurrentDuplicate_ThrowsConflict() {
        ConsentRepository mockRepo = mock(ConsentRepository.class);
        ConsentService serviceWithMock = new ConsentService(mockRepo, domainRepository, userRepository);

        String orgId = "org-1";
        String domainId = "domain-1";
        Domain domain = Domain.restore(domainId, orgId, "example.com", Instant.now());
        when(domainRepository.findById(domainId)).thenReturn(Optional.of(domain));
        
        User user = User.restore("user-1", new Email("test@test.com"), "hash", com.mailsent.identity.authentication.domain.Role.OWNER, Instant.now(), orgId);
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        
        doThrow(new org.springframework.dao.DataIntegrityViolationException("duplicate key value violates unique constraint"))
            .when(mockRepo).save(any(Consent.class));

        ConsentAlreadyExistsException exception = assertThrows(ConsentAlreadyExistsException.class, () -> 
            serviceWithMock.grantConsent(orgId, domainId, "user-1", ActionType.OBSERVE_DOMAIN_INFRASTRUCTURE));
            
        assertEquals("Active consent already exists for this domain and action type", exception.getMessage());
    }
}
