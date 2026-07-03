#!/usr/bin/env bash
set -euo pipefail

# OneStep UIKit KMP — Publish XCFramework for SPM consumption
# Builds the XCFramework, zips it (with compose resources), uploads to GitHub
# Releases, refreshes the checked-in compose-resources, and rewrites the root
# Package.swift with the download URL + checksum.
#
# Snapshot mode (--snapshot) publishes to a mutable prerelease tag
# (uikit-kmp-<version>-SNAPSHOT) that is recreated on every run — consumers
# track the `main` branch of this repo via SPM.

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

VERSION=""
SNAPSHOT=false
LOCAL_ONLY=false
SKIP_BUILD=false
DRY_RUN=false

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
GRADLE="$REPO_ROOT/gradlew"
MODULE=":uikit-kmp"
FRAMEWORK_NAME="OSTUIKit"
XCFRAMEWORK_DIR="$REPO_ROOT/uikit-kmp/build/XCFrameworks/release"
COMPOSE_RESOURCES_DIR="$XCFRAMEWORK_DIR/compose-resources"
CHECKED_IN_RESOURCES="$REPO_ROOT/uikit-kmp/OSTUIKitKMP/Sources/Resources/compose-resources"
STAGING_DIR="$REPO_ROOT/uikit-kmp/build/xcframework-staging"
GITHUB_REPO="OneStepRND/onestep-uikit-kmp"

show_help() {
  echo -e "${BLUE}OneStep UIKit KMP — Publish XCFramework for SPM${NC}"
  echo ""
  echo "Usage: ./scripts/publish-xcframework.sh [OPTIONS]"
  echo ""
  echo -e "${GREEN}Options:${NC}"
  echo "  --version VERSION   Version (e.g., 0.1.0). Default: read from uikit-kmp/build.gradle.kts"
  echo "  --snapshot          Publish as a snapshot (mutable prerelease tag, recreated each run)"
  echo "  --local             Build and zip only, skip GitHub release upload"
  echo "  --skip-build        Skip Gradle build (use existing XCFramework in build/)"
  echo "  --dry-run           Show what would happen without executing"
  echo "  -h, --help          Show this help message"
  echo ""
  echo -e "${YELLOW}Requirements:${NC} macOS + Xcode, gh CLI (authenticated), JDK 17+"
}

parse_args() {
  while [[ $# -gt 0 ]]; do
    case $1 in
      --version) VERSION="$2"; shift 2 ;;
      --snapshot) SNAPSHOT=true; shift ;;
      --local) LOCAL_ONLY=true; shift ;;
      --skip-build) SKIP_BUILD=true; shift ;;
      --dry-run) DRY_RUN=true; shift ;;
      -h|--help) show_help; exit 0 ;;
      *)
        echo -e "${RED}Error: Unknown argument: $1${NC}"
        show_help
        exit 1
        ;;
    esac
  done
}

resolve_version() {
  if [[ -z "$VERSION" ]]; then
    local build_file="$REPO_ROOT/uikit-kmp/build.gradle.kts"
    local major minor patch
    major=$(grep 'val versionMajor' "$build_file" | head -1 | sed 's/[^0-9]//g')
    minor=$(grep 'val versionMinor' "$build_file" | head -1 | sed 's/[^0-9]//g')
    patch=$(grep 'val versionPatch' "$build_file" | head -1 | sed 's/[^0-9]//g')
    if [[ -z "$major" || -z "$minor" || -z "$patch" ]]; then
      echo -e "${RED}Error: Could not parse version from $build_file${NC}"
      exit 1
    fi
    VERSION="${major}.${minor}.${patch}"
  fi
  if $SNAPSHOT && [[ "$VERSION" != *-SNAPSHOT ]]; then
    VERSION="${VERSION}-SNAPSHOT"
  fi
  echo -e "${BLUE}Publishing version: ${GREEN}$VERSION${NC}"
}

