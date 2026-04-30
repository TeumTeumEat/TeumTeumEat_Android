package com.teumteumeat.teumteumeat.ui.screen.c3_edit_user_info

import com.teumteumeat.teumteumeat.domain.model.on_boarding.TimeState
import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.NameViolation


data class UiStateEditUserInfo(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,

    /** 입력 값 */
    val charName: String = "",
    // 이름 설정
    val originalCharName: String = "",
    val nameErrorMessage: String = "", // 이름 수정시 발생할 에러 표시
    val isNameValid: Boolean = false,
    val violation: NameViolation = NameViolation.None,

    val isSetWorkInTime: Boolean = false,
    val isSetWorkOutTime: Boolean = false,

    // 🔹 출/퇴근 시간 설정
    val tempTime: TimeState = TimeState.amTime(), // 임시 시간
    val workInTime: TimeState = TimeState.amTime(),  // 집에서 나오는 시간
    val workOutTime: TimeState = TimeState.pmTime(), // 집으로 가는 시간

    val originalWorkInTime: TimeState = TimeState.amTime(),  // 집에서 나오는 시간
    val originalWorkOutTime: TimeState = TimeState.pmTime(), // 집으로 가는 시간

    // 🔹 BottomSheet 상태
    val showBottomSheet: Boolean = false,
    val currentBottomSheetType: BottomSheetType? = BottomSheetType.WorkInTime,


    // 틈틈잇 사용 시간 (분)
    val tempUseMinutes: Int = 0,
    val useMinutes: Int = 5,
    val originalUseMinutes: Int = 0,

    /** UI 제어 */
    val isSaveEnabled: Boolean = false,
    val isChanged: Boolean = false,
    val isShowSaveDialog: Boolean = false,
)

enum class BottomSheetType {
    WorkInTime, WorkOutTime, UsingTime
}

