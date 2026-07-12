import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

// Shared test-app UI (unpublished): the single source of truth for the test harness screens,
// consumed by androidTestApp (project dep) and iosTestApp (via the OSTUIKit framework below).
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.android.library)
}

kotlin {
    targets.configureEach {
        compilations.configureEach {
            compileTaskProvider.configure {
                compilerOptions {
                    freeCompilerArgs.add("-Xexpect-actual-classes")
                }
            }
        }
    }

    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8)
        }
    }

    // The test-app framework keeps the library's module name (OSTUIKit) and re-exports
    // :uikit-kmp, so the OSTUIKitKMP Swift package (bridges + SwiftUI wrappers) compiles
    // against it unchanged. Built only by iosTestApp/rebuild.sh — never published.
    val xcf = XCFramework("OSTUIKit")
    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "OSTUIKit"
            isStatic = true
            xcf.add(this)
            export(project(":uikit-kmp"))
            export(libs.onestep.design.system)
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":uikit-kmp"))
            api(libs.onestep.design.system)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.kotlinx.serialization.json)
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${libs.versions.coroutines.get()}")
        }

        androidMain.dependencies {
            // Native OneStep Android SDK — the Android TestAppShell implementation lives here.
            val githubSnapshot = (findProperty("githubSnapshot") as String?)?.toBoolean() == true
            val coreVersion = (findProperty("coreVersion") as String?)
                ?: if (githubSnapshot) {
                    "${libs.versions.coreVersion.get()}-SNAPSHOT"
                } else {
                    libs.versions.coreVersion.get()
                }
            implementation("co.onestep.android:core:$coreVersion")
        }
    }
}

android {
    namespace = "co.onestep.kmp.uikit.testapp.shared"
    compileSdk = 36

    defaultConfig {
        minSdk = 28
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures {
        compose = true
    }
}
