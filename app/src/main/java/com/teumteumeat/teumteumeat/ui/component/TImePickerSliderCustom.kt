package com.teumteumeat.teumteumeat.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vsnappy1.timepicker.TimePicker
import com.vsnappy1.timepicker.enums.MinuteGap
import com.vsnappy1.timepicker.ui.model.TimePickerConfiguration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerLikeImage(
    hour: Int,
    minute: Int,
    isAm: Boolean,
    onTimeChanged: (Int, Int, Boolean) -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .padding(16.dp)
            .wrapContentSize()
    ) {
        TimePicker(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
            onTimeSelected = { hour, minute ->
                // 결과는 항상 minute = 0, 10, 20, 30, 40, 50
                println("선택된 시간: $hour:$minute")
            },
            is24Hour = false,                // 12시간제 (AM/PM)
            minuteGap = MinuteGap.TEN,       // ✅ 핵심: 10분 단위
            configuration = TimePickerConfiguration.Builder()
                .selectedTimeScaleFactor(1.4f)  // 중앙 강조
                .selectedTimeAreaHeight(48.dp)
                .selectedTimeAreaColor(MaterialTheme.colorScheme.secondaryContainer)
                // 중앙 선택 영역 강조 (흰 박스 느낌)
                .selectedTimeAreaShape(RoundedCornerShape(24.dp))
                // 선택 / 비선택 텍스트 스타일
                .timeTextStyle(
                    TextStyle(
                        fontSize = 18.sp,
                        color = Color(0xFF9F9F9F)
                    )
                )
                .selectedTimeTextStyle(
                    TextStyle(
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Bold
                    )
                )
                .build()
        )
    }
}

data class PreviewTime(
    val hour: Int,
    val minute: Int,
    val isAm: Boolean
)

@Preview(showBackground = true)
@Composable
fun TimePickerLikeImagePreview() {


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        TimePickerLikeImage(
            hour = 1,
            minute = 10,
            isAm = true,

            onTimeChanged = { hour, minute, isAm ->
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        /*Text(
            text = buildString {
                append(if (time.isAm) "오전 " else "오후 ")
                append("${time.hour}시 ")
                append("${"%02d".format(time.minute)}분")
            },
            style = MaterialTheme.typography.bodyMedium
        )*/
    }
}




