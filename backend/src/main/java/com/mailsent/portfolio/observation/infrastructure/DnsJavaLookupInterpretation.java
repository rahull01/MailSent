package com.mailsent.portfolio.observation.infrastructure;

import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;

/**
 * DnsJavaLookupInterpretation — maps a dnsjava {@link Lookup} result to records
 * using the same unavailable-versus-empty semantics as INF-007, without
 * modifying {@code DnsJavaDnsHealthObserver}.
 *
 * <p>{@code null} is observation error/unavailable ({@link Lookup#UNRECOVERABLE},
 * {@link Lookup#TRY_AGAIN}, and similar). An empty array is a completed lookup
 * with no records: {@link Lookup#SUCCESSFUL} empty, {@link Lookup#TYPE_NOT_FOUND},
 * or {@link Lookup#HOST_NOT_FOUND} (NXDOMAIN). Successful empty is not failure.</p>
 */
final class DnsJavaLookupInterpretation {

    private DnsJavaLookupInterpretation() {
    }

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

    static String normalize(String name) {
        return name.endsWith(".") ? name.substring(0, name.length() - 1) : name;
    }
}
