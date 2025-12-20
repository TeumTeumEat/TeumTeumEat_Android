package com.teumteumeat.teumteumeat.data.network.model

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit


@Singleton
class TokenLocalDataSource @Inject constructor(
    @ApplicationContext context: Context
) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ACCESS = "access_token"
        private const val KEY_REFRESH = "refresh_token"
    }

    fun save(token: AuthToken) {
        prefs.edit {
            putString(KEY_ACCESS, token.accessToken)
                .putString(KEY_REFRESH, token.refreshToken)
        }
    }

    fun getAccessToken(): String? =
        prefs.getString(KEY_ACCESS, null)

    fun getRefreshToken(): String? =
        prefs.getString(KEY_REFRESH, null)

    fun clear() {
        prefs.edit { clear() }
    }
}


