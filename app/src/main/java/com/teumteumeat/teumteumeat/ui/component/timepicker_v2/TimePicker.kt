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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment

// Modifier
import androidx.compose.ui.Modifier

// 단위
import androidx.compose.ui.unit.dp

// 🔽 프로젝트 내부 타입 (같은 패키지가 아니라면 필요)
import com.teumteumeat.teumteumeat.ui.component.timepicker_v2.TimePickerState
import com.teumteumeat.teumteumeat.ui.component.timepicker_v2.Meridiem

// 🔽 WheelPicker (이미 구현된 공용 컴포넌트)
import com.teumteumeat.teumteumeat.ui.component.timepicker_v2.WheelPicker

// 🔽 Picker 데이터
import com.teumteumeat.teumteumeat.ui.component.timepicker_v2.meridiemItems
import com.teumteumeat.teumteumeat.ui.component.timepicker_v2.hourItems
import com.teumteumeat.teumteumeat.ui.component.timepicker_v2.minuteItems

// 🔽 Picker 내부 텍스트 UI
import com.teumteumeat.teumteumeat.ui.component.timepicker_v2.TimePickerText
import com.teumteumeat.teumteumeat.ui.theme.Blue500
import com.teumteumeat.teumteumeat.ui.theme.Gray40

// 오전 / 오후
private val meridiemItems = listOf("오전", "오후")

// 1시 ~ 12시
private val hourItems = (1..12).map { "${it}시" }

// 00분 ~ 59분
private val minuteItems = (0..59).map { "%02d분".format(it) }

private const val INFINITE_MULTIPLIER = 1000

val infiniteHourItems = List(12 * INFINITE_MULTIPLIER) { index ->
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
            MaterialTheme.typography.titleMedium
        else
            MaterialTheme.typography.bodyMedium,
        color = if (isSelected) Blue500 else Gray40
    )
}

@Composable
fun TimePicker(
    modifier: Modifier = Modifier,
    initialState: TimePickerState,
    onTimeChanged: (TimePickerState) -> Unit
) {
    // 현재 선택된 상태를 remember로 관리
    var timeState by remember { mutableStateOf(initialState) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp), // 사진과 유사한 높이
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
            }
        ) { text, isSelected ->
            TimePickerText(text, isSelected)
        }

        // ==========
        // 2️⃣ 시 스피너
        // ==========
        WheelPicker(
            modifier = Modifier.weight(1f),
            items = infiniteHourItems,
            initialItem = "${timeState.hour}시",
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
            onItemSelected = { _, value ->
                val minute = value.replace("분", "").toInt()

                timeState = timeState.copy(minute = minute)
                onTimeChanged(timeState)
            }
        ) { text, isSelected ->
            TimePickerText(text, isSelected)
        }
    }
}

fun normalizeMinute(minute: Int): Int =
    (minute / 10) * 10
