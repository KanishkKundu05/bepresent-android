package com.bepresent.android.data.convex

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.convex.android.ConvexClientWithAuth
import dev.convex.android.auth0.Auth0Provider
import com.bepresent.android.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

sealed class AuthState {
    data object Unauthenticated : AuthState()
    data object Loading : AuthState()
    data object Authenticated : AuthState()
}

@Singleton
class ConvexManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val isConfigured: Boolean =
        BuildConfig.AUTH0_DOMAIN.isNotBlank() &&
        BuildConfig.AUTH0_CLIENT_ID.isNotBlank() &&
        BuildConfig.CONVEX_URL.isNotBlank()

    private val auth0Provider: Auth0Provider? = if (isConfigured) {
        Auth0Provider(
            context,
            clientId = BuildConfig.AUTH0_CLIENT_ID,
            domain = BuildConfig.AUTH0_DOMAIN,
            scheme = "https",
            enableCachedLogins = true
        )
    } else null

    val client = if (isConfigured && auth0Provider != null) {
        ConvexClientWithAuth(
            deploymentUrl = BuildConfig.CONVEX_URL,
            authProvider = auth0Provider
        )
    } else null

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState

    val isAuthenticated: Boolean
        get() = _authState.value is AuthState.Authenticated

    suspend fun login() {
        val c = client ?: return
        _authState.value = AuthState.Loading
        try {
            c.login(context)
            _authState.value = AuthState.Authenticated
        } catch (e: Exception) {
            _authState.value = AuthState.Unauthenticated
        }
    }

    suspend fun logout() {
        val c = client ?: return
        try {
            c.logout(context)
        } finally {
            _authState.value = AuthState.Unauthenticated
        }
    }

    suspend fun loginFromCache() {
        val c = client ?: return
        try {
            c.loginFromCache()
            _authState.value = AuthState.Authenticated
        } catch (e: Exception) {
            _authState.value = AuthState.Unauthenticated
        }
    }
}
