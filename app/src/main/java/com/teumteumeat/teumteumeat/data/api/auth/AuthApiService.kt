package com.teumteumeat.teumteumeat.data.api.auth

import com.teumteumeat.teumteumeat.data.network.model.ApiResponse
import com.teumteumeat.teumteumeat.domain.model.ResponseBody
import com.teumteumeat.teumteumeat.domain.model.UserName
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApiService {
    @GET("/api/v1/users/name")
    suspend fun checkMyToken(): ApiResponse<UserName>

    @POST("/api/v1/users/reissue")
    suspend fun reissueAccessToken(
        @Body refreshToken: ResponseBody
    ): ApiResponse<String>
}