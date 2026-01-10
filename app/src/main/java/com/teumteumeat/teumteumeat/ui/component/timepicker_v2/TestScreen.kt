package com.teumteumeat.teumteumeat.ui.component.timepicker_v2

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding

import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp



@Preview(
    name = "TimePicker Preview",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
@Composable
fun TimePickerPreview() {

    // 🔹 Preview에서 선택된 시간을 저장하는 상태
    var selectedTime by remember {
        mutableStateOf(
            TimePickerState(
                meridiem = Meridiem.AM,
                hour = 9,
                minute = 30
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // =========================
        // 1️⃣ 현재 선택된 시간 표시
        // =========================
        Text(
            text = buildString {
                append(
                    if (selectedTime.meridiem == Meridiem.AM) "오전 " else "오후 "
                )
                append("${selectedTime.hour}시 ")
                append("%02d분".format(selectedTime.minute))
            },
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        // =========================
        // 2️⃣ 타임 피커
        // =========================
        TimePicker(
            initialState = selectedTime,
            onTimeChanged = { newTime ->
                // 🔸 TimePicker에서 변경된 값을 그대로 반영
                selectedTime = newTime
            }
        )
    }
}
