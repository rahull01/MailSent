package com.mailsent.identity.authentication.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mailsent.identity.authentication.application.UserRepository;
import com.mailsent.identity.authentication.domain.DuplicateEmailException;
import com.mailsent.identity.authentication.domain.Email;
import com.mailsent.identity.authentication.domain.User;

/**
 * JpaUserRepositoryAdapter — production {@link UserRepository} backed by PostgreSQL.
 *
 * <p>Active in every non-test profile; the hermetic {@code InMemoryUserRepository} and
 * the JPA adapter are never both a {@code UserRepository} bean at the same time (the
 * test profile excludes the DataSource/JPA autoconfig, so this adapter is not created
 * there).</p>
 *
 * <p><strong>Transaction boundary.</strong> The repository owns the transaction: each
 * write is {@link Transactional}. The database's {@code uk_users_email} unique
 * constraint is the authoritative, race-safe guard against duplicate emails; a
 * concurrent duplicate insert surfaces as a {@link DataIntegrityViolationException},
 * mapped here to {@link DuplicateEmailException} so the use case stays unaware of the
 * persistence technology.</p>
 */
@Repository
@Profile("!test")
public class JpaUserRepositoryAdapter implements UserRepository {

    private final JpaUserRepository jpa;

    public JpaUserRepositoryAdapter(JpaUserRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(Email email) {
        return jpa.existsByEmail(email.value());
    }

    @Override
    @Transactional
    public User save(User user) {
        UserJpaEntity entity = new UserJpaEntity(
            user.id(), user.email().value(), user.passwordHash(), user.role(), user.createdAt(),
            user.organizationId());
        try {
            UserJpaEntity saved = jpa.saveAndFlush(entity);
            return toUser(saved);
        } catch (DataIntegrityViolationException ex) {
            // The uk_users_email constraint rejected a concurrent/duplicate insert.
            throw new DuplicateEmailException("An account already exists for that email address.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(Email email) {
        return jpa.findByEmail(email.value()).map(this::toUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(String id) {
        return jpa.findById(id).map(this::toUser);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAllByOrganizationId(String organizationId) {
        if (organizationId == null || organizationId.isBlank()) {
            return List.of();
        }
        return jpa.findByOrganizationIdOrderByCreatedAtAscIdAsc(organizationId).stream()
                .map(this::toUser)
                .toList();
    }

    private User toUser(UserJpaEntity entity) {
        return User.restore(entity.id(), new Email(entity.email()), entity.passwordHash(), entity.role(),
            entity.createdAt(), entity.organizationId());
    }
}