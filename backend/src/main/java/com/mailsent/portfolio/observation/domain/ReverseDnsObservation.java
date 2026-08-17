package com.mailsent.portfolio.observation.domain;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.mailsent.portfolio.domain.domain.Domain;

/**
 * ReverseDnsObservation — an immutable persisted snapshot of one raw reverse-DNS
 * (PTR) observation for an IP of an observed MX host of a registered Domain
 * (INF-005).
 *
 * <p>Owned by the Delivery Observation bounded context. It is not Observation
 * Evidence, not FCrDNS, not Health Signal, and not a pass/fail or score. It
 * records facts actually returned: identity from the registered {@link Domain},
 * the source MX hostname taken from Observation Evidence, the resolved IP and
 * address family, the PTR values as observed (including a successful empty
 * list), and when it was observed. Tenant binding is derived from that Domain
 * (organization and domain identity never come from the caller).</p>
 *
 * <p>Factory-based creation only ({@link #capture}, {@link #restore}). No setters,
 * no update, no delete. A later observation of the same domain is a new
 * snapshot.</p>
 */
public final class ReverseDnsObservation {

    private final String id;
    private final String organizationId;
    private final String domainId;
    private final String domainName;
    private final String sourceMxHostname;
    private final String ip;
    private final IpAddressFamily addressFamily;
    private final List<String> ptrValues;
    private final Instant observedAt;

    private ReverseDnsObservation(String id, String organizationId, String domainId, String domainName,
                                  String sourceMxHostname, String ip, IpAddressFamily addressFamily,
                                  List<String> ptrValues, Instant observedAt) {
        this.id = id;
        this.organizationId = organizationId;
        this.domainId = domainId;
        this.domainName = domainName;
        this.sourceMxHostname = sourceMxHostname;
        this.ip = ip;
        this.addressFamily = addressFamily;
        this.ptrValues = List.copyOf(ptrValues);
        this.observedAt = observedAt;
    }

    /**
     * Captures one factual reverse-DNS observation against the registered domain.
     * Identity and tenant fields are taken from {@code domain}. PTR values are the
     * list actually returned by a successful lookup (may be empty). Unavailable
     * lookups must not call this factory.
     */
    public static ReverseDnsObservation capture(Domain domain, String sourceMxHostname, String ip,
                                                IpAddressFamily addressFamily, List<String> ptrValues,
                                                Instant observedAt) {
        if (domain == null) {
            throw new IllegalArgumentException("A registered domain is required.");
        }
        if (sourceMxHostname == null || sourceMxHostname.isBlank()) {
            throw new IllegalArgumentException("A source MX hostname is required.");
        }
        if (ip == null || ip.isBlank()) {
            throw new IllegalArgumentException("An IP address is required.");
        }
        if (addressFamily == null) {
            throw new IllegalArgumentException("An address family is required.");
        }
        if (ptrValues == null) {
            throw new IllegalArgumentException("PTR values are required (may be empty).");
        }
        if (observedAt == null) {
            throw new IllegalArgumentException("An observation instant is required.");
        }
        return new ReverseDnsObservation(
            UUID.randomUUID().toString(),
            domain.organizationId(),
            domain.id(),
            domain.name(),
            sourceMxHostname,
            ip,
            addressFamily,
            ptrValues,
            observedAt);
    }

    /**
     * Reconstructs ReverseDnsObservation from durable storage, adopting the stored
     * identity, tenant binding, MX/IP facts, PTR values, and observation instant
     * verbatim.
     */
    public static ReverseDnsObservation restore(String id, String organizationId, String domainId,
                                                String domainName, String sourceMxHostname, String ip,
                                                IpAddressFamily addressFamily, List<String> ptrValues,
                                                Instant observedAt) {
        return new ReverseDnsObservation(id, organizationId, domainId, domainName, sourceMxHostname, ip,
            addressFamily, ptrValues, observedAt);
    }

    /** The stable identity of this snapshot. */
    public String id() {
        return id;
    }

    /** The owning organization, derived from the registered Domain. */
    public String organizationId() {
        return organizationId;
    }

    /** The registered Domain's stable identity. */
    public String domainId() {
        return domainId;
    }

    /** The registered Domain's canonical name. */
    public String domainName() {
        return domainName;
    }

    /** The MX hostname taken from Observation Evidence that was resolved to this IP. */
    public String sourceMxHostname() {
        return sourceMxHostname;
    }

    /** The observed IP address, as returned by A or AAAA. */
    public String ip() {
        return ip;
    }

    /** The address family of {@link #ip()} as observed ({@code IPV4} or {@code IPV6}). */
    public IpAddressFamily addressFamily() {
        return addressFamily;
    }

    /**
     * PTR names actually returned for this IP. An empty list is a successful lookup
     * with no PTR records (NODATA), not unavailability.
     */
    public List<String> ptrValues() {
        return ptrValues;
    }

    /** The instant this reverse-DNS observation was made. */
    public Instant observedAt() {
        return observedAt;
    }
}
