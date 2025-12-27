package com.teumteumeat.teumteumeat.data.network.model_request

import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.enum_type.Difficulty
import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.enum_type.GoalType

data class CreateGoalRequest(
    val type: GoalType,
    val endDate: String,           // yyyy-MM-dd
    val difficulty: Difficulty,
    val prompt: String?,           // null 허용
    val categoryId: Int?           // DOCUMENT면 null
)
