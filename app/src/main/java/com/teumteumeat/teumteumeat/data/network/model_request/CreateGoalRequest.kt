package com.teumteumeat.teumteumeat.data.network.model_request

import com.teumteumeat.teumteumeat.domain.model.common.GoalTypeUiState
import com.teumteumeat.teumteumeat.domain.model.goal.Difficulty

data class CreateGoalRequest(
    val type: GoalTypeUiState,
    val studyPeriod: String,
    // val endDate: String,           // yyyy-MM-dd
    val difficulty: Difficulty,
    val prompt: String?,           // null 허용
    val categoryId: Int?           // DOCUMENT면 null
)
