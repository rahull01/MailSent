/**
 * Authentication types for the frontend.
 *
 * <p><strong>Foundation scope.</strong> These are pure contract types for the
 * authentication feature — they define the shape of an authenticated user and the
 * role model per the Glossary and the Role model (AUTH-003). No auth business logic
 * lives here.</p>
 *
 * <p>Naming follows the Glossary: <em>User</em>, <em>Role</em>, <em>Consent</em>, and
 * the Identity and Access bounded context. Roles are Owner / Analyst / Viewer with
 * least authority (see 07_DATA_MODEL.md and the Role value object).</p>
 */

/**
 * The role a user holds within an organization, per the Role model (AUTH-003).
 *
 * <p>Each role is a defined set of what a user may see and do, kept to the least
 * authority needed.</p>
 */
export type AuthRole = "Owner" | "Analyst" | "Viewer";

/**
 * The identity of a person acting within an organization (Glossary: User).
 *
 * <p>Owned by the Identity and Access context. An authenticated user carries a stable
 * identity, the email they sign in with, and the roles that bound their access.</p>
 */
export interface AuthUser {
  /** The stable identity of the user. */
  id: string;
  /** The email address used to authenticate. */
  email: string;
  /** The roles that bound what the user may see and do. */
  roles: readonly AuthRole[];
}

/**
 * The lifecycle state of a frontend authentication flow.
 *
 * <p>Used by the auth feature to represent whether the user is unknown, being
 * verified, or authenticated. It carries no credential or session material.</p>
 */
export type AuthStatus = "unknown" | "authenticating" | "authenticated";
