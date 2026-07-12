plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose.compiler)
}

android {
    namespace = "co.onestep.kmp.uikit.testapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "co.onestep.kmp.uikit.testapp"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
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

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8)
    }
}

dependencies {
    implementation(project(":uikit-kmp"))
    implementation(project(":testAppShared"))

    // Core SDK — same resolution logic as uikit-kmp's androidMain dependency:
    // -PcoreVersion=X overrides verbatim; -PgithubSnapshot=true appends -SNAPSHOT.
    val githubSnapshot = (findProperty("githubSnapshot") as String?)?.toBoolean() == true
    val coreVersion = (findProperty("coreVersion") as String?)
        ?: if (githubSnapshot) {
            "${libs.versions.coreVersion.get()}-SNAPSHOT"
        } else {
            libs.versions.coreVersion.get()
        }
    implementation("co.onestep.android:core:$coreVersion")

    implementation(compose.runtime)
    implementation(compose.foundation)
    implementation(compose.material3)
    implementation(compose.ui)
    implementation(compose.components.uiToolingPreview)
    debugImplementation(compose.uiTooling)

    implementation(libs.androidx.activity.compose)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${libs.versions.coroutines.get()}")
}
