package com.teumteumeat.teumteumeat.data.repository.document

import android.net.Uri
import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model_response.PresignedResponse

interface DocumentRepository {

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
     * - 서버 API ❌
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
        goalId: Long,
        fileName: String,
        fileKey: String
    ): ApiResultV2<Unit>
}

