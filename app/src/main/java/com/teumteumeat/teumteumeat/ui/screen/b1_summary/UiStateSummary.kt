package com.teumteumeat.teumteumeat.ui.screen.b1_summary


data class UiStateSummary(
    val isLoading: Boolean = false,
    val title: String = "휴리스틱 평가",
    val dateText: String = "1월 3일",
    val summary: String = "",
    val hasSolvedToday: Boolean = true,
    val errorMessage: String? = null,
    val isFirstTime: Boolean = false,
    val categoryDocumentId: Int? = null,
    val categoryId: Int? = null,
)

data class DocumentSummaryResponse(
    val documentId: Int,
    val fileName: String,
    val fileKey: String,
    val summary: String,
    val status: String,
    val hasSolvedToday: Boolean,
    val isFirstTime: Boolean
)

