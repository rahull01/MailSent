package com.mailsent.portfolio.observation.application;

/**
 * Conservative production-safety limits for INT-003 receive and INT-004 unwrap.
 *
 * <p>These are ingest hardening, not product semantics. Values are fail-closed
 * ceilings, not tuning knobs for report quality.</p>
 *
 * <ul>
 *   <li>{@link #MAX_HTTP_PAYLOAD_BYTES} = 5 MiB — typical gzipped RUA reports
 *       are tens to hundreds of KiB. Unbounded {@code readAllBytes()} is
 *       rejected. The HTTP body is capped before uuid persistence.</li>
 *   <li>{@link #MAX_DECOMPRESSED_BYTES} = 20 MiB — absolute ceiling on gzip/zip
 *       inflation and on the XML document presented to the parser.</li>
 *   <li>{@link #MAX_COMPRESSION_RATIO} = 50 — legitimate XML gzip is usually
 *       well under 20:1. Zip/gzip bombs are orders of magnitude higher.</li>
 *   <li>{@link #MAX_ZIP_ENTRIES} = 32 — a RUA zip is one XML plus incidental
 *       files. Thousands of entries are treated as a bomb.</li>
 * </ul>
 */
public final class DmarcIngestLimits {

    public static final int MAX_HTTP_PAYLOAD_BYTES = 5 * 1024 * 1024;
    public static final int MAX_DECOMPRESSED_BYTES = 20 * 1024 * 1024;
    public static final int MAX_COMPRESSION_RATIO = 50;
    public static final int MAX_ZIP_ENTRIES = 32;

    private final int maxHttpPayloadBytes;
    private final int maxDecompressedBytes;
    private final int maxCompressionRatio;
    private final int maxZipEntries;

    public DmarcIngestLimits(int maxHttpPayloadBytes,
                             int maxDecompressedBytes,
                             int maxCompressionRatio,
                             int maxZipEntries) {
        this.maxHttpPayloadBytes = maxHttpPayloadBytes;
        this.maxDecompressedBytes = maxDecompressedBytes;
        this.maxCompressionRatio = maxCompressionRatio;
        this.maxZipEntries = maxZipEntries;
    }

    public static DmarcIngestLimits production() {
        return new DmarcIngestLimits(
            MAX_HTTP_PAYLOAD_BYTES,
            MAX_DECOMPRESSED_BYTES,
            MAX_COMPRESSION_RATIO,
            MAX_ZIP_ENTRIES);
    }

    public int maxHttpPayloadBytes() {
        return maxHttpPayloadBytes;
    }

    public int maxDecompressedBytes() {
        return maxDecompressedBytes;
    }

    public int maxCompressionRatio() {
        return maxCompressionRatio;
    }

    public int maxZipEntries() {
        return maxZipEntries;
    }

    /**
     * Ceiling for one inflate of {@code compressedBytes}, the tighter of the
     * absolute decompressed cap and {@code compressedBytes * ratio}.
     */
    public int inflationCeiling(int compressedBytes) {
        int absolute = maxDecompressedBytes;
        if (compressedBytes <= 0) {
            return absolute;
        }
        long ratioCap = (long) compressedBytes * (long) maxCompressionRatio;
        if (ratioCap > Integer.MAX_VALUE) {
            return absolute;
        }
        return (int) Math.min(absolute, ratioCap);
    }
}
