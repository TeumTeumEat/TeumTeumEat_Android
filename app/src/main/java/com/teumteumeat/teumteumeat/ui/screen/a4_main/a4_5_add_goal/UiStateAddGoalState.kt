package com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_5_add_goal

import android.net.Uri
import com.teumteumeat.teumteumeat.domain.model.on_boarding.TimeState
import com.teumteumeat.teumteumeat.domain.model.common.GoalTypeUiState
import com.teumteumeat.teumteumeat.domain.model.goal.Difficulty
import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.BottomSheetType
import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.Category
import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.CategorySelectionState

sealed class UiStateAddGoalScreenState {

    /** 아무 일도 하지 않는 초기 상태 */
    data object Idle : UiStateAddGoalScreenState()

    /** 로딩 화면 표시 */
    data object Loading : UiStateAddGoalScreenState()

    /** 등록 성공 */
    data object Success : UiStateAddGoalScreenState()

    /** 오류 발생 */
    data class Error(
        val message: String
    ) : UiStateAddGoalScreenState()
}

data class UiStateAddGoalState(
//    val currentPage: Int = 0,
//    val totalPage: Int = 5,
    // 학습 방법 선택 여부
    val goalTypeUiState: GoalTypeUiState = GoalTypeUiState.NONE,

    val popoUpErrorTitle: String? = null,
    val popUpErrorMessage: String? = null,


    val errorMessage: String = "",
    // ⭐ 서버에 보낼 실제 categoryId
    val selectedCategoryId: Int? = null,

    val documentId: Int = 0,
    val goalId: Int = 0,
    val currentPage: Int = 1,
    val totalPage: Int = 5,

    val isSetWorkInTime: Boolean = false,
    val isSetWorkOutTime: Boolean = false,

    // 🔹 출/퇴근 시간 설정
    val workInTime: TimeState = TimeState.amTime(),  // 집에서 나오는 시간
    val workOutTime: TimeState = TimeState.pmTime(), // 집으로 가는 시간

    // ✅ 바텀시트에서 조작 중인 임시 시간
    val tempTime: TimeState = TimeState.amTime(),

    // 🔹 BottomSheet 상태
    val showBottomSheet: Boolean = false,

    /**
     * 앱 이용시간
     */
    val selectedMinute: Int? = null,

    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,

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
    val isFileUploadComplete: Boolean = false,
    val isCategorySelectionComplete: Boolean = false,

    val isSkipTypeSelect: Boolean = false,
){
/*    val currentPage: Int
        get() = AddGoalFlow.currentPage(currentScreen, goalTypeUiState)

    val totalPage: Int
        get() = AddGoalFlow.totalCount(goalTypeUiState)*/

    val canGoBack: Boolean
        get() = currentPage > 1
}
