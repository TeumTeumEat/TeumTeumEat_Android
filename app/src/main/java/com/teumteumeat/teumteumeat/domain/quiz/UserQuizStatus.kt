package com.teumteumeat.teumteumeat.domain.quiz

data class UserQuizStatus(
    val hasSolvedToday: Boolean,
    val isFirstTime: Boolean,
    val hasCreatedToday: Boolean,
    val isQuizGuideSeen: Boolean,
    val availableQuizCount: Int,
    val targetQuizSetCount: Int,
    val completedQuizSetCount: Int,
    val isCompleted: Boolean,
)