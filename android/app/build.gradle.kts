plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
}

// ================== Descope Configuration ==================
// Update these values with your Descope project settings from https://app.descope.com

// Your Descope project ID
val descopeProjectId = "YOUR_DESCOPE_PROJECT_ID"

// The Descope API base URL (usually "https://api.descope.com")
val descopeBaseUrl = "https://api.descope.com"

// Your app's domain, used for deep links and enchanted link verification
val deepLinkHost = "YOUR_APP_DOMAIN"

// Comma-separated flow IDs configured in the Descope console
val descopeFlowIds = "YOUR_FLOW_IDS"
val descopeAuthenticatedFlowIds = "YOUR_AUTHENTICATED_FLOW_IDS"
// =========================================================

android {
    namespace = "com.descope.testapp" // Replace with your app's package name
    compileSdk = 36

    defaultConfig {
        applicationId = "com.descope.testapp" // Replace with your app's application ID
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "0.0.1"

        buildConfigField("String", "DESCOPE_PROJECT_ID", "\"$descopeProjectId\"")
        buildConfigField("String", "DESCOPE_BASE_URL", "\"$descopeBaseUrl\"")
        buildConfigField("String", "DESCOPE_FLOW_IDS", "\"$descopeFlowIds\"")
        buildConfigField("String", "DESCOPE_AUTHENTICATED_FLOW_IDS", "\"$descopeAuthenticatedFlowIds\"")
        buildConfigField("String", "DEEP_LINK_HOST", "\"$deepLinkHost\"")

        // Manifest placeholder for deep links
        manifestPlaceholders["DEEP_LINK_HOST"] = deepLinkHost
    }

    signingConfigs {
        create("release") {
            storeFile = file("YOUR_KEYSTORE_FILE") // e.g., "release-key.jks"
            storePassword = "YOUR_KEYSTORE_PASSWORD"
            keyAlias = "YOUR_KEY_ALIAS"
            keyPassword = "YOUR_KEY_PASSWORD"
        }
    }
    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("release")
        }
        release {
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
}

dependencies {
    // Core
    implementation(libs.androidx.core.ktx)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)

    // Lifecycle
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.process)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Descope SDK
    // Option 1: Use the published version from Maven Central
    implementation(libs.descope.kotlin)
    // Option 2: Use local version for development (requires settings.gradle.kts adjustment)
//    implementation(project(":descopesdk"))

    // Debug
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
