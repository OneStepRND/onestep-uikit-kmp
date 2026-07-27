#!/usr/bin/env bash
set -euo pipefail

# OneStep UIKit KMP — Keep the native iOS SDK (onestep-sdk-ios) pin aligned everywhere.
#
# The onestep-sdk-ios version is referenced in several files across three ecosystems
# (SwiftPM manifests, a bash generator, xcodegen YAML, the pbxproj, and the SPM lock).
# SwiftPM has no BOM, so the only way to guarantee alignment is a single source of truth
# plus a check. This script is the same pattern as scripts/update-readme-versions.sh.
#
# Source of truth:
#   gradle/libs.versions.toml -> onestepSdkIos
#
# Propagated pins (kept identical to the source of truth):
#   Package.swift                                   .package(... exact: "X")
#   scripts/publish-xcframework.sh                  (template that regenerates Package.swift)
#   uikit-kmp/OSTUIKitKMP/Package.swift             .package(... exact: "X")
#   iosTestApp/project.yml                          exactVersion: X
#   iosTestApp/.../project.pbxproj                  version = "X";
#   iosTestApp/.../Package.resolved                 "version" : "X" (+ tag "revision")
#
# Usage:
#   scripts/sync-ios-sdk-version.sh            Rewrite every pin to match the source of truth
#   scripts/sync-ios-sdk-version.sh --check    Verify alignment; exit 1 on drift (for CI)
#
# --check is hermetic (string comparison only, no network). Write mode refreshes the
# Package.resolved commit via `git ls-remote` on the tag, so run it where git can reach the
# private onestep-sdk-ios repo, then commit the result.

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m'

CHECK=false

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
VERSIONS_TOML="$REPO_ROOT/gradle/libs.versions.toml"
SDK_REPO="https://github.com/OneStepRND/onestep-sdk-ios"

show_help() {
  echo -e "${BLUE}OneStep UIKit KMP — Sync the native iOS SDK version${NC}"
  echo ""
  echo "Usage: ./scripts/sync-ios-sdk-version.sh [OPTIONS]"
  echo ""
  echo -e "${GREEN}Options:${NC}"
  echo "  --check      Verify every iOS SDK pin matches the source of truth; exit 1 on drift"
  echo "  -h, --help   Show this help message"
}

while [[ $# -gt 0 ]]; do
  case $1 in
    --check) CHECK=true; shift ;;
    -h|--help) show_help; exit 0 ;;
    *) echo -e "${RED}Error: Unknown argument: $1${NC}"; show_help; exit 1 ;;
  esac
done

# --- Source of truth --------------------------------------------------------
TRUTH="$(grep -E '^[[:space:]]*onestepSdkIos[[:space:]]*=' "$VERSIONS_TOML" \
  | head -1 | sed -E 's/.*=[[:space:]]*"([^"]*)".*/\1/')"
if [[ -z "$TRUTH" ]]; then
  echo -e "${RED}Error: Could not parse onestepSdkIos from $VERSIONS_TOML${NC}"
  exit 1
fi

# Version-controlled pins ("id|relative-path"). These exist in a fresh CI checkout and ARE the
# source of truth the check enforces.
TRACKED_TARGETS="
swift_root|Package.swift
swift_pub|scripts/publish-xcframework.sh
swift_dev|uikit-kmp/OSTUIKitKMP/Package.swift
yml|iosTestApp/project.yml
"

# Generated, git-ignored artifacts: xcodegen writes the .xcodeproj (pbxproj) from project.yml and
# SPM writes the lock, so iosTestApp/OSTUIKitTestApp.xcodeproj/ is in .gitignore. They don't exist
# in CI, so they are NEVER part of --check — aligning project.yml is what guarantees them. Write
# mode updates them best-effort when present, as a local convenience so an already-generated
# xcodeproj builds against the new pin without re-running iosTestApp/rebuild.sh.
GENERATED_TARGETS="
pbxproj|iosTestApp/OSTUIKitTestApp.xcodeproj/project.pbxproj
resolved|iosTestApp/OSTUIKitTestApp.xcodeproj/project.xcworkspace/xcshareddata/swiftpm/Package.resolved
"

# Extract the version currently pinned in a given file. Never fails the pipeline (|| true) so a
# missing pin surfaces as an empty string (reported as a mismatch) rather than aborting the script.
read_pin() { # id file
  local id="$1" f="$2"
  case "$id" in
    swift_root|swift_pub|swift_dev)
      { grep 'onestep-sdk-ios' "$f" | grep 'exact:' \
          | sed -E 's/.*exact:[[:space:]]*"([^"]+)".*/\1/' | head -1; } || true ;;
    yml)
      { grep -E '^[[:space:]]*exactVersion:' "$f" \
          | sed -E 's/.*exactVersion:[[:space:]]*//' | tr -d '[:space:]' | head -1; } || true ;;
    pbxproj)
      # lowercase "version = " only appears in the SPM requirement (objectVersion has a capital V)
      { grep 'version = ' "$f" \
          | sed -E 's/.*version = "?([^";]+)"?;.*/\1/' | head -1; } || true ;;
    resolved)
      # the quoted string form ("version" : "X"); the pins-format `"version" : 2` has no quote
      { grep '"version" : "' "$f" \
          | sed -E 's/.*"version" : "([^"]+)".*/\1/' | head -1; } || true ;;
  esac
}

