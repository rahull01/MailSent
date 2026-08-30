package com.mailsent.resolution.domain;

import java.util.List;

/**
 * DnsProvider — recognized DNS hosting providers for tailored guidance (RES-003 / FUN-G3).
 */
public enum DnsProvider {
    CLOUDFLARE,
    ROUTE53,
    GODADDY,
    GOOGLE_CLOUD_DNS,
    NAMECHEAP,
    GENERIC_DNS;

    public static DnsProvider fromNameservers(List<String> nameservers) {
        if (nameservers == null || nameservers.isEmpty()) {
            return GENERIC_DNS;
        }

        for (String ns : nameservers) {
            if (ns == null) continue;
            String lower = ns.toLowerCase();
            if (lower.contains("cloudflare.com")) {
                return CLOUDFLARE;
            }
            if (lower.contains("awsdns-") || lower.contains(".route53.")) {
                return ROUTE53;
            }
            if (lower.contains("domaincontrol.com") || lower.contains("godaddy.com")) {
                return GODADDY;
            }
            if (lower.contains("googledomains.com") || lower.contains("cloud-dns") || lower.contains("google.com")) {
                return GOOGLE_CLOUD_DNS;
            }
            if (lower.contains("registrar-servers.com") || lower.contains("namecheap.com")) {
                return NAMECHEAP;
            }
        }

        return GENERIC_DNS;
    }
}
