package com.bepresent.android.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bepresent.android.data.convex.AuthState
import com.bepresent.android.data.convex.ConvexManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val convexManager: ConvexManager
) : ViewModel() {

    val authState: StateFlow<AuthState> = convexManager.authState

    val isLoggedIn: Boolean
        get() = convexManager.isAuthenticated

    fun login() {
        viewModelScope.launch {
            convexManager.login()
            // After successful login, upsert user in Convex
            if (convexManager.isAuthenticated) {
                try {
                    convexManager.client?.mutation<String>("users:store")
                } catch (_: Exception) {
                    // Non-critical: user store can be retried
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            convexManager.logout()
        }
    }

    fun loginFromCache() {
        viewModelScope.launch {
            convexManager.loginFromCache()
            if (convexManager.isAuthenticated) {
                try {
                    convexManager.client?.mutation<String>("users:store")
                } catch (_: Exception) {}
            }
        }
    }
}
