package com.mailsent.portfolio.observation.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.xbill.DNS.DClass;
import org.xbill.DNS.DNSKEYRecord;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.MXRecord;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.TXTRecord;

import com.mailsent.portfolio.observation.domain.DnssecObservation;
import com.mailsent.portfolio.observation.domain.DnssecPresence;
import com.mailsent.portfolio.observation.domain.DnssecRecordFact;
import com.mailsent.portfolio.observation.domain.PropagationAnswer;
import com.mailsent.portfolio.observation.domain.TtlObservation;

/**
 * Deterministic mapping tests for INF-007 dnsjava adapters — no network.
 */
class DnsJavaDnsHealthObserverTest {

    @Test
    void mapTtlPreservesTypeOwnerRdataAndTtl() throws Exception {
        Record[] records = {
            new MXRecord(Name.fromString("example.com."), DClass.IN, 3600, 10,
                Name.fromString("mail.example.com."))
        };

        List<TtlObservation> ttl = DnsJavaDnsHealthObserver.mapTtl(records, "MX");

        assertEquals(1, ttl.size());
        assertEquals("MX", ttl.get(0).recordType());
        assertEquals("example.com", ttl.get(0).owner());
        assertEquals(3600L, ttl.get(0).ttlSeconds());
        assertTrue(ttl.get(0).rdata().contains("mail.example.com"));
    }

    @Test
    void mapDnssecPreservesOwnerAndRdata() throws Exception {
        Record[] records = {
            new TXTRecord(Name.fromString("example.com."), DClass.IN, 300, List.of("placeholder"))
        };

        List<DnssecRecordFact> facts = DnsJavaDnsHealthObserver.mapDnssec(records);
        assertEquals("example.com", facts.get(0).owner());
        assertTrue(!facts.get(0).rdata().isBlank());
    }

    @Test
    void mapAnswersMapsMxRdataAndTtl() throws Exception {
        Record[] records = {
            new MXRecord(Name.fromString("example.com."), DClass.IN, 120, 20,
                Name.fromString("backup.example.com."))
        };

        List<PropagationAnswer> answers = DnsJavaNamedResolverQuery.mapAnswers(records);
        assertEquals(1, answers.size());
        assertEquals(120L, answers.get(0).ttlSeconds());
        assertTrue(answers.get(0).rdata().contains("backup.example.com"));
    }

    @Test
    void successfulEmptyLookupIsAbsentNotUnavailable() {
        Record[] interpreted = DnsJavaDnsHealthObserver.interpretLookupResult(
            Lookup.SUCCESSFUL, new Record[0]);
        DnssecObservation observation = DnsJavaDnsHealthObserver.dnssecFromLookup("DNSKEY", interpreted);

        assertEquals(0, interpreted.length);
        assertEquals(DnssecPresence.ABSENT, observation.presence());
        assertTrue(observation.records().isEmpty());
    }

    @Test
    void typeNotFoundAndNxdomainAreAbsent() {
        assertEquals(DnssecPresence.ABSENT,
            DnsJavaDnsHealthObserver.dnssecFromLookup("DS",
                DnsJavaDnsHealthObserver.interpretLookupResult(Lookup.TYPE_NOT_FOUND, null)).presence());
        assertEquals(DnssecPresence.ABSENT,
            DnsJavaDnsHealthObserver.dnssecFromLookup("DNSKEY",
                DnsJavaDnsHealthObserver.interpretLookupResult(Lookup.HOST_NOT_FOUND, null)).presence());
    }

    @Test
    void unrecoverableAndTryAgainAreUnavailableNotAbsent() {
        assertNull(DnsJavaDnsHealthObserver.interpretLookupResult(Lookup.UNRECOVERABLE, null));
        assertNull(DnsJavaDnsHealthObserver.interpretLookupResult(Lookup.TRY_AGAIN, new Record[0]));

        DnssecObservation unrecoverable = DnsJavaDnsHealthObserver.dnssecFromLookup(
            "DNSKEY", DnsJavaDnsHealthObserver.interpretLookupResult(Lookup.UNRECOVERABLE, null));
        DnssecObservation tryAgain = DnsJavaDnsHealthObserver.dnssecFromLookup(
            "DS", DnsJavaDnsHealthObserver.interpretLookupResult(Lookup.TRY_AGAIN, null));

        assertEquals(DnssecPresence.UNAVAILABLE, unrecoverable.presence());
        assertEquals(DnssecPresence.UNAVAILABLE, tryAgain.presence());
        assertTrue(unrecoverable.records().isEmpty());
    }

    @Test
    void observedDnssecRecordsArePresent() throws Exception {
        Record[] records = {
            new DNSKEYRecord(Name.fromString("example.com."), DClass.IN, 3600,
                256, 3, 13, new byte[] { 1, 2, 3, 4 })
        };

        Record[] interpreted = DnsJavaDnsHealthObserver.interpretLookupResult(Lookup.SUCCESSFUL, records);
        DnssecObservation observation = DnsJavaDnsHealthObserver.dnssecFromLookup("DNSKEY", interpreted);

        assertEquals(DnssecPresence.PRESENT, observation.presence());
        assertEquals(1, observation.records().size());
        assertEquals("example.com", observation.records().get(0).owner());
        assertTrue(!observation.records().get(0).rdata().isBlank());
    }
}
