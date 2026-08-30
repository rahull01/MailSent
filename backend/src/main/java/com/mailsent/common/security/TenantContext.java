package com.mailsent.common.security;

import java.util.Optional;

/**
 * TenantContext — ThreadLocal context holder for the current request's authenticated tenant.
 *
 * <p>Enforces multi-tenant isolation (INV-09). The context is populated by
 * {@link SessionAuthenticationFilter} on verified requests and is guaranteed to be
 * cleared in a {@code finally} block upon request completion.</p>
 */
public final class TenantContext {

    private static final ThreadLocal<AuthenticatedIdentity> CURRENT = new ThreadLocal<>();

    private TenantContext() {
        // Static utility class
    }

    /**
     * Binds the authenticated identity to the current thread.
     */
    public static void set(AuthenticatedIdentity identity) {
        CURRENT.set(identity);
    }

    /**
     * Retrieves the optional authenticated identity for the current thread.
     */
    public static Optional<AuthenticatedIdentity> get() {
        return Optional.ofNullable(CURRENT.get());
    }

    /**
     * Retrieves the authenticated identity or throws {@link UnauthenticatedException} if absent.
     */
    public static AuthenticatedIdentity getRequired() {
        return get().orElseThrow(() -> new UnauthenticatedException("No authenticated tenant context available."));
    }

    /**
     * Shorthand to retrieve the validated organizationId for the current caller.
     */
    public static String getOrganizationId() {
        return getRequired().organizationId();
    }

    /**
     * Shorthand to retrieve the validated userId for the current caller.
     */
    public static String getUserId() {
        return getRequired().userId();
    }

    /**
     * Clears the current thread's context. Guaranteed to be invoked in a filter finally block.
     */
    public static void clear() {
        CURRENT.remove();
    }
}
