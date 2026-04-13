package com.teumteumeat.teumteumeat.domain.model.goal

import java.time.LocalDate

data class UserGoal(
    val goalId: Long,
    val type: DomainGoalType,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val isExpired: Boolean,
    val isCompleted: Boolean,
    val studyPeriod: String,
    val difficulty: Difficulty,
    val fileName: String?,
    val documentId: Long?,
    val prompt: String?,
    val category: GoalCategory?
)


