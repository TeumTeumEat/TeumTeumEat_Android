package com.teumteumeat.teumteumeat.domain.mapper.goal

import com.teumteumeat.teumteumeat.data.network.model_response.goal.UserGoalResponse
import com.teumteumeat.teumteumeat.domain.model.goal.*
import java.time.LocalDate

fun UserGoalResponse.toDomain(): UserGoal {
    return UserGoal(
        goalId = goalId,
        type = type.toDomain(), // ✅ enum 전용 매퍼 사용,
        startDate = LocalDate.parse(startDate),
        endDate = LocalDate.parse(endDate),
        isExpired = isExpired,
        studyPeriod = studyPeriod,
        difficulty = Difficulty.valueOf(difficulty),
        prompt = prompt,
        category = category?.let {
            GoalCategory(
                categoryId = it.categoryId,
                name = it.name,
                path = it.path
            )
        }
    )
}


fun com.teumteumeat.teumteumeat.domain.model.common.GoalType.toDomain(): DomainGoalType {
    return when (this) {
        com.teumteumeat.teumteumeat.domain.model.common.GoalType.CATEGORY -> DomainGoalType.CATEGORY
        com.teumteumeat.teumteumeat.domain.model.common.GoalType.DOCUMENT -> DomainGoalType.DOCUMENT
    }
}