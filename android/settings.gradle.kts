pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "DescopeSampleApp"
include(":app")

/*
// This section connects this application to the local Descope SDK module for development purposes.
// If changes are required to the Descope SDK during testing or development, this setup allows
// you to make those changes locally without needing to publish the SDK to a remote repository.
// The path may need adjustment based on your local setup
include(":descopesdk")
project(":descopesdk").projectDir = File(settingsDir, "../../../descope/descope-kotlin/descopesdk")
dependencyResolutionManagement {
    versionCatalogs {
        create("descopeLibs") {
            from(files("../../../descope/descope-kotlin/gradle/libs.versions.toml"))
        }
    }
}
*/
