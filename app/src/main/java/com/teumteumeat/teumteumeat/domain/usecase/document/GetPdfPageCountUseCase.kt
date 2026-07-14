package com.teumteumeat.teumteumeat.domain.usecase.document

import android.net.Uri
import com.teumteumeat.teumteumeat.domain.repository.pff_document.PdfDocumentRepository
import javax.inject.Inject

class GetPdfPageCountUseCase @Inject constructor(
    private val repository: PdfDocumentRepository
) {
    suspend operator fun invoke(uri: Uri): Result<Int> = repository.getPdfPageCount(uri)
}