package com.mailsent.portfolio.observation.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.xbill.DNS.DClass;
import org.xbill.DNS.MXRecord;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.TXTRecord;

import com.mailsent.portfolio.observation.domain.MxRecord;

/**
 * Deterministic unit tests for {@link DnsJavaDnsResolver}'s record mapping — fed with
 * synthetic dnsjava records (no network, no dependence on public DNS).
 */
class DnsJavaDnsResolverTest {

    @Test
    void mapMxMapsAndNormalizesRecords() throws Exception {
        Record[] records = {
            new MXRecord(Name.fromString("example.com."), DClass.IN, 3600, 10,
                Name.fromString("mail.example.com.")),
            new MXRecord(Name.fromString("example.com."), DClass.IN, 3600, 20,
                Name.fromString("backup.example.com."))
        };

        List<MxRecord> mx = DnsJavaDnsResolver.mapMx(records);

        assertEquals(2, mx.size());
        assertEquals(10, mx.get(0).priority());
        assertEquals("mail.example.com", mx.get(0).exchange(), "trailing root dot stripped");
        assertEquals(20, mx.get(1).priority());
    }

    @Test
    void mapTxtCollectsTxtStrings() throws Exception {
        Record[] records = {
            new TXTRecord(Name.fromString("example.com."), DClass.IN, 3600, List.of("v=spf1 -all")),
            new TXTRecord(Name.fromString("example.com."), DClass.IN, 3600, List.of("google-site-verification=abc"))
        };

        assertEquals(List.of("v=spf1 -all", "google-site-verification=abc"), DnsJavaDnsResolver.mapTxt(records));
    }

    @Test
    void normalizeStripsTrailingDot() {
        assertEquals("mail.example.com", DnsJavaDnsResolver.normalize("mail.example.com."));
        assertEquals("example.com", DnsJavaDnsResolver.normalize("example.com"));
    }

    @Test
    void findSpfDetectsVspf1AndIgnoresOthers() {
        assertEquals("v=spf1 -all",
            DnsJavaDnsResolver.findSpf(List.of("dkim=test", "v=spf1 -all")));
        assertNull(DnsJavaDnsResolver.findSpf(List.of("dkim=test", "google-site-verification=abc")));
    }
}