package com.mailsent.portfolio.observation.application;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Fail-closed receive rate/volume ceilings for {@code POST /ingest/dmarc/rua/{uuid}}.
 *
 * <p>Applied to every request for the path uuid and client IP before Domain
 * lookup so known and unknown uuid share the same 429 behavior (no existence
 * oracle). Defaults are conservative for public HTTP RUA: a handful of
 * reporters per domain per day, with retry headroom.</p>
 */
@ConfigurationProperties(prefix = "mailsent.ingest.dmarc.rate-limit")
public class DmarcIngestRateLimitProperties {

    /** Per client IP across all uuids in a UTC minute. */
    private int perIpPerMinute = 60;

    /** Per path uuid (known or unknown) in a UTC minute. */
    private int perUuidPerMinute = 20;

    /** Per path uuid in a UTC hour — bounds V14 receipt growth. */
    private int perUuidPerHour = 40;

    /** Per client IP in a UTC hour. */
    private int perIpPerHour = 120;

    public int getPerIpPerMinute() {
        return perIpPerMinute;
    }

    public void setPerIpPerMinute(int perIpPerMinute) {
        this.perIpPerMinute = perIpPerMinute;
    }

    public int getPerUuidPerMinute() {
        return perUuidPerMinute;
    }

    public void setPerUuidPerMinute(int perUuidPerMinute) {
        this.perUuidPerMinute = perUuidPerMinute;
    }

    public int getPerUuidPerHour() {
        return perUuidPerHour;
    }

    public void setPerUuidPerHour(int perUuidPerHour) {
        this.perUuidPerHour = perUuidPerHour;
    }

    public int getPerIpPerHour() {
        return perIpPerHour;
    }

    public void setPerIpPerHour(int perIpPerHour) {
        this.perIpPerHour = perIpPerHour;
    }
}
