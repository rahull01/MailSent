package com.mailsent.feedback.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;

import org.junit.jupiter.api.Test;

class ContextualSuggestionTest {

    @Test
    void createsAndUpdatesSuggestion() {
        Instant now = Instant.now();
        ContextualSuggestion suggestion = ContextualSuggestion.create(
                "org-1",
                "usr-1",
                "DIAGNOSIS",
                "diag-123",
                "Include PTR reverse DNS lookup verification steps",
                "Many mailbox providers reject mail without matching PTR records",
                now
        );

        assertNotNull(suggestion.id());
        assertEquals("org-1", suggestion.organizationId());
        assertEquals("usr-1", suggestion.userId());
        assertEquals("DIAGNOSIS", suggestion.contextType());
        assertEquals("diag-123", suggestion.contextId());
        assertEquals("Include PTR reverse DNS lookup verification steps", suggestion.suggestedChange());
        assertEquals("Many mailbox providers reject mail without matching PTR records", suggestion.reasoning());
        assertEquals(SuggestionStatus.PENDING_REVIEW, suggestion.status());
        assertEquals(now, suggestion.submittedAt());

        suggestion.updateStatus(SuggestionStatus.ACCEPTED);
        assertEquals(SuggestionStatus.ACCEPTED, suggestion.status());
    }

    @Test
    void validatesBlankInputs() {
        assertThrows(IllegalArgumentException.class, () ->
                ContextualSuggestion.create("", "usr-1", "DIAG", "id", "change", "reason", Instant.now())
        );
        assertThrows(IllegalArgumentException.class, () ->
                ContextualSuggestion.create("org-1", "", "DIAG", "id", "change", "reason", Instant.now())
        );
        assertThrows(IllegalArgumentException.class, () ->
                ContextualSuggestion.create("org-1", "usr-1", "DIAG", "id", "", "reason", Instant.now())
        );
        assertThrows(IllegalArgumentException.class, () ->
                ContextualSuggestion.create("org-1", "usr-1", "DIAG", "id", "change", "", Instant.now())
        );
    }
}
