package com.mailsent.portfolio.observation.application;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.mailsent.portfolio.observation.domain.IpAddressFamily;

/**
 * Test double for {@link ForwardAddressLookup}. Never performs network I/O.
 * Unconfigured hostnames are unavailable (empty Optional), not fabricated empty
 * success.
 */
public final class FakeForwardAddressLookup implements ForwardAddressLookup {

    public int lookupCount;

    private final Map<String, Optional<List<String>>> byKey = new HashMap<>();

    public void succeed(String hostname, IpAddressFamily family, List<String> addresses) {
        byKey.put(key(hostname, family), Optional.of(List.copyOf(addresses)));
    }

    public void unavailable(String hostname, IpAddressFamily family) {
        byKey.put(key(hostname, family), Optional.empty());
    }

    public void fail(String hostname, IpAddressFamily family) {
        byKey.put(key(hostname, family), null);
    }

    @Override
    public Optional<List<String>> lookup(String hostname, IpAddressFamily family) {
        lookupCount++;
        Optional<List<String>> configured = byKey.get(key(hostname, family));
        if (configured == null && !byKey.containsKey(key(hostname, family))) {
            return Optional.empty();
        }
        if (configured == null) {
            throw new RuntimeException("forward lookup failed for " + hostname);
        }
        return configured;
    }

    private static String key(String hostname, IpAddressFamily family) {
        return hostname + "|" + family.name();
    }
}
