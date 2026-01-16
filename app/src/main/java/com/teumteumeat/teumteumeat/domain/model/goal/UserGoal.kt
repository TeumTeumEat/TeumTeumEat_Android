package com.teumteumeat.teumteumeat.domain.model.goal

import java.time.LocalDate

data class UserGoal(
    val goalId: Long,
    val type: DomainGoalType,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val isExpired: Boolean,
    val studyPeriod: String,
    val difficulty: Difficulty,
    val fileName: String?,
    val documentId: Long?,
    val prompt: String?,
    val category: GoalCategory?
)

data class GoalCategory(
    val categoryId: Long,
    val name: String,
    val path: String
)

enum class DomainGoalType {
    CATEGORY, DOCUMENT
}

enum class Difficulty {
    EASY, MEDIUM, HARD, NONE
}

fun mapDifficultyToKorean(difficulty: Difficulty): String {
    return when (difficulty) {
        Difficulty.EASY -> "하"
        Difficulty.MEDIUM -> "중"
        Difficulty.HARD -> "상"
        Difficulty.NONE -> "선택안함"
    }
}