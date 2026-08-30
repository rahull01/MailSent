package com.mailsent.resolution.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class DnsProviderTest {

    @Test
    void shouldDetectCloudflareFromNameservers() {
        assertThat(DnsProvider.fromNameservers(List.of("art.ns.cloudflare.com", "emma.ns.cloudflare.com")))
                .isEqualTo(DnsProvider.CLOUDFLARE);
    }

    @Test
    void shouldDetectRoute53FromNameservers() {
        assertThat(DnsProvider.fromNameservers(List.of("ns-123.awsdns-45.org", "ns-678.awsdns-90.co.uk")))
                .isEqualTo(DnsProvider.ROUTE53);
    }

    @Test
    void shouldDetectGoDaddyFromNameservers() {
        assertThat(DnsProvider.fromNameservers(List.of("ns01.domaincontrol.com", "ns02.domaincontrol.com")))
                .isEqualTo(DnsProvider.GODADDY);
    }

    @Test
    void shouldDetectGoogleCloudDnsFromNameservers() {
        assertThat(DnsProvider.fromNameservers(List.of("ns-cloud-a1.googledomains.com", "ns-cloud-a2.googledomains.com")))
                .isEqualTo(DnsProvider.GOOGLE_CLOUD_DNS);
    }

    @Test
    void shouldDetectNamecheapFromNameservers() {
        assertThat(DnsProvider.fromNameservers(List.of("dns1.registrar-servers.com", "dns2.registrar-servers.com")))
                .isEqualTo(DnsProvider.NAMECHEAP);
    }

    @Test
    void shouldFallbackToGenericDnsWhenUnknownOrEmpty() {
        assertThat(DnsProvider.fromNameservers(List.of("ns1.customhost.net")))
                .isEqualTo(DnsProvider.GENERIC_DNS);
        assertThat(DnsProvider.fromNameservers(List.of()))
                .isEqualTo(DnsProvider.GENERIC_DNS);
        assertThat(DnsProvider.fromNameservers(null))
                .isEqualTo(DnsProvider.GENERIC_DNS);
    }
}
