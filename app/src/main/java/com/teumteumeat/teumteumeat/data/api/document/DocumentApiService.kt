package com.teumteumeat.teumteumeat.data.api.document

import com.teumteumeat.teumteumeat.data.network.model.ApiResponse
import com.teumteumeat.teumteumeat.data.network.model_request.PresignedRequest
import com.teumteumeat.teumteumeat.data.network.model_request.RegisterDocumentRequest
import com.teumteumeat.teumteumeat.data.network.model_response.GetDocumentsResponse
import com.teumteumeat.teumteumeat.data.network.model_response.PresignedResponse
import com.teumteumeat.teumteumeat.data.document.response.DocumentSummaryResponse
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

    /**
     * ## PDF 문서 목표의 요약글 조회 요청
     * * ❗호출 전 주의 사항 : 요약글 생성 요청 응답으로 status(OCR 처리상태)가 'completed' 되었는지 확인 후 요청
     */
    @GET("/api/v1/goals/{goalId}/documents/{documentId}/summary")
    suspend fun getDocumentSummary(
        @Path("goalId") goalId: Int,
        @Path("documentId") documentId: Int
    ): ApiResponse<DocumentSummaryResponse, Any?>

    @POST("/api/v1/goals/{goalId}/documents/{documentId}/summary")
    suspend fun createDocumentSummary(
        @Path("goalId") goalId: Int,
        @Path("documentId") documentId: Int
    ): ApiResponse<DocumentSummaryResponse, Any?>

}