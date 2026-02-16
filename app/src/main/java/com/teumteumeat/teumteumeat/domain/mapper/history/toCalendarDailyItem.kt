package com.teumteumeat.teumteumeat.domain.mapper.history

import com.teumteumeat.teumteumeat.data.network.model_response.history.DailyHistoryTypeResponse
import com.teumteumeat.teumteumeat.data.network.model_response.history.HistoryItemResponse
import com.teumteumeat.teumteumeat.domain.model.common.GoalType
import com.teumteumeat.teumteumeat.domain.model.history.CalendarDailyItem
import java.time.LocalDateTime

fun HistoryItemResponse.toCalendarDailyItem(): CalendarDailyItem {
    return CalendarDailyItem(
        id = id,
        type = type.toDomainGoalType(),
        title = title,
        summarySnippet = summarySnippet,
        lastStudiedAt = LocalDateTime.parse(lastStudiedAt)
    )
}

/**
 * Network enum → Domain GoalType
 * ❗ NONE은 절대 반환하지 않음
 */
private fun DailyHistoryTypeResponse.toDomainGoalType(): GoalType {
    return when (this) {
        DailyHistoryTypeResponse.CATEGORY -> GoalType.CATEGORY
        DailyHistoryTypeResponse.DOCUMENT -> GoalType.DOCUMENT
    }
}


