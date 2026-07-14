package com.teumteumeat.teumteumeat.domain.repository.summary

import com.teumteumeat.teumteumeat.domain.model.sse.SseEvent
import kotlinx.coroutines.flow.Flow

/**
 * 일일 학습 요약 SSE 스트리밍을 담당하는 Repository 인터페이스.
 *
 * ### 이벤트 순서 계약
 * 구현체는 아래 순서를 보장해야 한다:
 * ```
 * SseEvent.Connected
 *     → SseEvent.Chunk (0~N회)
 *         → SseEvent.TitleReceived  ← Flow 정상 완료
 *
 * 오류 발생 시 (재연결 소진 후):
 *     → SseEvent.StreamError        ← Flow 정상 완료
 * ```
 * `SseEvent.TitleReceived` 또는 `SseEvent.StreamError` 수신 후
 * Flow는 추가 이벤트 없이 완료(complete)된다.
 *
 * ### 소비 예시
 * ```kotlin
 * summaryStreamRepository.streamDailySummary(categoryId)
 *     .collect { event ->
 *         when (event) {
 *             is SseEvent.Connected     -> // 로딩 상태 표시
 *             is SseEvent.Chunk         -> // 텍스트 누적
 *             is SseEvent.TitleReceived -> // 제목 설정 + 완료 처리
 *             is SseEvent.StreamError   -> // 에러 UI 표시
 *         }
 *     }
 * ```
 */
interface SummaryStreamRepository {

    /**
     * 지정된 카테고리의 일일 요약 SSE 스트리밍을 시작한다.
     *
     * Cold Flow로 반환되므로, 수집(collect)을 시작해야 실제 연결이 열린다.
     * Flow 수집 취소 시 서버 연결이 즉시 해제된다.
     *
     * @param categoryId 요약 스트리밍을 요청할 카테고리 ID.
     * @return [SseEvent]를 방출하는 Cold [Flow].
     */
    fun streamDailySummary(categoryId: Long): Flow<SseEvent>
}