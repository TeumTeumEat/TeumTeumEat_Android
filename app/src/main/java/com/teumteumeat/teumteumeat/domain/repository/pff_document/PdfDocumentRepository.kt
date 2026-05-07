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
        fileName: String,
        fileSize: Long
    ): ApiResultV2<PresignedResponse>

    /**
     * 1.5️⃣ URI → ByteArray 읽기
     * - ContentProvider에서 파일 바이트를 한 번만 읽어 반환
     * - presigned URL 발급 전 호출하여 fileSize 확정에 사용
     */
    suspend fun readFileBytes(uri: Uri): Result<ByteArray>

    /**
     * 2️⃣ S3 presigned PUT 업로드
     * - 서버 API 가 아님❌
     * - 단순 성공 / 실패만 판단
     */
    suspend fun uploadFileToS3(
        presignedUrl: String,
        bytes: ByteArray,
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