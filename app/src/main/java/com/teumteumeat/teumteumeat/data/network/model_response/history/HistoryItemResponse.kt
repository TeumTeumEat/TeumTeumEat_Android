package com.teumteumeat.teumteumeat.data.network.model_response.history

data class CategoryHistoryResponse(
    val categoryName: String,
    val histories: List<HistoryItemResponse>
)

data class HistoryItemResponse(
    val id: Long,
    val type: DailyHistoryTypeResponse,
    val title: String,
    val summarySnippet: String,
    val lastStudiedAt: String,
)