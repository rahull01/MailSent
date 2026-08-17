package com.mailsent.portfolio.observation.interfaces;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.mailsent.portfolio.domain.application.DomainRepository;
import com.mailsent.portfolio.domain.domain.Domain;
import com.mailsent.portfolio.observation.application.DmarcIngestLimits;
import com.mailsent.portfolio.observation.application.DmarcIngestRateLimiter;
import com.mailsent.portfolio.observation.application.DmarcReportReceiptRepository;
import com.mailsent.portfolio.observation.application.ObservationEvidenceRepository;
import com.mailsent.portfolio.observation.domain.DmarcReportReceipt;
import com.mailsent.portfolio.observation.infrastructure.InMemoryDmarcIngestRateLimiter;

/**
 * HTTP tests for the INT-003 ingest webhook — not a public listing API, RUA
 * only, uuid capability, same empty ack for unknown and known Domain.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DmarcReportReceiveControllerTest {

    private static final byte[] PAYLOAD = "<feedback/>".getBytes();

    @Autowired private MockMvc mockMvc;
    @Autowired private DomainRepository domains;
    @Autowired private DmarcReportReceiptRepository receipts;
    @Autowired private ObservationEvidenceRepository evidence;
    @Autowired private DmarcIngestLimits limits;
    @Autowired private DmarcIngestRateLimiter rateLimiter;

    @BeforeEach
    void resetRateLimiter() {
        if (rateLimiter instanceof InMemoryDmarcIngestRateLimiter inMemory) {
            inMemory.clear();
        }
    }

    @Test
    void unknownUuidReturnsNoContentAndPersistsNothing() throws Exception {
        String unknown = UUID.randomUUID().toString();

        mockMvc.perform(post("/ingest/dmarc/rua/" + unknown)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .content(PAYLOAD))
            .andExpect(status().isNoContent())
            .andExpect(content().string(""));

        assertTrue(receipts.findByDomainId(unknown).isEmpty());
        assertTrue(evidence.findByDomainId(unknown).isEmpty());
    }

    @Test
    void registeredDomainUuidPersistsBytesWithoutListingBody() throws Exception {
        Domain domain = domains.save(Domain.register("org-webhook", "ingest.example.com", Instant.now()));

        mockMvc.perform(post("/ingest/dmarc/rua/" + domain.id())
                .contentType(MediaType.APPLICATION_XML)
                .content(PAYLOAD))
            .andExpect(status().isNoContent())
            .andExpect(content().string(""));

        var stored = receipts.findByDomainId(domain.id());
        assertEquals(1, stored.size());
        DmarcReportReceipt receipt = stored.get(0);
        assertEquals(domain.organizationId(), receipt.organizationId());
        assertArrayEquals(PAYLOAD, receipt.payload());
        assertTrue(receipt.reportId() == null);
        assertTrue(evidence.findByDomainId(domain.id()).isEmpty());
    }

    @Test
    void getOnReceivePathIsNotAListingApi() throws Exception {
        var getReceive = mockMvc.perform(get("/ingest/dmarc/rua/" + UUID.randomUUID())).andReturn();
        assertTrue(getReceive.getResponse().getStatus() != 200);
        String body = getReceive.getResponse().getContentAsString();
        assertTrue(body == null || !body.contains("payload"));

        mockMvc.perform(get("/ingest/dmarc/reports"))
            .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/dmarc/reports"))
            .andExpect(status().isNotFound());
    }

    @Test
    void rufHasNoSeparatePath() throws Exception {
        mockMvc.perform(post("/ingest/dmarc/ruf/" + UUID.randomUUID())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .content(PAYLOAD))
            .andExpect(status().isNotFound());
    }

    @Test
    void oversizedHttpPayloadReturns413AndDoesNotPersist() throws Exception {
        Domain domain = domains.save(Domain.register("org-webhook", "oversize.example.com", Instant.now()));
        int before = receipts.findByDomainId(domain.id()).size();
        byte[] oversized = new byte[limits.maxHttpPayloadBytes() + 1];

        mockMvc.perform(post("/ingest/dmarc/rua/" + domain.id())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .content(oversized))
            .andExpect(status().isPayloadTooLarge())
            .andExpect(content().string(""));

        assertEquals(before, receipts.findByDomainId(domain.id()).size());
        assertTrue(evidence.findByDomainId(domain.id()).isEmpty());
    }

    @Test
    void oversizedHttpPayloadForUnknownUuidStillReturns413WithoutPersisting() throws Exception {
        String unknown = UUID.randomUUID().toString();
        byte[] oversized = new byte[limits.maxHttpPayloadBytes() + 1];

        mockMvc.perform(post("/ingest/dmarc/rua/" + unknown)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .content(oversized))
            .andExpect(status().isPayloadTooLarge())
            .andExpect(content().string(""));

        assertTrue(receipts.findByDomainId(unknown).isEmpty());
    }

    @Test
    void inLimitPayloadStillReturns204AndPersists() throws Exception {
        Domain domain = domains.save(Domain.register("org-webhook", "at-cap.example.com", Instant.now()));
        byte[] atCap = new byte[64];
        Arrays.fill(atCap, (byte) 'x');

        mockMvc.perform(post("/ingest/dmarc/rua/" + domain.id())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .content(atCap))
            .andExpect(status().isNoContent())
            .andExpect(content().string(""));

        var stored = receipts.findByDomainId(domain.id());
        assertEquals(1, stored.size());
        assertArrayEquals(atCap, stored.get(0).payload());
    }

    @Test
    void readCappedRejectsPastLimitWithoutThrowing() throws Exception {
        byte[] four = {1, 2, 3, 4};
        assertEquals(null, DmarcReportReceiveController.readCapped(new ByteArrayInputStream(four), 3));
        assertArrayEquals(new byte[] {1, 2, 3, 4},
            DmarcReportReceiveController.readCapped(new ByteArrayInputStream(four), 4));
        assertArrayEquals(new byte[] {1, 2, 3, 4},
            DmarcReportReceiveController.readCapped(new ByteArrayInputStream(four), 8));
        assertArrayEquals(new byte[0],
            DmarcReportReceiveController.readCapped(new ByteArrayInputStream(new byte[0]), 5));
    }

    @Test
    void rateLimitAcceptedRequestRemains204() throws Exception {
        Domain domain = domains.save(Domain.register("org-webhook", "rate-ok.example.com", Instant.now()));

        mockMvc.perform(post("/ingest/dmarc/rua/" + domain.id())
                .with(request -> {
                    request.setRemoteAddr("203.0.113.10");
                    return request;
                })
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .content(PAYLOAD))
            .andExpect(status().isNoContent())
            .andExpect(content().string(""));

        assertEquals(1, receipts.findByDomainId(domain.id()).size());
    }

    @Test
    void rateLimitExceededReturns429AndDoesNotPersistFurtherReceipts() throws Exception {
        Domain domain = domains.save(Domain.register("org-webhook", "rate-429.example.com", Instant.now()));
        String ip = "203.0.113.20";
        int allowed = 20;
        for (int i = 0; i < allowed; i++) {
            mockMvc.perform(post("/ingest/dmarc/rua/" + domain.id())
                    .with(request -> {
                        request.setRemoteAddr(ip);
                        return request;
                    })
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .content(PAYLOAD))
                .andExpect(status().isNoContent());
        }
        int stored = receipts.findByDomainId(domain.id()).size();
        mockMvc.perform(post("/ingest/dmarc/rua/" + domain.id())
                .with(request -> {
                    request.setRemoteAddr(ip);
                    return request;
                })
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .content(PAYLOAD))
            .andExpect(status().isTooManyRequests())
            .andExpect(content().string(""));
        assertEquals(stored, receipts.findByDomainId(domain.id()).size());
    }

    @Test
    void rateLimitIsIsolatedPerUuid() throws Exception {
        Domain first = domains.save(Domain.register("org-webhook", "rate-a.example.com", Instant.now()));
        Domain second = domains.save(Domain.register("org-webhook", "rate-b.example.com", Instant.now()));
        String ip = "203.0.113.30";
        for (int i = 0; i < 20; i++) {
            mockMvc.perform(post("/ingest/dmarc/rua/" + first.id())
                    .with(request -> {
                        request.setRemoteAddr(ip);
                        return request;
                    })
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .content(PAYLOAD))
                .andExpect(status().isNoContent());
        }
        mockMvc.perform(post("/ingest/dmarc/rua/" + first.id())
                .with(request -> {
                    request.setRemoteAddr(ip);
                    return request;
                })
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .content(PAYLOAD))
            .andExpect(status().isTooManyRequests());
        mockMvc.perform(post("/ingest/dmarc/rua/" + second.id())
                .with(request -> {
                    request.setRemoteAddr(ip);
                    return request;
                })
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .content(PAYLOAD))
            .andExpect(status().isNoContent());
        assertEquals(1, receipts.findByDomainId(second.id()).size());
    }

    @Test
    void rateLimitIsIsolatedPerIp() throws Exception {
        String ipOne = "203.0.113.40";
        String ipTwo = "203.0.113.41";
        for (int i = 0; i < 60; i++) {
            mockMvc.perform(post("/ingest/dmarc/rua/" + UUID.randomUUID())
                    .with(request -> {
                        request.setRemoteAddr(ipOne);
                        return request;
                    })
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .content(PAYLOAD))
                .andExpect(status().isNoContent());
        }
        mockMvc.perform(post("/ingest/dmarc/rua/" + UUID.randomUUID())
                .with(request -> {
                    request.setRemoteAddr(ipOne);
                    return request;
                })
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .content(PAYLOAD))
            .andExpect(status().isTooManyRequests());
        mockMvc.perform(post("/ingest/dmarc/rua/" + UUID.randomUUID())
                .with(request -> {
                    request.setRemoteAddr(ipTwo);
                    return request;
                })
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .content(PAYLOAD))
            .andExpect(status().isNoContent());
    }

    @Test
    void unknownUuidRateLimitMatchesKnownUuidSoExistenceIsNotLeaked() throws Exception {
        Domain known = domains.save(Domain.register("org-webhook", "rate-known.example.com", Instant.now()));
        String unknown = UUID.randomUUID().toString();
        String knownIp = "203.0.113.50";
        String unknownIp = "203.0.113.51";
        for (int i = 0; i < 20; i++) {
            mockMvc.perform(post("/ingest/dmarc/rua/" + known.id())
                    .with(request -> {
                        request.setRemoteAddr(knownIp);
                        return request;
                    })
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .content(PAYLOAD))
                .andExpect(status().isNoContent());
            mockMvc.perform(post("/ingest/dmarc/rua/" + unknown)
                    .with(request -> {
                        request.setRemoteAddr(unknownIp);
                        return request;
                    })
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .content(PAYLOAD))
                .andExpect(status().isNoContent());
        }
        mockMvc.perform(post("/ingest/dmarc/rua/" + known.id())
                .with(request -> {
                    request.setRemoteAddr(knownIp);
                    return request;
                })
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .content(PAYLOAD))
            .andExpect(status().isTooManyRequests())
            .andExpect(content().string(""));
        mockMvc.perform(post("/ingest/dmarc/rua/" + unknown)
                .with(request -> {
                    request.setRemoteAddr(unknownIp);
                    return request;
                })
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .content(PAYLOAD))
            .andExpect(status().isTooManyRequests())
            .andExpect(content().string(""));
        assertTrue(receipts.findByDomainId(unknown).isEmpty());
    }

    @Test
    void controllerExposesOnlyPostReceive() {
        RequestMapping mapping = DmarcReportReceiveController.class.getAnnotation(RequestMapping.class);
        assertEquals("/ingest/dmarc/rua", mapping.value()[0]);
        assertTrue(Arrays.stream(DmarcReportReceiveController.class.getDeclaredMethods())
            .noneMatch(m -> m.isAnnotationPresent(GetMapping.class)));
        assertTrue(Arrays.stream(DmarcReportReceiveController.class.getDeclaredMethods())
            .filter(m -> "receive".equals(m.getName()))
            .map(Method::getReturnType)
            .noneMatch(t -> t.getSimpleName().contains("List")));
        assertTrue(Arrays.stream(DmarcReportReceiveController.class.getDeclaredFields())
            .noneMatch(f -> f.getType().getSimpleName().contains("Trigger")
                || f.getType().getSimpleName().contains("Parse")
                || f.getType().getSimpleName().contains("Aggregate")));
    }
}
