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
import com.teumteumeat.teumteumeat.domain.model.on_boarding.TimeState
import com.teumteumeat.teumteumeat.ui.component.timepicker_v2.Meridiem
import com.teumteumeat.teumteumeat.ui.component.timepicker_v2.TimePicker
import com.teumteumeat.teumteumeat.ui.component.timepicker_v2.TimePickerState

@Composable
fun TimeSliderWithPickTime(
    state: TimeState,
    onChange: (TimeState) -> Unit,
    modifier: Modifier = Modifier,
) {

    // ✅ TimePicker가 사용하는 상태로 변환
    var pickerState by remember(state) {
        mutableStateOf(state.toPickerState())
    }


    TimePicker(
        modifier = modifier
            .fillMaxWidth(),
        initialState = pickerState,
        onTimeChanged = { newPickerState ->

            // 🔄 Picker 상태 갱신
            pickerState = newPickerState

            // 🔄 외부에서 쓰는 TimeState로 다시 변환
            onChange(newPickerState.toTimeState())
        }
    )
}

private fun TimeState.toPickerState(): TimePickerState {
    return TimePickerState(
        meridiem = if (this.amPm == AmPm.AM) Meridiem.AM else Meridiem.PM,
        hour = this.hour,
        minute = this.minute
    )
}

private fun TimePickerState.toTimeState(): TimeState {
    return TimeState(
        hour = this.hour,
        minute = this.minute,
        amPm = if (this.meridiem == Meridiem.AM) AmPm.AM else AmPm.PM
    )
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



