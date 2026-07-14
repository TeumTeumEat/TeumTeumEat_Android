package com.teumteumeat.teumteumeat.domain.usecase.document

import android.net.Uri
import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.domain.repository.pff_document.PdfDocumentRepository
import javax.inject.Inject

class UploadDocumentUseCase @Inject constructor(
    private val repository: PdfDocumentRepository
) {

    suspend operator fun invoke(
        goalId: Int,
        uri: Uri,
        fileName: String,
        mimeType: String
    ): ApiResultV2<Unit> {

        // 1️⃣ URI → ByteArray 읽기 (fileSize 확정 및 재읽기 방지)
        val bytes = repository.readFileBytes(uri).getOrElse {
            return ApiResultV2.UnknownError(
                message = "파일을 읽을 수 없습니다.",
                throwable = it
            )
        }

        // 2️⃣ presigned URL 발급 (실제 바이트 크기로 서명 요청)
        val presignedResult = repository.issuePresignedUrl(
            fileName = fileName,
            fileSize = bytes.size.toLong()
        )

        val presigned = when (presignedResult) {
            is ApiResultV2.Success -> presignedResult.data

            // 🔐 인증 만료 / 서버 에러 / 네트워크 에러는 그대로 위로 전달
            else -> return presignedResult as ApiResultV2<Unit>
        }

        // 3️⃣ S3 PUT 업로드 (읽어둔 bytes 재사용 — ContentProvider 재호출 없음)
        val uploadResult = repository.uploadFileToS3(
            presignedUrl = presigned.presignedUrl,
            bytes = bytes,
            mimeType = mimeType
        )

        if (uploadResult.isFailure) {
            // 👉 S3 업로드 실패는 서버 에러가 아니므로 UnknownError로 래핑
            return ApiResultV2.UnknownError(
                message = "파일 업로드에 실패했습니다.",
                throwable = uploadResult.exceptionOrNull()
            )
        }

        // 4️⃣ 서버에 문서 등록
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