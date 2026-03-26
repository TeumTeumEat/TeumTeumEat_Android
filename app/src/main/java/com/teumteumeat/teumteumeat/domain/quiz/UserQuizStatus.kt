package com.teumteumeat.teumteumeat.domain.quiz

data class UserQuizStatus(
    val hasSolvedToday: Boolean,
    val isFirstTime: Boolean,
    val hasCreatedToday: Boolean,
    val isQuizGuideSeen: Boolean,
    val availableQuizCount: Int,
    val dailyAdRewardCount: Int,
    val canIssueCoupon: Boolean,
    val targetQuizSetCount: Int,
    val completedQuizSetCount: Int,
    val isCompleted: Boolean,
)