package com.mailsent.identity.authentication.application;

import java.util.Optional;

import com.mailsent.identity.authentication.domain.Session;

/**
 * SessionRepository — the port for persisting and retrieving sessions.
 *
 * <p>Owned by the application; satisfied by an infrastructure adapter. Sign-in persists a
 * session atomically. Only the token hash is stored, never the raw bearer value.</p>
 */
public interface SessionRepository {

    /** Persists a session and returns it, assigning the storage identity if any. */
    Session save(Session session);

    /** Loads a session by its token hash for authentication and session consumption. */
    Optional<Session> findByTokenHash(String tokenHash);
}