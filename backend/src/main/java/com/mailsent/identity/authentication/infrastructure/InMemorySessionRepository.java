package com.mailsent.identity.authentication.infrastructure;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.mailsent.identity.authentication.application.SessionRepository;
import com.mailsent.identity.authentication.domain.Session;

/**
 * InMemorySessionRepository — a hermetic, thread-safe {@link SessionRepository} for the
 * test profile. Mirrors the production adapter's shape. Never holds the raw token.</p>
 */
public class InMemorySessionRepository implements SessionRepository {

    private final Map<String, Session> byTokenHash = new ConcurrentHashMap<>();

    @Override
    public Session save(Session session) {
        byTokenHash.put(session.tokenHash(), session);
        return session;
    }

    @Override
    public java.util.Optional<Session> findByTokenHash(String tokenHash) {
        if (tokenHash == null || tokenHash.isBlank()) {
            return java.util.Optional.empty();
        }
        return java.util.Optional.ofNullable(byTokenHash.get(tokenHash));
    }

    /** Count of sessions, for tests and observability. */
    public int count() {
        return byTokenHash.size();
    }

    /** Clears all state; used by tests to reset the shared bean between cases. */
    public void clear() {
        byTokenHash.clear();
    }
}