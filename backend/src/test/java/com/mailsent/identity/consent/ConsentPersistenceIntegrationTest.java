package com.mailsent.identity.consent;

import com.mailsent.identity.consent.application.ConsentRepository;
import com.mailsent.identity.consent.domain.ActionType;
import com.mailsent.identity.consent.domain.Consent;
import com.mailsent.identity.consent.domain.Status;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("it")
@EnabledIfSystemProperty(named = "mailsent.it.postgres", matches = "true")
class ConsentPersistenceIntegrationTest {

    @Autowired
    private ConsentRepository consentRepository;

    @Test
    void saveAndFindById() {
        String orgId = "org-" + UUID.randomUUID();
        String domainId = "domain-" + UUID.randomUUID();
        String userId = "user-" + UUID.randomUUID();
        Consent consent = Consent.grant(orgId, domainId, userId, ActionType.OBSERVE_DOMAIN_INFRASTRUCTURE);
        consentRepository.save(consent);

        Optional<Consent> retrieved = consentRepository.findById(consent.getId());
        assertTrue(retrieved.isPresent());
        assertEquals(orgId, retrieved.get().getOrganizationId());
        assertEquals(domainId, retrieved.get().getDomainId());
        assertEquals(Status.GRANTED, retrieved.get().getStatus());
    }

    @Test
    void findByDomainIdAndActionTypeAndStatus() {
        String orgId = "org-" + UUID.randomUUID();
        String domainId = "domain-" + UUID.randomUUID();
        String userId = "user-" + UUID.randomUUID();
        Consent consent = Consent.grant(orgId, domainId, userId, ActionType.OBSERVE_DOMAIN_INFRASTRUCTURE);
        consentRepository.save(consent);

        Optional<Consent> retrieved = consentRepository.findByDomainIdAndActionTypeAndStatus(domainId, ActionType.OBSERVE_DOMAIN_INFRASTRUCTURE, Status.GRANTED);
        assertTrue(retrieved.isPresent());
        assertEquals(consent.getId(), retrieved.get().getId());
    }
}
