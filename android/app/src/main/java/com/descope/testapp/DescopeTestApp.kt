package com.descope.testapp

import android.app.Application
import com.descope.Descope
import com.descope.sdk.DescopeLogger

class DescopeTestApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize Descope SDK
        Descope.setup(this, projectId = BuildConfig.DESCOPE_PROJECT_ID) {
            baseUrl = BuildConfig.DESCOPE_BASE_URL
            logger = DescopeLogger.debugLogger
        }
    }
}
