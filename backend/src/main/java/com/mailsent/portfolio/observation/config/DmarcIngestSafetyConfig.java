package com.mailsent.portfolio.observation.config;

import java.time.Clock;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.mailsent.portfolio.observation.application.DmarcIngestRateLimitProperties;
import com.mailsent.portfolio.observation.application.DmarcIngestRateLimiter;
import com.mailsent.portfolio.observation.infrastructure.InMemoryDmarcIngestRateLimiter;
import com.mailsent.portfolio.observation.infrastructure.RedisDmarcIngestRateLimiter;
import com.mailsent.portfolio.observation.interfaces.DmarcIngestContentLengthFilter;

/**
 * Receive-path safety: Redis (or in-memory) rate/volume limits and Content-Length
 * rejection. Not a product API and not a scheduler.
 */
@Configuration
@EnableConfigurationProperties(DmarcIngestRateLimitProperties.class)
public class DmarcIngestSafetyConfig {

    @Bean
    @ConditionalOnBean(StringRedisTemplate.class)
    DmarcIngestRateLimiter redisDmarcIngestRateLimiter(StringRedisTemplate redis,
                                                       DmarcIngestRateLimitProperties properties,
                                                       Clock clock) {
        return new RedisDmarcIngestRateLimiter(redis, properties, clock);
    }

    @Bean
    @ConditionalOnMissingBean(DmarcIngestRateLimiter.class)
    DmarcIngestRateLimiter inMemoryDmarcIngestRateLimiter(DmarcIngestRateLimitProperties properties,
                                                          Clock clock) {
        return new InMemoryDmarcIngestRateLimiter(properties, clock);
    }

    @Bean
    FilterRegistrationBean<DmarcIngestContentLengthFilter> dmarcIngestContentLengthFilter() {
        FilterRegistrationBean<DmarcIngestContentLengthFilter> registration =
            new FilterRegistrationBean<>(new DmarcIngestContentLengthFilter());
        registration.addUrlPatterns("/ingest/dmarc/rua/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }
}
