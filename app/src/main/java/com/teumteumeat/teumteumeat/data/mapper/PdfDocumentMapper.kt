package com.teumteumeat.teumteumeat.data.mapper

import com.teumteumeat.teumteumeat.data.document.response.DocumentSummaryResponse
import com.teumteumeat.teumteumeat.domain.model.document.DocumentId
import com.teumteumeat.teumteumeat.domain.model.document.PdfDocumentSummary
import com.teumteumeat.teumteumeat.domain.model.document.DocumentSummaryId

fun DocumentSummaryResponse.toDocumentId() = DocumentId(documentId)

fun DocumentSummaryResponse.toDocumentSummaryId() = DocumentSummaryId(documentSummaryId)

fun DocumentSummaryResponse.toPdfDocumentSummary(): PdfDocumentSummary {
    return PdfDocumentSummary(
        fileName = fileName,
        updatedAt = updatedAt,
        summary = summary,
        status = status,
    )
}

