package com.teumteumeat.teumteumeat.ui.screen.c4_notice_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * 공지사항 목록(무한 스크롤) ViewModel.
 *
 * TODO: 현재는 UI 검증용 더미 데이터를 사용한다.
 *  서버 연동 시 [fetchPage]를 `GetNoticeListUseCase(page, size)` 호출로 교체한다.
 */
@HiltViewModel
class NoticeListViewModel @Inject constructor() : ViewModel() {

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
            val result = fetchPage(page = 0)
            _uiState.update {
                it.copy(
                    notices = result.notices,
                    page = 0,
                    hasNext = result.hasNext,
                    isInitialLoading = false,
                )
            }
        }
    }

    /** 스크롤이 목록 하단에 가까워지면 호출된다. 중복 요청과 마지막 페이지 이후 요청은 무시한다. */
    fun loadNextPage() {
        val current = _uiState.value
        if (current.isInitialLoading || current.isLoadingMore || !current.hasNext) return

        _uiState.update { it.copy(isLoadingMore = true) }
        viewModelScope.launch {
            val nextPage = current.page + 1
            val result = fetchPage(page = nextPage)
            _uiState.update {
                it.copy(
                    notices = it.notices + result.notices,
                    page = nextPage,
                    hasNext = result.hasNext,
                    isLoadingMore = false,
                )
            }
        }
    }

    // ================= 더미 데이터 (서버 연동 시 제거) =================

    private data class NoticePage(
        val notices: List<NoticeItemUiModel>,
        val hasNext: Boolean,
    )

    private suspend fun fetchPage(page: Int): NoticePage {
        delay(DUMMY_LATENCY_MS)

        val from = page * PAGE_SIZE
        val to = minOf(from + PAGE_SIZE, DUMMY_TOTAL_COUNT)
        val today = LocalDate.now()

        val notices = (from until to).map { index ->
            NoticeItemUiModel(
                noticeId = (DUMMY_TOTAL_COUNT - index).toLong(),
                title = if (index % 2 == 0) "틈틈잇 공지사항입니다!" else "리그 관련 오픈 이벤트",
                dateText = today.minusDays(index * 7L).format(DATE_FORMATTER),
                isNew = index < NEW_BADGE_COUNT,
            )
        }
        return NoticePage(notices = notices, hasNext = to < DUMMY_TOTAL_COUNT)
    }

    private companion object {
        const val PAGE_SIZE = 20
        const val DUMMY_TOTAL_COUNT = 45
        const val NEW_BADGE_COUNT = 2
        const val DUMMY_LATENCY_MS = 600L
        val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("M월 d일")
    }
}
