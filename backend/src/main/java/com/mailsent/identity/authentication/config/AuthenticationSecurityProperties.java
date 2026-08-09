package com.mailsent.identity.authentication.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import jakarta.annotation.PostConstruct;

/**
 * AuthenticationSecurityProperties — externalized, configurable security policies for
 * the authentication module.
 *
 * <p>Security values are never hardcoded; operators override them per environment via
 * Spring Boot configuration. The defaults are secure <strong>and non-negotiable
 * downward</strong>: {@link #validateFloors()} rejects any configuration that would
 * weaken a policy below its safe floor, so an environment cannot silently reduce its
 * security.</p>
 *
 * <p>Bound prefix: {@code mailsent.auth.authentication}.</p>
 */
@ConfigurationProperties(prefix = "mailsent.auth.authentication")
public class AuthenticationSecurityProperties {

    /** Absolute floor beneath which a password policy is not acceptable (NIST 2023). */
    private static final int MIN_PASSWORD_LENGTH_FLOOR = 12;

    /** Absolute floor for PBKDF2-HMAC-SHA256 iterations (OWASP 2023). */
    private static final int MIN_PBKDF2_ITERATIONS_FLOOR = 600_000;

    /** Absolute floor for the random salt length in bytes. */
    private static final int MIN_SALT_BYTES_FLOOR = 16;

    /** Enforced minimum password length at sign up (default: OWASP/NIST floor). */
    private int passwordMinLength = MIN_PASSWORD_LENGTH_FLOOR;

    /** PBKDF2-HMAC-SHA256 iteration count (default: OWASP floor). */
    private int pbkdf2Iterations = MIN_PBKDF2_ITERATIONS_FLOOR;

    /** Random salt length in bytes for password hashing (never below 16). */
    private int pbkdf2SaltBytes = MIN_SALT_BYTES_FLOOR;

    /** Session lifetime in minutes (bounded: never zero/negative, capped by floor). */
    private long sessionLifetimeMinutes = 60;

    /**
     * Rejects configuration that would reduce security below the safe floor.
     *
     * <p>Throws on startup so a misconfigured environment fails fast rather than
     * shipping a weaker hash than the platform promises.</p>
     */
    @PostConstruct
    void validateFloors() {
        if (passwordMinLength < MIN_PASSWORD_LENGTH_FLOOR) {
            throw new IllegalStateException(
                "mailsent.auth.authentication.password-min-length must be >= " + MIN_PASSWORD_LENGTH_FLOOR);
        }
        if (pbkdf2Iterations < MIN_PBKDF2_ITERATIONS_FLOOR) {
            throw new IllegalStateException(
                "mailsent.auth.authentication.pbkdf2-iterations must be >= " + MIN_PBKDF2_ITERATIONS_FLOOR);
        }
        if (pbkdf2SaltBytes < MIN_SALT_BYTES_FLOOR) {
            throw new IllegalStateException(
                "mailsent.auth.authentication.pbkdf2-salt-bytes must be >= " + MIN_SALT_BYTES_FLOOR);
        }
        if (sessionLifetimeMinutes <= 0) {
            throw new IllegalStateException(
                "mailsent.auth.authentication.session-lifetime-minutes must be positive");
        }
    }

    public int getPasswordMinLength() {
        return passwordMinLength;
    }

    public void setPasswordMinLength(int passwordMinLength) {
        this.passwordMinLength = passwordMinLength;
    }

    public int getPbkdf2Iterations() {
        return pbkdf2Iterations;
    }

    public void setPbkdf2Iterations(int pbkdf2Iterations) {
        this.pbkdf2Iterations = pbkdf2Iterations;
    }

    public int getPbkdf2SaltBytes() {
        return pbkdf2SaltBytes;
    }

    public void setPbkdf2SaltBytes(int pbkdf2SaltBytes) {
        this.pbkdf2SaltBytes = pbkdf2SaltBytes;
    }

    public long getSessionLifetimeMinutes() {
        return sessionLifetimeMinutes;
    }

    public void setSessionLifetimeMinutes(long sessionLifetimeMinutes) {
        this.sessionLifetimeMinutes = sessionLifetimeMinutes;
    }

    /** Security floor for password length, for tests and audits. */
    static int minPasswordLengthFloor() {
        return MIN_PASSWORD_LENGTH_FLOOR;
    }

    /** Security floor for PBKDF2 iterations, for tests and audits. */
    static int minPbkdf2IterationsFloor() {
        return MIN_PBKDF2_ITERATIONS_FLOOR;
    }
}