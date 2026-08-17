package com.mailsent.reporting.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class ReportTest {

    @Test
    void generatesValidDomainReport() {
        String domainId = UUID.randomUUID().toString();
        Instant now = Instant.now();
        String content = "Health Score: 100\nStatus: Healthy";

        Report report = Report.generate(domainId, Report.TargetScope.DOMAIN, content, now);

        assertNotNull(report.id());
        assertEquals(domainId, report.targetId());
        assertEquals(Report.TargetScope.DOMAIN, report.scope());
        assertEquals(content, report.contentBody());
        assertEquals(now, report.generatedAt());
    }

    @Test
    void rejectsEmptyContentBody() {
        assertThrows(IllegalArgumentException.class, () -> 
            Report.generate(UUID.randomUUID().toString(), Report.TargetScope.PORTFOLIO, "   ", Instant.now())
        );
    }
}
