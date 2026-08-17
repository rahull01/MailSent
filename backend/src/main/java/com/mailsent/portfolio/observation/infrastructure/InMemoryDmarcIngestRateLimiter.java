package com.mailsent.portfolio.observation.infrastructure;

import java.time.Clock;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.mailsent.portfolio.observation.application.DmarcIngestRateLimitProperties;
import com.mailsent.portfolio.observation.application.DmarcIngestRateLimiter;

/**
 * Hermetic limiter for test / Redis-absent profiles. Not shared across
 * processes — production uses {@link RedisDmarcIngestRateLimiter}.
 */
public final class InMemoryDmarcIngestRateLimiter implements DmarcIngestRateLimiter {

    private final DmarcIngestRateLimitProperties limits;
    private final Clock clock;
    private final ConcurrentHashMap<String, AtomicInteger> counters = new ConcurrentHashMap<>();

    public InMemoryDmarcIngestRateLimiter(DmarcIngestRateLimitProperties limits, Clock clock) {
        this.limits = limits;
        this.clock = clock;
    }

    @Override
    public boolean allow(String clientIp, String uuid) {
        String ip = clientIp == null || clientIp.isBlank() ? "unknown" : clientIp;
        String id = uuid == null ? "" : uuid;
        long epochSecond = clock.instant().getEpochSecond();
        long minute = epochSecond / 60L;
        long hour = epochSecond / 3600L;
        int ipMinute = increment("ip:m:" + ip + ":" + minute);
        int uuidMinute = increment("uuid:m:" + id + ":" + minute);
        int uuidHour = increment("uuid:h:" + id + ":" + hour);
        int ipHour = increment("ip:h:" + ip + ":" + hour);
        return ipMinute <= limits.getPerIpPerMinute()
            && uuidMinute <= limits.getPerUuidPerMinute()
            && uuidHour <= limits.getPerUuidPerHour()
            && ipHour <= limits.getPerIpPerHour();
    }

    public void clear() {
        counters.clear();
    }

    private int increment(String key) {
        return counters.computeIfAbsent(key, ignored -> new AtomicInteger()).incrementAndGet();
    }
}
