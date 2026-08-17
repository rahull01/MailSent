package com.mailsent.portfolio.observation.interfaces;

import java.io.IOException;

import com.mailsent.portfolio.observation.application.DmarcIngestLimits;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Rejects ingest POSTs whose {@code Content-Length} exceeds the 5 MiB cap
 * before the body is read. Chunked/missing length still hits
 * {@code readNBytes} in the controller.
 */
public final class DmarcIngestContentLengthFilter extends OncePerRequestFilter {

    static final String INGEST_PREFIX = "/ingest/dmarc/rua/";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (isIngestPost(request) && declaredLengthExceedsCap(request)) {
            response.setStatus(HttpStatus.PAYLOAD_TOO_LARGE.value());
            return;
        }
        filterChain.doFilter(request, response);
    }

    private static boolean isIngestPost(HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod())
            && request.getRequestURI() != null
            && request.getRequestURI().contains(INGEST_PREFIX);
    }

    private static boolean declaredLengthExceedsCap(HttpServletRequest request) {
        long length = request.getContentLengthLong();
        return length > DmarcIngestLimits.MAX_HTTP_PAYLOAD_BYTES;
    }
}
