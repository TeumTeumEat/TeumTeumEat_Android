package com.teumteumeat.teumteumeat.domain.usecase.document

import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model_response.PresignedResponse
import com.teumteumeat.teumteumeat.data.repository.document.DocumentRepository
import javax.inject.Inject

class IssuePresignedUrlUseCase @Inject constructor(
    private val documentRepository: DocumentRepository
) {

    suspend operator fun invoke(
        fileName: String
    ): ApiResultV2<PresignedResponse> {

        // 🔹 서버에 presigned URL 발급 요청
        return documentRepository.issuePresignedUrl(
            fileName = fileName
        )
    }
}