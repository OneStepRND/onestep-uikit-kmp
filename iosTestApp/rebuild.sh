#!/bin/bash
set -euo pipefail

cd "$(dirname "$0")/.."

echo "=== Building debug XCFramework ==="
./gradlew :uikit-kmp:assembleOSTUIKitDebugXCFramework

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
