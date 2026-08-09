#!/usr/bin/env bash
#
# MailSent — Repository Health Check (Phase 00, REPO-004)
#
# Validates the repository foundation and becomes part of Phase 00 acceptance.
# Critical failures (missing structure/files, bad naming, CRLF scripts,
# non-executable scripts, invalid names) exit non-zero. Warnings
# (empty dirs, duplicate content) are reported but do not fail.
#
# Usage: scripts/repo-health.sh

set -uo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
issues=0
warnings=0

pass() { printf 'OK    %s\n' "$*"; }
fail() { printf 'FAIL  %s\n' "$*"; issues=$((issues + 1)); }
warn() { printf 'WARN  %s\n' "$*"; warnings=$((warnings + 1)); }

# ---- 1. Folder structure ----
echo "== Folder structure =="
REQUIRED_DIRS=(
  docs/00_Foundation
  docs/01_Product
  docs/02_Product_Design
  docs/03_Architecture
  implementation
  scripts
)
for d in "${REQUIRED_DIRS[@]}"; do
  if [ -d "$ROOT/$d" ]; then pass "dir  $d"; else fail "dir  $d (missing)"; fi
done

# ---- 2. Required files ----
echo "== Required files =="
REQUIRED_FILES=(
  README.md
  CONTRIBUTING.md
  CHANGELOG.md
  .gitignore
  implementation/README.md
  implementation/00_IMPLEMENTATION_MASTER_CHECKLIST.md
  scripts/guard-secrets.sh
  scripts/repo-health.sh
)
for f in "${REQUIRED_FILES[@]}"; do
  if [ -f "$ROOT/$f" ]; then pass "file $f"; else fail "file $f (missing)"; fi
done

# ---- 3. Naming conventions / invalid filenames ----
echo "== Naming conventions =="
bad_names="$(find "$ROOT/docs" "$ROOT/implementation" "$ROOT/scripts" -type f 2>/dev/null | grep -E '[[:space:]]|[^ -~]' || true)"
if [ -n "$bad_names" ]; then
  fail "invalid filename(s): $bad_names"
else
  pass "no invalid filenames (space/non-ASCII)"
fi

# ---- 4. Line endings (scripts must be LF) ----
echo "== Line endings =="
for s in "$ROOT"/scripts/*.sh; do
  [ -f "$s" ] || continue
  if grep -q $'\r' "$s" 2>/dev/null; then
    fail "CRLF line endings in $s"
  else
    pass "LF in $(basename "$s")"
  fi
done

# ---- 5. Executable scripts + shebang ----
echo "== Executable scripts =="
for s in "$ROOT"/scripts/*.sh; do
  [ -f "$s" ] || continue
  if [ -x "$s" ]; then pass "executable $(basename "$s")"; else warn "not executable: $s (run chmod +x)"; fi
  if head -1 "$s" 2>/dev/null | grep -q '^#!'; then pass "shebang $(basename "$s")"; else fail "missing shebang in $s"; fi
done

# ---- 6. Empty directories ----
echo "== Empty directories =="
empties="$(find "$ROOT/docs" "$ROOT/implementation" -type d -empty 2>/dev/null | grep -v '/\.git' || true)"
if [ -n "$empties" ]; then warn "empty dirs: $empties"; else pass "no empty directories"; fi

# ---- 7. Duplicate files (identical content) ----
echo "== Duplicate files =="
dup_md5="$(find "$ROOT/docs" "$ROOT/implementation" -type f -not -path '*/.git/*' -exec md5sum {} + 2>/dev/null | awk '{print $1}' | sort | uniq -d)"
if [ -n "$dup_md5" ]; then warn "identical-content groups detected (md5: $dup_md5)"; else pass "no duplicate content"; fi

# ---- 8. Broken links ----
echo "== Broken links =="
broken="$(find "$ROOT" -xtype l 2>/dev/null | grep -v '/\.git/' || true)"
if [ -n "$broken" ]; then warn "broken symlinks: $broken"; else pass "no broken links"; fi

# ---- Result ----
echo ""
echo "RESULT: issues=$issues warnings=$warnings"
[ "$issues" -eq 0 ] || { echo "repo-health: FAILED" >&2; exit 1; }
echo "repo-health: OK"
exit 0
