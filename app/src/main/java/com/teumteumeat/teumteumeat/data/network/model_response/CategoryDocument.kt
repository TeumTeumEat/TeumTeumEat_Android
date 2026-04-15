package com.teumteumeat.teumteumeat.data.network.model_response

data class CategoryDocument(
    val documentId: Long,
    val title: String,
    val content: String,
    val hasSolvedToday: Boolean,
    val isFirstTime: Boolean,
    val createdAt: String
)