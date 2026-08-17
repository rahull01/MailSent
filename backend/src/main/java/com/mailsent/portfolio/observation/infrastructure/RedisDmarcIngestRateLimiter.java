package com.mailsent.portfolio.observation.infrastructure;

import java.time.Clock;
import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;

import com.mailsent.portfolio.observation.application.DmarcIngestRateLimitProperties;
import com.mailsent.portfolio.observation.application.DmarcIngestRateLimiter;

/**
 * Distributed INT-003 limiter. Fail closed: Redis errors deny the request.
 */
public final class RedisDmarcIngestRateLimiter implements DmarcIngestRateLimiter {

    private static final String PREFIX = "mailsent:ingest:rua:";

    private final StringRedisTemplate redis;
    private final DmarcIngestRateLimitProperties limits;
    private final Clock clock;

    public RedisDmarcIngestRateLimiter(StringRedisTemplate redis,
                                       DmarcIngestRateLimitProperties limits,
                                       Clock clock) {
        this.redis = redis;
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
        try {
            long ipMinute = increment(PREFIX + "ip:m:" + ip + ":" + minute, Duration.ofMinutes(2));
            long uuidMinute = increment(PREFIX + "uuid:m:" + id + ":" + minute, Duration.ofMinutes(2));
            long uuidHour = increment(PREFIX + "uuid:h:" + id + ":" + hour, Duration.ofHours(2));
            long ipHour = increment(PREFIX + "ip:h:" + ip + ":" + hour, Duration.ofHours(2));
            return ipMinute <= limits.getPerIpPerMinute()
                && uuidMinute <= limits.getPerUuidPerMinute()
                && uuidHour <= limits.getPerUuidPerHour()
                && ipHour <= limits.getPerIpPerHour();
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private long increment(String key, Duration ttl) {
        Long value = redis.opsForValue().increment(key);
        if (value != null && value == 1L) {
            redis.expire(key, ttl);
        }
        return value == null ? Long.MAX_VALUE : value;
    }
}
