package com.teumteumeat.teumteumeat.domain.mapper

import com.teumteumeat.teumteumeat.data.network.model_response.quiz.UserQuizStatusResponse
import com.teumteumeat.teumteumeat.domain.quiz.UserQuizStatus
import kotlin.Int

fun UserQuizStatusResponse.toDomain(): UserQuizStatus =
    UserQuizStatus(
        hasSolvedToday = hasSolvedToday,
        isFirstTime = isFirstTime,
        hasCreatedToday = hasCreatedToday,
        isQuizGuideSeen = isQuizGuideSeen,
        availableQuizCount = availableQuizCount,
        targetQuizSetCount = targetQuizSetCount,
        completedQuizSetCount = completedQuizSetCount,
        isCompleted = isCompleted,
    )
