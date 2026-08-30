package com.mailsent.identity.consent.application;

import com.mailsent.identity.consent.domain.ActionType;
import com.mailsent.identity.consent.domain.Consent;
import com.mailsent.identity.consent.domain.Status;
import com.mailsent.identity.authentication.application.UserRepository;
import com.mailsent.identity.authentication.domain.User;
import com.mailsent.portfolio.domain.application.DomainRepository;
import com.mailsent.portfolio.domain.domain.Domain;
import java.time.Instant;

public class ConsentService {
    private final ConsentRepository consentRepository;
    private final DomainRepository domainRepository;
    private final UserRepository userRepository;

    public ConsentService(ConsentRepository consentRepository, DomainRepository domainRepository, UserRepository userRepository) {
        this.consentRepository = consentRepository;
        this.domainRepository = domainRepository;
        this.userRepository = userRepository;
    }

    public Consent grantConsent(String organizationId, String domainId, String grantedByUserId, ActionType actionType) {
        Domain domain = domainRepository.findById(domainId)
            .orElseThrow(() -> new IllegalArgumentException("Domain not found"));
        
        if (!domain.organizationId().equals(organizationId)) {
            throw new IllegalArgumentException("Domain does not belong to the organization");
        }

        User user = userRepository.findById(grantedByUserId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
        if (!user.organizationId().equals(organizationId)) {
            throw new IllegalArgumentException("User does not belong to the organization");
        }
        
        if (!user.role().canChange()) {
            throw new SecurityException("User role does not have permission to grant consent");
        }

        consentRepository.findByDomainIdAndActionTypeAndStatus(domainId, actionType, Status.GRANTED)
            .ifPresent(c -> {
                throw new ConsentAlreadyExistsException("Active consent already exists for this domain and action type");
            });

        Consent consent = Consent.grant(organizationId, domainId, grantedByUserId, actionType);
        try {
            consentRepository.save(consent);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new ConsentAlreadyExistsException("Active consent already exists for this domain and action type");
        }
        return consent;
    }

    public Consent revokeConsent(String consentId, String organizationId, String revokedByUserId) {
        Consent consent = consentRepository.findById(consentId)
            .orElseThrow(() -> new IllegalArgumentException("Consent not found"));
        
        if (!consent.getOrganizationId().equals(organizationId)) {
            throw new IllegalArgumentException("Consent does not belong to the organization");
        }

        User user = userRepository.findById(revokedByUserId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
        if (!user.organizationId().equals(organizationId)) {
            throw new IllegalArgumentException("User does not belong to the organization");
        }
        
        if (!user.role().canChange()) {
            throw new SecurityException("User role does not have permission to revoke consent");
        }

        consent.revoke(revokedByUserId, Instant.now());
        consentRepository.save(consent);
        return consent;
    }

    /**
     * Asserts that the domain's owning organization holds a GRANTED consent for the action
     * (INV-02). The gate is tenant-bound (INV-09): a consent granted by another organization
     * never satisfies the check, even for the same domainId.
     */
    public void requireConsent(String organizationId, String domainId, ActionType actionType) {
        consentRepository.findByDomainIdAndActionTypeAndStatus(domainId, actionType, Status.GRANTED)
            .filter(consent -> consent.getOrganizationId().equals(organizationId))
            .orElseThrow(() -> new ConsentRequiredException("Consent is required for this action"));
    }
}
