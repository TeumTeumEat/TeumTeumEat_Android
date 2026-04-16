package com.teumteumeat.teumteumeat.domain.mapper.history

import com.teumteumeat.teumteumeat.data.network.model_response.history.DailyHistoryTypeResponse
import com.teumteumeat.teumteumeat.data.network.model_response.history.HistoryItemResponse
import com.teumteumeat.teumteumeat.domain.model.common.DomainGoalType_v1
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
private fun DailyHistoryTypeResponse.toDomainGoalType(): DomainGoalType_v1 {
    return when (this) {
        DailyHistoryTypeResponse.CATEGORY -> DomainGoalType_v1.CATEGORY
        DailyHistoryTypeResponse.DOCUMENT -> DomainGoalType_v1.DOCUMENT
    }
}


