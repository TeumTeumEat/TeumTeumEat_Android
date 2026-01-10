package com.teumteumeat.teumteumeat.data.network.model_response

data class DailyCategoryDocumentDto(
    val documentId: Long,
    val title: String,
    val content: String,
    val hasSolvedToday: Boolean,
    val isFirstTime: Boolean,
    val createdAt: String
)

fun DailyCategoryDocumentDto.toDomain(): DailyCategoryDocument {
    return DailyCategoryDocument(
        title = this.title,
        documentId = this.documentId,
        content = this.content,
        hasSolvedToday = this.hasSolvedToday,
        isFirstTime = this.isFirstTime,
        createdAt = this.createdAt
    )
}