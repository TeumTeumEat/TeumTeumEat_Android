package com.teumteumeat.teumteumeat.domain.usecase.document

import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model_response.DocumentResponse
import com.teumteumeat.teumteumeat.domain.repository.pff_document.PdfDocumentRepository
import javax.inject.Inject

class GetDocumentsUseCase @Inject constructor(
    private val pdfDocumentRepository: PdfDocumentRepository
) {

    suspend operator fun invoke(
        goalId: Int
    ): ApiResultV2<List<DocumentResponse>> {
        return pdfDocumentRepository.getDocuments(goalId)
    }
}
