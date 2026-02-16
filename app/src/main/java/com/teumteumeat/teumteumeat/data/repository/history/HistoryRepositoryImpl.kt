package com.teumteumeat.teumteumeat.data.repository.history

import com.teumteumeat.teumteumeat.data.api.HistoryApiService
import com.teumteumeat.teumteumeat.data.api.auth.AuthApiService
import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model.TokenLocalDataSource
import com.teumteumeat.teumteumeat.data.network.model_response.CalendarHistoryResponse
import com.teumteumeat.teumteumeat.data.network.model_response.toDomain
import com.teumteumeat.teumteumeat.data.repository.BaseRepository
import com.teumteumeat.teumteumeat.domain.mapper.history.toCalendarDailyItem
import com.teumteumeat.teumteumeat.domain.model.common.GoalType
import com.teumteumeat.teumteumeat.domain.model.history.CalendarDailyItem
import com.teumteumeat.teumteumeat.domain.model.history.CategoryHistoryUiModel
import com.teumteumeat.teumteumeat.domain.model.history.DailySummary
import com.teumteumeat.teumteumeat.domain.model.history.LearningHistoryUiModel
import com.teumteumeat.teumteumeat.utils.Utils.RepositoryUtils.requireNotNullOrError
import com.teumteumeat.teumteumeat.utils.Utils.TypeUtils.toMMdd
import javax.inject.Inject

class HistoryRepositoryImpl @Inject constructor(
    private val historyApiService: HistoryApiService,
    authApiService: AuthApiService,
    tokenLocalDataSource: TokenLocalDataSource
) : BaseRepository(authApiService, tokenLocalDataSource), HistoryRepository {

    override suspend fun getCategoryHistories(): ApiResultV2<List<CategoryHistoryUiModel>> {
        return safeApiVer2(
            apiCall = { historyApiService.getCategoryHistories() },
            mapper = { response ->
                response.requireNotNullOrError("/api/library/category-histories")
                    .map { category ->
                        CategoryHistoryUiModel(
                            categoryName = category.categoryName,
                            histories = category.histories.map { history ->
                                LearningHistoryUiModel(
                                    id = history.id,
                                    title = history.title,
                                    description = history.summarySnippet,
                                    dateText = history.lastStudiedAt.toMMdd(),
                                    goalType = GoalType.valueOf(history.type.toString())
                                )
                            }
                        )
                    }
            }
        )
    }

    override suspend fun getDailySummary(
        type: GoalType,
        id: Long,
        date: String
    ): ApiResultV2<DailySummary> {

        val requestUrl =
            "/api/v1/history/details/summary/${type.name}/$id?date=$date"

        return safeApiVer2(
            apiCall = {
                historyApiService.getHistorySummary(
                    type = type.name,
                    id = id,
                    date = date
                )
            },
            mapper = { data ->
                data
                    .requireNotNullOrError(requestUrl)
                    .toDomain()
            }
        )
    }

    override suspend fun getCalendarDailyHistory(
        date: String
    ): ApiResultV2<List<CalendarDailyItem>> {

        val requestUrl = "/api/v1/history/date/$date"

        return safeApiVer2(
            apiCall = {
                historyApiService.getHistoryByDate(date)
            },
            mapper = { data ->
                data
                    .requireNotNullOrError(requestUrl)      // null 방어
                    .map { it.toCalendarDailyItem() }
            }
        )
    }

    override suspend fun getCalendarHistory(
        year: Int,
        month: Int
    ): ApiResultV2<CalendarHistoryResponse> =
        safeApiVer2(
            apiCall = {
                historyApiService.getCalendarHistory(
                    year = year,
                    month = month
                )
            },
            mapper = { data ->
                data.requireNotNullOrError("api/v1/history/calendar")
            }
        )
}
