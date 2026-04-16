package com.teumteumeat.teumteumeat.domain.usecase.document

import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model_response.PresignedResponse
import com.teumteumeat.teumteumeat.domain.repository.pff_document.PdfDocumentRepository
import javax.inject.Inject

class IssuePresignedUrlUseCase @Inject constructor(
    private val pdfDocumentRepository: PdfDocumentRepository
) {

    suspend operator fun invoke(
        fileName: String
    ): ApiResultV2<PresignedResponse> {

        // 🔹 서버에 presigned URL 발급 요청
        return pdfDocumentRepository.issuePresignedUrl(
            fileName = fileName
        )
    }
}