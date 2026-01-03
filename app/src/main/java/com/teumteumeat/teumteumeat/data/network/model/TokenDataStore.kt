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
        // ✅ 소셜 로그인 정보
        private const val KEY_PROVIDER = "provider"
        private const val KEY_ID_TOKEN = "id_token"
    }

    @Volatile
    private var cachedAccessToken: String? = null

    @Volatile
    private var cachedRefreshToken: String? = null

    @Volatile
    private var cachedProvider: String? = null

    @Volatile
    private var cachedIdToken: String? = null


    init {
        // 앱 시작 시 캐시 초기화
        cachedAccessToken = prefs.getString(KEY_ACCESS, null)
        cachedRefreshToken = prefs.getString(KEY_REFRESH, null)
        // ✅ 소셜 로그인 정보 캐시
        cachedProvider = prefs.getString(KEY_PROVIDER, null)
        cachedIdToken = prefs.getString(KEY_ID_TOKEN, null)
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

    fun saveSocialLoginInfo(
        provider: String,
        idToken: String,
    ) {
        // 1️⃣ 메모리 캐시
        cachedProvider = provider
        cachedIdToken = idToken

        // 2️⃣ 디스크 저장
        prefs.edit {
            putString(KEY_PROVIDER, provider)
            putString(KEY_ID_TOKEN, idToken)
        }
    }

    fun getAccessToken(): String? = cachedAccessToken

    fun getRefreshToken(): String? = cachedRefreshToken

    fun getProvider(): String? = cachedProvider

    fun getIdToken(): String? = cachedIdToken


    fun clear() {
        cachedAccessToken = null
        cachedRefreshToken = null
        cachedProvider = null
        cachedIdToken = null
        prefs.edit { clear() }
    }
}