# Rewrite the pin in a given file to $TRUTH (perl -i: consistent on macOS + Linux).
write_pin() { # id file
  local id="$1" f="$2"
  case "$id" in
    swift_root|swift_pub|swift_dev)
      V="$TRUTH" perl -pi -e 's/(onestep-sdk-ios",\s*exact:\s*")[^"]*(")/$1$ENV{V}$2/' "$f" ;;
    yml)
      V="$TRUTH" perl -pi -e 's/^(\s*exactVersion:\s*).*/$1$ENV{V}/' "$f" ;;
    pbxproj)
      # normalise to the quoted form so pre-release identifiers (with '-') stay valid
      V="$TRUTH" perl -pi -e 's/(version = )"?[^";]*"?(;)/$1"$ENV{V}"$2/' "$f" ;;
    resolved)
      V="$TRUTH" perl -pi -e 's/("version" : ")[^"]*(")/$1$ENV{V}$2/' "$f" ;;
  esac
}

# --- Check mode -------------------------------------------------------------
if $CHECK; then
  drift=0
  echo -e "${BLUE}Source of truth (onestepSdkIos): ${TRUTH}${NC}"
  while IFS='|' read -r id rel; do
    [[ -z "$id" ]] && continue
    f="$REPO_ROOT/$rel"
    if [[ ! -f "$f" ]]; then
      echo -e "  ${RED}✗ missing file: $rel${NC}"; drift=1; continue
    fi
    cur="$(read_pin "$id" "$f")"
    if [[ "$cur" == "$TRUTH" ]]; then
      echo -e "  ${GREEN}✓${NC} $rel"
    else
      echo -e "  ${RED}✗ $rel pinned '${cur:-<none>}' (expected '$TRUTH')${NC}"; drift=1
    fi
  done <<< "$TRACKED_TARGETS"

  # Generated artifacts are informational only (absent in CI; regenerated from project.yml locally),
  # so a mismatch here never fails the check.
  while IFS='|' read -r id rel; do
    [[ -z "$id" ]] && continue
    f="$REPO_ROOT/$rel"
    [[ -f "$f" ]] || continue
    cur="$(read_pin "$id" "$f")"
    [[ "$cur" == "$TRUTH" ]] && continue
    echo -e "  ${YELLOW}• $rel pinned '${cur:-<none>}' (generated — regenerate via iosTestApp/rebuild.sh)${NC}"
  done <<< "$GENERATED_TARGETS"

  if [[ "$drift" -ne 0 ]]; then
    echo -e "${RED}✗ iOS SDK pins are out of sync — run ./scripts/sync-ios-sdk-version.sh${NC}"
    exit 1
  fi
  echo -e "${GREEN}✓ All tracked iOS SDK pins aligned at $TRUTH${NC}"
  exit 0
fi

# --- Write mode -------------------------------------------------------------
echo -e "${BLUE}Aligning iOS SDK pins to ${TRUTH}${NC}"
while IFS='|' read -r id rel; do
  [[ -z "$id" ]] && continue
  f="$REPO_ROOT/$rel"
  if [[ ! -f "$f" ]]; then
    echo -e "  ${YELLOW}• skipped (missing): $rel${NC}"; continue
  fi
  write_pin "$id" "$f"
  echo -e "  ${GREEN}✓${NC} $rel"
done <<< "$TRACKED_TARGETS$GENERATED_TARGETS"

# Refresh the Package.resolved commit for the tag (best-effort; needs repo access).
RESOLVED="$REPO_ROOT/iosTestApp/OSTUIKitTestApp.xcodeproj/project.xcworkspace/xcshareddata/swiftpm/Package.resolved"
REV="$(git ls-remote "$SDK_REPO" "refs/tags/$TRUTH" 2>/dev/null | awk '{print $1}' | head -1 || true)"
if [[ -n "$REV" && -f "$RESOLVED" ]]; then
  REV="$REV" perl -pi -e 's/("revision" : ")[^"]*(")/$1$ENV{REV}$2/' "$RESOLVED"
  echo -e "  ${GREEN}✓${NC} Package.resolved revision -> ${REV}"
else
  echo -e "  ${YELLOW}! Could not resolve the tag commit (no repo access?). Package.resolved${NC}"
  echo -e "  ${YELLOW}  revision left unchanged — refresh it with an Xcode/SPM package resolve.${NC}"
fi

echo -e "${GREEN}✓ Done. Review 'git diff' and commit.${NC}"
