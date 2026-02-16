package com.teumteumeat.teumteumeat.data.api

import com.teumteumeat.teumteumeat.data.network.model.ApiResponse
import com.teumteumeat.teumteumeat.data.network.model_response.CalendarHistoryResponse
import com.teumteumeat.teumteumeat.data.network.model_response.HistorySummaryResponse
import com.teumteumeat.teumteumeat.data.network.model_response.history.CategoryHistoryResponse
import com.teumteumeat.teumteumeat.data.network.model_response.history.HistoryItemResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query


interface HistoryApiService {

    @GET("/api/v1/history/topics")
    suspend fun getCategoryHistories(): ApiResponse<List<CategoryHistoryResponse>, Any?>

    @GET("/api/v1/history/details/summary/{type}/{id}")
    suspend fun getHistorySummary(
        @Path("type") type: String,   // DOCUMENT / CATEGORY
        @Path("id") id: Long,
        @Query("date") date: String   // yyyy-MM-dd
    ): ApiResponse<HistorySummaryResponse?, Any?>

    @GET("/api/v1/history/calendar")
    suspend fun getCalendarHistory(
        @Query("year") year: Int,    // $int32
        @Query("month") month: Int   // $int32
    ): ApiResponse<CalendarHistoryResponse?, Any?>

    @GET("/api/v1/history/date/{date}")
    suspend fun getHistoryByDate(
        @Path("date") date: String // yyyy-MM-dd
    ): ApiResponse<List<HistoryItemResponse>?, Any?>
}
