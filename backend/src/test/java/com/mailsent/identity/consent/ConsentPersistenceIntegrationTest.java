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
        Consent consent = Consent.grant("org-1", "domain-1", "user-1", ActionType.OBSERVE_DOMAIN_INFRASTRUCTURE);
        consentRepository.save(consent);

        Optional<Consent> retrieved = consentRepository.findById(consent.getId());
        assertTrue(retrieved.isPresent());
        assertEquals("org-1", retrieved.get().getOrganizationId());
        assertEquals("domain-1", retrieved.get().getDomainId());
        assertEquals(Status.GRANTED, retrieved.get().getStatus());
    }

    @Test
    void findByDomainIdAndActionTypeAndStatus() {
        Consent consent = Consent.grant("org-2", "domain-2", "user-2", ActionType.OBSERVE_DOMAIN_INFRASTRUCTURE);
        consentRepository.save(consent);

        Optional<Consent> retrieved = consentRepository.findByDomainIdAndActionTypeAndStatus("domain-2", ActionType.OBSERVE_DOMAIN_INFRASTRUCTURE, Status.GRANTED);
        assertTrue(retrieved.isPresent());
        assertEquals(consent.getId(), retrieved.get().getId());
    }
}
