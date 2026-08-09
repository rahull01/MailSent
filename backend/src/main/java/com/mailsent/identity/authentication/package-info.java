/**
 * Authentication module of the {@code Identity and Access} bounded context.
 *
 * <p>This package is the <strong>module foundation only</strong>. It establishes the
 * DDD layer boundaries for authenticating users and holding the User/Role/Consent
 * concepts of the Identity and Access context (see 03_BOUNDED_CONTEXTS.md). No
 * sign-up, sign-in, session, JWT, credential, entity, or repository is implemented
 * here; later AUTH tasks (AUTH-000…AUTH-006) fill these layers with contract-level
 * behavior.</p>
 *
 * <p>Layers follow the frozen architecture:</p>
 * <ul>
 *   <li>{@link com.mailsent.identity.authentication.domain} — the domain model and rules.</li>
 *   <li>{@link com.mailsent.identity.authentication.application} — application/use-case services.</li>
 *   <li>{@link com.mailsent.identity.authentication.infrastructure} — adapters to the outside world.</li>
 *   <li>{@link com.mailsent.identity.authentication.interfaces} — inbound/outbound transport ports.</li>
 *   <li>{@link com.mailsent.identity.authentication.config} — module configuration and wiring.</li>
 * </ul>
 */
package com.mailsent.identity.authentication;
