package com.teumteumeat.teumteumeat.data.network.model_response

import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.enum_type.Difficulty
import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.enum_type.GoalType

data class GoalsData(
    val goalResponses: List<GetGoalResponse>
)

// 2️⃣ Goal
data class GetGoalResponse(
    val goalId: Int,
    val type: GoalType,
    val startDate: String,
    val endDate: String,
    val studyPeriod: String,
    val difficulty: Difficulty,
    val prompt: String?,
    val category: CategoryResponse?
)

data class CategoryResponse(
    val categoryId: Int,
    val name: String,
    val path: String,
    val description: String?
)