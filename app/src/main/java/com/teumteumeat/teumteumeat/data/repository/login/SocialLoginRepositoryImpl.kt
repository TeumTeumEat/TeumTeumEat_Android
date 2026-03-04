package com.teumteumeat.teumteumeat.data.repository.login

import android.os.Build
import com.teumteumeat.teumteumeat.BuildConfig
import com.teumteumeat.teumteumeat.data.api.auth.AuthApiService
import com.teumteumeat.teumteumeat.data.api.user.UserApiService
import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model.AuthToken
import com.teumteumeat.teumteumeat.data.network.model.TokenLocalDataSource
import com.teumteumeat.teumteumeat.data.network.model_request.AuthResponse
import com.teumteumeat.teumteumeat.data.network.model_response.SocialLoginRequest
import com.teumteumeat.teumteumeat.data.repository.BaseRepository
import com.teumteumeat.teumteumeat.domain.model.auth.ResponseBody
import com.teumteumeat.teumteumeat.domain.model.auth.SessionResult
import java.io.IOException
import java.net.UnknownHostException
import javax.inject.Inject

class SocialLoginRepositoryImpl @Inject constructor(
    private val userApiService: UserApiService,
    private val authApiService: AuthApiService,
    private val tokenLocalDataSource: TokenLocalDataSource,
) : BaseRepository(authApiService, tokenLocalDataSource), SocialLoginRepository {

    override suspend fun logout(
        refreshToken: String
    ): ApiResultV2<Unit> {
        return safeApiVer2(
            apiCall = { authApiService.logout(refreshToken) },
            mapper = {
                // ✅ 로그아웃 성공 시 토큰 삭제
                tokenLocalDataSource.clear()
                Unit
            }
        )
    }


    override suspend fun withdrawUser(): ApiResultV2<Unit> =
        safeApiVer2(
            apiCall = { authApiService.withdrawUser() },
            mapper = {
                // data는 의미 없으므로 버림
                Unit
            }
        )

    override suspend fun validateSession(): SessionResult {

        // 1️⃣ refreshToken 확인
        val refreshToken = tokenLocalDataSource.getRefreshToken()
            ?: return SessionResult.Failed(message = "로그인이 필요합니다.")

        return try {
            if (BuildConfig.VERSION_CODE >= 11) {
                val reissueResponse = authApiService.reissueAccessTokenV2(
                    ResponseBody(refreshToken)
                )

                val tokenData = reissueResponse.data

                // ❌ 서버 응답이 비정상
                if (reissueResponse.code != "OK" || tokenData.accessToken.isBlank()) {
                    tokenLocalDataSource.clear()
                    return SessionResult.Expired(
                        code = reissueResponse.code,
                        message = reissueResponse.message ?: "토큰 갱신 실패"
                    )
                }

                // ✅ refreshToken 교체 여부 판단
                val newRefreshToken =
                    tokenData.refreshToken ?: refreshToken

                tokenLocalDataSource.save(
                    AuthToken(
                        accessToken = tokenData.accessToken,
                        refreshToken = newRefreshToken
                    )
                )

                SessionResult.Success

            }else{
                val reissueResponse = authApiService.reissueAccessToken(
                    ResponseBody(refreshToken)
                )
                val accessToken = reissueResponse.data

                // ❌ 서버 응답이 비정상
                if (reissueResponse.code != "OK" || accessToken.isBlank()) {
                    tokenLocalDataSource.clear()
                    return SessionResult.Expired(
                        code = reissueResponse.code,
                        message = reissueResponse.message ?: "토큰 갱신 실패"
                    )
                }

                // ✅ refreshToken 교체 여부 판단
                val newRefreshToken = refreshToken

                tokenLocalDataSource.save(
                    AuthToken(
                        accessToken = accessToken,
                        refreshToken = newRefreshToken
                    )
                )

                SessionResult.Success
            }

        }catch (e: retrofit2.HttpException) {

            if (e.code() == 401) {
                tokenLocalDataSource.clear()
                return SessionResult.Expired(
                    code = "AUTH-003",
                    message = "토큰이 만료되었습니다."
                )
            }

            SessionResult.Failed(message = "서버 오류가 발생했습니다.")

        } catch (e: UnknownHostException) {
            SessionResult.NetworkError("인터넷 연결이 필요합니다.")
        } catch (e: IOException) {
            SessionResult.NetworkError("네트워크 연결을 확인해주세요.")
        }
    }


    override suspend fun socialLogin(
        provider: String,
        request: SocialLoginRequest,
    ): ApiResultV2<AuthResponse?> {

        return safeApiVer2(
            apiCall = {
                authApiService.socialLogin(
                    provider = provider,
                    request = request,
                )
            },
            mapper = {
                it
            }
        )
    }


}
