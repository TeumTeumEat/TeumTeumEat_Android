package com.teumteumeat.teumteumeat.data.network.model_response.quiz

data class UserQuizStatusResponse(
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
