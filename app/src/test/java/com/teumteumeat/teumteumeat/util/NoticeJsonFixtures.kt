package com.teumteumeat.teumteumeat.util

import okhttp3.mockwebserver.MockResponse

/**
 * 공지 API 테스트(계약·기능)가 공유하는 서버 응답 JSON 조립 헬퍼.
 * 필드 구성은 `GET /api/v1/notices` 명세(응답 `code`, `message`, `data.notices[]`, `data.hasNext`)를 따른다.
 */
object NoticeJsonFixtures {

    /** 명세서 응답 예시 그대로의 JSON */
    val SPEC_SAMPLE = """
        {
          "code": "OK",
          "message": "요청이 성공적으로 처리되었습니다.",
          "data": {
            "notices": [
              {
                "noticeId": 1,
                "title": "서비스 신규 오픈 안내",
                "createdDate": "2026-10-01T12:00:00",
                "detail": {
                  "fullScreenImageUrl": "https://teumteumeat.s3.ap-northeast-2.amazonaws.com/prod/notice/6cd45ae3-87a5_full.png",
                  "externalLink": "https://example.com/event",
                  "deepLink": "teumteumeat://quiz",
                  "actionButtonText": "이벤트 참여하기"
                }
              }
            ],
            "hasNext": true
          }
        }
    """.trimIndent()

    /** detail 없이 제목·날짜만 가진 공지 한 건 */
    fun notice(
        id: Long,
        title: String = "공지 $id",
        createdDate: String = "2026-10-01T12:00:00"
    ): String = """{"noticeId":$id,"title":"$title","createdDate":"$createdDate"}"""

    /** [ids] 순서대로 공지를 담은 성공 응답 JSON */
    fun page(hasNext: Boolean, vararg ids: Long): String =
        pageOf(hasNext, ids.map { notice(it) })

    /** 이미 조립된 공지 JSON 조각들로 성공 응답 JSON을 만든다. */
    fun pageOf(hasNext: Boolean, notices: List<String>): String =
        """{"code":"OK","message":"요청이 성공적으로 처리되었습니다.","data":{"notices":[${notices.joinToString(",")}],"hasNext":$hasNext}}"""

    fun jsonResponse(body: String, httpCode: Int = 200): MockResponse =
        MockResponse()
            .setResponseCode(httpCode)
            .setHeader("Content-Type", "application/json")
            .setBody(body)
}
