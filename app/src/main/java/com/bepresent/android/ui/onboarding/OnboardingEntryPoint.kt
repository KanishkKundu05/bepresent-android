package com.bepresent.android.ui.onboarding

import com.bepresent.android.data.datastore.PreferencesManager
import com.bepresent.android.permissions.PermissionManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface OnboardingEntryPoint {
    fun permissionManager(): PermissionManager
    fun preferencesManager(): PreferencesManager
}
