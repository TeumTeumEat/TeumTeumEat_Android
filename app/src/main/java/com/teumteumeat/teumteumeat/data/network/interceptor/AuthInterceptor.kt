package com.teumteumeat.teumteumeat.data.network.interceptor

import com.teumteumeat.teumteumeat.data.network.exception.UnauthorizedException
import com.teumteumeat.teumteumeat.data.network.model.TokenLocalDataSource
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenLocalDataSource: TokenLocalDataSource
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url

        // ✅ S3 presigned URL은 인증 헤더 제외
        if (url.host.contains("amazonaws.com")) {
            return chain.proceed(request)
        }
        val path = request.url.encodedPath


        // 🔴 reissue 와 회원가입 API는 인증 헤더를 붙이지 않는다
        if (path == "/api/v1/users/reissue" ||
            path == "/api/v1/auth/oauth/register") {
            return chain.proceed(request)
        }

        val accessToken = tokenLocalDataSource.getAccessToken()
        val newRequest = request.newBuilder().apply {
            if (!accessToken.isNullOrBlank()) {
                addHeader("Authorization", "Bearer $accessToken")
            }
        }.build()

        return chain.proceed(newRequest)
    }
}
