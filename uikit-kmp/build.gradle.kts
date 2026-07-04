import io.github.frankois944.spmForKmp.swiftPackageConfig
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.android.library)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.signing)
    alias(libs.plugins.nmcp)
    // Phase 0 spike: SPM import into Kotlin (see spike/SpikeObjCKit)
    id("io.github.frankois944.spmForKmp") version "1.9.4"
}

group = "co.onestep.kmp"

val versionMajor = 0
val versionMinor = 1
val versionPatch = 0

val baseVersionName = "$versionMajor.$versionMinor.$versionPatch"
val githubSnapshot = (findProperty("githubSnapshot") as String?)?.toBoolean() == true
val publishVersion = if (githubSnapshot && !baseVersionName.endsWith("-SNAPSHOT")) {
    "$baseVersionName-SNAPSHOT"
} else {
    baseVersionName
}
version = publishVersion

kotlin {
    // Suppress expect/actual class beta warnings
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
        publishLibraryVariants("release")
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8)
        }
    }

    val xcf = XCFramework("OSTUIKit")
    // iosX64 (Intel simulator) was dropped: Compose Multiplatform stopped publishing x64 iOS
    // artifacts after 1.11.0-alpha01, so the target can no longer resolve compose dependencies.
    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "OSTUIKit"
            isStatic = true
            xcf.add(this)
            export(libs.onestep.design.system)
        }
        // Phase 0 spike: local @objc Swift package imported into iosMain via spm4Kmp.
        iosTarget.swiftPackageConfig(cinteropName = "spikeInterop") {
            minIos = "16.0"
            dependency {
                localPackage(
                    path = "${rootProject.projectDir}/spike/SpikeObjCKit",
                    packageName = "SpikeObjCKit",
                    products = {
                        add("SpikeObjCKit", exportToKotlin = true)
                    },
                )
                // Spike goal (a): binary-target SPM package (XCFramework). Expected to link,
                // but expose ~no API to Kotlin (pure-Swift surface, no @objc facade yet).
                remotePackageVersion(
                    url = uri("https://github.com/OneStepRND/onestep-sdk-ios"),
                    version = "2.0.8-rc1",
                    packageName = "onestep-sdk-ios",
                    products = {
                        add("OneStepSDK", exportToKotlin = true)
                    },
                )
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.animation)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.kotlinx.serialization.json)
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${libs.versions.coroutines.get()}")
            // KMP ViewModel + Lifecycle
            implementation(libs.lifecycle.viewmodel.compose)
            implementation(libs.lifecycle.runtime.compose)
            // KMP Navigation 3
            implementation(libs.navigation3.ui)
            implementation(libs.lifecycle.viewmodel.navigation3)
            // Design System
            api(libs.onestep.design.system)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }

        androidMain.dependencies {
            implementation(compose.uiTooling)
            // Core module dependency (resolved from Maven Central / GitHub Packages / mavenLocal).
            // -PcoreVersion=X overrides verbatim; otherwise the catalog version is used,
            // with -SNAPSHOT appended for githubSnapshot builds.
            val coreVersion = (findProperty("coreVersion") as String?)
                ?: if (githubSnapshot) {
                    "${libs.versions.coreVersion.get()}-SNAPSHOT"
                } else {
                    libs.versions.coreVersion.get()
                }
            implementation("co.onestep.android:core:$coreVersion")

            implementation(libs.androidx.activity.compose)
            implementation(libs.coil)
            implementation(libs.coil.gif)
            implementation(libs.coil.compose)
            implementation(libs.media3.expoplyaer)
            implementation(libs.media3.ui)
            // Android navigation-compose now provided by KMP navigation in commonMain
        }

        iosMain.dependencies {
        }
    }
}

android {
    namespace = "co.onestep.kmp.uikit"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            pickFirsts += "META-INF/LICENSE.md"
            pickFirsts += "META-INF/LICENSE-notice.md"
        }
    }
}

