package com.mailsent.identity.authentication.application;

/**
 * Inbound port for the authentication module.
 *
 * <p><strong>Placeholder seam only.</strong> This interface marks the boundary at which
 * the application layer exposes authentication use cases to the interfaces layer. It
 * deliberately declares no methods yet: AUTH-000…AUTH-006 add the contract-level
 * operations (sign-up, authenticate, current-user, password reset) without inventing
 * stack-specific names, per the frozen architecture.</p>
 */
public interface AuthenticationPort {
    // Foundation placeholder — method seams filled by later AUTH tasks.
}