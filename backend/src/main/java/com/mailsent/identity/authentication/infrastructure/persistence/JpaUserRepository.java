package com.mailsent.identity.authentication.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JpaUserRepository — the Spring Data repository over {@link UserJpaEntity}.
 *
 * <p>Leverages the {@code uk_users_email} unique constraint for race-safe uniqueness:
 * two concurrent sign-ups for the same email cannot both insert; the database rejects
 * the second with a constraint violation, which the adapter maps to
 * {@code DuplicateEmailException}. Predicate methods are derived from the field name.</p>
 */
public interface JpaUserRepository extends JpaRepository<UserJpaEntity, String> {

    /** Whether a user with the given normalized email exists. */
    boolean existsByEmail(String email);

    /** Loads a user by its normalized email, if present. */
    Optional<UserJpaEntity> findByEmail(String email);

    /** Loads users by organization id ordered deterministically. */
    List<UserJpaEntity> findByOrganizationIdOrderByCreatedAtAscIdAsc(String organizationId);
}