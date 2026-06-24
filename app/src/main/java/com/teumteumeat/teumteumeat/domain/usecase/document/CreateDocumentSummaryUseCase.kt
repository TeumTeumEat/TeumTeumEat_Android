package com.teumteumeat.teumteumeat.domain.usecase.document

import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.domain.repository.pff_document.PdfDocumentRepository
import javax.inject.Inject

class CreateDocumentSummaryUseCase @Inject constructor(
    private val repository: PdfDocumentRepository
) {
    suspend operator fun invoke(goalId: Int, documentId: Int): ApiResultV2<Unit> =
        when (val result = repository.createDocumentSummary(goalId, documentId)) {
            is ApiResultV2.Success -> ApiResultV2.Success(message = result.message, data = Unit)
            is ApiResultV2.SessionExpired -> result
            is ApiResultV2.ServerError -> result
            is ApiResultV2.NetworkError -> result
            is ApiResultV2.UnknownError -> result
        }
}