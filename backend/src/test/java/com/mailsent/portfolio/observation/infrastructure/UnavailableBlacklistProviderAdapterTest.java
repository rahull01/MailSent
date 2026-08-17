package com.mailsent.portfolio.observation.infrastructure;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Production blacklist provider adapters report unavailability and do not invent
 * HTTP APIs, DNS zones, credentials, or fake blacklist facts.
 */
class UnavailableBlacklistProviderAdapterTest {

    @Test
    void spamhausAdapterReturnsEmpty() {
        assertTrue(new UnavailableSpamhausAdapter().query("example.com").isEmpty());
    }

    @Test
    void barracudaAdapterReturnsEmpty() {
        assertTrue(new UnavailableBarracudaAdapter().query("example.com").isEmpty());
    }

    @Test
    void surblAdapterReturnsEmpty() {
        assertTrue(new UnavailableSurblAdapter().query("example.com").isEmpty());
    }

    @Test
    void uriblAdapterReturnsEmpty() {
        assertTrue(new UnavailableUriblAdapter().query("example.com").isEmpty());
    }
}
