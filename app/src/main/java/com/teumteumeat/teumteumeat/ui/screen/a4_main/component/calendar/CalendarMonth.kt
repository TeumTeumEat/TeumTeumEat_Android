package com.teumteumeat.teumteumeat.ui.screen.a4_main.component.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teumteumeat.teumteumeat.utils.appTypography
import com.teumteumeat.teumteumeat.utils.extendedColors
import java.time.LocalDate
import java.time.YearMonth


data class CalendarDayInfo(
    val date: LocalDate,
    val isInCurrentMonth: Boolean,
)

fun buildMonthDays(yearMonth: YearMonth): List<CalendarDayInfo> {
    val firstDay = yearMonth.atDay(1)

    // ✅ 월요일 시작 기준 offset
    val leadingCount = firstDay.dayOfWeek.value - 1
    // 월=1 → 0, 화=2 → 1, ..., 일=7 → 6

    val list = mutableListOf<CalendarDayInfo>()

    // ✅ 첫째 주: 이전 달 마지막 날짜들로 채움
    for (i in leadingCount downTo 1) {
        list.add(CalendarDayInfo(firstDay.minusDays(i.toLong()), isInCurrentMonth = false))
    }

    for (day in 1..yearMonth.lengthOfMonth()) {
        list.add(CalendarDayInfo(yearMonth.atDay(day), isInCurrentMonth = true))
    }

    // ✅ 마지막 주: 7의 배수가 될 때까지 다음 달 날짜들로 채움
    val lastDay = yearMonth.atEndOfMonth()
    var trailing = 1L
    while (list.size % 7 != 0) {
        list.add(CalendarDayInfo(lastDay.plusDays(trailing++), isInCurrentMonth = false))
    }

    return list
}

// ✅ 달력 최대 행 수 (31일 달 + leading offset 5 이상 = 6행)
private const val MAX_WEEK_ROWS = 6

@Composable
fun CalendarMonth(
    yearMonth: YearMonth,
    selectedDate: LocalDate?,
    solvedDates: Set<LocalDate>,
    onDateClick: (LocalDate) -> Unit,
    weekSpacing: Dp = 0.dp,
) {
    val days = remember(yearMonth) {
        buildMonthDays(yearMonth)
    }

    Column {
        WeekHeaderRow()

        Column(
            verticalArrangement = Arrangement.spacedBy(weekSpacing),
        ) {
            val weeks = days.chunked(7)

            weeks.forEach { week ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    week.forEach { dayInfo ->
                        Box(modifier = Modifier.weight(1f)) {
                            CalendarDayCell(
                                date = dayInfo.date,
                                isSelected = dayInfo.date == selectedDate,
                                isSolved = solvedDates.contains(dayInfo.date),
                                isOutsideMonth = !dayInfo.isInCurrentMonth,
                                onClick = { onDateClick(dayInfo.date) }
                            )
                        }
                    }
                }
            }

            // ✅ 6행 고정: 5주 달은 빈 행으로 채워 월 스와이프 시 높이 통일
            repeat(MAX_WEEK_ROWS - weeks.size) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    repeat(7) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WeekHeaderRow() {
    val days = listOf("월", "화", "수", "목", "금", "토", "일")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        days.forEach {
            Text(
                text = it,
                style = MaterialTheme.appTypography.bodyMedium16.copy(
                    lineHeight = 22.sp,
                    color = MaterialTheme.extendedColors.textTeritory
                )
            )
        }
    }
}

@Composable
fun CalendarDayCell(
    date: LocalDate,
    isSelected: Boolean,
    onClick: () -> Unit,
    isSolved: Boolean,
    isOutsideMonth: Boolean = false,
) {
    val today = LocalDate.now()
    val isToday = date == today

    val isEnabled = isSolved   // ✅ 핵심 규칙

    Box(
        modifier = Modifier
            .aspectRatio(1f)  // ✅ 셀을 무조건 정사각형으로
            .padding(6.dp)
            .then(
                when {
                    // ✅ 선택된 날짜
                    isSelected -> Modifier.background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )

                    // ✅ 퀴즈를 푼 날짜 (원색)
                    isSolved -> Modifier
                        .padding(1.5.dp)
                        .background(
                            color = MaterialTheme.extendedColors.btnFillDisabledColor,
                            shape = CircleShape
                        )

                    else -> Modifier
                }
            )
            .clickable(
                enabled = isEnabled,
                indication = null, // 🔥 리플 제거
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.appTypography.bodyMedium16,
            color = when {
                isSolved ->
                    MaterialTheme.colorScheme.onPrimary

                isSelected ->
                    MaterialTheme.colorScheme.onPrimary

                isToday ->
                    MaterialTheme.extendedColors.textSecondary

                // ✅ 학습하지 않은 인접 달 날짜는 더 흐리게 구분
                isOutsideMonth ->
                    MaterialTheme.extendedColors.textGhost.copy(alpha = 0.4f)

                else ->
                    MaterialTheme.extendedColors.textGhost
            }
        )
    }
}



