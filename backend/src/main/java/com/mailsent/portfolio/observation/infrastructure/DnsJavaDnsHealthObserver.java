package com.mailsent.portfolio.observation.infrastructure;

import java.net.UnknownHostException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

import com.mailsent.portfolio.observation.application.DnsHealthObserver;
import com.mailsent.portfolio.observation.application.DnsObservationException;
import com.mailsent.portfolio.observation.application.NamedDnsResolverQuery;
import com.mailsent.portfolio.observation.domain.DnsHealthFacts;
import com.mailsent.portfolio.observation.domain.DnssecObservation;
import com.mailsent.portfolio.observation.domain.DnssecPresence;
import com.mailsent.portfolio.observation.domain.DnssecRecordFact;
import com.mailsent.portfolio.observation.domain.PropagationObservation;
import com.mailsent.portfolio.observation.domain.TtlObservation;

/**
 * DnsJavaDnsHealthObserver — production {@link DnsHealthObserver} using dnsjava
 * {@link Lookup}. Additive to {@code DnsJavaDnsResolver}; it does not change
 * INF-001 MX/TXT observation.
 *
 * <p>TTL: MX, TXT, and A of the domain, with per-record TTL. DNSSEC: Type.DNSKEY
 * and Type.DS as raw owner/rdata and present/absent/unavailable — not
 * cryptographic validation. Propagation: {@code system} and {@code 8.8.8.8} via
 * {@link NamedDnsResolverQuery}. If one named resolver fails, the snapshot is
 * still returned when any real observation exists; the failed resolver is
 * unavailable with empty answers.</p>
 */
public class DnsJavaDnsHealthObserver implements DnsHealthObserver {

    static final List<String> PROPAGATION_RESOLVERS = List.of("system", "8.8.8.8");

    private static final Duration TIMEOUT = Duration.ofSeconds(2);

    private final NamedDnsResolverQuery namedResolvers;

    public DnsJavaDnsHealthObserver(NamedDnsResolverQuery namedResolvers) {
        this.namedResolvers = namedResolvers;
    }

    @Override
    public DnsHealthFacts observe(String domainName) {
        Instant observedAt = Instant.now();
        SimpleResolver system = systemResolver();
        List<TtlObservation> ttl = new ArrayList<>();
        ttl.addAll(lookupTtl(domainName, Type.MX, "MX", system));
        ttl.addAll(lookupTtl(domainName, Type.TXT, "TXT", system));
        ttl.addAll(lookupTtl(domainName, Type.A, "A", system));

        List<DnssecObservation> dnssec = List.of(
            lookupDnssec(domainName, Type.DNSKEY, "DNSKEY", system),
            lookupDnssec(domainName, Type.DS, "DS", system));

        List<PropagationObservation> propagation = new ArrayList<>();
        for (String identity : PROPAGATION_RESOLVERS) {
            propagation.add(namedResolvers.queryMx(identity, domainName));
        }

        DnsHealthFacts facts = DnsHealthFacts.of(observedAt, ttl, dnssec, propagation);
        if (!facts.hasRealObservation()) {
            throw new DnsObservationException(
                "DNS health observation failed for '" + domainName + "' (no real observation)");
        }
        return facts;
    }

    private List<TtlObservation> lookupTtl(String domainName, int type, String typeName, SimpleResolver resolver) {
        Record[] records = lookup(domainName, type, resolver);
        if (records == null) {
            return List.of();
        }
        return mapTtl(records, typeName);
    }

    private DnssecObservation lookupDnssec(String domainName, int type, String typeName, SimpleResolver resolver) {
        return dnssecFromLookup(typeName, lookup(domainName, type, resolver));
    }

    /**
     * Bounded lookup. {@code null} means unavailable/error for this type;
     * an empty array is a successful lookup with no records (absent).
     */
    private Record[] lookup(String domainName, int type, SimpleResolver resolver) {
        try {
            Lookup lookup = new Lookup(domainName, type);
            lookup.setResolver(resolver);
            lookup.setCache(null);
            Record[] records = lookup.run();
            return interpretLookupResult(lookup.getResult(), records);
        } catch (TextParseException e) {
            throw new DnsObservationException("Invalid DNS name: '" + domainName + "'", e);
        } catch (RuntimeException e) {
            return null;
        }
    }

    /**
     * Maps a dnsjava {@link Lookup} result to records.
     * {@code null} is observation error/unavailable ({@link Lookup#UNRECOVERABLE},
     * {@link Lookup#TRY_AGAIN}, and similar). An empty array is a completed
     * lookup with no records: {@link Lookup#SUCCESSFUL} empty, {@link Lookup#TYPE_NOT_FOUND},
     * or {@link Lookup#HOST_NOT_FOUND} (NXDOMAIN).
     */
    static Record[] interpretLookupResult(int result, Record[] records) {
        if (result == Lookup.UNRECOVERABLE || result == Lookup.TRY_AGAIN) {
            return null;
        }
        if (result == Lookup.SUCCESSFUL || result == Lookup.TYPE_NOT_FOUND
            || result == Lookup.HOST_NOT_FOUND) {
            return records == null ? new Record[0] : records;
        }
        return null;
    }

    /**
     * DNSSEC presence from interpreted lookup records: {@code null} → UNAVAILABLE,
     * empty → ABSENT, otherwise PRESENT. Not cryptographic validation.
     */
    static DnssecObservation dnssecFromLookup(String typeName, Record[] interpreted) {
        if (interpreted == null) {
            return new DnssecObservation(typeName, DnssecPresence.UNAVAILABLE, List.of());
        }
        List<DnssecRecordFact> facts = mapDnssec(interpreted);
        DnssecPresence presence = facts.isEmpty() ? DnssecPresence.ABSENT : DnssecPresence.PRESENT;
        return new DnssecObservation(typeName, presence, facts);
    }

    private SimpleResolver systemResolver() {
        try {
            SimpleResolver resolver = new SimpleResolver();
            resolver.setTimeout(TIMEOUT);
            return resolver;
        } catch (UnknownHostException e) {
            throw new DnsObservationException("Unable to configure the DNS resolver", e);
        }
    }

    /** Deterministic TTL mapping (testable without network). */
    static List<TtlObservation> mapTtl(Record[] records, String typeName) {
        List<TtlObservation> observations = new ArrayList<>();
        for (Record record : records) {
            observations.add(new TtlObservation(typeName, normalize(record.getName().toString()),
                record.rdataToString(), record.getTTL()));
        }
        return List.copyOf(observations);
    }

    /** Deterministic DNSSEC rdata mapping (testable without network). */
    static List<DnssecRecordFact> mapDnssec(Record[] records) {
        List<DnssecRecordFact> facts = new ArrayList<>();
        for (Record record : records) {
            facts.add(new DnssecRecordFact(normalize(record.getName().toString()), record.rdataToString()));
        }
        return List.copyOf(facts);
    }

    static String normalize(String name) {
        return name.endsWith(".") ? name.substring(0, name.length() - 1) : name;
    }
}
