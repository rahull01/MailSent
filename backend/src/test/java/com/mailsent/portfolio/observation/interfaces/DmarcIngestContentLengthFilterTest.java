package com.mailsent.portfolio.observation.interfaces;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import com.mailsent.portfolio.observation.application.DmarcIngestLimits;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

class DmarcIngestContentLengthFilterTest {

    @Test
    void declaredLengthOverCapIs413AndDoesNotContinue() throws Exception {
        DmarcIngestContentLengthFilter filter = new DmarcIngestContentLengthFilter();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/ingest/dmarc/rua/cap");
        when(request.getContentLengthLong()).thenReturn(DmarcIngestLimits.MAX_HTTP_PAYLOAD_BYTES + 1L);
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean continued = new AtomicBoolean(false);
        filter.doFilter(request, response, (req, res) -> continued.set(true));
        assertEquals(413, response.getStatus());
        assertFalse(continued.get());
    }

    @Test
    void inCapLengthContinues() throws Exception {
        DmarcIngestContentLengthFilter filter = new DmarcIngestContentLengthFilter();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/ingest/dmarc/rua/ok");
        when(request.getContentLengthLong()).thenReturn(12L);
        HttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean continued = new AtomicBoolean(false);
        filter.doFilter(request, response, (req, res) -> continued.set(true));
        assertTrue(continued.get());
        assertEquals(200, ((MockHttpServletResponse) response).getStatus());
    }
}
