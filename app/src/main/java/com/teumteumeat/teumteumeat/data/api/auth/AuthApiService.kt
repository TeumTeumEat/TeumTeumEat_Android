package com.teumteumeat.teumteumeat.data.api.auth

import com.teumteumeat.teumteumeat.data.network.model.ApiResponse
import com.teumteumeat.teumteumeat.data.network.model_request.AuthResponse
import com.teumteumeat.teumteumeat.data.network.model_response.SocialLoginRequest
import com.teumteumeat.teumteumeat.domain.model.auth.ResponseBody
import com.teumteumeat.teumteumeat.domain.model.on_boarding.UserName
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthApiService {
    @POST("/api/v1/auth/logout")
    suspend fun logout(): ApiResponse<Unit, Any?>

    @GET("/api/v1/users/name")
    suspend fun checkMyToken(): ApiResponse<UserName, Unit>

    @POST("/api/v1/users/reissue")
    suspend fun reissueAccessToken(
        @Body refreshToken: ResponseBody
    ): ApiResponse<String, Unit>

    @POST("/api/v1/auth/oauth/register")
    suspend fun socialLogin(
        @Query("provider") provider: String,
        @Body request: SocialLoginRequest
    ): ApiResponse<AuthResponse?, Any?>

    @DELETE("/api/v1/users/withdrawal")
    suspend fun withdrawUser(): ApiResponse<String, Any?>
}