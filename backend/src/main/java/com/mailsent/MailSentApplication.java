package com.mailsent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * MailSent — application entry point.
 *
 * This is the Engineering Foundation shell. It starts the Spring context,
 * actuator observability, validation, JPA (PostgreSQL), Redis, and the
 * cross-cutting error/logging foundation. Business features are layered on
 * top of this shell in later phases.
 */
@SpringBootApplication
public class MailSentApplication {

    public static void main(String[] args) {
        SpringApplication.run(MailSentApplication.class, args);
    }
}