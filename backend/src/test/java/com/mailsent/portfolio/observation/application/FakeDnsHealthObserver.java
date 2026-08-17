package com.mailsent.portfolio.observation.application;

import java.time.Instant;
import java.util.List;

import com.mailsent.portfolio.observation.domain.DnsHealthFacts;
import com.mailsent.portfolio.observation.domain.DnssecObservation;
import com.mailsent.portfolio.observation.domain.DnssecPresence;
import com.mailsent.portfolio.observation.domain.DnssecRecordFact;
import com.mailsent.portfolio.observation.domain.PropagationAnswer;
import com.mailsent.portfolio.observation.domain.PropagationObservation;
import com.mailsent.portfolio.observation.domain.TtlObservation;

/**
 * Test double for {@link DnsHealthObserver}. Never performs network I/O.
 */
public final class FakeDnsHealthObserver implements DnsHealthObserver {

    public Instant observedAt = Instant.parse("2026-08-08T10:00:00Z");
    public List<TtlObservation> ttl = List.of(new TtlObservation("MX", "example.com", "10 mail.example.com.", 3600));
    public List<DnssecObservation> dnssec = List.of(
        new DnssecObservation("DNSKEY", DnssecPresence.ABSENT, List.of()),
        new DnssecObservation("DS", DnssecPresence.ABSENT, List.of()));
    public List<PropagationObservation> propagation = List.of(
        new PropagationObservation("system", "MX", "example.com", true,
            List.of(new PropagationAnswer("10 mail.example.com.", 3600))),
        new PropagationObservation("8.8.8.8", "MX", "example.com", true,
            List.of(new PropagationAnswer("10 mail.example.com.", 3600))));
    public boolean fail;

    @Override
    public DnsHealthFacts observe(String domainName) {
        if (fail) {
            throw new DnsObservationException("DNS health observation failed for " + domainName);
        }
        return DnsHealthFacts.of(observedAt, ttl, dnssec, propagation);
    }
}
