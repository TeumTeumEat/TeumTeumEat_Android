package com.teumteumeat.teumteumeat.data.network.model_response.goal

import com.teumteumeat.teumteumeat.domain.model.common.GoalType

data class UserGoalResponse(
    val goalId: Long,
    val type: GoalType,
    val startDate: String,
    val endDate: String,
    val isExpired: Boolean,
    val studyPeriod: String,
    val difficulty: String,
    val prompt: String,
    val category: CategoryResponse?
)

data class CategoryResponse(
    val categoryId: Long,
    val name: String,
    val path: String
)