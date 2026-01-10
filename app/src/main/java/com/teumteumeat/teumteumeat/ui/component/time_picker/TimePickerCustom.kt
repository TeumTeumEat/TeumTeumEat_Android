package com.teumteumeat.teumteumeat.ui.component.time_picker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teumteumeat.teumteumeat.ui.component.time_picker.wheel.Wheel
import com.teumteumeat.teumteumeat.ui.component.time_picker.wheel.WheelSlot
import kotlin.math.abs

@Composable
fun PickHourMinuteCustom(
    hour: Int,
    minute: Int,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit,

    amPmLabels: List<String> = listOf("오전", "오후"),

    verticalSpace: Dp = 10.dp,
    horizontalSpace: Dp = 10.dp,
    isLoopingMinute: Boolean = true,
) {

    val hourRange = (1..12).toList()
    val minuteRange = listOf(0, 10, 20, 30, 40, 50)

    val isAm = hour < 12
    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }

    val selectedAmPmIndex = if (isAm) 0 else 1

    val selectedTextStyle = PickTimeTextStyle(
        color = Color.Black,
        fontSize = 24.sp,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
    )

    val unselectedTextStyle = PickTimeTextStyle(
        color = Color.Gray,
        fontSize = 18.sp,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Normal
    )

    // ✅ 반드시 Row 로 감싼다
    Row(
        modifier = Modifier.wrapContentWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // ✅ 오전 / 오후
        WheelSlot(minWidth = 48.dp) {
            StringWheel(
                items = amPmLabels,
                selectedIndex = selectedAmPmIndex,
                onItemSelected = { index ->
                    val newHour = when (index) {
                        0 -> if (hour >= 12) hour - 12 else hour
                        1 -> if (hour < 12) hour + 12 else hour
                        else -> hour
                    }
                    onHourChange(newHour)
                },
                space = verticalSpace,
                selectedTextStyle = selectedTextStyle,
                unselectedTextStyle = unselectedTextStyle,
                extraRow = 1,
                overlayColor = Color.White
            )
        }

        Spacer(modifier = Modifier.width(horizontalSpace))

        // ✅ 시
        WheelSlot(minWidth = 48.dp) {
            NumberWheel(
                items = hourRange,
                selectedItem = displayHour,
                onItemSelected = { selected ->
                    val newHour = if (isAm) {
                        if (selected == 12) 0 else selected
                    } else {
                        if (selected == 12) 12 else selected + 12
                    }
                    onHourChange(newHour)
                },
                space = verticalSpace,
                selectedTextStyle = selectedTextStyle,
                unselectedTextStyle = unselectedTextStyle,
                extraRow = 2,
                isLooping = true,
                overlayColor = Color.White
            )
        }

        Spacer(modifier = Modifier.width(horizontalSpace))
        Text(":", style = TextStyle(fontSize = 22.sp))
        Spacer(modifier = Modifier.width(horizontalSpace))

        // ✅ 분 (10분 단위 + 무한 스크롤)
        WheelSlot(minWidth = 48.dp) {
            NumberWheel(
                items = minuteRange,
                selectedItem = minuteRange.minBy { abs(it - minute) },
                onItemSelected = onMinuteChange,
                space = verticalSpace,
                selectedTextStyle = selectedTextStyle,
                unselectedTextStyle = unselectedTextStyle,
                extraRow = 2,
                isLooping = isLoopingMinute,
                overlayColor = Color.White
            )
        }
    }
}


private fun roundToNearestTen(minute: Int): Int {
    return ((minute + 5) / 10) * 10
        .coerceIn(0, 50)
}
