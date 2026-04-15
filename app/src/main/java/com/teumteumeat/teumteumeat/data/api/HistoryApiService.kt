package com.teumteumeat.teumteumeat.data.api

import com.teumteumeat.teumteumeat.data.network.model.ApiResponse
import com.teumteumeat.teumteumeat.data.network.model_response.CalendarHistoryResponse
import com.teumteumeat.teumteumeat.data.history.response.HistorySummaryResponse
import com.teumteumeat.teumteumeat.data.network.model_response.history.CategoryHistoryResponse
import com.teumteumeat.teumteumeat.data.network.model_response.history.HistoryItemResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query


interface HistoryApiService {

    /**
     * ### 주제별 요약글 리스트 조회
     * * 학습한 모든 내역을 카테고리 또는 pdf 문서별로 그룹화하여 보여줍니다.
     */
    @GET("/api/v1/history/topics")
    suspend fun getCategoryHistories(): ApiResponse<List<CategoryHistoryResponse>, Any?>

    /**
     * ### 날짜별 요약글 리스트 조회
     * * 특정 날짜에 학습한 요약글을 시간순으로 조회합니다.
     */
    @GET("/api/v1/history/date/{date}")
    suspend fun getHistoryByDate(
        @Path("date") date: String // yyyy-MM-dd
    ): ApiResponse<List<HistoryItemResponse>?, Any?>

    /**
     * ### 요약글 상세 내용 보기
     * * 특정 학습 기록의 전체 요약글(카테고리 자료, PDF 요약본) 내용을 조회합니다.
     * * 사용용도 : 히스토리 목록에서 제공된 id 로 요약글을 조회하고자 할 때
     * @param type 학습 목표 타입 -  DOCUMENT(PDF 문서) / CATEGORY(카테고리)
     * @param id 요약글id - 날짜별/주제별 요약글 리스트에서 응답받은 id
     * @param date 조회하고자 하는 날짜 (현재 일자) - yyyy-MM-dd
     */
    @GET("/api/v1/history/details/summary/{type}/{id}")
    suspend fun getHistorySummary(
        @Path("type") type: String,   // DOCUMENT / CATEGORY
        @Path("id") id: Long, // 히스토리
        @Query("date") date: String   // yyyy-MM-dd
    ): ApiResponse<HistorySummaryResponse?, Any?>

    @GET("/api/v1/history/calendar")
    suspend fun getCalendarHistory(
        @Query("year") year: Int,    // $int32
        @Query("month") month: Int   // $int32
    ): ApiResponse<CalendarHistoryResponse?, Any?>



}
