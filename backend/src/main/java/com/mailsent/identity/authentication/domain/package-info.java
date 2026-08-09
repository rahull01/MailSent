/**
 * Domain layer of the authentication module.
 *
 * <p>Holds the business concepts and rules of identity within the Identity and Access
 * bounded context: the <em>User</em>, <em>Role</em>, and the value/port contracts that
 * sign up needs (see 07_DATA_MODEL.md), plus the invariants that govern them
 * (INV-10 stable identity, INV-13 ownership, NFR-S2 hashed-at-rest).</p>
 *
 * <p><strong>Implemented (AUTH-000):</strong> {@link User} entity, {@link Email} value
 * object, {@link Role} enum, {@link PasswordPolicy} rule, {@link PasswordHasher} port,
 * and the sign-up domain exceptions. Consent (INV-02) and role escalation remain later
 * AUTH tasks.</p>
 */
package com.mailsent.identity.authentication.domain;
