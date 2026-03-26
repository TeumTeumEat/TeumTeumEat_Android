package com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_2_library

import com.teumteumeat.teumteumeat.domain.model.history.CategoryHistoryUiModel
import com.teumteumeat.teumteumeat.ui.screen.a4_main.component.LibraryTabType
import com.teumteumeat.teumteumeat.ui.screen.a4_main.component.MotivationUiState
import com.teumteumeat.teumteumeat.ui.screen.a4_main.component.calendar.CalendarUiState

data class UiStateLibrary(
    val currentPage: Int = 0,
    val totalPage: Int = 5,

    val currentStreak: Int = 0,
    val stampCount: Int = 0,
    val monthStampCount: Int = 0,

    val calendarUiState: CalendarUiState = CalendarUiState(),

    val categoryHistories: List<CategoryHistoryUiModel> = emptyList<CategoryHistoryUiModel>(),
    // ✅ 주제별 탭 선택 상태
    val selectedCategoryName: String? = null,

    val isStreakBroken: Boolean = false,

    val selectedLibraryTab: LibraryTabType = LibraryTabType.DATE,

    val motivationUiState: MotivationUiState = MotivationUiState(),

    val errorMessage: String = "",

    val isSolvedToday: Boolean = false,
)