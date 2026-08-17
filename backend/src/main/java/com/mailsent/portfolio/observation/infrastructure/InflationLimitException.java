package com.mailsent.portfolio.observation.infrastructure;

import java.io.IOException;

/** Gzip/zip inflation exceeded {@link com.mailsent.portfolio.observation.application.DmarcIngestLimits}. */
final class InflationLimitException extends IOException {

    private static final long serialVersionUID = 1L;

    InflationLimitException() {
        super("decompression limit exceeded");
    }
}
