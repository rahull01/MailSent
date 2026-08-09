package com.mailsent.identity.authentication.domain;

/**
 * Role — a set of what a user may see and do within an organization (Glossary;
 * Data Model value object; AUTH-003 Role model).
 *
 * <p>An immutable, canonical value object. Roles are Owner / Analyst / Viewer, kept to
 * the least authority each requires (NFR-S1). The documented semantics (Functional
 * Requirement D5: "roles that reflect who can see health and who can make changes")
 * are modelled explicitly: each role states whether it can see delivery health and
 * whether it can make changes. This is the access bound the product policy enforces —
 * not a numeric hierarchy, and not a list of arbitrary permissions.</p>
 *
 * <p>Least-authority default: a newly registered account receives {@link #VIEWER},
 * which can see health but cannot make changes. Escalation to analyst or owner is a
 * governed decision for a later authorization task.</p>
 */
public enum Role {

    /** Full access: can see delivery health and can make changes. */
    OWNER("Owner", true, true),

    /** Can see delivery health and can make changes within the delivery scope. */
    ANALYST("Analyst", true, true),

    /** Can see delivery health only — the least-authority default. */
    VIEWER("Viewer", true, false);

    private final String label;
    private final boolean canSee;
    private final boolean canChange;

    Role(String label, boolean canSee, boolean canChange) {
        this.label = label;
        this.canSee = canSee;
        this.canChange = canChange;
    }

    /** The business label of the role (Owner / Analyst / Viewer). */
    public String label() {
        return label;
    }

    /**
     * Whether this role may see delivery health. Every role in the canonical set may
     * observe delivery health (D5: "who can see health").
     */
    public boolean canSee() {
        return canSee;
    }

    /**
     * Whether this role may make changes to delivery. Owner and Analyst may; Viewer —
     * the least-authority default — may not.
     */
    public boolean canChange() {
        return canChange;
    }

    /** The role a newly registered user is created with — least authority. */
    public static Role defaultForSignUp() {
        return VIEWER;
    }
}