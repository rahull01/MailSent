package com.mailsent.resolution.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

class ResolutionActionTest {

    @Test
    void createsActionWithAllFields() {
        List<String> steps = List.of("Log in to DNS provider", "Add TXT record");
        ResolutionAction action = ResolutionAction.create("Fix SPF", "Update SPF record", steps, 1);

        assertNotNull(action.id());
        assertEquals("Fix SPF", action.title());
        assertEquals("Update SPF record", action.description());
        assertEquals(2, action.exactSteps().size());
        assertEquals(1, action.priority());
        assertEquals(DnsProvider.GENERIC_DNS, action.dnsProvider());
    }

    @Test
    void createsProviderTailoredActionForIssue() {
        List<String> steps = List.of("Open Cloudflare", "Set TXT record with Proxy DNS Only");
        ResolutionAction action = ResolutionAction.forIssue("iss-1", "Fix DMARC in Cloudflare",
                "Publish DMARC record", DnsProvider.CLOUDFLARE, steps, "TXT", "_dmarc.example.com",
                "v=DMARC1; p=none", 1);

        assertThat(action.issueId()).isEqualTo("iss-1");
        assertThat(action.dnsProvider()).isEqualTo(DnsProvider.CLOUDFLARE);
        assertThat(action.recordType()).isEqualTo("TXT");
        assertThat(action.recordName()).isEqualTo("_dmarc.example.com");
        assertThat(action.recordValue()).isEqualTo("v=DMARC1; p=none");
        assertThat(action.exactSteps()).containsExactlyElementsOf(steps);
    }

    @Test
    void requiresNonNullFields() {
        assertThrows(NullPointerException.class, () ->
            new ResolutionAction("id-1", null, null, "desc", DnsProvider.GENERIC_DNS, List.of("step"), null, null, null, 1)
        );
        assertThrows(NullPointerException.class, () ->
            new ResolutionAction("id-1", null, "title", null, DnsProvider.GENERIC_DNS, List.of("step"), null, null, null, 1)
        );
        assertThrows(NullPointerException.class, () ->
            new ResolutionAction("id-1", null, "title", "desc", null, List.of("step"), null, null, null, 1)
        );
        assertThrows(NullPointerException.class, () ->
            new ResolutionAction("id-1", null, "title", "desc", DnsProvider.GENERIC_DNS, null, null, null, null, 1)
        );
    }
}
