package com.mailsent.identity.authentication.application;

import java.util.Optional;

import com.mailsent.identity.authentication.domain.Email;
import com.mailsent.identity.authentication.domain.User;

/**
 * UserRepository — the port for persisting and retrieving users.
 *
 * <p>Owned by the application; consumed by an infrastructure adapter. Sign-up depends
 * on this port, so the use case is independent of the storage technology. The durable
 * PostgreSQL adapter is AUTH-002 (User identity entity); a hermetic in-memory adapter
 * backs the module until then.</p>
 */
public interface UserRepository {

    /** Whether a user with the given canonical email already exists (uniqueness rule). */
    boolean existsByEmail(Email email);

    /** Persists a new user and returns it, assigning the storage identity if any. */
    User save(User user);

    /**
     * Loads the user for a canonical email, used by sign-in to verify the stored hash and
     * to bind the session. Empty when no such identity exists.
     */
    Optional<User> findByEmail(Email email);
}