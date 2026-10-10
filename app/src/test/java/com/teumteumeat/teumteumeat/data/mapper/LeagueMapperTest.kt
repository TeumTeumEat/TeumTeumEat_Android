package com.teumteumeat.teumteumeat.data.mapper

import com.google.gson.Gson
import com.teumteumeat.teumteumeat.data.network.model.ApiResponse
import com.teumteumeat.teumteumeat.data.network.model_response.league.LeagueLatestResultResponse
import com.teumteumeat.teumteumeat.data.network.model_response.league.LeagueMeDto
import com.teumteumeat.teumteumeat.data.network.model_response.league.LeagueRankingResponse
import com.google.gson.reflect.TypeToken
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/** GET /api/v1/league 응답 예시 JSON 이 도메인 모델로 맞게 변환되는지 검증한다. */
class LeagueMapperTest {

    private val type = object : TypeToken<ApiResponse<LeagueRankingResponse, Any?>>() {}.type

    private fun parse(json: String): LeagueRankingResponse =
        Gson().fromJson<ApiResponse<LeagueRankingResponse, Any?>>(json, type).data

    @Test
    fun `서버 응답 예시가 도메인 모델로 변환된다(앱에서 안 쓰는 resetAt 필드는 무시된다)`() {
        val response = parse(
            """
            {"code":"OK","message":"요청이 성공적으로 처리되었습니다.","data":{
              "weekStartDate":"2026-10-05","resetAt":"2026-10-12T00:00:00","remainingSeconds":295953,
              "rankers":[{"rank":1,"name":"크","weeklySnackCount":2,"isMe":false}],
              "me":{"name":"병*","weeklySnackCount":0,"todaySnackCount":0}}}
            """.trimIndent()
        )

        val ranking = response.toDomain()

        assertEquals(LocalDate.of(2026, 10, 5), ranking.weekStart)
        assertEquals(295_953L, ranking.remainingSeconds)
        assertEquals(1, ranking.rankers.size)
        with(ranking.rankers.first()) {
            assertEquals(1, rank)
            assertEquals("크", nickname)
            assertEquals(2, snackCount)
            assertFalse(isMe)
        }
    }

    @Test
    fun `이번 주 스낵이 0개라 me 에 rank 키가 없으면 rank 는 null 이다`() {
        val me = parse(
            """{"data":{"weekStartDate":"2026-10-05","resetAt":"2026-10-12T00:00:00","remainingSeconds":1,
               "rankers":[],"me":{"name":"병*","weeklySnackCount":0,"todaySnackCount":0}}}"""
        ).toDomain().me

        assertNull(me!!.rank)
        assertEquals("병*", me.nickname)
        assertEquals(0, me.weeklySnackCount)
        assertEquals(0, me.todaySnackCount)
    }

    @Test
    fun `me 에 rank 가 있으면 그대로 매핑되고 isMe 랭커도 유지된다`() {
        val ranking = parse(
            """{"data":{"weekStartDate":"2026-10-05","resetAt":"2026-10-12T00:00:00","remainingSeconds":1,
               "rankers":[{"rank":2,"name":"병*","weeklySnackCount":5,"isMe":true}],
               "me":{"name":"병*","weeklySnackCount":5,"todaySnackCount":2,"rank":2}}}"""
        ).toDomain()

        assertEquals(2, ranking.me!!.rank)
        assertTrue(ranking.rankers.single().isMe)
    }

    @Test
    fun `rankers 가 누락되면 빈 목록이 되고 음수 남은 시간은 0 으로 보정된다`() {
        val ranking = parse(
            """{"data":{"weekStartDate":"2026-10-05","resetAt":"2026-10-12T00:00:00","remainingSeconds":-5}}"""
        ).toDomain()

        assertTrue(ranking.rankers.isEmpty())
        assertNull(ranking.me)
        assertEquals(0L, ranking.remainingSeconds)
    }

    // ---- GET /api/v1/league/results/latest ----

    private fun parseLatest(json: String): LeagueLatestResultResponse =
        Gson().fromJson<ApiResponse<LeagueLatestResultResponse, Any?>>(
            json, object : TypeToken<ApiResponse<LeagueLatestResultResponse, Any?>>() {}.type,
        ).data

    @Test
    fun `지난주 결과 예시가 도메인 모델로 변환된다`() {
        val result = parseLatest(
            """{"code":"OK","message":"ok","data":{"weekStartDate":"2026-09-28","rank":1,"weeklySnackCount":42}}"""
        ).toDomain()

        assertEquals(LocalDate.of(2026, 9, 28), result!!.weekStart)
        assertEquals(1, result.rank)
        assertEquals(42, result.snackCount)
    }

    @Test
    fun `직전 주 스낵이 0개라 rank 가 null 이면 보여줄 결과가 없다`() {
        val explicitNull = parseLatest("""{"data":{"weekStartDate":"2026-09-28","rank":null,"weeklySnackCount":0}}""").toDomain()
        val missingKey = parseLatest("""{"data":{"weekStartDate":"2026-09-28","weeklySnackCount":0}}""").toDomain()

        assertNull(explicitNull)
        assertNull(missingKey)
    }

    // ---- GET /api/v1/league/me ----

    private fun parseMe(json: String): LeagueMeDto =
        Gson().fromJson<ApiResponse<LeagueMeDto, Any?>>(
            json, object : TypeToken<ApiResponse<LeagueMeDto, Any?>>() {}.type,
        ).data

    @Test
    fun `내 순위 조회 예시가 도메인 모델로 변환된다`() {
        val me = parseMe(
            """{"code":"OK","message":"ok","data":{"rank":15,"name":"김*민","weeklySnackCount":12,"todaySnackCount":3}}"""
        ).toDomain()

        assertEquals(15, me.rank)
        assertEquals("김*민", me.nickname)
        assertEquals(12, me.weeklySnackCount)
        assertEquals(3, me.todaySnackCount)
    }

    @Test
    fun `내 순위 조회에서 이번 주 스낵이 0개라 rank 가 null 이거나 키가 없으면 rank 는 null 이다`() {
        val explicitNull = parseMe("""{"data":{"rank":null,"name":"김*민","weeklySnackCount":0,"todaySnackCount":0}}""").toDomain()
        val missingKey = parseMe("""{"data":{"name":"김*민","weeklySnackCount":0,"todaySnackCount":0}}""").toDomain()

        assertNull(explicitNull.rank)
        assertNull(missingKey.rank)
    }
}
