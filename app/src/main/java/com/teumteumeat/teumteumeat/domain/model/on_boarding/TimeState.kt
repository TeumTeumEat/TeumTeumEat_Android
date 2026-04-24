package com.teumteumeat.teumteumeat.domain.model.on_boarding

import com.teumteumeat.teumteumeat.ui.component.AmPm


data class TimeState(
    val hour: Int,      // 1 ~ 12
    val minute: Int,    // 0,10,20...
    val amPm: AmPm,
    val isSelected: Boolean = true // ⭐ 핵심
) {
    companion object {

        fun firstTime() = TimeState(
            amPm = AmPm.AM,
            hour = 8,
            minute = 0,
        )

        fun secondTime() = TimeState(
            amPm = AmPm.PM,
            hour = 6,
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