package com.mailsent.identity.team.domain;

import java.time.Instant;
import java.util.UUID;

import com.mailsent.identity.authentication.domain.Email;
import com.mailsent.identity.authentication.domain.Role;

/**
 * Invitation — a secure offer for a user to join a target Organization (TEAM-001).
 *
 * <p>An invitation binds an invitee email to a target organization with a granted role
 * (the AUTH-003 Owner/Analyst/Viewer role model; the full role→capability authorization
 * matrix is a later TEAM task). Acceptance is single-use: a {@code PENDING} invitation is
 * transitioned to {@code ACCEPTED} exactly once by {@link #accept()}; an already-accepted
 * invitation cannot grant membership again.</p>
 *
 * <p><strong>Security.</strong> Only the SHA-256 {@code tokenHash} of the acceptance token is
 * held — never the raw bearer value, so a database leak cannot disclose a usable token
 * (mirrors the Session token rule). The owner identity (who invited) is deliberately absent
 * here: binding an authenticated inviter is the deferred AUTH-006 current-user contract and
 * is not invented in TEAM-001.</p>
 */
public final class Invitation {

    /** Lifecycle state of an invitation. */
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

    private Invitation(String id, String organizationId, Email inviteeEmail, Role role,
                       String tokenHash, Status status, Instant createdAt) {
        this.id = id;
        this.organizationId = organizationId;
        this.inviteeEmail = inviteeEmail;
        this.role = role;
        this.tokenHash = tokenHash;
        this.status = status;
        this.createdAt = createdAt;
    }

    /**
     * Registers a new invitation in its PENDING state.
     *
     * @param organizationId the target Organization's stable identity, required
     * @param inviteeEmail   the email invited to join, required (validated by {@link Email})
     * @param role           the role granted on acceptance, required
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
            tokenHash, Status.PENDING, createdAt);
    }

    /**
     * Reconstructs an invitation from durable storage, adopting its stored state verbatim.
     */
    public static Invitation restore(String id, String organizationId, Email inviteeEmail, Role role,
                                     String tokenHash, Status status, Instant createdAt) {
        return new Invitation(id, organizationId, inviteeEmail, role, tokenHash, status, createdAt);
    }

    /** Whether the invitation has already been accepted (single-use). */
    public boolean isAccepted() {
        return status == Status.ACCEPTED;
    }

    /**
     * Accepts the invitation. A PENDING invitation becomes ACCEPTED exactly once; accepting
     * an already-accepted invitation is a no-op (single-use is enforced at the service layer
     * by {@link #isAccepted()} before issuing a membership).
     */
    public void accept() {
        if (status == Status.PENDING) {
            status = Status.ACCEPTED;
        }
    }

    public String id() {
        return id;
    }

    /** The target Organization's stable identity. */
    public String organizationId() {
        return organizationId;
    }

    /** The invited email. */
    public Email inviteeEmail() {
        return inviteeEmail;
    }

    /** The role granted on acceptance. */
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
}