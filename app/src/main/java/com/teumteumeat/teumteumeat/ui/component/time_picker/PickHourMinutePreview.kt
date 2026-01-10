package com.teumteumeat.teumteumeat.ui.component.time_picker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Preview(
    name = "PickHourMinute – 오전 9시 시작",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    widthDp = 360
)
@Composable
fun PickHourMinutePreview() {

    /** ✅ 상태: 24시간 기준 */
    var hour by remember { mutableStateOf(9) }     // 오전 9시
    var minute by remember { mutableStateOf(0) }   // 00분

    Surface(
        modifier = Modifier.wrapContentWidth(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            /** 🔍 현재 선택된 값 표시 */
            Text(
                text = "선택된 시간",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = String.format("%02d:%02d", hour, minute),
                fontSize = 28.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(24.dp))

            /** 🕒 시간 선택 휠 */
            PickHourMinuteCustom(
                hour = hour,
                minute = minute,
                onHourChange = { newHour ->
                    hour = newHour
                },
                onMinuteChange = { newMinute ->
                    minute = newMinute
                },
                amPmLabels = listOf("오전", "오후"),
                verticalSpace = 12.dp,
                horizontalSpace = 12.dp,
                isLoopingMinute = true
            )
        }
    }
}
