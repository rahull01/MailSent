package com.mailsent.common.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import javax.crypto.SecretKey;

import org.junit.jupiter.api.Test;

class SecurityHardeningTest {

    @Test
    void verifiesHardenedConsentFlow() {
        String payload = "{\"action\":\"FLATTEN_SPF\",\"record\":\"v=spf1 include:_spf.google.com ~all\"}";
        HardenedConsentRecord consent = HardenedConsentRecord.grant(
                "org-1",
                "usr-1",
                "FLATTEN_SPF",
                payload,
                "198.51.100.5",
                "Mozilla/5.0",
                Instant.now()
        );

        assertNotNull(consent.id());
        assertEquals("org-1", consent.organizationId());
        assertTrue(consent.verifyPayload(payload));
        assertFalse(consent.verifyPayload("tampered payload"));
    }

    @Test
    void enforcesPrivilegeMatrix() {
        // Viewer can only view health
        assertTrue(PrivilegeEnforcer.hasPermission(PrivilegeEnforcer.Role.VIEWER, PrivilegeEnforcer.Permission.VIEW_HEALTH));
        assertFalse(PrivilegeEnforcer.hasPermission(PrivilegeEnforcer.Role.VIEWER, PrivilegeEnforcer.Permission.GRANT_CONSENT_FIX));

        assertThrows(SecurityException.class, () ->
                PrivilegeEnforcer.checkPermission(PrivilegeEnforcer.Role.VIEWER, PrivilegeEnforcer.Permission.DELETE_DOMAIN)
        );

        // Owner has all permissions
        assertTrue(PrivilegeEnforcer.hasPermission(PrivilegeEnforcer.Role.OWNER, PrivilegeEnforcer.Permission.DELETE_DOMAIN));
    }

    @Test
    void validatesContractInputs() {
        assertTrue(ContractInputValidator.isValidDomain("acme-corp.com"));
        assertTrue(ContractInputValidator.isValidDomain("mail.sub.domain.co.uk"));
        assertFalse(ContractInputValidator.isValidDomain("-bad.domain"));
        assertFalse(ContractInputValidator.isValidDomain("bad_domain.com"));

        assertTrue(ContractInputValidator.isValidIpv4("192.0.2.1"));
        assertFalse(ContractInputValidator.isValidIpv4("999.0.0.1"));

        assertEquals("abc", ContractInputValidator.sanitizeString("  abcdef  ", 3));
    }

    @Test
    void encryptsAndDecryptsPayloads() {
        SecretKey key = PayloadEncryptor.generateKey();
        String secret = "dmarc_report_api_token_secret_12345";

        String encrypted = PayloadEncryptor.encrypt(secret, key);
        assertNotNull(encrypted);

        String decrypted = PayloadEncryptor.decrypt(encrypted, key);
        assertEquals(secret, decrypted);
    }
}
