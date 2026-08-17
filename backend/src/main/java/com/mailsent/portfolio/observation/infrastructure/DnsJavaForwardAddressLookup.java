package com.mailsent.portfolio.observation.infrastructure;

import java.net.UnknownHostException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.xbill.DNS.AAAARecord;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

import com.mailsent.portfolio.observation.application.DnsObservationException;
import com.mailsent.portfolio.observation.application.ForwardAddressLookup;
import com.mailsent.portfolio.observation.domain.IpAddressFamily;

/**
 * DnsJavaForwardAddressLookup — production {@link ForwardAddressLookup} using
 * dnsjava {@link Lookup} for Type.A and Type.AAAA. Additive to
 * {@code DnsJavaDnsResolver}; it does not change INF-001 MX/TXT observation.
 */
public class DnsJavaForwardAddressLookup implements ForwardAddressLookup {

    private static final Duration TIMEOUT = Duration.ofSeconds(2);

    @Override
    public Optional<List<String>> lookup(String hostname, IpAddressFamily family) {
        int type = family == IpAddressFamily.IPV6 ? Type.AAAA : Type.A;
        Record[] records = lookup(hostname, type);
        if (records == null) {
            return Optional.empty();
        }
        return Optional.of(mapAddresses(records, family));
    }

    private Record[] lookup(String hostname, int type) {
        try {
            Lookup lookup = new Lookup(hostname, type);
            lookup.setResolver(resolver());
            lookup.setCache(null);
            Record[] records = lookup.run();
            return DnsJavaLookupInterpretation.interpretLookupResult(lookup.getResult(), records);
        } catch (TextParseException e) {
            throw new DnsObservationException("Invalid DNS name: '" + hostname + "'", e);
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

    /** Deterministic A/AAAA mapping (testable without network). */
    static List<String> mapAddresses(Record[] records, IpAddressFamily family) {
        List<String> addresses = new ArrayList<>();
        for (Record record : records) {
            if (family == IpAddressFamily.IPV4 && record instanceof ARecord a) {
                addresses.add(a.getAddress().getHostAddress());
            }
            if (family == IpAddressFamily.IPV6 && record instanceof AAAARecord aaaa) {
                addresses.add(aaaa.getAddress().getHostAddress());
            }
        }
        return List.copyOf(addresses);
    }
}
