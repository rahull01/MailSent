/**
 * Infrastructure layer of the authentication module.
 *
 * <p>Adapts the application ports to concrete technology: persistence of User/Role,
 * session/credential storage, and external providers. This is where the transport,
 * database, and security libraries the frozen architecture leaves "decided elsewhere"
 * are bound to the domain.</p>
 *
 * <p><strong>Foundation scope:</strong> no adapters, repositories, or credential
 * handling are implemented yet. This package is the boundary where AUTH-000…AUTH-006
 * register their persistence and transport adapters.</p>
 */
package com.mailsent.identity.authentication.infrastructure;
