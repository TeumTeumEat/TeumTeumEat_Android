package com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_3_daily_summary_detail

import com.teumteumeat.teumteumeat.domain.model.common.GoalType
import java.time.LocalDate


data class UiStateDailySummary(
    val isLoading: Boolean = false,

    // 📌 화면 표시용
    val title: String = "",
    val dateText: String = "",
    val summary: String = "",

    // 📌 요청 파라미터 (상태로 보존)
    val id: Long? = null,
    val type: GoalType? = null,
    val date: LocalDate? = null,

    // 📌 에러
    val errorMessage: String? = null,
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

