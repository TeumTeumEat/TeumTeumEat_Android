package com.teumteumeat.teumteumeat.data.api.document

import com.teumteumeat.teumteumeat.data.network.model.ApiResponse
import com.teumteumeat.teumteumeat.data.network.model_request.PresignedRequest
import com.teumteumeat.teumteumeat.data.network.model_request.RegisterDocumentRequest
import com.teumteumeat.teumteumeat.data.network.model_response.PresignedResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface DocumentApiService {

    // 🔹 1. presigned URL 발급
    @POST("/api/v1/s3/presigned")
    suspend fun issuePresignedUrl(
        @Body request: PresignedRequest
    ): ApiResponse<PresignedResponse, Any?>

    // 🔹 2. 업로드 완료 후 문서 등록
    @POST("/api/v1/s3/goals/{goalId}/document")
    suspend fun registerDocument(
        @Path("goalId") goalId: Long,
        @Body request: RegisterDocumentRequest
    ): ApiResponse<Unit, Any?>
}