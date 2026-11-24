package com.teumteumeat.teumteumeat.data.api.notice

import com.teumteumeat.teumteumeat.util.NoticeJsonFixtures
import com.teumteumeat.teumteumeat.util.NoticeJsonFixtures.jsonResponse
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * `GET /api/v1/notices` 계약(Contract) 테스트.
 *
 * 실제 Retrofit + Gson을 MockWebServer에 연결해, 모킹으로는 잡을 수 없는 두 가지를 검증한다.
 *  1. 요청: HTTP 메서드·경로·쿼리 파라미터(`lastNoticeId` 생략 규칙 포함)가 명세와 같은가
 *  2. 응답: 명세서의 JSON을 DTO로 올바르게 파싱하는가 (필드명 오타, null 허용 여부)
 */
class NoticeApiServiceContractTest {

    private lateinit var server: MockWebServer
    private lateinit var api: NoticeApiService

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NoticeApiService::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    // ── 요청 계약 ────────────────────────────────────────────────────────────

    @Test
    fun `첫 요청은 GET 방식으로 lastNoticeId 없이 size만 보낸다`() = runTest {
        server.enqueue(jsonResponse(NoticeJsonFixtures.page(false)))

        api.getNotices(lastNoticeId = null, size = 20)

        val request = server.takeRequest(5, TimeUnit.SECONDS)!!
        assertEquals("GET", request.method)
        assertEquals("/api/v1/notices", request.requestUrl!!.encodedPath)
        assertEquals(setOf("size"), request.requestUrl!!.queryParameterNames)
        assertEquals("20", request.requestUrl!!.queryParameter("size"))
        assertNull(request.requestUrl!!.queryParameter("lastNoticeId"))
    }

    @Test
    fun `다음 요청은 lastNoticeId와 size를 함께 보낸다`() = runTest {
        server.enqueue(jsonResponse(NoticeJsonFixtures.page(false)))

        api.getNotices(lastNoticeId = 42L, size = 20)

        val request = server.takeRequest(5, TimeUnit.SECONDS)!!
        assertEquals(setOf("lastNoticeId", "size"), request.requestUrl!!.queryParameterNames)
        assertEquals("42", request.requestUrl!!.queryParameter("lastNoticeId"))
        assertEquals("20", request.requestUrl!!.queryParameter("size"))
    }

    @Test
    fun `Long 범위의 큰 공지 ID도 손실 없이 전달된다`() = runTest {
        server.enqueue(jsonResponse(NoticeJsonFixtures.page(false)))

        api.getNotices(lastNoticeId = 9_007_199_254_740_993L, size = 50)

        val request = server.takeRequest(5, TimeUnit.SECONDS)!!
        assertEquals("9007199254740993", request.requestUrl!!.queryParameter("lastNoticeId"))
    }

    // ── 응답 계약 ────────────────────────────────────────────────────────────

    @Test
    fun `명세서 응답 예시가 DTO로 정확히 파싱된다`() = runTest {
        server.enqueue(jsonResponse(NoticeJsonFixtures.SPEC_SAMPLE))

        val response = api.getNotices(lastNoticeId = null, size = 20)

        assertEquals("OK", response.code)
        assertEquals("요청이 성공적으로 처리되었습니다.", response.message)
        val data = response.data
        assertNotNull(data)
        assertTrue(data!!.hasNext)
        val notice = data.notices.single()
        assertEquals(1L, notice.noticeId)
        assertEquals("서비스 신규 오픈 안내", notice.title)
        assertEquals("2026-10-01T12:00:00", notice.createdDate)
        val detail = notice.detail!!
        assertEquals(
            "https://teumteumeat.s3.ap-northeast-2.amazonaws.com/prod/notice/6cd45ae3-87a5_full.png",
            detail.fullScreenImageUrl
        )
        assertEquals("https://example.com/event", detail.externalLink)
        assertEquals("teumteumeat://quiz", detail.deepLink)
        assertEquals("이벤트 참여하기", detail.actionButtonText)
    }

    @Test
    fun `detail의 링크와 버튼 문구가 null이어도 파싱된다`() = runTest {
        server.enqueue(
            jsonResponse(
                """
                {"code":"OK","message":"ok","data":{"notices":[
                  {"noticeId":1,"title":"t","createdDate":"2026-10-01T12:00:00",
                   "detail":{"fullScreenImageUrl":"https://cdn.example.com/a.png",
                             "externalLink":null,"deepLink":null,"actionButtonText":null}}
                ],"hasNext":false}}
                """.trimIndent()
            )
        )

        val detail = api.getNotices(null, 20).data!!.notices.single().detail!!

        assertEquals("https://cdn.example.com/a.png", detail.fullScreenImageUrl)
        assertNull(detail.externalLink)
        assertNull(detail.deepLink)
        assertNull(detail.actionButtonText)
    }

    @Test
    fun `detail 키가 아예 없어도 파싱되고 detail은 null이다`() = runTest {
        server.enqueue(jsonResponse(NoticeJsonFixtures.page(false, 1L)))

        val notice = api.getNotices(null, 20).data!!.notices.single()

        assertNull(notice.detail)
    }

    @Test
    fun `명세에 없는 추가 필드가 와도 무시하고 파싱한다`() = runTest {
        server.enqueue(
            jsonResponse(
                """
                {"code":"OK","message":"ok","unknownTop":1,"data":{"notices":[
                  {"noticeId":1,"title":"t","createdDate":"2026-10-01T12:00:00","newField":"x"}
                ],"hasNext":false,"page":0}}
                """.trimIndent()
            )
        )

        val data = api.getNotices(null, 20).data!!

        assertEquals(1L, data.notices.single().noticeId)
        assertFalse(data.hasNext)
    }

    @Test
    fun `공지가 없으면 빈 배열과 hasNext false로 파싱된다`() = runTest {
        server.enqueue(jsonResponse(NoticeJsonFixtures.page(false)))

        val data = api.getNotices(null, 20).data!!

        assertTrue(data.notices.isEmpty())
        assertFalse(data.hasNext)
    }
}
