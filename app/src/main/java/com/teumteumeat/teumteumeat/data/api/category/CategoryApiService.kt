package com.teumteumeat.teumteumeat.data.api.category

import com.teumteumeat.teumteumeat.data.network.model.ApiResponse
import com.teumteumeat.teumteumeat.domain.model.on_boarding.CategoriesResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import com.teumteumeat.teumteumeat.data.network.model_response.DailyCategoryDocumentDto
import retrofit2.http.POST

interface CategoryApiService {
    @GET("api/v1/categories/{categoryId}/documents/daily")
    suspend fun getDailyCategoryDocument(
        @Path("categoryId") categoryId: Long
    ): ApiResponse<DailyCategoryDocumentDto, Any?>

    @POST("api/v1/categories/{categoryId}/documents/daily")
    suspend fun createDailyCategoryDocument(
        @Path("categoryId") categoryId: Long
    ): ApiResponse<DailyCategoryDocumentDto, Any?>

    @GET("/api/v1/categories")
    suspend fun getCategories(): ApiResponse<CategoriesResponseDto, Any?>
}