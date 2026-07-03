pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("com.gradleup.nmcp.settings") version "1.4.4"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        mavenLocal()
        // Own packages (published uikit-kmp snapshots)
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/OneStepRND/onestep-uikit-kmp")
            credentials {
                username = providers.gradleProperty("gpr.user").orNull
                    ?: System.getenv("GITHUB_ACTOR")
                password = providers.gradleProperty("gpr.key").orNull
                    ?: System.getenv("GITHUB_TOKEN")
            }
        }
        // co.onestep.android:core (snapshots)
        maven {
            name = "GitHubPackagesSDK"
            url = uri("https://maven.pkg.github.com/OneStepRND/onestep-sdk-android")
            credentials {
                username = providers.gradleProperty("gpr.user").orNull
                    ?: System.getenv("GITHUB_ACTOR")
                password = providers.gradleProperty("gpr.key").orNull
                    ?: System.getenv("GITHUB_TOKEN")
            }
        }
        // co.onestep:design-system (KMP)
        maven {
            name = "GitHubPackagesPatientApp"
            url = uri("https://maven.pkg.github.com/OneStepRND/PatientApp")
            credentials {
                username = providers.gradleProperty("gpr.user").orNull
                    ?: System.getenv("GITHUB_ACTOR")
                password = providers.gradleProperty("gpr.key").orNull
                    ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

rootProject.name = "onestep-uikit-kmp"
include(":uikit-kmp")
