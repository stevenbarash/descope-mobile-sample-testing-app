package com.descope.testapp.ui.screens.flow

import android.net.Uri
import com.descope.android.DescopeFlowView
import java.lang.ref.WeakReference

/**
 * Holds a weak reference to the active DescopeFlowView instance.
 * Used to pass deep link URIs to the flow view for authentication callbacks.
 * TODO: Consider implementing a "Descope.resume()" function like iOS to avoid this global holder.
 */
object FlowViewHolder {
    private var activeFlowView: WeakReference<DescopeFlowView>? = null

    fun setActiveFlowView(flowView: DescopeFlowView?) {
        activeFlowView = flowView?.let { WeakReference(it) }
    }

    fun resumeFromDeepLink(uri: Uri): Boolean {
        val flowView = activeFlowView?.get() ?: return false
        flowView.resumeFromDeepLink(uri)
        return true
    }

    fun clear() {
        activeFlowView = null
    }
}
