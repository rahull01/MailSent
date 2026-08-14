package com.mailsent.portfolio.observation.infrastructure;

import java.net.UnknownHostException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.xbill.DNS.Lookup;
import org.xbill.DNS.MXRecord;
import org.xbill.DNS.Record;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.TXTRecord;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

import com.mailsent.portfolio.observation.application.DnsObservationException;
import com.mailsent.portfolio.observation.application.DnsResolver;
import com.mailsent.portfolio.observation.domain.DnsObservation;
import com.mailsent.portfolio.observation.domain.MxRecord;

/**
 * DnsJavaDnsResolver — a production {@link DnsResolver} backed by the dnsjava library.
 *
 * <p>Performs REAL, bounded DNS reads for the observed domain: MX and TXT records, with a
 * fixed timeout and a single-shot (uncached) lookup, so resource usage is bounded. NXDOMAIN,
 * timeouts, and resolver failures surface as a clear {@link DnsObservationException}; a
 * successful lookup with no records of a type yields an empty list — never a fabricated
 * record (INV-01). The raw-record → value-object mapping ({@link #mapMx}, {@link #mapTxt})
 * is deterministic and independently testable without network access.</p>
 */
public class DnsJavaDnsResolver implements DnsResolver {

    /** Bounded lookup timeout — a slow DNS answer is a failure, not a hang. */
    private static final Duration TIMEOUT = Duration.ofSeconds(2);

    @Override
    public DnsObservation observe(String domainName) {
        Instant observedAt = Instant.now();
        List<MxRecord> mx = mapMx(lookup(domainName, Type.MX));
        List<String> txt = mapTxt(lookup(domainName, Type.TXT));
        String spf = findSpf(txt);
        return DnsObservation.of(domainName, mx, txt, spf, observedAt);
    }

    /** Performs a bounded, single-shot DNS lookup for one record type. */
    private Record[] lookup(String domainName, int type) {
        try {
            Lookup lookup = new Lookup(domainName, type);
            lookup.setResolver(resolver());
            Record[] records = lookup.run();
            int result = lookup.getResult();
            if (result == Lookup.HOST_NOT_FOUND || result == Lookup.TRY_AGAIN) {
                throw new DnsObservationException(
                    "DNS lookup failed for '" + domainName + "' (result=" + result + ")");
            }
            // SUCCESSFUL / TYPE_NOT_FOUND: a successful name with no records of this type is a
            // valid empty observation — never a fabricated record.
            return records == null ? new Record[0] : records;
        } catch (TextParseException e) {
            throw new DnsObservationException("Invalid DNS name: '" + domainName + "'", e);
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

    /** Deterministic mapping of raw MX records to value objects (testable without network). */
    static List<MxRecord> mapMx(Record[] records) {
        return Arrays.stream(records)
            .filter(MXRecord.class::isInstance)
            .map(MXRecord.class::cast)
            .map(record -> new MxRecord(record.getPriority(), normalize(record.getTarget().toString())))
            .toList();
    }

    /** Deterministic mapping of raw TXT records to strings (testable without network). */
    static List<String> mapTxt(Record[] records) {
        List<String> strings = new ArrayList<>();
        for (Record record : records) {
            if (record instanceof TXTRecord txt) {
                strings.addAll(txt.getStrings());
            }
        }
        return strings;
    }

    /** Strips the DNS trailing root dot for a stable hostname. */
    static String normalize(String name) {
        return name.endsWith(".") ? name.substring(0, name.length() - 1) : name;
    }

    /** The SPF record, the TXT string beginning with {@code v=spf1}, if any. */
    static String findSpf(List<String> txt) {
        return txt.stream().filter(s -> s.startsWith("v=spf1")).findFirst().orElse(null);
    }
}