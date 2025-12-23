package com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding

import com.teumteumeat.teumteumeat.domain.model.on_boarding.TimeState
import com.teumteumeat.teumteumeat.ui.component.AmPm

data class UiStateOnBoardingMain(
    val currentPage: Int = 0,
    val totalPage: Int = 5,

    // 이름 설정
    val charName: String = "",
    val errorMessage: String = "",
    val isNameValid: Boolean = false,
    val violation: NameViolation = NameViolation.None,

    val isSetWorkInTime: Boolean= false,
    val isSetWorkOutTime: Boolean= false,

    // 🔹 출/퇴근 시간 설정
    val workInTime: TimeState = TimeState.amTime(),  // 집에서 나오는 시간
    val workOutTime: TimeState = TimeState.pmTime(), // 집으로 가는 시간

    // 🔹 BottomSheet 상태
    val showBottomSheet: Boolean = false,
    val currentTimeType: TimeType = TimeType.NOTTING,

    // 🔹 체크박스 상태 (추가)
    val isCheckedAgreement: Boolean = false,

    // 🔔 알림 체크 상태 (UI 표현)
    val isNotificationChecked: Boolean = false,

    // 🔔 실제 권한 허용 여부
    val isNotificationGranted: Boolean = false,

    // 🔔 권한 요청 트리거 (이벤트성)
    val requestNotificationPermission: Boolean = false,

    val selectedMinute: Int? = null,

    val isLoading: Boolean = false,
    val isSuccess: Boolean = false
)

enum class TimeType {
    OUT, // 집을 나오는 시간
    IN,   // 집을 들어가는 시간
    NOTTING //
}


fun TimeState.toDisplayText(): String {
    val isAm = hour in 0..11

    val amPmText = if (isAm) "오전" else "오후"

    val displayHour = when {
        hour == 0 -> 12          // 00:xx → 오전 12시
        hour in 1..12 -> hour    // 01~12
        else -> hour - 12        // 13~23 → 오후
    }

    return "%s %02d시 %02d분".format(
        amPmText,
        displayHour,
        minute
    )
}

sealed interface NameViolation {
    data object None : NameViolation
    data object Empty : NameViolation
    data object TooLong : NameViolation
    data object HasSpace : NameViolation
    data object HasSpecialChar : NameViolation
}