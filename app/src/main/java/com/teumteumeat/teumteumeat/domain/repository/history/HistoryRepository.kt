package com.teumteumeat.teumteumeat.domain.repository.history

import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model_response.CalendarHistoryResponse
import com.teumteumeat.teumteumeat.domain.model.goal.DomainGoalType
import com.teumteumeat.teumteumeat.domain.model.history.CalendarDailyItem
import com.teumteumeat.teumteumeat.domain.model.history.CategoryHistoryUiModel
import com.teumteumeat.teumteumeat.domain.model.history.DailySummary

interface HistoryRepository {

    suspend fun getCategoryHistories(): ApiResultV2<List<CategoryHistoryUiModel>>

    suspend fun getCalendarDailyHistory(
        date: String
    ): ApiResultV2<List<CalendarDailyItem>>

    suspend fun getCalendarHistory(
        year: Int,
        month: Int
    ): ApiResultV2<CalendarHistoryResponse>

    suspend fun getLearningHistorySummary(
        type: DomainGoalType,
        id: Long,
        date: String
    ): ApiResultV2<DailySummary>
}