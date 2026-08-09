/**
 * Application layer of the authentication module.
 *
 * <p>Coordinates domain behavior for inbound use cases (sign-up, sign-in, password
 * reset, current-user) and defines the ports the infrastructure adapts to. Per the
 * frozen architecture, the application layer is where use cases are orchestrated and
 * where the anti-corruption to the domain is maintained.</p>
 *
 * <p><strong>Implemented (AUTH-000):</strong> the {@code SignUpService} use case
 * (validate email + password, enforce uniqueness, hash, assign least-authority role,
 * persist atomically) and its supporting {@code SignUpCommand}, {@code SignUpResult},
 * and {@code UserRepository} port. The {@code AuthenticationPort} placeholder seam
 * remains for the sign-in and current-user contracts (AUTH-001, AUTH-006).</p>
 */
package com.mailsent.identity.authentication.application;
