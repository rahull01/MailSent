/**
 * Frontend auth service — typed contract seam.
 *
 * <p><strong>Scope.</strong> Defines the contract surface for talking to the
 * authentication backend (sign-up, sign-in, current-user). Transport calls for the live
 * endpoint are wired in a later AUTH task; the methods below are the stable names the
 * feature and its consumers build against, kept inert here so the seam typechecks.</p>
 */

import type { AuthRole, AuthUser } from "@/types/auth";

/** The result of a successful sign-up, mirroring the backend identity contract. */
export interface SignUpInput {
  email: string;
  password: string;
}

/** The created identity returned by the sign-up endpoint. */
export interface SignUpResult {
  id: string;
  email: string;
  role: AuthRole;
}

/** The offered credentials for sign-in (AUTH-001 backend contract). */
export interface SignInInput {
  email: string;
  password: string;
}

/**
 * The created session returned by the sign-in endpoint. The raw {@code token} is shown
 * once and is what the caller presents on subsequent requests; it is never persisted
 * server-side.
 */
export interface SignInResult {
  sessionId: string;
  userId: string;
  email: string;
  role: AuthRole;
  token: string;
  expiresAtEpochSecond: number;
}

/**
 * The client contract the authentication feature depends on.
 */
export interface AuthClient {
  /**
   * Registers a new identity (AUTH-000 backend contract).
   *
   * <p>Inert at this stage — wired to the live endpoint in a later AUTH task. A
   * successful sign-up does not start a session (that is AUTH-001).</p>
   */
  signUp(input: SignUpInput): Promise<SignUpResult>;

  /**
   * Authenticates credentials and establishes a session (AUTH-001 backend contract).
   *
   * <p>Inert at this stage — wired to the live sign-in endpoint in a later AUTH task.</p>
   */
  signIn(input: SignInInput): Promise<SignInResult>;

  /**
   * Returns the currently authenticated user, or {@code null} when unknown.
   *
   * <p>Inert until the session-aware contract is implemented.</p>
   */
  currentUser(): Promise<AuthUser | null>;
}

/**
 * The default auth client used by the authentication feature.
 *
 * <p>Responses are inert placeholders until the transport is wired.</p>
 */
export const authClient: AuthClient = {
  signUp: async (_input: SignUpInput) => {
    // Placeholder — wired to the live sign-up endpoint in a later stage.
    throw new Error("signUp is not yet wired to the backend");
  },
  signIn: async (_input: SignInInput) => {
    // Placeholder — wired to the live sign-in endpoint in a later stage.
    throw new Error("signIn is not yet wired to the backend");
  },
  currentUser: async () => null,
};