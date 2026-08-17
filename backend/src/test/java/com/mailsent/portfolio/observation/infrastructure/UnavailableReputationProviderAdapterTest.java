package com.mailsent.portfolio.observation.infrastructure;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Production provider adapters report unavailability and do not invent HTTP
 * APIs, credentials, or fake reputation facts.
 */
class UnavailableReputationProviderAdapterTest {

    @Test
    void googlePostmasterAdapterReturnsEmpty() {
        assertTrue(new UnavailableGooglePostmasterAdapter().query("example.com").isEmpty());
    }

    @Test
    void microsoftSndsAdapterReturnsEmpty() {
        assertTrue(new UnavailableMicrosoftSndsAdapter().query("example.com").isEmpty());
    }
}
