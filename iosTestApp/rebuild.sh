#!/bin/bash
set -euo pipefail

cd "$(dirname "$0")/.."

echo "=== Building debug XCFramework (testAppShared: shared test-app UI + uikit-kmp) ==="
./gradlew :testAppShared:assembleOSTUIKitDebugXCFramework

# The OSTUIKitKMP Swift package's binary target points at uikit-kmp/build/XCFrameworks/debug.
# For the test harness we substitute the testAppShared-built framework (same module name
# "OSTUIKit", same uikit-kmp API surface re-exported, plus the shared test-app entry points).
rm -rf uikit-kmp/build/XCFrameworks/debug/OSTUIKit.xcframework
mkdir -p uikit-kmp/build/XCFrameworks/debug
cp -R testAppShared/build/XCFrameworks/debug/OSTUIKit.xcframework uikit-kmp/build/XCFrameworks/debug/

echo ""
echo "=== Copying compose resources ==="
RESOURCES_SRC="uikit-kmp/build/generated/compose/resourceGenerator/preparedResources/commonMain"
RESOURCES_DST="uikit-kmp/OSTUIKitKMP/Sources/Resources/compose-resources"
NAMESPACE="co.onestep.kmp.uikit_kmp.generated.resources"
rm -rf "$RESOURCES_DST"
mkdir -p "$RESOURCES_DST/composeResources/$NAMESPACE"
cp -R "$RESOURCES_SRC"/composeResources/* "$RESOURCES_DST/composeResources/$NAMESPACE/"
echo "Copied compose resources to $RESOURCES_DST/composeResources/$NAMESPACE/"

echo ""
echo "=== Generating Xcode project ==="
cd iosTestApp
xcodegen generate
cd ..

echo ""
echo "=== Opening Xcode ==="
open iosTestApp/OSTUIKitTestApp.xcodeproj

echo ""
echo "Done! Select an iOS 16+ simulator in Xcode and press Cmd+R to run."
