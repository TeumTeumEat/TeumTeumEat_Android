package com.teumteumeat.teumteumeat.domain.model.on_boarding

import com.teumteumeat.teumteumeat.ui.component.AmPm


data class TimeState(
    val hour: Int,      // 1 ~ 12
    val minute: Int,    // 0,10,20...
    val amPm: AmPm,
    val isSelected: Boolean = false // ⭐ 핵심
) {
    companion object {
        fun initial(isSelected: Boolean) = TimeState(
            amPm = AmPm.AM,
            hour = 0,
            minute = 0,
            isSelected = false
        )

        fun amTime() = TimeState(
            amPm = AmPm.AM,
            hour = 0,
            minute = 0,
        )

        fun pmTime() = TimeState(
            amPm = AmPm.PM,
            hour = 0,
            minute = 0,
        )
    }
}

fun TimeState.toServerTime(): String {
    val hour24 = when (amPm) {
        AmPm.AM -> if (hour == 12) 0 else hour
        AmPm.PM -> if (hour == 12) 12 else hour + 12
    }

    return String.format("%02d:%02d:00", hour24, minute)
}