build_xcframework() {
  if $SKIP_BUILD; then
    echo -e "${YELLOW}▶ Skipping build (--skip-build)${NC}"
    if [[ ! -d "$XCFRAMEWORK_DIR/$FRAMEWORK_NAME.xcframework" ]]; then
      echo -e "${RED}Error: No XCFramework at $XCFRAMEWORK_DIR/$FRAMEWORK_NAME.xcframework${NC}"
      exit 1
    fi
    return
  fi

  echo -e "${BLUE}▶ Step 1: Building release XCFramework...${NC}"
  if $DRY_RUN; then
    echo -e "  ${YELLOW}[dry-run] Would run: $GRADLE $MODULE:assembleOSTUIKitReleaseXCFramework${NC}"
    return
  fi

  cd "$REPO_ROOT"
  "$GRADLE" "$MODULE:assembleOSTUIKitReleaseXCFramework"

  # Compose resources are copied next to the XCFramework by a finalizedBy task
  if [[ ! -d "$COMPOSE_RESOURCES_DIR" ]]; then
    echo -e "${YELLOW}⚠ Compose resources missing; running copy task...${NC}"
    "$GRADLE" "$MODULE:copyComposeResourcesRelease"
  fi
  echo -e "${GREEN}✓ XCFramework built${NC}"
}

stage_and_zip() {
  echo -e "${BLUE}▶ Step 2: Staging and zipping XCFramework...${NC}"
  local zip_path="$STAGING_DIR/$FRAMEWORK_NAME.xcframework.zip"

  if $DRY_RUN; then
    echo -e "  ${YELLOW}[dry-run] Would zip to $zip_path${NC}"
    CHECKSUM="<dry-run-checksum>"
    return
  fi

  rm -rf "$STAGING_DIR"
  mkdir -p "$STAGING_DIR"

  cd "$XCFRAMEWORK_DIR"
  zip -r -y -q "$zip_path" "$FRAMEWORK_NAME.xcframework"

  if [[ -d "$COMPOSE_RESOURCES_DIR/composeResources" ]]; then
    cd "$COMPOSE_RESOURCES_DIR"
    zip -r -y -q "$zip_path" "composeResources"
  fi

  CHECKSUM=$(shasum -a 256 "$zip_path" | awk '{print $1}')
  echo -e "  ${GREEN}✓ Zip: $zip_path ($(du -h "$zip_path" | awk '{print $1}'))${NC}"
  echo -e "  ${GREEN}✓ SHA-256: $CHECKSUM${NC}"
}

refresh_checked_in_resources() {
  echo -e "${BLUE}▶ Step 3: Refreshing checked-in compose-resources...${NC}"
  if $DRY_RUN; then
    echo -e "  ${YELLOW}[dry-run] Would rsync $COMPOSE_RESOURCES_DIR/composeResources → $CHECKED_IN_RESOURCES${NC}"
    return
  fi
  if [[ ! -d "$COMPOSE_RESOURCES_DIR/composeResources" ]]; then
    echo -e "${YELLOW}⚠ No composeResources in build output; keeping checked-in copy as-is${NC}"
    return
  fi
  mkdir -p "$CHECKED_IN_RESOURCES"
  rsync -a --delete "$COMPOSE_RESOURCES_DIR/composeResources/" "$CHECKED_IN_RESOURCES/composeResources/"
  echo -e "  ${GREEN}✓ compose-resources refreshed (commit this if it changed)${NC}"
}

