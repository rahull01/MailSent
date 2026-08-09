/**
 * Authentication feature folder.
 *
 * <p><strong>Foundation scope.</strong> This is the frontend feature module for
 * authentication. It currently re-exports the hook and service seams the UI consumes.
 * Registration (sign-up), login (sign-in), and session handling are implemented in
 * later AUTH tasks; nothing here performs authentication.</p>
 */

export { useAuth } from "@/hooks/useAuth";
export type { UseAuth } from "@/hooks/useAuth";
export { authClient } from "@/services/auth/authService";
export type {
  AuthClient,
  SignInInput,
  SignInResult,
  SignUpInput,
  SignUpResult,
} from "@/services/auth/authService";
export type { AuthRole, AuthStatus, AuthUser } from "@/types/auth";