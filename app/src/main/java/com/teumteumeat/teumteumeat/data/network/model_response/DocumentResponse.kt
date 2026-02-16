package com.teumteumeat.teumteumeat.data.network.model_response

data class GetDocumentsResponse(
    val documents: List<DocumentResponse>
)

data class DocumentResponse(
    val documentId: Int,
    val fileName: String,
    val fileKey: String,
    val status: DocumentStatus,
    val estimateTime: Long?,
)

enum class DocumentStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
}