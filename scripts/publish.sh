#!/usr/bin/env bash
set -euo pipefail

# OneStep UIKit KMP — Android/Maven publish script
# Publishes the uikit-kmp module to Maven Local, GitHub Packages (snapshot), or Maven Central.
# For iOS/SPM distribution use: scripts/publish-xcframework.sh

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

target="local"
skip_confirmation=false

show_help() {
  echo -e "${BLUE}OneStep UIKit KMP — Publish Script${NC}"
  echo ""
  echo "Usage: ./scripts/publish.sh [TARGET] [OPTIONS]"
  echo ""
  echo -e "${GREEN}Targets:${NC}"
  echo "  local             Publish to Maven Local (~/.m2/repository) [default]"
  echo "  snapshot          Publish snapshot to GitHub Packages (onestep-uikit-kmp)"
  echo "  central           Publish release to Maven Central"
  echo ""
  echo -e "${GREEN}Options:${NC}"
  echo "  -l, --local       Same as: local"
  echo "  -s, --snapshot    Same as: snapshot"
  echo "  -c, --central     Same as: central"
  echo "  -y, --yes         Skip confirmation prompt for central publishing"
  echo "  -h, --help        Show this help message"
  echo ""
  echo -e "${YELLOW}Note:${NC} GitHub Packages requires gpr.user/gpr.key in ~/.gradle/gradle.properties"
  echo "      or GITHUB_ACTOR/GITHUB_TOKEN environment variables."
}

parse_args() {
  while [[ $# -gt 0 ]]; do
    case $1 in
      -l|--local) target="local"; shift ;;
      -s|--snapshot) target="snapshot"; shift ;;
      -c|--central) target="central"; shift ;;
      -y|--yes) skip_confirmation=true; shift ;;
      -h|--help) show_help; exit 0 ;;
      local|snapshot|central) target="$1"; shift ;;
      uikit-kmp) shift ;; # tolerated for muscle-memory parity with the SDK repo script
      *)
        echo -e "${RED}Error: Unknown argument: $1${NC}"
        show_help
        exit 1
        ;;
    esac
  done
}

confirm_central() {
  if [[ "$skip_confirmation" == true ]]; then
    echo -e "${YELLOW}Skipping confirmation (--yes flag)${NC}"
    return 0
  fi
  echo -e "${YELLOW}⚠️  WARNING: You are about to publish to Maven Central!${NC}"
  echo -e "Type ${GREEN}publish${NC} to confirm: "
  read -r confirmation
  if [[ "$confirmation" != "publish" ]]; then
    echo -e "${RED}Publishing cancelled.${NC}"
    exit 1
  fi
}

main() {
  parse_args "$@"

  if [[ ! -f "./gradlew" ]]; then
    echo -e "${RED}Error: gradlew not found. Run from the repository root.${NC}"
    exit 1
  fi

  local gradle_props=""
  local publish_task=""
  local target_display=""

  case "$target" in
    local)
      publish_task="uikit-kmp:publishToMavenLocal"
      target_display="Maven Local"
      ;;
    snapshot)
      publish_task="uikit-kmp:publishAllPublicationsToGitHubPackagesRepository"
      gradle_props="-PgithubSnapshot=true"
      target_display="GitHub Packages (Snapshot)"
      ;;
    central)
      publish_task="uikit-kmp:publishAllPublicationsToCentralPortal"
      target_display="Maven Central"
      ;;
  esac

  echo ""
  echo -e "${BLUE}╔════════════════════════════════════════════╗${NC}"
  echo -e "${BLUE}║${NC}  OneStep UIKit KMP — Publishing            ${BLUE}║${NC}"
  echo -e "${BLUE}╠════════════════════════════════════════════╣${NC}"
  echo -e "${BLUE}║${NC}  Target: ${GREEN}${target_display}${NC}"
  echo -e "${BLUE}╚════════════════════════════════════════════╝${NC}"
  echo ""

  if [[ "$target" == "central" ]]; then
    confirm_central
  fi

  echo -e "${BLUE}▶ Step 1/2: Assembling release...${NC}"
  ./gradlew uikit-kmp:assembleRelease $gradle_props

  echo -e "${BLUE}▶ Step 2/2: Publishing to ${target_display}...${NC}"
  ./gradlew $publish_task $gradle_props

  echo ""
  echo -e "${GREEN}✓ Successfully published!${NC}"
  case "$target" in
    local)
      echo -e "${BLUE}Published to:${NC} ~/.m2/repository/co/onestep/kmp/uikit-kmp/"
      ;;
    snapshot)
      echo -e "${BLUE}Published to:${NC} https://github.com/OneStepRND/onestep-uikit-kmp/packages"
      ;;
    central)
      echo -e "${BLUE}Published to:${NC} https://central.sonatype.com/"
      ;;
  esac
}

main "$@"
