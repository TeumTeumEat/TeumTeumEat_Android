package com.teumteumeat.teumteumeat.data.document.response

data class DocumentSummaryResponse(
    val documentId: Int,
    val documentSummaryId: Int,
    val fileName: String,
    val fileKey: String,
    val updatedAt: String,
    val summary: String,
    val status: String,
    val hasSolvedToday: Boolean,
    val isFirstTime: Boolean
)