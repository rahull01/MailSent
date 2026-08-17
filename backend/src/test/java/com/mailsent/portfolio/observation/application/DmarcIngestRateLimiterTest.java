package com.mailsent.portfolio.observation.application;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.mailsent.portfolio.observation.infrastructure.InMemoryDmarcIngestRateLimiter;
import com.mailsent.portfolio.observation.infrastructure.RedisDmarcIngestRateLimiter;

@ExtendWith(MockitoExtension.class)
class DmarcIngestRateLimiterTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-08-15T12:00:00Z"), ZoneOffset.UTC);

    @Mock private StringRedisTemplate redis;
    @Mock private ValueOperations<String, String> values;

    @Test
    void inMemoryAllowsThenRejectsPerUuid() {
        DmarcIngestRateLimitProperties properties = tight();
        InMemoryDmarcIngestRateLimiter limiter = new InMemoryDmarcIngestRateLimiter(properties, CLOCK);
        assertTrue(limiter.allow("198.51.100.1", "uuid-a"));
        assertTrue(limiter.allow("198.51.100.1", "uuid-a"));
        assertFalse(limiter.allow("198.51.100.1", "uuid-a"));
        assertTrue(limiter.allow("198.51.100.2", "uuid-b"));
    }

    @Test
    void inMemoryIsolatesPerIp() {
        DmarcIngestRateLimitProperties properties = tight();
        InMemoryDmarcIngestRateLimiter limiter = new InMemoryDmarcIngestRateLimiter(properties, CLOCK);
        assertTrue(limiter.allow("198.51.100.8", "u1"));
        assertTrue(limiter.allow("198.51.100.8", "u2"));
        assertFalse(limiter.allow("198.51.100.8", "u3"));
        assertTrue(limiter.allow("198.51.100.9", "u4"));
    }

    @Test
    void redisFailClosedOnError() {
        when(redis.opsForValue()).thenReturn(values);
        when(values.increment(anyString())).thenThrow(new IllegalStateException("redis down"));
        RedisDmarcIngestRateLimiter limiter =
            new RedisDmarcIngestRateLimiter(redis, tight(), CLOCK);
        assertFalse(limiter.allow("198.51.100.1", "uuid-a"));
    }

    @Test
    void redisAllowsUnderCeiling() {
        when(redis.opsForValue()).thenReturn(values);
        when(values.increment(anyString())).thenReturn(1L);
        when(redis.expire(anyString(), any())).thenReturn(true);
        RedisDmarcIngestRateLimiter limiter =
            new RedisDmarcIngestRateLimiter(redis, tight(), CLOCK);
        assertTrue(limiter.allow("198.51.100.1", "uuid-a"));
    }

    private static DmarcIngestRateLimitProperties tight() {
        DmarcIngestRateLimitProperties properties = new DmarcIngestRateLimitProperties();
        properties.setPerIpPerMinute(2);
        properties.setPerUuidPerMinute(2);
        properties.setPerUuidPerHour(10);
        properties.setPerIpPerHour(10);
        return properties;
    }
}
