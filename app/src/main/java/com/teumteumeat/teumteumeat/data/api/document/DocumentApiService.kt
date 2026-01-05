package com.teumteumeat.teumteumeat.data.api.document

import com.teumteumeat.teumteumeat.data.network.model.ApiResponse
import com.teumteumeat.teumteumeat.data.network.model_request.PresignedRequest
import com.teumteumeat.teumteumeat.data.network.model_request.RegisterDocumentRequest
import com.teumteumeat.teumteumeat.data.network.model_response.GetDocumentsResponse
import com.teumteumeat.teumteumeat.data.network.model_response.PresignedResponse
import com.teumteumeat.teumteumeat.data.network.model_response.QuizListResponse
import com.teumteumeat.teumteumeat.ui.screen.b1_summary.DocumentSummaryResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface DocumentApiService {

    // 🔹 1. presigned URL 발급
    @POST("/api/v1/s3/presigned")
    suspend fun issuePresignedUrl(
        @Body request: PresignedRequest
    ): ApiResponse<PresignedResponse, Any?>

    // 🔹 2. 업로드 완료 후 문서 등록
    @POST("/api/v1/goals/{goalId}/documents")
    suspend fun registerDocument(
        @Path("goalId") goalId: Int,
        @Body request: RegisterDocumentRequest
    ): ApiResponse<Unit, Any?>

    @GET("/api/v1/goals/{goalId}/documents")
    suspend fun getDocuments(
        @Path("goalId") goalId: Int
    ): ApiResponse<GetDocumentsResponse, Any?>

    // todo. summary 조회 전 OCR 처리가 되었는지 확인 api 구현
    // todo. 처리가 안됬으면 30초 대기로딩 표시 후 api 처리 완료 확인 후 아래 api 재요청

    // todo. OCR 처리가 되었는지 확인 api 확인 후 아래 호출 구현
    // ---- 유저 퀴즈 생성하는데 summary 요약글 조회 필요 ---
    @GET("/api/v1/goals/{goalId}/documents/{documentId}/summary")
    suspend fun getDocumentSummary(
        @Path("goalId") goalId: Int,
        @Path("documentId") documentId: Int
    ): ApiResponse<DocumentSummaryResponse, Any?>

}