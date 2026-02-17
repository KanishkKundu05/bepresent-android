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
    private val auth0Provider = Auth0Provider(
        context,
        clientId = BuildConfig.AUTH0_CLIENT_ID,
        domain = BuildConfig.AUTH0_DOMAIN,
        scheme = "https",
        enableCachedLogins = true
    )

    val client = ConvexClientWithAuth(
        deploymentUrl = BuildConfig.CONVEX_URL,
        authProvider = auth0Provider
    )

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState

    val isAuthenticated: Boolean
        get() = _authState.value is AuthState.Authenticated

    suspend fun login() {
        _authState.value = AuthState.Loading
        try {
            client.login(context)
            _authState.value = AuthState.Authenticated
        } catch (e: Exception) {
            _authState.value = AuthState.Unauthenticated
        }
    }

    suspend fun logout() {
        try {
            client.logout(context)
        } finally {
            _authState.value = AuthState.Unauthenticated
        }
    }

    suspend fun loginFromCache() {
        try {
            client.loginFromCache()
            _authState.value = AuthState.Authenticated
        } catch (e: Exception) {
            _authState.value = AuthState.Unauthenticated
        }
    }
}
