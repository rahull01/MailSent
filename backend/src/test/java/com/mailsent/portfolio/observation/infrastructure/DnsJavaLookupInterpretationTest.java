package com.mailsent.portfolio.observation.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;

/**
 * Deterministic tests for unavailable-versus-empty lookup interpretation
 * (copied INF-007 semantics, without editing DnsJavaDnsHealthObserver).
 */
class DnsJavaLookupInterpretationTest {

    @Test
    void successfulEmptyIsEmptyArrayNotUnavailable() {
        Record[] interpreted = DnsJavaLookupInterpretation.interpretLookupResult(
            Lookup.SUCCESSFUL, new Record[0]);
        assertEquals(0, interpreted.length);
    }

    @Test
    void successfulNullRecordsBecomeEmptyArray() {
        Record[] interpreted = DnsJavaLookupInterpretation.interpretLookupResult(Lookup.SUCCESSFUL, null);
        assertEquals(0, interpreted.length);
    }

    @Test
    void typeNotFoundAndNxdomainAreSuccessfulEmpty() {
        assertEquals(0, DnsJavaLookupInterpretation.interpretLookupResult(Lookup.TYPE_NOT_FOUND, null).length);
        assertEquals(0, DnsJavaLookupInterpretation.interpretLookupResult(Lookup.HOST_NOT_FOUND, null).length);
    }

    @Test
    void unrecoverableAndTryAgainAreUnavailable() {
        assertNull(DnsJavaLookupInterpretation.interpretLookupResult(Lookup.UNRECOVERABLE, null));
        assertNull(DnsJavaLookupInterpretation.interpretLookupResult(Lookup.TRY_AGAIN, new Record[0]));
    }

    @Test
    void normalizeStripsTrailingDot() {
        assertEquals("mail.example.com", DnsJavaLookupInterpretation.normalize("mail.example.com."));
        assertEquals("mail.example.com", DnsJavaLookupInterpretation.normalize("mail.example.com"));
    }

    @Test
    void unknownResultIsUnavailable() {
        assertNull(DnsJavaLookupInterpretation.interpretLookupResult(99, new Record[0]));
    }
}
