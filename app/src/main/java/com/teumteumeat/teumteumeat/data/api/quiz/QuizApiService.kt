package com.teumteumeat.teumteumeat.data.api.quiz

import com.teumteumeat.teumteumeat.data.network.model.ApiResponse
import com.teumteumeat.teumteumeat.data.network.model_request.SubmitUserQuizRequest
import com.teumteumeat.teumteumeat.data.network.model_response.QuizHistoryData
import com.teumteumeat.teumteumeat.data.network.model_response.SubmitUserQuizResponse
import com.teumteumeat.teumteumeat.data.network.model_response.UserQuizResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface QuizApiService {

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
}
