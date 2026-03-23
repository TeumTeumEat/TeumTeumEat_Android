package com.teumteumeat.teumteumeat.data.api.quiz

import com.teumteumeat.teumteumeat.data.network.model.ApiResponse
import com.teumteumeat.teumteumeat.data.network.model_request.SubmitUserQuizRequest
import com.teumteumeat.teumteumeat.data.network.model_response.QuizGuideResponse
import com.teumteumeat.teumteumeat.data.network.model_response.QuizHistoryData
import com.teumteumeat.teumteumeat.data.network.model_response.SubmitUserQuizResponse
import com.teumteumeat.teumteumeat.data.network.model_response.UserQuizResponse
import com.teumteumeat.teumteumeat.data.network.model_response.quiz.UserQuizStatusResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface QuizApiService {

    @POST("/api/v1/user-quizzes/guide")
    suspend fun confirmQuizGuide(): ApiResponse<QuizGuideResponse, Any?>

    /**
     * 유저의 오늘 퀴즈 풀이 여부 및 최초 풀이 여부,
     * 오늘 요약글 생성 여부를 반환합니다. (홈/인트로 화면용)
     */
    @GET("/api/v1/user-quizzes/status")
    suspend fun getUserQuizStatus():
            ApiResponse<UserQuizStatusResponse, Any?>

    @POST("/api/v1/user-quizzes/submit")
    suspend fun submitUserQuiz(
        @Body request: SubmitUserQuizRequest
    ): ApiResponse<SubmitUserQuizResponse, Any?>

    @GET("/api/v1/user-quizzes")
    suspend fun getuserQuizzes(
        @Query("documentId") documentId: Int,
        @Query("documentType") documentType: String = "CATEGORY"
    ): ApiResponse<List<UserQuizResponse>, Any?>

    @GET("api/v1/history/details/quizzes/{type}/{id}")
    suspend fun getQuizHistory(
        @Path("type") type: String, // CATEGORY / DOCUMENT
        @Path("id") id: Long,
        @Query("date") date: String
    ): ApiResponse<QuizHistoryData, Any?>

    @POST("/api/v1/user-quizzes/ad-reward")
    suspend fun submitAdWatching(): ApiResponse<Unit, Any?>

    /**
     * 완료된 퀴즈 세트를 서버에 제출하는 API 입니다.
     */
    @POST("/api/v1/user-quizzes/complete-set")
    suspend fun submitCompleteQuizSet(): ApiResponse<Unit, Any?>

}
