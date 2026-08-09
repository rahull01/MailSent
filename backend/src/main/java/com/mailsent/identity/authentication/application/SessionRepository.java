package com.mailsent.identity.authentication.application;

import com.mailsent.identity.authentication.domain.Session;

/**
 * SessionRepository — the port for persisting sessions.
 *
 * <p>Owned by the application; satisfied by an infrastructure adapter. Sign-in persists a
 * session atomically. The durable PostgreSQL adapter is AUTH-002; a hermetic in-memory
 * adapter backs the module until then. Only the token hash is stored, never the raw
 * bearer value.</p>
 *
 * <p><strong>Scope.</strong> AUTH-001 only needs to create sessions. Retrieval by token
 * (session consumption) belongs to AUTH-006 and is deliberately not declared here.</p>
 */
public interface SessionRepository {

    /** Persists a session and returns it, assigning the storage identity if any. */
    Session save(Session session);
}