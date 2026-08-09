import { authClient } from "@/services/auth/authService";
import type { AuthStatus, AuthUser } from "@/types/auth";

/**
 * React hook for the authentication feature.
 *
 * <p><strong>Foundation scope.</strong> Establishes the hook boundary the UI consumes:
 * the current authenticated user and the auth flow status. It is inert until the
 * sign-in contract is wired in a later AUTH task — {@code user} starts {@code null}
 * and {@code status} starts {@code "unknown"}.</p>
 */

export interface UseAuth {
  /** The current authenticated user, or {@code null} when unknown. */
  user: AuthUser | null;
  /** The lifecycle state of the authentication flow. */
  status: AuthStatus;
  /** The auth client the feature is wired to. */
  client: typeof authClient;
}

export function useAuth(): UseAuth {
  return { user: null, status: "unknown", client: authClient };
}