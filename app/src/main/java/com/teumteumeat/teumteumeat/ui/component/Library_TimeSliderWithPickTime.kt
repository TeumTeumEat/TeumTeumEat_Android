package com.teumteumeat.teumteumeat.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.anhaki.picktime.PickHourMinute
import com.anhaki.picktime.utils.PickTimeFocusIndicator
import com.anhaki.picktime.utils.PickTimeTextStyle
import com.anhaki.picktime.utils.TimeFormat

enum class AmPm { AM, PM }

data class TimeState(
    val amPm: AmPm = AmPm.AM,
    val hour: Int = 8,      // 1~12
    val minute: Int = 0     // 0~59 (여기서는 0,10,20..도 가능)
)

@Composable
fun AmPmWheel(
    amPm: AmPm,
    onChange: (AmPm) -> Unit,
) {
    val items = listOf("오전", "오후")
    val selectedIndex = if (amPm == AmPm.AM) 0 else 1

    // 아주 간단한 2개짜리 휠: LazyColumn + 중앙 강조는 생략하고,
    // 사진처럼 가운데 선택만 보이게 하고 싶으면 이후에 스냅/강조 넣어줄 수 있어.
    Column(
        modifier = Modifier.width(60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items.forEachIndexed { idx, text ->
            Text(
                text = text,
                style = if (idx == selectedIndex)
                    MaterialTheme.typography.titleMedium
                else
                    MaterialTheme.typography.bodyMedium,
                color = if (idx == selectedIndex)
                    MaterialTheme.colorScheme.onSurface
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .clickable { onChange(if (idx == 0) AmPm.AM else AmPm.PM) }
            )
        }
    }
}

@Composable
fun TimeSliderWithPickTime(
    state: TimeState,
    onChange: (TimeState) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ✅ 좌측 AM/PM
        AmPmWheel(
            amPm = state.amPm,
            onChange = { onChange(state.copy(amPm = it)) }
        )

        Spacer(Modifier.width(12.dp))

        // ✅ 가운데: 시/분 휠 (PickTime-Compose)
        PickHourMinute(
            initialHour = state.hour,
            onHourChange = { newHour ->
                // ✅ 11 ↔ 12 넘어갈 때 AM/PM 자동 변경
                val isForward = state.hour == 11 && newHour == 12
                val isBackward = state.hour == 12 && newHour == 11

                val newAmPm = when {
                    isForward -> if (state.amPm == AmPm.AM) AmPm.PM else AmPm.AM
                    isBackward -> if (state.amPm == AmPm.PM) AmPm.AM else AmPm.PM
                    else -> state.amPm
                }

                onChange(state.copy(hour = newHour, amPm = newAmPm))
            },
            initialMinute = state.minute,
            onMinuteChange = { newMinute ->
                onChange(state.copy(minute = newMinute))
            },
            timeFormat = TimeFormat.HOUR_12, // ✅ 1~12로 맞춤
            isLooping = true,                // ✅ 무한 스크롤
            extraRow = 2,                    // ✅ 위/아래 회색 행 2줄(사진 느낌)
            verticalSpace = 10.dp,
            horizontalSpace = 16.dp,
            containerColor = Color.White,

            // ✅ 중앙 선택 스타일(사진 느낌)
            selectedTextStyle = PickTimeTextStyle(
                color = Color(0xFF404040),
                fontSize = 24.sp,
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Bold,
            ),
            unselectedTextStyle = PickTimeTextStyle(
                color = Color(0xFF9F9F9F),
                fontSize = 18.sp,
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Normal,
            ),

            // ✅ 중앙 포커스 영역(사진의 흰 라운드 박스 느낌)
            focusIndicator = PickTimeFocusIndicator(
                enabled = true,
                widthFull = false,
                background = Color(0xFFF6F6F6),
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(0.dp, Color.Transparent),
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TimeSliderWithPickTimePreview() {
    var time by remember {
        mutableStateOf(TimeState(amPm = AmPm.AM, hour = 8, minute = 0))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TimeSliderWithPickTime(
            state = time,
            onChange = { time = it }
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = "${if (time.amPm == AmPm.AM) "오전" else "오후"} ${time.hour}시 ${"%02d".format(time.minute)}분",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}



