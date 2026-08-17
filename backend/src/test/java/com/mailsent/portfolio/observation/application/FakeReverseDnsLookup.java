package com.mailsent.portfolio.observation.application;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Test double for {@link ReverseDnsLookup}. Never performs network I/O.
 * Unconfigured IPs are unavailable (empty Optional), not fabricated PTR.
 */
public final class FakeReverseDnsLookup implements ReverseDnsLookup {

    public int lookupCount;

    private final Map<String, Optional<List<String>>> byIp = new HashMap<>();

    public void succeed(String ip, List<String> ptrValues) {
        byIp.put(ip, Optional.of(List.copyOf(ptrValues)));
    }

    public void unavailable(String ip) {
        byIp.put(ip, Optional.empty());
    }

    public void fail(String ip) {
        byIp.put(ip, null);
    }

    @Override
    public Optional<List<String>> lookupPtr(String ipAddress) {
        lookupCount++;
        if (!byIp.containsKey(ipAddress)) {
            return Optional.empty();
        }
        Optional<List<String>> configured = byIp.get(ipAddress);
        if (configured == null) {
            throw new RuntimeException("PTR lookup failed for " + ipAddress);
        }
        return configured;
    }
}
