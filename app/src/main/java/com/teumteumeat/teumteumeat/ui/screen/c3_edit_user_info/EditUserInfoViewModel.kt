package com.teumteumeat.teumteumeat.ui.screen.c3_edit_user_info

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model.DomainError
import com.teumteumeat.teumteumeat.data.network.model.uiMessage
import com.teumteumeat.teumteumeat.data.network.model_response.GetGoalResponse
import com.teumteumeat.teumteumeat.data.network.model_response.GoalsData
import com.teumteumeat.teumteumeat.data.repository.document.DocumentRepository
import com.teumteumeat.teumteumeat.data.repository.user.UserRepository
import com.teumteumeat.teumteumeat.domain.model.on_boarding.TimeState
import com.teumteumeat.teumteumeat.domain.model.on_boarding.toServerTime
import com.teumteumeat.teumteumeat.domain.usecase.GetGoalListUseCase
import com.teumteumeat.teumteumeat.domain.usecase.on_boarding.RegisterUserNameUseCase
import com.teumteumeat.teumteumeat.domain.usecase.on_boarding.UpdateCommuteTimeUseCase
import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.NameViolation
import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.enum_type.Difficulty
import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.enum_type.GoalType
import com.teumteumeat.teumteumeat.ui.screen.b1_summary.UiStateSummary
import com.teumteumeat.teumteumeat.ui.screen.c1_mypage.UiStateMyPage
import com.teumteumeat.teumteumeat.ui.screen.c2_goal_list.UiStateGoalList
import com.teumteumeat.teumteumeat.ui.screen.c2_goal_list.toUiModel
import com.teumteumeat.teumteumeat.utils.Utils.InfoUtil.getAppVersion
import com.teumteumeat.teumteumeat.utils.Utils.PrefsUtil
import com.teumteumeat.teumteumeat.utils.Utils.TimeUtil.fromServerTime
import com.teumteumeat.teumteumeat.utils.Utils.UiUtils.to24Hour
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditUserInfoViewModel @Inject constructor(
    application: Application,
    private val documentRepository: DocumentRepository,
    private val getGoalListUseCase: GetGoalListUseCase,
    private val userRepository: UserRepository,
    private val registerUserNameUseCase: RegisterUserNameUseCase,
    private val updateCommuteTimeUseCase: UpdateCommuteTimeUseCase,
) : ViewModel() {
    companion object {
        private const val MIN_LENGTH = 1
        private const val MAX_LENGTH = 10
        private val ALLOWED_REGEX = Regex("^[가-힣a-zA-Z0-9]*$")
    }

    private val _uiState = MutableStateFlow(UiStateEditUserInfo())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            loadCommuteInfo()
            loadUserInfo()
        }
    }

    internal suspend fun loadUserInfo() {
        viewModelScope.launch {

            // 1️⃣ 로딩 시작
            _uiState.update {
                it.copy(isLoading = true, errorMessage = null)
            }
            // 2️⃣ 이름 조회 처리
            fetchUserName()
            // end. 로딩 끝
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = null,
                )
            }
        }
    }

    fun saveUserInfo(){
        viewModelScope.launch{ // 1️⃣ 이름 등록
            val nameResult = setUserNameInternal()
            if (nameResult !is ApiResultV2.Success) {
                return@launch
            }


            // 2️⃣ 출퇴근 정보 저장
            val commuteResult = saveCommuteInfoInternal()
            if (commuteResult !is ApiResultV2.Success) {
                return@launch
            }
        }
    }

    private suspend fun saveCommuteInfoInternal(): ApiResultV2<Unit> {
        val current = _uiState.value

        val usageTime = current.useMinutes

        return updateCommuteTimeUseCase(
            startTime = current.workInTime.toServerTime(),
            endTime = current.workOutTime.toServerTime(),
            usageTime = usageTime
        )
    }

    private suspend fun setUserNameInternal(): ApiResultV2<Any> {
        val state = _uiState.value

        if (!state.isNameValid) {
            return ApiResultV2.UnknownError(
                message = "이름이 올바르지 않습니다."
            )
        }

        return when (val result = registerUserNameUseCase(state.charName)) {

            is ApiResultV2.Success -> {
                _uiState.update {
                    it.copy(
                        isNameValid = true,
                        errorMessage = ""
                    )
                }
                ApiResultV2.Success(result.message, Unit)
            }

            is ApiResultV2.ServerError -> {
                val errorMessage = when (val error = result.errorType) {
                    is DomainError.Validation -> {
                        error.errors.find { it.field == "name" }?.message
                            ?: result.uiMessage
                    }
                    else -> result.uiMessage
                }

                _uiState.update {
                    it.copy(
                        isNameValid = false,
                        errorMessage = errorMessage
                    )
                }

                result
            }

            else -> {
                _uiState.update {
                    it.copy(
                        isNameValid = false,
                        errorMessage = result.uiMessage
                    )
                }
                result
            }
        }
    }

    /**
     * 🔹 출퇴근 정보 조회
     */
    internal suspend fun loadCommuteInfo() {
        viewModelScope.launch {

            // 1️⃣ 로딩 시작
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            // 2️⃣ API 호출
            when (val result = userRepository.getCommuteInfo()) {

                is ApiResultV2.Success -> {
                    val data = result.data

                    // ⏰ 서버 시간 → TimeState 변환
                    val workIn = TimeState.fromServerTime(data.startTime)
                    val workOut = TimeState.fromServerTime(data.endTime)
                    Log.d(
                        "EditUserInfoViewModel",
                        "workInTime: ${workIn}, workWoutTime: ${workOut}"
                    )
                    _uiState.update { prev ->
                        val next = prev.copy(
                            isLoading = false,
                            workInTime = workIn,
                            workOutTime = workOut,
                            useMinutes = data.usageTime,
                            tempUseMinutes = data.usageTime,
                            isSetWorkInTime = true,
                            isSetWorkOutTime = true,
                            isChanged = false
                        )

                        Log.d(
                            "UI_STATE_UPDATE",
                            """
                            🔄 loadCommuteInfo update
                            prev.workInTime=${prev.workInTime}
                            next.workInTime=${next.workInTime}
                            prev.workOutTime=${prev.workOutTime}
                            next.workOutTime=${next.workOutTime}
                            prev.useMinutes=${prev.useMinutes}
                            next.useMinutes=${next.useMinutes}
                            """.trimIndent()
                        )

                        next
                    }
                }

                else -> {
                    // 3️⃣ 에러 처리 (공통 uiMessage 사용)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.uiMessage
                        )
                    }
                }
            }
        }
    }

    fun openBottomSheet(type: BottomSheetType) {
        _uiState.update { state ->
            when (type) {

                BottomSheetType.WorkInTime -> state.copy(
                    showBottomSheet = true,
                    currentBottomSheetType = type,
                    tempTime = state.workInTime,
                    tempUseMinutes = 0
                )

                BottomSheetType.WorkOutTime -> state.copy(
                    showBottomSheet = true,
                    currentBottomSheetType = type,
                    tempTime = state.workOutTime,
                    tempUseMinutes = 0
                )

                BottomSheetType.UsingTime -> state.copy(
                    showBottomSheet = true,
                    currentBottomSheetType = type,
                    tempUseMinutes = state.useMinutes,
                    tempTime = TimeState.amTime()
                )
            }
        }
    }

    fun confirmBottomSheet() {
        _uiState.update { state ->

            Log.d(
                "TIME_SAVE_CHECK",
                """
                🧪 Save TimeState
                hour=${state.workInTime.hour}
                minute=${state.workInTime.minute}
                amPm=${state.workInTime.amPm}
                serverTime=${state.workInTime.toServerTime()}
                """.trimIndent()
            )

            when (state.currentBottomSheetType) {

                BottomSheetType.WorkInTime -> {
                    val (hour24, minute) = state.tempTime.to24Hour()
                    state.copy(
                        workInTime = state.tempTime,
                        isChanged = true,
                        showBottomSheet = false,
                        tempTime = TimeState.amTime()
                    )
                }

                BottomSheetType.WorkOutTime -> {
                    val (hour24, minute) = state.tempTime.to24Hour()
                    state.copy(
                        workOutTime = state.tempTime,
                        isChanged = true,
                        showBottomSheet = false,
                        tempTime = TimeState.amTime()
                    )
                }

                BottomSheetType.UsingTime -> state.copy(
                    useMinutes = state.tempUseMinutes,
                    isChanged = true,
                    showBottomSheet = false,
                    tempUseMinutes = 0
                )

                null -> state
            }
        }
    }

    fun updateTempTime(time: TimeState) {
        _uiState.update {
            it.copy(tempTime = time)
        }
    }

    fun updateTempUseMinutes(minutes: Int) {
        _uiState.update {
            it.copy(tempUseMinutes = minutes)
        }
    }

    fun closeBottomSheet() {
        _uiState.update {
            it.copy(
                showBottomSheet = false,
                currentBottomSheetType = null,
                tempTime = TimeState.amTime(),
                tempUseMinutes = 0
            )
        }
    }

    private suspend fun fetchUserName() {
        when (val result = userRepository.getUserName()) {

            is ApiResultV2.Success -> {
                val name = result.data.name

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        charName = name,
                        isNameValid = name.length in MIN_LENGTH..MAX_LENGTH,
                        nameErrorMessage = "",
                        violation = NameViolation.None
                    )
                }
            }

            else -> {
                // 🔴 Success 외 모든 케이스 공통 처리
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.uiMessage
                    )
                }
            }
        }
    }


    fun onNameTextChanged(input: String) {
        viewModelScope.launch {
            // ✅ 입력은 최대 10자까지만 "받는다"(저장)
            val trimmedToMax = if (input.length > MAX_LENGTH) input.take(MAX_LENGTH) else input

            // ✅ 유효성은 별도로 판단 (입력은 되지만 invalid 가능)
            val violation = when {
                trimmedToMax.isEmpty() -> NameViolation.Empty
                trimmedToMax.length < MIN_LENGTH -> NameViolation.Empty // 사실상 동일
                trimmedToMax.contains(" ") -> NameViolation.HasSpace
                !trimmedToMax.matches(ALLOWED_REGEX) -> NameViolation.HasSpecialChar
                else -> NameViolation.None
            }

            val isValid =
                violation == NameViolation.None && trimmedToMax.length in MIN_LENGTH..MAX_LENGTH

            val message = when (violation) {
                NameViolation.None -> ""
                NameViolation.Empty -> "1자 이상 입력해주세요"
                NameViolation.HasSpace -> "공백은 사용할 수 없어요"
                NameViolation.HasSpecialChar -> "특수문자는 사용할 수 없어요 (한글/영문/숫자만)"
                NameViolation.TooLong -> "10자 이하로 입력해주세요" // 현재 take(MAX)라 실제로는 잘 안 옴
            }

            _uiState.update {
                it.copy(
                    charName = trimmedToMax,
                    isNameValid = isValid,
                    nameErrorMessage = message,
                    violation = violation
                )
            }
        }
    }
}
