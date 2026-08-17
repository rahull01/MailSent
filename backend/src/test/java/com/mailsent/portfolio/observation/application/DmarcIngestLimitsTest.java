package com.mailsent.portfolio.observation.application;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class DmarcIngestLimitsTest {

    @Test
    void productionCeilingsAreTheDocumentedFailClosedValues() {
        DmarcIngestLimits limits = DmarcIngestLimits.production();
        assertEquals(5 * 1024 * 1024, DmarcIngestLimits.MAX_HTTP_PAYLOAD_BYTES);
        assertEquals(20 * 1024 * 1024, DmarcIngestLimits.MAX_DECOMPRESSED_BYTES);
        assertEquals(50, DmarcIngestLimits.MAX_COMPRESSION_RATIO);
        assertEquals(32, DmarcIngestLimits.MAX_ZIP_ENTRIES);
        assertEquals(5 * 1024 * 1024, limits.maxHttpPayloadBytes());
        assertEquals(20 * 1024 * 1024, limits.maxDecompressedBytes());
        assertEquals(50, limits.maxCompressionRatio());
        assertEquals(32, limits.maxZipEntries());
    }

    @Test
    void inflationCeilingIsTheTighterOfAbsoluteAndRatioCaps() {
        DmarcIngestLimits limits = new DmarcIngestLimits(1024, 1000, 4, 8);
        assertEquals(400, limits.inflationCeiling(100));
        assertEquals(1000, limits.inflationCeiling(400));
        assertEquals(1000, limits.inflationCeiling(0));
    }
}
