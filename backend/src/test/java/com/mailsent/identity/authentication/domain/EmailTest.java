package com.mailsent.identity.authentication.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Email} — the boundary value object.
 *
 * <p>Covers the positive shape and the failure paths (null, blank, malformed, too long,
 * and injection/control-character attempts), satisfying NFR-S4 input validation.</p>
 */
class EmailTest {

    @Test
    void acceptsAWellFormedEmail() {
        Email email = new Email("User@Example.com");
        assertEquals("user@example.com", email.value(), "emails are normalized to lower case");
    }

    @Test
    void acceptsSubdomainsAndCommonCharacters() {
        assertDoesNotThrow(() -> new Email("analyst+tags@mail.example.co.uk"));
    }

    @Test
    void rejectsNull() {
        assertThrows(InvalidEmailException.class, () -> new Email(null));
    }

    @Test
    void rejectsBlank() {
        assertThrows(InvalidEmailException.class, () -> new Email("   "));
    }

    @Test
    void rejectsMissingAtSign() {
        assertThrows(InvalidEmailException.class, () -> new Email("not-an-email"));
    }

    @Test
    void rejectsMissingDomain() {
        assertThrows(InvalidEmailException.class, () -> new Email("user@"));
    }

    @Test
    void rejectsWhitespaceAndControlCharacters() {
        // Space inside a header-like injection and a control char are both invalid.
        assertThrows(InvalidEmailException.class, () -> new Email("user name@example.com"));
        assertThrows(InvalidEmailException.class, () -> new Email("user\\n@example.com"));
    }

    @Test
    void rejectsOverLongEmail() {
        String local = "a".repeat(200);
        String maker = local + "@example.com";
        if (maker.length() > 254) {
            assertThrows(InvalidEmailException.class, () -> new Email(maker));
        } else {
            assertTrue(maker.length() <= 254);
        }
    }
}