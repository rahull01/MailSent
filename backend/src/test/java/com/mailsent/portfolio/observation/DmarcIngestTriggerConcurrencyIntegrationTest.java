package com.mailsent.portfolio.observation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import com.mailsent.portfolio.domain.application.DomainService;
import com.mailsent.portfolio.domain.domain.Domain;
import com.mailsent.portfolio.observation.application.Diag001AlignmentDerivationPort;
import com.mailsent.portfolio.observation.application.DmarcAggregateEvidenceService;
import com.mailsent.portfolio.observation.application.DmarcIngestTriggerService;
import com.mailsent.portfolio.observation.application.DmarcReportReceiveService;
import com.mailsent.portfolio.observation.domain.DmarcReportReceipt;
import com.mailsent.portfolio.organization.application.OrganizationService;

/**
 * PostgreSQL concurrent INT-006: one DIAG-001 trigger for one occupancy.
 */
@SpringBootTest
@ActiveProfiles("it")
@EnabledIfSystemProperty(named = "mailsent.it.postgres", matches = "true")
class DmarcIngestTriggerConcurrencyIntegrationTest {

    private static final String XML = """
        <?xml version="1.0" encoding="UTF-8"?>
        <feedback>
          <report_metadata>
            <org_name>google.com</org_name>
            <report_id>it-concurrent-trigger</report_id>
            <date_range><begin>1609459200</begin><end>1609545599</end></date_range>
          </report_metadata>
          <policy_published><domain>other.example</domain><p>none</p></policy_published>
          <record>
            <row>
              <source_ip>192.0.2.1</source_ip>
              <count>1</count>
              <policy_evaluated><disposition>none</disposition><dkim>pass</dkim><spf>fail</spf></policy_evaluated>
            </row>
            <identifiers><header_from>example.com</header_from></identifiers>
          </record>
        </feedback>
        """;

    @MockBean private Diag001AlignmentDerivationPort diag001;
    @Autowired private OrganizationService organizationService;
    @Autowired private DomainService domainService;
    @Autowired private DmarcReportReceiveService receiveService;
    @Autowired private DmarcIngestTriggerService triggerService;
    @Autowired private DmarcAggregateEvidenceService aggregateService;

    @Test
    void concurrentProcessTriggersDiag001Once() throws Exception {
        AtomicInteger triggers = new AtomicInteger();
        doAnswer(invocation -> {
            triggers.incrementAndGet();
            return null;
        }).when(diag001).trigger(any());

        String orgId = organizationService.create("DMARC-Trigger-C " + UUID.randomUUID()).id();
        Domain domain = domainService.register(orgId, "it-trig-c-" + UUID.randomUUID() + ".com");
        String xml = XML.replace("it-concurrent-trigger", "it-c-" + UUID.randomUUID());
        byte[] payload = xml.getBytes(StandardCharsets.UTF_8);
        DmarcReportReceipt first = receiveService.receive(domain.id(), payload).orElseThrow();
        DmarcReportReceipt second = receiveService.receive(domain.id(), payload).orElseThrow();

        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch go = new CountDownLatch(1);
        try {
            pool.submit(() -> {
                ready.countDown();
                go.await();
                triggerService.process(first.id());
                return null;
            });
            pool.submit(() -> {
                ready.countDown();
                go.await();
                triggerService.process(second.id());
                return null;
            });
            assertTrue(ready.await(5, TimeUnit.SECONDS));
            go.countDown();
            pool.shutdown();
            assertTrue(pool.awaitTermination(20, TimeUnit.SECONDS));
        } finally {
            pool.shutdownNow();
        }

        assertEquals(1, aggregateService.findByDomainId(domain.id()).size());
        assertEquals(1, triggers.get());
    }
}
