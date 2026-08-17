package com.mailsent.portfolio.observation.infrastructure;

import java.io.IOException;
import java.io.InputStream;

import com.mailsent.portfolio.observation.application.DmarcIngestLimits;

/**
 * Counts bytes read from an already-decompressing stream and fails closed
 * when {@link DmarcIngestLimits#inflationCeiling(int)} is exceeded.
 *
 * <p>Wrap the <em>decompressed</em> stream, not the compressed bytes:
 * {@code new BoundedInflatingInputStream(new GZIPInputStream(compressed),
 * compressed.length, limits)}. Closing this stream does not close the
 * delegate so a {@code ZipInputStream} can advance to the next entry.</p>
 */
final class BoundedInflatingInputStream extends InputStream {

    private final InputStream inflated;
    private final int ceiling;
    private int total;

    BoundedInflatingInputStream(InputStream inflated, int compressedBytes, DmarcIngestLimits limits) {
        this(inflated, limits.inflationCeiling(compressedBytes));
    }

    BoundedInflatingInputStream(InputStream inflated, int ceiling) {
        this.inflated = inflated;
        this.ceiling = ceiling;
    }

    @Override
    public int read() throws IOException {
        int b = inflated.read();
        if (b >= 0) {
            account(1);
        }
        return b;
    }

    @Override
    public int read(byte[] buffer, int off, int len) throws IOException {
        int n = inflated.read(buffer, off, len);
        if (n > 0) {
            account(n);
        }
        return n;
    }

    @Override
    public long skip(long n) throws IOException {
        long skipped = inflated.skip(n);
        if (skipped > 0) {
            if (skipped > Integer.MAX_VALUE) {
                throw new InflationLimitException();
            }
            account((int) skipped);
        }
        return skipped;
    }

    @Override
    public int available() throws IOException {
        return inflated.available();
    }

    @Override
    public void close() {
        // Caller owns the delegate (especially ZipInputStream).
    }

    private void account(int n) throws IOException {
        int next = total + n;
        if (next < total || next > ceiling) {
            throw new InflationLimitException();
        }
        total = next;
    }
}
