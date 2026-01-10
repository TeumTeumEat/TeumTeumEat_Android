package com.teumteumeat.teumteumeat.ui.screen.a4_main.component.calendar

import android.util.Log
import java.time.LocalDate
import java.time.YearMonth
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.teumteumeat.teumteumeat.domain.model.history.CalendarDailyItem
import com.teumteumeat.teumteumeat.utils.appTypography
import com.teumteumeat.teumteumeat.utils.extendedColors
import kotlinx.coroutines.launch

data class CalendarUiState(
    val currentMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate? = null,

    // ✅ 선택한 날짜의 학습 내역
    val solvedDates: Set<LocalDate> = emptySet(),
    val dailyLearningList: List<CalendarDailyItem> = emptyList<CalendarDailyItem>(),

    // ✅ 로딩 / 에러 UI 제어용
    val isDailyLoading: Boolean = false,
    val dailyErrorMessage: String? = null,
)

@Composable
fun CalendarHeader(
    yearMonth: YearMonth,
    onPrev: () -> Unit,
    onNext: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        IconButton(onClick = onPrev) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "prev",
                tint = MaterialTheme.extendedColors.btnGray800
            )
        }

        Spacer(Modifier.width(32.dp))

        Text(
            text = "${yearMonth.year}년 ${yearMonth.monthValue}월",
            style = MaterialTheme.appTypography.subtitleSemiBold18.copy(
                color = MaterialTheme.extendedColors.textSecondary
            )
        )

        Spacer(Modifier.width(32.dp))

        IconButton(onClick = onNext) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "next",
                tint = MaterialTheme.extendedColors.btnGray800
            )
        }
    }
}

@Composable
fun CalendarPager(
    modifier: Modifier = Modifier,
    uiState: CalendarUiState,
    onMonthChange: (YearMonth) -> Unit,
    onDateClick: (LocalDate) -> Unit,
) {
    val totalPage = 120                 // ±5년
    val startPage = totalPage / 2       // 현재 월 기준
    val scope = rememberCoroutineScope()

    // ✅ 기준 월(Anchor)은 "처음 진입 시" 값으로 고정 (드리프트 방지)
    val anchorMonth = remember { uiState.currentMonth }

    val pagerState = rememberPagerState(
        initialPage = startPage,
        pageCount = { totalPage }
    )

    // ✅ 현재 페이지가 가리키는 월 (anchor 기준으로만 계산)
    val visibleMonth = anchorMonth.plusMonths(
        pagerState.currentPage.toLong() - startPage
    )


    Column(modifier = modifier) {

        // 🔹 헤더 (Pager만 이동)
        CalendarHeader(
            yearMonth = visibleMonth,
            onPrev = {
                scope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                }
            },
            onNext = {
                scope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                }
            }
        )
        Spacer(Modifier.height(12.dp))

        // 🔹 Pager
        HorizontalPager(
            state = pagerState,
        ) { page ->
            // ✅ 각 페이지의 월도 anchor 기준으로 계산해야 함
            val month = anchorMonth.plusMonths(page.toLong() - startPage)

            val selectedDateForMonth = remember(uiState.selectedDate, month) {
                uiState.selectedDate?.takeIf {
                    YearMonth.from(it) == month
                }
            }

            CalendarMonth(
                yearMonth = month,
                selectedDate = selectedDateForMonth,
                onDateClick = onDateClick,
                solvedDates = uiState.solvedDates,
            )
        }
    }

    // ✅ 월 상태 변경은 여기서만
    LaunchedEffect(pagerState.currentPage) {
        Log.d("pagerState.currentPage: ", "${pagerState.currentPage}")
        onMonthChange(visibleMonth)
    }
}
