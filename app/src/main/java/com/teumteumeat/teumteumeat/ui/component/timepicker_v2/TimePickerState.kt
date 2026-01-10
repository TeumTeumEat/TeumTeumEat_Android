package com.teumteumeat.teumteumeat.ui.component.timepicker_v2

/**
 * 타임 피커에서 선택된 값을 하나로 관리하기 위한 상태 클래스
 *
 * why:
 * - 오전/오후, 시, 분이 따로 놀면 관리가 어려움
 * - 하나의 객체로 묶으면 ViewModel, API 전달이 쉬움
 */
data class TimePickerState(
    val meridiem: Meridiem, // 오전/오후
    val hour: Int,          // 1 ~ 12
    val minute: Int         // 0 ~ 59
)

enum class Meridiem {
    AM, PM
}