create_github_release() {
  if $LOCAL_ONLY; then
    echo -e "${YELLOW}▶ Skipping GitHub release (--local mode)${NC}"
    return
  fi

  local tag_name="uikit-kmp-${VERSION}"
  local zip_path="$STAGING_DIR/$FRAMEWORK_NAME.xcframework.zip"

  echo -e "${BLUE}▶ Step 4: Creating GitHub Release ${tag_name}...${NC}"

  if ! command -v gh &>/dev/null; then
    echo -e "${RED}Error: 'gh' CLI not found${NC}"
    exit 1
  fi

  if $DRY_RUN; then
    echo -e "  ${YELLOW}[dry-run] Would create release: $tag_name${NC}"
    return
  fi

  # Snapshots are mutable: delete + recreate the tag/release each run
  if gh release view "$tag_name" --repo "$GITHUB_REPO" &>/dev/null; then
    echo -e "  ${YELLOW}Release $tag_name already exists, recreating...${NC}"
    gh release delete "$tag_name" --repo "$GITHUB_REPO" --yes --cleanup-tag
  fi

  local prerelease_flag=""
  if $SNAPSHOT; then
    prerelease_flag="--prerelease"
  fi

  gh release create "$tag_name" \
    "$zip_path" \
    --repo "$GITHUB_REPO" \
    --title "UIKit KMP $VERSION" \
    $prerelease_flag \
    --notes "$(cat <<EOF
## OSTUIKitKMP $VERSION

XCFramework binary for Swift Package Manager consumption.

### Installation (SPM)

Add this repository as a package dependency:
\`\`\`
https://github.com/$GITHUB_REPO
\`\`\`
$(if $SNAPSHOT; then echo "Snapshot build — track the \`main\` branch."; else echo "Use version/tag: \`$tag_name\`"; fi)

### Checksums
- **SHA-256:** \`$CHECKSUM\`
EOF
)"

  echo -e "  ${GREEN}✓ Release created: $tag_name${NC}"
}

update_package_swift() {
  local package_swift="$REPO_ROOT/Package.swift"
  local tag_name="uikit-kmp-${VERSION}"

  echo -e "${BLUE}▶ Step 5: Updating root Package.swift...${NC}"

  local binary_target
  if $LOCAL_ONLY; then
    binary_target=$(cat <<'EOF'
        .binaryTarget(
            name: "OSTUIKit",
            path: "uikit-kmp/build/XCFrameworks/release/OSTUIKit.xcframework"
        ),
EOF
)
  else
    local url="https://github.com/$GITHUB_REPO/releases/download/$tag_name/$FRAMEWORK_NAME.xcframework.zip"
    binary_target=$(cat <<EOF
        .binaryTarget(
            name: "OSTUIKit",
            url: "$url",
            checksum: "$CHECKSUM"
        ),
EOF
)
  fi

  if $DRY_RUN; then
    echo -e "  ${YELLOW}[dry-run] Would rewrite $package_swift${NC}"
    return
  fi

  cat > "$package_swift" << SWIFT
// swift-tools-version:5.9
import PackageDescription

// GENERATED by scripts/publish-xcframework.sh — do not edit the binaryTarget by hand.
let package = Package(
    name: "OSTUIKitKMP",
    platforms: [.iOS(.v16)],
    products: [
        .library(name: "OSTUIKitKMP", targets: ["OSTUIKitKMP"])
    ],
    dependencies: [
        // Native OneStep iOS SDK. The permission flow is vendored so this package
        // has NO dependency on the native iOS UIKit.
        .package(url: "https://github.com/OneStepRND/onestep-sdk-ios", exact: "2.0.8-rc1")
    ],
    targets: [
$binary_target
        .target(
            // Target name == Swift module name: must match the product so \`import OSTUIKitKMP\` works.
            name: "OSTUIKitKMP",
            dependencies: [
                "OSTUIKit",
                .product(name: "OneStepSDK", package: "onestep-sdk-ios")
            ],
            path: "uikit-kmp/OSTUIKitKMP/Sources",
            resources: [
                .copy("Resources/compose-resources"),
                .process("Resources/Media.xcassets"),
                .process("Resources/Fonts.xcassets"),
                .process("Resources/Localizable.xcstrings")
            ]
        )
    ]
)
SWIFT
  echo -e "  ${GREEN}✓ Package.swift updated${NC}"
}

show_summary() {
  local tag_name="uikit-kmp-${VERSION}"
  echo ""
  echo -e "${GREEN}✓ XCFramework published successfully!${NC}"
  if $LOCAL_ONLY; then
    echo -e "  Zip:      $STAGING_DIR/$FRAMEWORK_NAME.xcframework.zip"
    echo -e "  Checksum: $CHECKSUM"
  else
    echo -e "  Release:  https://github.com/$GITHUB_REPO/releases/tag/$tag_name"
    echo -e "  Checksum: $CHECKSUM"
    echo ""
    echo -e "${BLUE}Consumers — SPM dependency:${NC}"
    echo -e "  URL: https://github.com/$GITHUB_REPO"
    if $SNAPSHOT; then
      echo -e "  Track: branch \"main\""
    else
      echo -e "  Tag: $tag_name"
    fi
    echo ""
    echo -e "${YELLOW}Commit the updated Package.swift (and compose-resources if changed)!${NC}"
  fi
}

main() {
  parse_args "$@"
  resolve_version
  build_xcframework
  stage_and_zip
  refresh_checked_in_resources
  create_github_release
  update_package_swift
  show_summary
}

main "$@"
