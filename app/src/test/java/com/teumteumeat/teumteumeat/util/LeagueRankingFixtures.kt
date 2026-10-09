package com.teumteumeat.teumteumeat.util

import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.domain.model.league.LeagueMe
import com.teumteumeat.teumteumeat.domain.model.league.LeagueRanker
import com.teumteumeat.teumteumeat.domain.model.league.LeagueRanking
import com.teumteumeat.teumteumeat.domain.usecase.league.GetLeagueRankingUseCase
import io.mockk.coEvery
import io.mockk.mockk
import java.time.Clock
import java.time.Duration
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters

/** 샘플 랭킹 — 기본값은 2위로 등록된 내 순위(rank=2)와 Top3 랭커. */
fun sampleLeagueRanking(
    weekStart: LocalDate = LocalDate.of(2026, 10, 5),
    remainingSeconds: Long = 295_953L,
    rankers: List<LeagueRanker> = listOf(
        LeagueRanker(rank = 1, nickname = "크", snackCount = 12, isMe = false),
        LeagueRanker(rank = 2, nickname = "병*", snackCount = 10, isMe = true),
        LeagueRanker(rank = 3, nickname = "이*", snackCount = 9, isMe = false),
    ),
    me: LeagueMe? = LeagueMe(nickname = "병*", weeklySnackCount = 10, todaySnackCount = 1, rank = 2),
) = LeagueRanking(
    weekStart = weekStart,
    remainingSeconds = remainingSeconds,
    rankers = rankers,
    me = me,
)

/** 항상 [ranking] 을 성공으로 돌려주는 [GetLeagueRankingUseCase] 스텁. */
fun rankingUseCaseReturning(ranking: LeagueRanking = sampleLeagueRanking()): GetLeagueRankingUseCase =
    mockk<GetLeagueRankingUseCase>().also { coEvery { it() } returns ApiResultV2.Success(message = null, data = ranking) }

/**
 * [clock] 의 '현재 시각' 기준으로 이번 주 랭킹과 리셋까지 남은 초를 계산해 돌려주는 스텁 — 서버처럼 시간에 따라 응답이 바뀐다.
 * (월요일 00:00 을 지나면 새 주차 + 7일이 남은 응답이 내려온다)
 */
fun clockAwareRankingUseCase(clock: Clock): GetLeagueRankingUseCase =
    mockk<GetLeagueRankingUseCase>().also { useCase ->
        coEvery { useCase() } answers {
            val now = LocalDateTime.now(clock)
            ApiResultV2.Success(
                message = null,
                data = sampleLeagueRanking(
                    weekStart = now.toLocalDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
                    remainingSeconds = secondsUntilNextMonday(now),
                ),
            )
        }
    }

/** [now] 기준 다음 월요일 00:00 까지 남은 초 — 테스트 서버 스텁이 서버처럼 응답을 만들 때 쓴다. */
private fun secondsUntilNextMonday(now: LocalDateTime): Long =
    Duration.between(now, now.toLocalDate().with(TemporalAdjusters.next(DayOfWeek.MONDAY)).atStartOfDay()).seconds
