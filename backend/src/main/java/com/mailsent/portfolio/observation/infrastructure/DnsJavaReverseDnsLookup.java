package com.mailsent.portfolio.observation.infrastructure;

import java.net.UnknownHostException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.xbill.DNS.Lookup;
import org.xbill.DNS.Name;
import org.xbill.DNS.PTRRecord;
import org.xbill.DNS.Record;
import org.xbill.DNS.ReverseMap;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.Type;

import com.mailsent.portfolio.observation.application.DnsObservationException;
import com.mailsent.portfolio.observation.application.ReverseDnsLookup;

/**
 * DnsJavaReverseDnsLookup — production {@link ReverseDnsLookup} using dnsjava
 * {@link Lookup} for Type.PTR. Additive to {@code DnsJavaDnsResolver}; it does
 * not change INF-001 MX/TXT observation. Unavailable lookups return empty
 * Optional; a successful empty PTR list is NODATA, not fabricated names.
 */
public class DnsJavaReverseDnsLookup implements ReverseDnsLookup {

    private static final Duration TIMEOUT = Duration.ofSeconds(2);

    @Override
    public Optional<List<String>> lookupPtr(String ipAddress) {
        Name reverseName;
        try {
            reverseName = ReverseMap.fromAddress(ipAddress);
        } catch (UnknownHostException e) {
            return Optional.empty();
        }
        Record[] records = lookup(reverseName);
        if (records == null) {
            return Optional.empty();
        }
        return Optional.of(mapPtr(records));
    }

    private Record[] lookup(Name reverseName) {
        try {
            Lookup lookup = new Lookup(reverseName, Type.PTR);
            lookup.setResolver(resolver());
            lookup.setCache(null);
            Record[] records = lookup.run();
            return DnsJavaLookupInterpretation.interpretLookupResult(lookup.getResult(), records);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private SimpleResolver resolver() {
        try {
            SimpleResolver resolver = new SimpleResolver();
            resolver.setTimeout(TIMEOUT);
            return resolver;
        } catch (UnknownHostException e) {
            throw new DnsObservationException("Unable to configure the DNS resolver", e);
        }
    }

    /** Deterministic PTR mapping (testable without network). */
    static List<String> mapPtr(Record[] records) {
        List<String> names = new ArrayList<>();
        for (Record record : records) {
            if (record instanceof PTRRecord ptr) {
                names.add(DnsJavaLookupInterpretation.normalize(ptr.getTarget().toString()));
            }
        }
        return List.copyOf(names);
    }
}
