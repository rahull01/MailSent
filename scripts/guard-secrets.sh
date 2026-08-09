#!/usr/bin/env bash
#
# MailSent — Secrets Guard (Phase 00, REPO-001)
#
# Pre-commit / CI guard that blocks secret-shaped content from entering the
# repository. It scans the working tree for known secret patterns and secret
# file names, and exits non-zero when any are found.
#
# Usage:
#   scripts/guard-secrets.sh            # scan the working tree
#   scripts/guard-secrets.sh --staged   # scan only staged files (pre-commit)
#
# This is a foundation guard. It is not a substitute for following the
# Security Architecture (docs/03_Architecture/09_SECURITY_ARCHITECTURE.md)
# and the Non-Functional Requirements (NFR S5): secrets are never written to
# source, logs, or fixtures in the first place.

set -uo pipefail

# Paths that are deliberately skipped (never contain committed secrets).
SKIP_DIRS='(^|/)(\.git|node_modules|vendor|dist|build|target|coverage|logs|tmp)(/|$)'

# Secret-shaped file names that must never be committed.
SECRET_FILENAMES=(
  '.env'
  'credentials.json'
  'id_rsa'
  'id_dsa'
  'id_ecdsa'
  'id_ed25519'
  '*.pem'
  '*.key'
  '*.p12'
  '*.pfx'
  '*.ppk'
  '*.secret'
  '*.token'
  '*.jks'
  '*.keystore'
  '*.p8'
  '*service-account*.json'
  '*service_account*.json'
  '*application-credentials.json'
)

SECRET_PATTERNS=(
  '-----BEGIN [A-Z0-9 ]*PRIVATE KEY-----'  # private SSH / TLS / PGP keys
  'AKIA[0-9A-Z]{16}'                        # AWS access key id
  'ASIA[0-9A-Z]{16}'                        # AWS temp access key id
  'aws_access_key_id\s*=\s*"?[A-Z0-9]{16,}"?'
  'aws_secret_access_key\s*=\s*"?[A-Za-z0-9/+=]{40,}"?'
  'sk-ant-[A-Za-z0-9_-]{20,}'               # Anthropic key
  'sk-[A-Za-z0-9]{20,}'                     # OpenAI / generic secret token
  'ghp_[A-Za-z0-9]{36,}'                    # GitHub PAT
  'gh[ousr]_[A-Za-z0-9]{20,}'               # GitHub (org/user/run) tokens
  'github_pat_[A-Za-z0-9_]{20,}'            # GitHub fine-grained PAT
  'xox[baprs]-[A-Za-z0-9-]{10,}'            # Slack token
  'AIza[0-9A-Za-z_-]{35}'                   # Google API key
  'GOOGLE_APPLICATION_CREDENTIALS\s*='        # GCP env var bound to a real credential path (bare mention is not a secret)
  '"type"\s*:\s*"service_account"'          # GCP service-account
  '"client_secret":'
  '"private_key":'                          # service-account / oauth private key
  'AZURE_CLIENT_SECRET\s*='
  'AccountKey=[A-Za-z0-9+/]{40,}'           # Azure storage connection key
  'postgres://[^:]+:[^@]+@'                 # db connection string with password
  'mongodb(\+srv)?://[^:]+:[^@]+@'
)

fail() { printf 'guard-secrets: %s\n' "$*" >&2; }

matches_filenames() {
  local path="$1" base
  base="$(basename "$path")"
  for pat in "${SECRET_FILENAMES[@]}"; do
    case "$base" in
      $pat) return 0 ;;
    esac
  done
  return 1
}

TARGETS=()
if [ "${1:-}" = "--staged" ]; then
  # Read staged (added/modified) files. Requires git.
  while IFS= read -r line; do
    [ -n "$line" ] || continue
    case "$line" in
      A*|M*|R*|C*) TARGETS+=("${line:2}") ;;
    esac
  done < <(git diff --cached --name-status)
  if [ "${#TARGETS[@]}" -eq 0 ]; then
    echo "guard-secrets: no staged files to check"
    exit 0
  fi
else
  # Scan the working tree (excluding skip dirs and .git).
  while IFS= read -r file; do
    [ -n "$file" ] || continue
    TARGETS+=("$file")
  done < <(find . -type f -not -path '*/.git/*' -not -path '*/node_modules/*' -not -path '*/vendor/*' -not -path '*/dist/*' -not -path '*/build/*' -not -path '*/target/*' -not -path '*/coverage/*' -not -path '*/logs/*' -not -path '*/tmp/*' 2>/dev/null)
fi

found=0
for file in "${TARGETS[@]}"; do
  [ -f "$file" ] || continue
  # The guard never scans its own definitions file: its pattern array
  # intentionally contains the very literals it detects, so scanning itself
  # would flag those definitions as secrets. This single-file self-exclusion
  # does not weaken detection of any other file.
  [ "$file" = "scripts/guard-secrets.sh" ] && continue
  if matches_filenames "$file"; then
    fail "secret file name: $file"
    found=1
    continue
  fi
  for pat in "${SECRET_PATTERNS[@]}"; do
    if grep -qE "$pat" "$file" 2>/dev/null; then
      fail "secret-like content in: $file (pattern: $pat)"
      found=1
      break
    fi
  done
done

if [ "$found" -ne 0 ]; then
  echo "guard-secrets: FAILED — secret-shaped content detected. Do not commit it." >&2
  exit 1
fi

echo "guard-secrets: OK — no secret-shaped content detected."
exit 0
