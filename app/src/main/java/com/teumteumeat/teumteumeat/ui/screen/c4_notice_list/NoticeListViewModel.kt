package com.teumteumeat.teumteumeat.ui.screen.c4_notice_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model.uiMessage
import com.teumteumeat.teumteumeat.domain.model.notice.Notice
import com.teumteumeat.teumteumeat.domain.usecase.SessionManager
import com.teumteumeat.teumteumeat.domain.usecase.notice.GetNoticeListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * 공지사항 목록(무한 스크롤) ViewModel.
 *
 * 서버의 커서 방식(`lastNoticeId`) 페이징을 사용한다. 첫 요청은 커서 없이 보내고,
 * 이후에는 직전에 불러온 마지막 공지의 ID를 커서로 보낸다.
 *
 * TODO: 읽지 않은 공지 빨간 점(`isNew`)은 로컬 저장소의 읽음 상태로 판단하기로 확정되었다 (SCRUM-176).
 *  읽음 상태 저장 구현 전까지는 항상 false다.
 */
@HiltViewModel
class NoticeListViewModel @Inject constructor(
    private val getNoticeListUseCase: GetNoticeListUseCase,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiStateNoticeList())
    val uiState = _uiState.asStateFlow()

    init {
        loadFirstPage()
    }

    /** 첫 페이지를 불러온다. 초기 진입과 에러 재시도에서 사용한다. */
    fun loadFirstPage() {
        _uiState.update {
            UiStateNoticeList(isInitialLoading = true)
        }
        viewModelScope.launch {
            when (val result = getNoticeListUseCase(lastNoticeId = null)) {
                is ApiResultV2.Success -> {
                    val page = result.data
                    _uiState.update {
                        it.copy(
                            notices = page.notices.map(Notice::toUiModel),
                            lastNoticeId = page.nextCursor,
                            hasNext = page.hasNext,
                            isInitialLoading = false,
                            errorMessage = null,
                        )
                    }
                }

                is ApiResultV2.SessionExpired -> sessionManager.expireSession()

                else -> _uiState.update {
                    it.copy(
                        isInitialLoading = false,
                        errorMessage = result.uiMessage,
                    )
                }
            }
        }
    }

    /**
     * 스크롤이 목록 하단에 가까워지면 호출된다. 중복 요청과 마지막 페이지 이후 요청은 무시한다.
     * 실패하면 목록은 그대로 두고 로딩 표시만 해제한다 (사용자가 다시 스크롤하면 재시도된다).
     */
    fun loadNextPage() {
        val current = _uiState.value
        if (current.isInitialLoading || current.isLoadingMore || !current.hasNext) return
        // 커서가 없으면 첫 페이지가 아직 성공하지 못한 상태이므로 다음 페이지를 요청하지 않는다.
        val cursor = current.lastNoticeId ?: return

        _uiState.update { it.copy(isLoadingMore = true) }
        viewModelScope.launch {
            when (val result = getNoticeListUseCase(lastNoticeId = cursor)) {
                is ApiResultV2.Success -> {
                    val page = result.data
                    _uiState.update {
                        it.copy(
                            notices = it.notices + page.notices.map(Notice::toUiModel),
                            // 응답이 비어 있으면 기존 커서를 유지해 같은 요청이 반복되지 않도록 hasNext도 닫는다.
                            lastNoticeId = page.nextCursor ?: it.lastNoticeId,
                            hasNext = page.hasNext && page.notices.isNotEmpty(),
                            isLoadingMore = false,
                        )
                    }
                }

                is ApiResultV2.SessionExpired -> {
                    _uiState.update { it.copy(isLoadingMore = false) }
                    sessionManager.expireSession()
                }

                else -> _uiState.update { it.copy(isLoadingMore = false) }
            }
        }
    }

}

private val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("M월 d일")

private fun Notice.toUiModel(): NoticeItemUiModel =
    NoticeItemUiModel(
        noticeId = noticeId,
        title = title,
        dateText = createdDate?.format(DATE_FORMATTER).orEmpty(),
        isNew = false,
        detail = detail,
    )
