/**
 * Interfaces (transport) layer of the authentication module.
 *
 * <p>Exposes the application use cases to the outside world through inbound ports such
 * as HTTP controllers and outbound ports such as client-facing contracts. Per the
 * frozen architecture (08_API_PHILOSOPHY.md), the interfaces layer translates external
 * messages into application calls without containing domain logic.</p>
 *
 * <p><strong>Foundation scope:</strong> no controllers, DTOs, or endpoints are
 * implemented yet. This package is the boundary where the sign-up/sign-in API
 * contracts (per 08_API_PHILOSOPHY.md) will be defined.</p>
 */
package com.mailsent.identity.authentication.interfaces;
