package com.teumteumeat.teumteumeat.domain.model.document

data class PdfDocumentSummary(
    val fileName: String,
    val updatedAt: String,
    val summary: String,
    val status: String,
)