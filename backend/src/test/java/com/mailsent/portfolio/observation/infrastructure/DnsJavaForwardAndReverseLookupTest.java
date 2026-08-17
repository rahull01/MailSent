package com.mailsent.portfolio.observation.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.InetAddress;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.xbill.DNS.AAAARecord;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.DClass;
import org.xbill.DNS.Name;
import org.xbill.DNS.PTRRecord;
import org.xbill.DNS.Record;

import com.mailsent.portfolio.observation.domain.IpAddressFamily;

/**
 * Deterministic mapping tests for INF-005 dnsjava adapters — no network.
 */
class DnsJavaForwardAndReverseLookupTest {

    @Test
    void mapAddressesExtractsIpv4FromARecords() throws Exception {
        Record[] records = {
            new ARecord(Name.fromString("mail.example.com."), DClass.IN, 300,
                InetAddress.getByName("192.0.2.10"))
        };

        List<String> addresses = DnsJavaForwardAddressLookup.mapAddresses(records, IpAddressFamily.IPV4);

        assertEquals(List.of("192.0.2.10"), addresses);
        assertTrue(DnsJavaForwardAddressLookup.mapAddresses(records, IpAddressFamily.IPV6).isEmpty());
    }

    @Test
    void mapAddressesExtractsIpv6FromAaaaRecords() throws Exception {
        Record[] records = {
            new AAAARecord(Name.fromString("mail.example.com."), DClass.IN, 300,
                InetAddress.getByName("2001:db8::10"))
        };

        List<String> addresses = DnsJavaForwardAddressLookup.mapAddresses(records, IpAddressFamily.IPV6);

        assertEquals(1, addresses.size());
        assertTrue(addresses.get(0).contains("2001:db8"));
        assertTrue(DnsJavaForwardAddressLookup.mapAddresses(records, IpAddressFamily.IPV4).isEmpty());
    }

    @Test
    void mapAddressesIgnoresEmptyRecordSet() {
        assertTrue(DnsJavaForwardAddressLookup.mapAddresses(new Record[0], IpAddressFamily.IPV4).isEmpty());
    }

    @Test
    void mapPtrExtractsNormalizedTargets() throws Exception {
        Record[] records = {
            new PTRRecord(Name.fromString("10.2.0.192.in-addr.arpa."), DClass.IN, 300,
                Name.fromString("one.example.net.")),
            new PTRRecord(Name.fromString("10.2.0.192.in-addr.arpa."), DClass.IN, 300,
                Name.fromString("two.example.net."))
        };

        assertEquals(List.of("one.example.net", "two.example.net"), DnsJavaReverseDnsLookup.mapPtr(records));
    }

    @Test
    void mapPtrEmptyIsSuccessfulEmptyNotFabricated() {
        assertTrue(DnsJavaReverseDnsLookup.mapPtr(new Record[0]).isEmpty());
    }
}
