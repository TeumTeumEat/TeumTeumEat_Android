package com.teumteumeat.teumteumeat.ui.screen.a4_main.component.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teumteumeat.teumteumeat.utils.appTypography
import com.teumteumeat.teumteumeat.utils.extendedColors
import java.time.LocalDate
import java.time.YearMonth


fun buildMonthDays(yearMonth: YearMonth): List<LocalDate?> {
    val firstDay = yearMonth.atDay(1)

    // ✅ 월요일 시작 기준 offset
    val dayOfWeek = firstDay.dayOfWeek.value - 1
    // 월=1 → 0, 화=2 → 1, ..., 일=7 → 6

    val totalDays = yearMonth.lengthOfMonth()
    val list = mutableListOf<LocalDate?>()

    repeat(dayOfWeek) {
        list.add(null)
    }

    for (day in 1..totalDays) {
        list.add(yearMonth.atDay(day))
    }

    return list
}

@Composable
fun CalendarMonth(
    yearMonth: YearMonth,
    selectedDate: LocalDate?,
    solvedDates: Set<LocalDate>,
    onDateClick: (LocalDate) -> Unit,
) {
    val days = remember(yearMonth) {
        buildMonthDays(yearMonth)
    }

    Column {
        WeekHeaderRow()

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.height(320.dp),
            userScrollEnabled = false
        ) {
            items(days) { date ->
                if (date == null) {
                    Spacer(Modifier.size(size = 40.dp))
                } else {
                    CalendarDayCell(
                        date = date,
                        isSelected = date == selectedDate,
                        isSolved = solvedDates.contains(date),
                        onClick = { onDateClick(date) }
                    )
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
                    isSolved -> Modifier.background(
                        color = MaterialTheme.extendedColors.btnFillSecondary,
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

                else ->
                    MaterialTheme.extendedColors.textGhost
            }
        )
    }
}



