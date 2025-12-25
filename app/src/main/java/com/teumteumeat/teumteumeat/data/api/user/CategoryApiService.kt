package com.teumteumeat.teumteumeat.data.api.user

import com.teumteumeat.teumteumeat.data.network.model.ApiResponse
import com.teumteumeat.teumteumeat.data.network.model.EmptyResponse
import com.teumteumeat.teumteumeat.data.network.model.FieldErrorDetail
import com.teumteumeat.teumteumeat.domain.model.on_boarding.CategoriesResponseDto
import com.teumteumeat.teumteumeat.domain.model.on_boarding.OnboardingStatus
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST

interface CategoryApiService {
    @GET("/api/v1/categories")
    suspend fun getCategories(): ApiResponse<CategoriesResponseDto, Any?>
}