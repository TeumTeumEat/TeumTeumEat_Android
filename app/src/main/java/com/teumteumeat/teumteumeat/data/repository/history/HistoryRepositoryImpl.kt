package com.teumteumeat.teumteumeat.data.repository.history

import com.teumteumeat.teumteumeat.data.api.HistoryApiService
import com.teumteumeat.teumteumeat.data.api.auth.AuthApiService
import com.teumteumeat.teumteumeat.data.mapper.toDomain
import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model.TokenLocalDataSource
import com.teumteumeat.teumteumeat.data.network.model_response.CalendarHistoryResponse
import com.teumteumeat.teumteumeat.data.repository.BaseRepository
import com.teumteumeat.teumteumeat.domain.mapper.history.toCalendarDailyItem
import com.teumteumeat.teumteumeat.domain.model.common.DomainGoalType_v1
import com.teumteumeat.teumteumeat.domain.model.goal.DomainGoalType
import com.teumteumeat.teumteumeat.domain.model.history.CalendarDailyItem
import com.teumteumeat.teumteumeat.domain.model.history.CategoryHistoryUiModel
import com.teumteumeat.teumteumeat.domain.model.history.DailySummary
import com.teumteumeat.teumteumeat.domain.model.history.LearningHistoryUiModel
import com.teumteumeat.teumteumeat.domain.repository.history.HistoryRepository
import com.teumteumeat.teumteumeat.utils.Utils.RepositoryUtils.requireNotNullOrError
import com.teumteumeat.teumteumeat.utils.Utils.TypeUtils.toMMdd
import java.time.LocalDateTime
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
                response.requireNotNullOrError("/api/v1/history/topics")
                    .map { category ->
                        CategoryHistoryUiModel(
                            categoryName = category.categoryName,
                            histories = category.histories.map { history ->
                                LearningHistoryUiModel(
                                    id = history.id,
                                    title = history.title,
                                    description = history.summarySnippet,
                                    date = LocalDateTime.parse(history.lastStudiedAt),
                                    dateText = history.lastStudiedAt.toMMdd(),
                                    domainGoalTypeV1 = DomainGoalType_v1.valueOf(history.type.toString())
                                )
                            }
                        )
                    }
            }
        )
    }

    /**
     * 특정 학습 기록의 요약글(카테고리 자료 또는 PDF 자료)를 조회합니다.
     * 이미 퀴즈를 풀었던 이력이 있는 요약글을 대상으로 합니다.
     *
     * @param type 학습 목표 타입 (예: 독해, 단어 등 - GoalType.name(String 형태) 으로 전달)
     * @param id 학습 기록의 고유 식별자
     * @param date 조회하고자 하는 날짜 (YYYY-MM-DD 형식)
     * @return [DailySummary] 도메인 모델을 담은 [ApiResultV2]
     */
    override suspend fun getLearningHistorySummary(
        type: DomainGoalType,
        id: Long,
        date: String
    ): ApiResultV2<DailySummary> {

        val requestUrl =
            "/api/v1/history/details/summary/${type.name}/$id?date=$date"

        return safeApiVer2(
            apiCall = {
                historyApiService.getHistorySummary(
                    type = type.name, // Enum을 String으로 변환하여 전달
                    id = id,
                    date = date
                )
            },
            mapper = { data ->
                data
                    .requireNotNullOrError(requestUrl) // 응답 데이터 null 체크 (BaseRepository 유틸 사용)
                    .toDomain() // DTO(HistorySummaryResponse)를 Domain Entity(DailySummary)로 변환
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
