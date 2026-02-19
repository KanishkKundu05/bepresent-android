package com.bepresent.android.service

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import com.bepresent.android.debug.RuntimeLog

class AccessibilityMonitorService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        RuntimeLog.i(TAG, "Accessibility service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Reserved for foreground app detection integration.
    }

    override fun onInterrupt() = Unit

    companion object {
        private const val TAG = "BP_A11y"
    }
}
