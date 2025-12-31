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

    @Volatile
    private var cachedAccessToken: String? = null

    @Volatile
    private var cachedRefreshToken: String? = null

    init {
        // 앱 시작 시 캐시 초기화
        cachedAccessToken = prefs.getString(KEY_ACCESS, null)
        cachedRefreshToken = prefs.getString(KEY_REFRESH, null)
    }

    fun save(token: AuthToken) {
        // 1️⃣ 메모리 캐시 즉시 반영
        cachedAccessToken = token.accessToken
        cachedRefreshToken = token.refreshToken

        // 2️⃣ 디스크 저장
        prefs.edit {
            putString(KEY_ACCESS, token.accessToken)
            putString(KEY_REFRESH, token.refreshToken)
        }
    }

    fun getAccessToken(): String? = cachedAccessToken

    fun getRefreshToken(): String? = cachedRefreshToken

    fun clear() {
        cachedAccessToken = null
        cachedRefreshToken = null
        prefs.edit { clear() }
    }
}


