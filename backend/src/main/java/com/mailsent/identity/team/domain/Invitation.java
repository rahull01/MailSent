package com.mailsent.identity.team.domain;

import java.time.Instant;
import java.util.UUID;

import com.mailsent.identity.authentication.domain.Email;
import com.mailsent.identity.authentication.domain.Role;

/**
 * Invitation — a secure, single-use offer addressed to an email for a target Organization
 * (TEAM-001, frozen contract v8).
 *
 * <p>An invitation binds a canonical invitee {@link Email} to a target {@code organizationId}
 * with a data-only {@link Role} and carries the SHA-256 hash of its acceptance token (the raw
 * token is never persisted). Its status is {@code PENDING → ACCEPTED} only. {@code ACCEPTED}
 * is <strong>only an invitation-lifecycle state</strong>: it is not an authorization grant,
 * membership grant, RBAC/capability grant, or organizational-scope grant, and TEAM-001 creates
 * no mechanism by which acceptance implies organization access.</p>
 *
 * <p><strong>Security.</strong> Only the token hash is held — never the raw bearer value, so a
 * database leak cannot disclose a usable token. The inviter identity is deliberately absent
 * (deferred to AUTH-006). Acceptance itself is enforced atomically at the repository boundary,
 * not by this aggregate.</p>
 */
public final class Invitation {

    /** Lifecycle state: PENDING → ACCEPTED only. */
    public enum Status {
        PENDING,
        ACCEPTED
    }

    private final String id;
    private final String organizationId;
    private final Email inviteeEmail;
    private final Role role;
    private final String tokenHash;
    private Status status;
    private final Instant createdAt;
    private Instant acceptedAt;

    private Invitation(String id, String organizationId, Email inviteeEmail, Role role,
                       String tokenHash, Status status, Instant createdAt, Instant acceptedAt) {
        this.id = id;
        this.organizationId = organizationId;
        this.inviteeEmail = inviteeEmail;
        this.role = role;
        this.tokenHash = tokenHash;
        this.status = status;
        this.createdAt = createdAt;
        this.acceptedAt = acceptedAt;
    }

    /**
     * Registers a new invitation in its PENDING state.
     *
     * @param organizationId the target Organization's stable identity, required
     * @param inviteeEmail   the invited email (canonical {@link Email}), required
     * @param role           the data-only role granted on the invitation, required
     * @param tokenHash      the stored hash of the acceptance token, required
     * @param createdAt      the instant the invitation is issued, required
     */
    public static Invitation register(String organizationId, Email inviteeEmail, Role role,
                                      String tokenHash, Instant createdAt) {
        if (organizationId == null || organizationId.isBlank()) {
            throw new IllegalArgumentException("A target organization id is required.");
        }
        if (inviteeEmail == null) {
            throw new IllegalArgumentException("An invitee email is required.");
        }
        if (role == null) {
            throw new IllegalArgumentException("A role is required for the invitation.");
        }
        if (tokenHash == null || tokenHash.isBlank()) {
            throw new IllegalArgumentException("An acceptance token hash is required.");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("An issue time is required.");
        }
        return new Invitation(UUID.randomUUID().toString(), organizationId, inviteeEmail, role,
            tokenHash, Status.PENDING, createdAt, null);
    }

    /**
     * Reconstructs an invitation from durable storage, adopting its stored state verbatim.
     */
    public static Invitation restore(String id, String organizationId, Email inviteeEmail, Role role,
                                     String tokenHash, Status status, Instant createdAt, Instant acceptedAt) {
        return new Invitation(id, organizationId, inviteeEmail, role, tokenHash, status, createdAt, acceptedAt);
    }

    /** Whether the invitation is already accepted (single-use decision read by the application). */
    public boolean isAccepted() {
        return status == Status.ACCEPTED;
    }

    /**
     * Transitions PENDING → ACCEPTED, recording the acceptance instant. Returns whether the
     * transition occurred (false if already ACCEPTED). The authoritative concurrent
     * single-use guarantee is the repository's atomic conditional transition
     * ({@code markAcceptedIfPending}); this method models the same rule on the aggregate.
     */
    public boolean accept(Instant acceptedAt) {
        if (status == Status.PENDING && acceptedAt != null) {
            this.status = Status.ACCEPTED;
            this.acceptedAt = acceptedAt;
            return true;
        }
        return false;
    }

    public String id() {
        return id;
    }

    /** The target Organization's stable identity. */
    public String organizationId() {
        return organizationId;
    }

    /** The invited email (the exact persisted canonical value). */
    public Email inviteeEmail() {
        return inviteeEmail;
    }

    /** The role, stored as data only. */
    public Role role() {
        return role;
    }

    /** The stored hash of the acceptance token (the raw token is never persisted). */
    public String tokenHash() {
        return tokenHash;
    }

    /** The current lifecycle state (PENDING or ACCEPTED). */
    public Status status() {
        return status;
    }

    /** The instant the invitation was issued. */
    public Instant createdAt() {
        return createdAt;
    }

    /** The instant of acceptance, or {@code null} while PENDING. */
    public Instant acceptedAt() {
        return acceptedAt;
    }
}