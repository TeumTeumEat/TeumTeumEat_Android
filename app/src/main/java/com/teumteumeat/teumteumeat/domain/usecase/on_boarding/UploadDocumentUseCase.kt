package com.teumteumeat.teumteumeat.domain.usecase.on_boarding

import android.net.Uri
import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.repository.document.DocumentRepository
import javax.inject.Inject

class UploadDocumentUseCase @Inject constructor(
    private val repository: DocumentRepository
) {

    suspend operator fun invoke(
        goalId: Long,
        uri: Uri,
        fileName: String,
        mimeType: String
    ): ApiResultV2<Unit> {

        // 1️⃣ presigned URL 발급
        val presignedResult = repository.issuePresignedUrl(fileName)

        val presigned = when (presignedResult) {
            is ApiResultV2.Success -> presignedResult.data

            // 🔐 인증 만료 / 서버 에러 / 네트워크 에러는 그대로 위로 전달
            else -> return presignedResult as ApiResultV2<Unit>
        }

        // 2️⃣ S3 PUT 업로드
        val uploadResult = repository.uploadFileToS3(
            presignedUrl = presigned.presignedUrl,
            uri = uri,
            mimeType = mimeType
        )

        if (uploadResult.isFailure) {
            // 👉 S3 업로드 실패는 서버 에러가 아니므로 UnknownError로 래핑
            return ApiResultV2.UnknownError(
                message = "파일 업로드에 실패했습니다.",
                throwable = uploadResult.exceptionOrNull()
            )
        }

        // 3️⃣ 서버에 문서 등록
        val registerResult = repository.registerDocument(
            goalId = goalId,
            fileName = fileName,
            fileKey = presigned.key
        )

        return when (registerResult) {
            is ApiResultV2.Success -> {
                ApiResultV2.Success(
                    message = "파일 업로드가 완료되었습니다.",
                    data = Unit
                )
            }

            else -> registerResult
        }
    }
}
