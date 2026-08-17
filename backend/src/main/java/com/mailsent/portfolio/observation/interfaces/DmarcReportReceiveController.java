package com.mailsent.portfolio.observation.interfaces;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mailsent.portfolio.observation.application.DmarcIngestLimits;
import com.mailsent.portfolio.observation.application.DmarcIngestRateLimiter;
import com.mailsent.portfolio.observation.application.DmarcReportReceiveService;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Inbound RUA aggregate ingest webhook (INT-003).
 *
 * <p>System ingest transport only — not a public product REST resource, not a
 * Diagnosis/Alignment API, and not an end-user session. {@code uuid} is the
 * registered Domain id (the RUA capability). Unknown uuid is discarded with the
 * same empty acknowledgement so existence is not leaked. There is no listing
 * or query mapping. There is no RUF path. INT-006 is not invoked here —
 * receive does not parse, store, or trigger DIAG-001.</p>
 */
@RestController
@RequestMapping("/ingest/dmarc/rua")
public class DmarcReportReceiveController {

    private final DmarcReportReceiveService receive;
    private final DmarcIngestLimits limits;
    private final DmarcIngestRateLimiter rateLimiter;

    public DmarcReportReceiveController(DmarcReportReceiveService receive,
                                        DmarcIngestLimits limits,
                                        DmarcIngestRateLimiter rateLimiter) {
        this.receive = receive;
        this.limits = limits;
        this.rateLimiter = rateLimiter;
    }

    /**
     * Accepts opaque report bytes for {@code uuid}. 204 when the body is within
     * the ingest size cap and rate/volume limits: stored if the Domain is
     * registered, discarded otherwise. Bodies larger than
     * {@link DmarcIngestLimits#maxHttpPayloadBytes()} are rejected with 413 and
     * not persisted. Rate/volume exceeded is 429 for known and unknown uuid
     * alike. Body is not parsed as XML.
     */
    @PostMapping("/{uuid}")
    public ResponseEntity<Void> receive(@PathVariable("uuid") String uuid, HttpServletRequest request)
            throws IOException {
        byte[] payload = readCapped(request.getInputStream(), limits.maxHttpPayloadBytes());
        if (payload == null) {
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).build();
        }
        if (!rateLimiter.allow(clientIp(request), uuid)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
        receive.receive(uuid, payload);
        return ResponseEntity.noContent().build();
    }

    static String clientIp(HttpServletRequest request) {
        String remote = request.getRemoteAddr();
        return remote == null || remote.isBlank() ? "unknown" : remote;
    }

    /**
     * Reads at most {@code maxBytes}. Returns {@code null} when the stream
     * exceeds the cap so the caller can fail closed without persisting.
     */
    static byte[] readCapped(InputStream in, int maxBytes) throws IOException {
        byte[] bounded = in.readNBytes(maxBytes + 1);
        if (bounded.length > maxBytes) {
            return null;
        }
        return bounded;
    }
}
