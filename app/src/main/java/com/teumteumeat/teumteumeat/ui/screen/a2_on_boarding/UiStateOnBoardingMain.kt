package com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding

import android.net.Uri
import com.teumteumeat.teumteumeat.domain.model.on_boarding.TimeState
import com.teumteumeat.teumteumeat.ui.component.AmPm
import com.teumteumeat.teumteumeat.domain.model.common.GoalTypeUiState
import com.teumteumeat.teumteumeat.domain.model.goal.Difficulty

sealed class UiStateOnBoardingMainState {

    /** 아무 일도 하지 않는 초기 상태 */
    data object Idle : UiStateOnBoardingMainState()

    /** 로딩 화면 표시 */
    data object Loading : UiStateOnBoardingMainState()

    /** 등록 성공 */
    data object Success : UiStateOnBoardingMainState()

    /** 오류 발생 */
    data class Error(
        val message: String
    ) : UiStateOnBoardingMainState()
}

data class UiStateOnBoardingMain(
//    val currentPage: Int = 0,
//    val totalPage: Int = 5,

    // ⭐ 서버에 보낼 실제 categoryId
    val selectedCategoryId: Int? = null,

    val documentId: Int = 0,
    val goalId: Int = 0,
    val currentScreen: OnBoardingScreens = OnBoardingScreens.FirstScreen,

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
    val goalTypeUiState: GoalTypeUiState = GoalTypeUiState.NONE,

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


    // 온보딩 응답 요청 별 에러 메시지
    val pageErrorMessage: String? = null,
    val isSessionExpired: Boolean = false,

    val difficulty: Difficulty = Difficulty.NONE,

    val bottomSheetType: BottomSheetType = BottomSheetType.NONE,

    val isPromptVaild: Boolean = true,
    val promptInput: String = "",
    val promptInputErrMsg: String? = null,

    val studyPeriod: Int? = null,
    val endDate: String = "",
){
    val currentPage: Int
        get() = OnBoardingFlow.currentPage(currentScreen)

    val totalPage: Int
        get() = OnBoardingFlow.totalCount

    val canGoBack: Boolean
        get() = currentPage > 1
}

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
    val depth1: Category? = null, // 내부 상태로만 유지
    val depth2: Category? = null,
    val depth3: Category? = null,
    val depth4: Category? = null, // ⭐ 진짜 leaf
) {

    /** Pager에서 사용할 현재 페이지 (온보딩 기준) */
    val currentPage: Int
        get() = when {
            depth4 != null -> 2
            depth3 != null -> 1
            else -> 0
        }

    /** 총 페이지 수 (온보딩은 최대 3페이지만) */
    val totalPage: Int
        get() = 3
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
    val isAm = amPm == AmPm.AM

    val amPmText = if (isAm) "오전" else "오후"

    val displayHour = when {
        hour == 0 || hour == 12 -> 12
        hour in 1..12 -> hour
        else -> hour - 12
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