// ── iOS Compose Resources ───────────────────────────────────────────────────────
// CMP does NOT embed compose resources inside static frameworks. We must copy them
// alongside the XCFramework so the iOS app can include them in its bundle.
listOf("debug", "release").forEach { variant ->
    val capitalizedVariant = variant.replaceFirstChar { it.uppercase() }
    tasks.register<Copy>("copyComposeResources${capitalizedVariant}") {
        // Kotlin 2.1 moved multiplatform resource assembly out of processedResources.
        from("build/kotlin-multiplatform-resources/assemble-hierarchically/iosArm64ResolveSelfResources/composeResources")
        into("build/XCFrameworks/${variant}/compose-resources/composeResources")
    }
    tasks.matching { it.name == "assembleOSTUIKit${capitalizedVariant}XCFramework" }.configureEach {
        finalizedBy("copyComposeResources${capitalizedVariant}")
    }
}

// ── Publishing ──────────────────────────────────────────────────────────────────
// KMP plugin auto-creates publications per target; we only add POM metadata + repos.
publishing {
    publications.withType<MavenPublication> {
        pom {
            name.set("OneStep UIKit KMP")
            description.set("Kotlin Multiplatform UI Kit for the OneStep SDK")
            url.set("http://www.onestep.co")

            developers {
                developer {
                    id.set("shahar@onestep.co")
                    name.set("Shahar Davidson")
                    email.set("shahar@onestep.co")
                }

                developer {
                    id.set("ziv@onestep.co")
                    name.set("Ziv Kesten")
                    email.set("ziv@onestep.co")
                }
            }

            licenses {
                license {
                    name.set("The Apache Software License, Version 2.0")
                    url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }

            scm {
                connection.set("scm:git:git://github.com/OneStepRND/onestep-uikit-kmp.git")
                developerConnection.set("scm:git:ssh://github.com/OneStepRND/onestep-uikit-kmp.git")
                url.set("scm:git:ssh://github.com/OneStepRND/onestep-uikit-kmp.git")
            }
        }
    }

    repositories {
        mavenLocal()
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/OneStepRND/onestep-uikit-kmp")
            credentials {
                username = findProperty("gpr.user") as String?
                    ?: System.getenv("GITHUB_ACTOR")
                password = findProperty("gpr.key") as String?
                    ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

// ── Signing: configure only when creds exist (so non-publish CI won't fail) ─────
val signingKeyPathEnv: String? = System.getenv("SIGNING_KEYID_PATH")
val signingKeyPathProp = findProperty("signing.keyPath") as String?
val signingPasswordEnv: String? = System.getenv("SIGNING_PASSWORD")
val signingPasswordProp = findProperty("signing.password") as String?

val signingKeyPath = signingKeyPathEnv ?: signingKeyPathProp
val signingPassword = signingPasswordEnv ?: signingPasswordProp

val haveSigning = !signingKeyPath.isNullOrBlank() && !signingPassword.isNullOrBlank()

if (haveSigning) {
    signing {
        val asciiArmoredKey = file(signingKeyPath!!).readText()
        useInMemoryPgpKeys(asciiArmoredKey, signingPassword!!)
        sign(publishing.publications)
    }
} else {
    tasks.withType<Sign>().configureEach {
        enabled = false
    }
    logger.lifecycle("⚠️  Skipping signing: SIGNING_KEYID_PATH / SIGNING_PASSWORD not provided.")
}

// ── NMCP (Sonatype Central Portal) ─────────────────────────────────────────────
nmcp {
    val ossUser = System.getenv("MAVEN_CENTRAL_USERNAME")
        ?: (findProperty("MAVEN_CENTRAL_USERNAME") as String?)
        ?: ""
    val ossPass = System.getenv("MAVEN_CENTRAL_PASSWORD")
        ?: (findProperty("MAVEN_CENTRAL_PASSWORD") as String?)
        ?: ""

    publishAllPublicationsToCentralPortal {
        if (ossUser.isNotBlank()) username = ossUser
        if (ossPass.isNotBlank()) password = ossPass
        publishingType = "AUTOMATIC"
    }
}
