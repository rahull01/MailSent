package com.mailsent.portfolio.observation.infrastructure;

import java.net.UnknownHostException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.xbill.DNS.Lookup;
import org.xbill.DNS.MXRecord;
import org.xbill.DNS.Record;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

import com.mailsent.portfolio.observation.application.DnsObservationException;
import com.mailsent.portfolio.observation.application.NamedDnsResolverQuery;
import com.mailsent.portfolio.observation.domain.PropagationAnswer;
import com.mailsent.portfolio.observation.domain.PropagationObservation;

/**
 * DnsJavaNamedResolverQuery — production {@link NamedDnsResolverQuery} using
 * dnsjava {@link Lookup} against a named resolver.
 *
 * <p>Supported identities: {@code system} (default resolver) and a configured
 * public resolver such as {@code 8.8.8.8} (a well-known DNS server, not a paid
 * API). A timeout, NXDOMAIN, or resolver failure is recorded as unavailable
 * with no fabricated MX answers.</p>
 */
public class DnsJavaNamedResolverQuery implements NamedDnsResolverQuery {

    private static final Duration TIMEOUT = Duration.ofSeconds(2);
    static final String QUERY_TYPE = "MX";

    @Override
    public PropagationObservation queryMx(String resolverIdentity, String domainName) {
        try {
            Lookup lookup = new Lookup(domainName, Type.MX);
            lookup.setResolver(resolverFor(resolverIdentity));
            lookup.setCache(null);
            Record[] records = lookup.run();
            int result = lookup.getResult();
            if (result == Lookup.HOST_NOT_FOUND || result == Lookup.TRY_AGAIN) {
                return PropagationObservation.unavailable(resolverIdentity, QUERY_TYPE, domainName);
            }
            return new PropagationObservation(resolverIdentity, QUERY_TYPE, domainName, true,
                mapAnswers(records == null ? new Record[0] : records));
        } catch (TextParseException e) {
            throw new DnsObservationException("Invalid DNS name: '" + domainName + "'", e);
        } catch (RuntimeException e) {
            return PropagationObservation.unavailable(resolverIdentity, QUERY_TYPE, domainName);
        }
    }

    private SimpleResolver resolverFor(String resolverIdentity) {
        try {
            SimpleResolver resolver = "system".equals(resolverIdentity)
                ? new SimpleResolver()
                : new SimpleResolver(resolverIdentity);
            resolver.setTimeout(TIMEOUT);
            return resolver;
        } catch (UnknownHostException e) {
            throw new DnsObservationException("Unable to configure DNS resolver '" + resolverIdentity + "'", e);
        }
    }

    /** Deterministic mapping of raw MX records to propagation answers. */
    static List<PropagationAnswer> mapAnswers(Record[] records) {
        List<PropagationAnswer> answers = new ArrayList<>();
        for (Record record : records) {
            if (record instanceof MXRecord mx) {
                answers.add(new PropagationAnswer(rdata(mx), mx.getTTL()));
            }
        }
        return List.copyOf(answers);
    }

    static String rdata(Record record) {
        return record.rdataToString();
    }
}
