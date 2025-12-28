package com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding

import android.net.Uri
import com.teumteumeat.teumteumeat.domain.model.on_boarding.TimeState
import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.enum_type.Difficulty
import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.enum_type.GoalType

data class UiStateOnBoardingMain(
    val currentPage: Int = 0,
    val totalPage: Int = 5,

    // 이름 설정
    val charName: String = "",
    val errorMessage: String = "",
    val isNameValid: Boolean = false,
    val violation: NameViolation = NameViolation.None,

    val isSetWorkInTime: Boolean = false,
    val isSetWorkOutTime: Boolean = false,

    // 🔹 출/퇴근 시간 설정
    val workInTime: TimeState = TimeState.amTime(),  // 집에서 나오는 시간
    val workOutTime: TimeState = TimeState.pmTime(), // 집으로 가는 시간

    // ✅ 바텀시트에서 조작 중인 임시 시간
    val tempTime: TimeState = TimeState.amTime(),

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

    // 🔔 권한 해제 안내 트리거
    val showNotificationDisableGuide: Boolean = false,

    // 🔹 한 번 거부 후 재시도 상태
    val showNotificationSettingGuide: Boolean = false,

    // 설정 안내 팝업 타입
    val notificationGuideType: NotificationSettingGuideType = NotificationSettingGuideType.NONE,

    /**
     * 앱 이용시간
     */
    val selectedMinute: Int? = null,

    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,

    // 학습 방법 선택 여부
    val goalType: GoalType = GoalType.NONE,

    // pdf 학습시 필요한 자료
    val selectedFileUri: Uri? = null,
    val selectedFileName: String = "",
    val selectedFileMimeType: String = "",
    val selectedFileSize: Long = 0L,

    val presignedUrl: String? = null,
    val fileKey: String? = null,

    // 카테고리 명 리스트
    val categories: List<Category> = emptyList(),

    // ✅ 카테고리 선택 상태 (추가)
    val categorySelection: CategorySelectionState = CategorySelectionState(),

    // 🔹 Pager 제어용 (UI가 이 값을 observe)
    val targetCategoryPage: Int = 0,

    // ⭐ 서버에 보낼 실제 categoryId
    val selectedCategoryId: Int? = null,

    // 온보딩 응답 요청 별 에러 메시지
    val pageErrorMessage: String? = null,
    val isSessionExpired: Boolean = false,

    val difficulty: Difficulty = Difficulty.NONE,

    val bottomSheetType: BottomSheetType = BottomSheetType.NONE,

    val isPromptVaild: Boolean = true,
    val promptInput: String = "",
    val promptInputErrMsg: String? = null,

    val selectedStudyWeek: Int? = null,
    val endDate: String = "",
)

enum class NotificationSettingGuideType {
    NONE,
    ENABLE,  // 🔔 알림 켜기(권한 없음 → 설정으로 유도)
    DISABLE  // 🔕 알림 끄기(권한 있음 → 설정으로 유도)
}

data class DifficultyOption(
    val label: String, // 화면 표시용
    val value: Difficulty     // 서버/로직용
)

sealed interface PromptViolation {
    data object None : PromptViolation
    data object Empty : PromptViolation
    data object TooShort : PromptViolation
    data object TooLong : PromptViolation
}

sealed interface BottomSheetType {
    data object NONE : BottomSheetType
    data object DIFFICULTY : BottomSheetType
    data object TIME : BottomSheetType
}

data class CategorySelectionState(
    val depth1: Category? = null,
    val depth2: Category? = null,
    val depth3: Category? = null
) {
    /** 현재 선택된 가장 깊은 depth → Pager의 currentPage */
    val currentPage: Int
        get() = when {
            depth3 != null -> 2
            depth2 != null -> 1
            depth1 != null -> 0
            else -> 0
        }

    /** 총 보여줘야 할 페이지 수 */
    val totalPage: Int
        get() = when {
            depth1 == null -> 1
            depth2 == null -> 2
            depth3 == null -> 3
            else -> 3
        }

}

data class StudyWeekOption(
    val label: String, // 화면 표시용 ("1주")
    val value: Int     // 실제 의미 값 (1)
)

data class MutableCategory(
    val id: String,
    val name: String,
    val serverCategoryId: Int? = null,
    val children: MutableMap<String, MutableCategory> = mutableMapOf()
) {
    fun toImmutable(): Category =
        Category(
            id = id,
            name = name,
            serverCategoryId = serverCategoryId,
            children = children.values.map { it.toImmutable() }
        )
}

data class Category(
    val id: String,
    val name: String,
    val serverCategoryId: Int? = null, // ⭐ 서버용 ID (leaf만 가짐)
    val children: List<Category> = emptyList()
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