#!/usr/bin/env bash
set -euo pipefail

# OneStep UIKit KMP — Sync the README version table with the source of truth.
# GitHub renders README.md statically, so "dynamic" here means: never hand-edit
# the versions in the README. Run this script (or let CI run it) after bumping a
# version and it regenerates the block between the <!-- versions:* --> markers
# from the actual build files.
#
# Sources of truth:
#   uikit-kmp    -> uikit-kmp/build.gradle.kts (versionMajor/Minor/Patch)
#   core         -> gradle/libs.versions.toml  (coreVersion)
#   design-system-> gradle/libs.versions.toml  (designSystem)

RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m'

CHECK=false

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
BUILD_FILE="$REPO_ROOT/uikit-kmp/build.gradle.kts"
VERSIONS_TOML="$REPO_ROOT/gradle/libs.versions.toml"
README="$REPO_ROOT/README.md"

START_MARKER="<!-- versions:start -->"
END_MARKER="<!-- versions:end -->"

show_help() {
  echo -e "${BLUE}OneStep UIKit KMP — Sync README version table${NC}"
  echo ""
  echo "Usage: ./scripts/update-readme-versions.sh [OPTIONS]"
  echo ""
  echo -e "${GREEN}Options:${NC}"
  echo "  --check      Verify the README is in sync; exit 1 if it is stale (for CI)"
  echo "  -h, --help   Show this help message"
}

while [[ $# -gt 0 ]]; do
  case $1 in
    --check) CHECK=true; shift ;;
    -h|--help) show_help; exit 0 ;;
    *) echo -e "${RED}Error: Unknown argument: $1${NC}"; show_help; exit 1 ;;
  esac
done

# --- Parse the source of truth --------------------------------------------

# uikit-kmp version: assembled from the three integer vals in build.gradle.kts.
major=$(grep 'val versionMajor' "$BUILD_FILE" | head -1 | sed 's/[^0-9]//g')
minor=$(grep 'val versionMinor' "$BUILD_FILE" | head -1 | sed 's/[^0-9]//g')
patch=$(grep 'val versionPatch' "$BUILD_FILE" | head -1 | sed 's/[^0-9]//g')
if [[ -z "$major" || -z "$minor" || -z "$patch" ]]; then
  echo -e "${RED}Error: Could not parse uikit-kmp version from $BUILD_FILE${NC}"
  exit 1
fi
UIKIT_VERSION="${major}.${minor}.${patch}"

# Reads a version-catalog entry: `key = "value"` -> value
toml_version() {
  local key="$1"
  grep -E "^[[:space:]]*${key}[[:space:]]*=" "$VERSIONS_TOML" \
    | head -1 | sed -E 's/.*=[[:space:]]*"([^"]*)".*/\1/'
}

CORE_VERSION="$(toml_version coreVersion)"
DESIGN_SYSTEM_VERSION="$(toml_version designSystem)"
if [[ -z "$CORE_VERSION" || -z "$DESIGN_SYSTEM_VERSION" ]]; then
  echo -e "${RED}Error: Could not parse coreVersion/designSystem from $VERSIONS_TOML${NC}"
  exit 1
fi

# --- Build the generated block ---------------------------------------------

# shields.io static-badge escaping: '-' -> '--', '_' -> '__', ' ' -> '%20'.
# Applied to both the label and the message segment of the badge URL.
badge_escape() {
  local s="$1"
  s="${s//_/__}"
  s="${s//-/--}"
  s="${s// /%20}"
  printf '%s' "$s"
}

badge() { # label message color
  printf '![%s](https://img.shields.io/badge/%s-%s-%s)' \
    "$1" "$(badge_escape "$1")" "$(badge_escape "$2")" "$3"
}

BLOCK="$START_MARKER
$(badge "uikit-kmp" "$UIKIT_VERSION" blue)
$(badge "core" "$CORE_VERSION" orange)
$(badge "design-system" "$DESIGN_SYSTEM_VERSION" green)
$END_MARKER"

if ! grep -qF "$START_MARKER" "$README" || ! grep -qF "$END_MARKER" "$README"; then
  echo -e "${RED}Error: Version markers not found in $README${NC}"
  echo "Add these two lines where the table should live:"
  echo "  $START_MARKER"
  echo "  $END_MARKER"
  exit 1
fi

# Replace everything between the markers (inclusive) with the fresh block.
NEW_README="$(BLOCK="$BLOCK" SM="$START_MARKER" EM="$END_MARKER" awk '
  index($0, ENVIRON["SM"]) { print ENVIRON["BLOCK"]; skip=1; next }
  index($0, ENVIRON["EM"]) { skip=0; next }
  !skip                    { print }
' "$README")"

if [[ "$NEW_README" == "$(cat "$README")" ]]; then
  echo -e "${GREEN}✓ README version table already in sync${NC}"
  echo "  uikit-kmp: $UIKIT_VERSION | core: $CORE_VERSION | design-system: $DESIGN_SYSTEM_VERSION"
  exit 0
fi

if $CHECK; then
  echo -e "${RED}✗ README version table is stale — run ./scripts/update-readme-versions.sh${NC}"
  exit 1
fi

printf '%s\n' "$NEW_README" > "$README"
echo -e "${GREEN}✓ Updated README version table${NC}"
echo "  uikit-kmp: $UIKIT_VERSION | core: $CORE_VERSION | design-system: $DESIGN_SYSTEM_VERSION"
