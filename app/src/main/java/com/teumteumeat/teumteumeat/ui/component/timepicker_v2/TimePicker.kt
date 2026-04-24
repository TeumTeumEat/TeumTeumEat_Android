package com.teumteumeat.teumteumeat.ui.component.timepicker_v2

// Compose 기본
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

// Layout
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height

// Arrangement / Alignment
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment

// Modifier
import androidx.compose.ui.Modifier

// 단위
import androidx.compose.ui.unit.dp

// 🔽 Picker 내부 텍스트 UI
import com.teumteumeat.teumteumeat.utils.appTypography
import com.teumteumeat.teumteumeat.utils.extendedColors

// 오전 / 오후
private val meridiemItems = listOf("오전", "오후")

private const val INFINITE_MULTIPLIER = 1000

private val hourSteps = listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11)

val infiniteHourItems = List(hourSteps.size * INFINITE_MULTIPLIER) { index ->
    val hour = (index % 12) + 1
    "%02d시".format(hour)
}

private val minuteSteps = listOf(0, 10, 20, 30, 40, 50)

val infiniteMinuteItems = List(minuteSteps.size * INFINITE_MULTIPLIER) { index ->
    val minute = minuteSteps[index % minuteSteps.size]
    "%02d분".format(minute)
}

@Composable
private fun TimePickerText(
    text: String,
    isSelected: Boolean
) {
    Text(
        text = text,
        style = if (isSelected)
            MaterialTheme.appTypography.subtitleSemiBold16
        else
            MaterialTheme.typography.bodyMedium,
        color = if (isSelected) MaterialTheme.extendedColors.primary
            else MaterialTheme.extendedColors.textGhost
    )
}

@Composable
fun TimePicker(
    modifier: Modifier = Modifier,
    initialState: TimePickerState,
    onTimeChanged: (TimePickerState) -> Unit
) {
    // 현재 선택된 상태를 remember로 관리
    // ✅ initialState가 바뀌면 내부 상태도 갱신
    var timeState by remember(initialState) {
        mutableStateOf(initialState)
    }

    LaunchedEffect(timeState) {
        println("🟣 [TimePicker render] timeState = $timeState")
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {

        // =====================
        // 1️⃣ 오전 / 오후 스피너
        // =====================
        WheelPicker(
            modifier = Modifier.weight(1f),
            items = meridiemItems,
            initialItem = if (timeState.meridiem == Meridiem.AM) "오전" else "오후",
            onItemSelected = { _, value ->
                val newMeridiem =
                    if (value == "오전") Meridiem.AM else Meridiem.PM

                // 상태 갱신
                timeState = timeState.copy(meridiem = newMeridiem)
                onTimeChanged(timeState)
            },
            selectedRowShape = RoundedCornerShape(topStart = 32.dp, bottomStart = 32.dp)
        ) { text, isSelected ->
            TimePickerText(text, isSelected)
        }

        // ==========
        // 2️⃣ 시 스피너
        // ==========
        WheelPicker(
            modifier = Modifier.weight(1f),
            items = infiniteHourItems,
            initialItem = "%02d시".format(timeState.hour),
            cycleSize = hourSteps.size,
            onItemSelected = { _, value ->
                val hour = value.replace("시", "").toInt()

                timeState = timeState.copy(hour = hour)
                onTimeChanged(timeState)
            }
        ) { text, isSelected ->
            TimePickerText(text, isSelected)
        }

        // ==========
        // 3️⃣ 분 스피너
        // ==========
        WheelPicker(
            modifier = Modifier.weight(1f),
            items = infiniteMinuteItems,
            initialItem = "%02d분".format(timeState.minute),
            cycleSize = minuteSteps.size,
            onItemSelected = { _, value ->
                val minute = value.replace("분", "").toInt()

                timeState = timeState.copy(minute = minute)
                onTimeChanged(timeState)
            },
            selectedRowShape = RoundedCornerShape(topEnd = 32.dp, bottomEnd = 32.dp)
        ) { text, isSelected ->
            TimePickerText(text, isSelected)
        }
    }
}

fun normalizeMinute(minute: Int): Int =
    (minute / 10) * 10


