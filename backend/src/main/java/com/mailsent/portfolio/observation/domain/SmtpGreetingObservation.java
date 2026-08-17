package com.mailsent.portfolio.observation.domain;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import com.mailsent.portfolio.domain.domain.Domain;

/**
 * SmtpGreetingObservation — an immutable persisted snapshot of one raw SMTP
 * greeting/banner and EHLO observation for an IP of an observed MX host of a
 * registered Domain (INF-006).
 *
 * <p>Owned by the Delivery Observation bounded context. It is not Observation
 * Evidence, not Reverse DNS Observation, not HELO/EHLO validation, and not
 * Health Signal. It records facts actually returned: identity from the
 * registered {@link Domain}, the source MX hostname taken from Observation
 * Evidence, the resolved IP and address family, the connection/probe outcome,
 * the raw greeting and EHLO text when those were actually observed, and when
 * it was observed. Tenant binding is derived from that Domain (organization
 * and domain identity never come from the caller).</p>
 *
 * <p>Factory-based creation only ({@link #capture}, {@link #restore}). No setters,
 * no update, no delete. A later observation of the same domain is a new
 * snapshot. Greeting and EHLO text must not be fabricated for unavailable
 * probes.</p>
 */
public final class SmtpGreetingObservation {

    private final String id;
    private final String organizationId;
    private final String domainId;
    private final String domainName;
    private final String sourceMxHostname;
    private final String ip;
    private final IpAddressFamily addressFamily;
    private final SmtpProbeOutcome probeOutcome;
    private final String rawGreeting;
    private final String rawEhlo;
    private final Instant observedAt;

    private SmtpGreetingObservation(String id, String organizationId, String domainId, String domainName,
                                    String sourceMxHostname, String ip, IpAddressFamily addressFamily,
                                    SmtpProbeOutcome probeOutcome, String rawGreeting, String rawEhlo,
                                    Instant observedAt) {
        this.id = id;
        this.organizationId = organizationId;
        this.domainId = domainId;
        this.domainName = domainName;
        this.sourceMxHostname = sourceMxHostname;
        this.ip = ip;
        this.addressFamily = addressFamily;
        this.probeOutcome = probeOutcome;
        this.rawGreeting = rawGreeting;
        this.rawEhlo = rawEhlo;
        this.observedAt = observedAt == null ? null : observedAt.truncatedTo(ChronoUnit.MICROS);
    }

    /**
     * Captures one factual SMTP greeting observation against the registered domain.
     * Identity and tenant fields are taken from {@code domain}. Greeting and EHLO
     * may be empty when the connection succeeded with no banner/capabilities text.
     * Non-{@link SmtpProbeOutcome#CONNECTED} outcomes must not carry fabricated
     * greeting or EHLO text.
     */
    public static SmtpGreetingObservation capture(Domain domain, String sourceMxHostname, String ip,
                                                 IpAddressFamily addressFamily, SmtpProbeOutcome probeOutcome,
                                                 String rawGreeting, String rawEhlo, Instant observedAt) {
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
        if (probeOutcome == null) {
            throw new IllegalArgumentException("A probe outcome is required.");
        }
        if (observedAt == null) {
            throw new IllegalArgumentException("An observation instant is required.");
        }
        rejectFabricatedText(probeOutcome, rawGreeting, rawEhlo);
        return new SmtpGreetingObservation(
            UUID.randomUUID().toString(),
            domain.organizationId(),
            domain.id(),
            domain.name(),
            sourceMxHostname,
            ip,
            addressFamily,
            probeOutcome,
            rawGreeting,
            rawEhlo,
            observedAt);
    }

    /**
     * Reconstructs SmtpGreetingObservation from durable storage, adopting the
     * stored identity, tenant binding, MX/IP facts, outcome, greeting/EHLO, and
     * observation instant verbatim.
     */
    public static SmtpGreetingObservation restore(String id, String organizationId, String domainId,
                                                  String domainName, String sourceMxHostname, String ip,
                                                  IpAddressFamily addressFamily, SmtpProbeOutcome probeOutcome,
                                                  String rawGreeting, String rawEhlo, Instant observedAt) {
        return new SmtpGreetingObservation(id, organizationId, domainId, domainName, sourceMxHostname, ip,
            addressFamily, probeOutcome, rawGreeting, rawEhlo, observedAt);
    }

    private static void rejectFabricatedText(SmtpProbeOutcome probeOutcome, String rawGreeting, String rawEhlo) {
        if (probeOutcome == SmtpProbeOutcome.CONNECTED) {
            return;
        }
        if (rawGreeting != null && !rawGreeting.isEmpty()) {
            throw new IllegalArgumentException("Greeting text is only stored when actually observed.");
        }
        if (rawEhlo != null && !rawEhlo.isEmpty()) {
            throw new IllegalArgumentException("EHLO text is only stored when actually observed.");
        }
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
     * Raw SMTP connection/probe outcome. Not health, validity, or deliverability.
     */
    public SmtpProbeOutcome probeOutcome() {
        return probeOutcome;
    }

    /**
     * Raw SMTP greeting/banner as observed, or {@code null} when not observed.
     * An empty string is a successful connection with no banner text.
     */
    public String rawGreeting() {
        return rawGreeting;
    }

    /**
     * Raw EHLO response as observed, or {@code null} when EHLO was not observed.
     * An empty string is a successful EHLO with no capability text.
     */
    public String rawEhlo() {
        return rawEhlo;
    }

    /** The instant this SMTP greeting observation was made. */
    public Instant observedAt() {
        return observedAt;
    }
}
