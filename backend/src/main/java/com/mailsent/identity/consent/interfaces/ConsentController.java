package com.mailsent.identity.consent.interfaces;

import com.mailsent.identity.consent.application.ConsentService;
import com.mailsent.identity.consent.domain.ActionType;
import com.mailsent.identity.consent.domain.Consent;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/consents")
public class ConsentController {

    private final ConsentService consentService;

    public ConsentController(ConsentService consentService) {
        this.consentService = consentService;
    }

    public static class GrantConsentRequest {
        public String organizationId;
        public String domainId;
        public String grantedByUserId;
        public ActionType actionType;
    }

    public static class RevokeConsentRequest {
        public String organizationId;
        public String revokedByUserId;
    }

    @PostMapping
    public ResponseEntity<Consent> grantConsent(@RequestBody GrantConsentRequest request) {
        Consent consent = consentService.grantConsent(
            request.organizationId,
            request.domainId,
            request.grantedByUserId,
            request.actionType
        );
        return ResponseEntity.ok(consent);
    }

    @DeleteMapping("/{consentId}")
    public ResponseEntity<Consent> revokeConsent(@PathVariable String consentId, @RequestBody RevokeConsentRequest request) {
        Consent consent = consentService.revokeConsent(
            consentId,
            request.organizationId,
            request.revokedByUserId
        );
        return ResponseEntity.ok(consent);
    }
}
