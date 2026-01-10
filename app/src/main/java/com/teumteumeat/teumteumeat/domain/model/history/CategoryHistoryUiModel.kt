package com.teumteumeat.teumteumeat.domain.model.history

import com.teumteumeat.teumteumeat.domain.model.common.GoalType

data class CategoryHistoryUiModel(
    val categoryName: String,
    val histories: List<LearningHistoryUiModel>
)

data class LearningHistoryUiModel(
    val id: Long,
    val title: String,
    val description: String,
    val dateText: String,
    val goalType: GoalType
)