package com.teumteumeat.teumteumeat.domain.repository.pff_document

import android.net.Uri
import com.teumteumeat.teumteumeat.data.document.response.DocumentSummaryResponse
import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model_response.DocumentResponse
import com.teumteumeat.teumteumeat.data.network.model_response.PresignedResponse
import com.teumteumeat.teumteumeat.domain.model.document.DocumentSummaryId
import com.teumteumeat.teumteumeat.domain.model.document.PdfDocumentSummary

interface PdfDocumentRepository {

    /**
     * 1️⃣ presigned URL 발급
     * - 서버 API 호출
     * - 성공 시 presigned 정보 반환
     */
    suspend fun issuePresignedUrl(
        fileName: String
    ): ApiResultV2<PresignedResponse>

    /**
     * 2️⃣ S3 presigned PUT 업로드
     * - 서버 API 가 아님❌
     * - 단순 성공 / 실패만 판단
     */
    suspend fun uploadFileToS3(
        presignedUrl: String,
        uri: Uri,
        mimeType: String
    ): Result<Unit>

    /**
     * 3️⃣ 문서 등록
     * - 서버 API 호출
     */
    suspend fun registerDocument(
        goalId: Int,
        fileName: String,
        fileKey: String
    ): ApiResultV2<Unit>

    suspend fun getDocuments(
        goalId: Int
    ): ApiResultV2<List<DocumentResponse>>

    suspend fun getPdfDocumentSummaryId(
        goalId: Int,
        documentId: Int,
    ): ApiResultV2<DocumentSummaryId>

    suspend fun getPdfDocumentSummary(
        goalId: Int,
        documentId: Int,
    ): ApiResultV2<PdfDocumentSummary>

    suspend fun createDocumentSummary(
        goalId: Int,
        documentId: Int,
    ): ApiResultV2<DocumentSummaryResponse>


}