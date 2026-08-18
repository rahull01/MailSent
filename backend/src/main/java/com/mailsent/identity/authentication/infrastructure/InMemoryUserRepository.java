package com.mailsent.identity.authentication.infrastructure;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.mailsent.identity.authentication.application.UserRepository;
import com.mailsent.identity.authentication.domain.DuplicateEmailException;
import com.mailsent.identity.authentication.domain.Email;
import com.mailsent.identity.authentication.domain.User;

/**
 * InMemoryUserRepository — a hermetic, thread-safe {@link UserRepository}.
 *
 * <p><strong>Test-only.</strong> Wired under the {@code test} profile so the Spring
 * context loads with no DataSource. It mirrors the production adapter's guarantees so
 * tests reflect production behavior:</p>
 * <ul>
 *   <li><strong>Uniqueness under concurrency</strong> — {@code save} uses
 *       {@code putIfAbsent} and rejects a duplicate with
 *       {@link DuplicateEmailException}, exactly as the production PostgreSQL
 *       {@code uk_users_email} constraint does via {@code JpaUserRepositoryAdapter}.</li>
 *   <li>Normalized email is the map key (case-folding already applied by the domain
 *       {@link Email}), so casing cannot create duplicate identities (INV-10).</li>
 * </ul>
 */
public class InMemoryUserRepository implements UserRepository {

    private final Map<String, User> byEmail = new ConcurrentHashMap<>();

    @Override
    public boolean existsByEmail(Email email) {
        return byEmail.containsKey(email.value());
    }

    @Override
    public User save(User user) {
        User previous = byEmail.putIfAbsent(user.email().value(), user);
        if (previous != null) {
            throw new DuplicateEmailException("An account already exists for that email address.");
        }
        return user;
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        return Optional.ofNullable(byEmail.get(email.value()));
    }

    @Override
    public Optional<User> findById(String id) {
        return byEmail.values().stream().filter(u -> u.id().equals(id)).findFirst();
    }

    @Override
    public List<User> findAllByOrganizationId(String organizationId) {
        if (organizationId == null || organizationId.isBlank()) {
            return List.of();
        }
        return byEmail.values().stream()
                .filter(u -> organizationId.equals(u.organizationId()))
                .sorted(Comparator.comparing(User::createdAt).thenComparing(User::id))
                .toList();
    }

    /** Count of users, for tests and observability. */
    public int count() {
        return byEmail.size();
    }

    /** Clears all state; used by tests to reset the shared bean between cases. */
    public void clear() {
        byEmail.clear();
    }
}