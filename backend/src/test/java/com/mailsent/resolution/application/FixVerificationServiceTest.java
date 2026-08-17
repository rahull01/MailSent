package com.mailsent.resolution.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.mailsent.explanation.domain.Explanation;
import com.mailsent.portfolio.observation.application.HealthSignalService;
import com.mailsent.portfolio.observation.application.ObservationEvidenceService;
import com.mailsent.portfolio.observation.application.ReputationObservationService;
import com.mailsent.portfolio.observation.domain.HealthIndication;
import com.mailsent.portfolio.observation.domain.HealthSignal;

class FixVerificationServiceTest {

    @Test
    void verifiesFixAndReturnsExplanation() {
        ObservationEvidenceService mockEvidence = org.mockito.Mockito.mock(ObservationEvidenceService.class);
        ReputationObservationService mockReputation = org.mockito.Mockito.mock(ReputationObservationService.class);
        HealthSignalService mockSignalService = org.mockito.Mockito.mock(HealthSignalService.class);

        org.mockito.Mockito.when(mockSignalService.derive("example.com"))
            .thenReturn(java.util.Optional.of(HealthSignal.restore("hs-1", "org-1", "dom-1", "example.com",
                HealthIndication.HEALTHY, "oe-1", List.of(), List.of(), Instant.now())));

        FixVerificationService service = new FixVerificationService(mockEvidence, mockReputation, mockSignalService);

        Explanation explanation = service.verifyFix("dom-1", "example.com");

        assertNotNull(explanation);
        assertEquals("dom-1", explanation.domainId());
        
        org.mockito.Mockito.verify(mockEvidence).record("example.com");
        org.mockito.Mockito.verify(mockReputation).record("example.com");
    }

    @Test
    void requiresDomainArgs() {
        FixVerificationService service = new FixVerificationService(null, null, null);
        assertThrows(IllegalArgumentException.class, () -> service.verifyFix(null, "example.com"));
        assertThrows(IllegalArgumentException.class, () -> service.verifyFix("dom-1", null));
    }
}
