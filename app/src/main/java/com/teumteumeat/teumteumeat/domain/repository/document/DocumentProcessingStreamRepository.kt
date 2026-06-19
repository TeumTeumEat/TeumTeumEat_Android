package com.teumteumeat.teumteumeat.domain.repository.document

import com.teumteumeat.teumteumeat.domain.model.sse.DocumentProcessingEvent
import kotlinx.coroutines.flow.Flow

/**
 * 문서 처리 상태를 SSE 로 스트리밍하는 Repository 인터페이스.
 *
 * ### 이벤트 순서 계약
 * ```
 * Connected → Pending? → Processing(remainMs)? → Completed   ← 정상 완료
 *                                              → Failed       ← 처리 실패
 * 연결 오류  → StreamError                                     ← 재연결 소진
 * ```
 * [Completed], [Failed], [StreamError] 수신 후 Flow 는 추가 이벤트 없이 완료된다.
 */
interface DocumentProcessingStreamRepository {

    /**
     * 지정된 문서의 처리 상태 SSE 스트리밍을 시작한다.
     *
     * Cold Flow 이므로 `collect` 시점에 실제 연결이 열린다.
     * Flow 수집 취소 시 서버 연결이 즉시 해제된다.
     *
     * @param goalId     목표 ID.
     * @param documentId 문서 ID.
     */
    fun streamDocumentProcessing(goalId: Long, documentId: Long): Flow<DocumentProcessingEvent>
}