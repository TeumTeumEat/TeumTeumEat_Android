package com.teumteumeat.teumteumeat.ui.screen.c3_edit_user_info

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model.DomainError
import com.teumteumeat.teumteumeat.data.network.model.uiMessage
import com.teumteumeat.teumteumeat.data.repository.user.UserRepository
import com.teumteumeat.teumteumeat.domain.model.on_boarding.TimeState
import com.teumteumeat.teumteumeat.domain.model.on_boarding.toServerTime
import com.teumteumeat.teumteumeat.domain.usecase.SessionManager
import com.teumteumeat.teumteumeat.domain.usecase.on_boarding.RegisterUserNameUseCase
import com.teumteumeat.teumteumeat.domain.usecase.on_boarding.UpdateCommuteTimeUseCase
import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.NameViolation
import com.teumteumeat.teumteumeat.utils.Utils.TimeUtil.fromServerTime
import com.teumteumeat.teumteumeat.utils.Utils.UiUtils.to24Hour
import com.teumteumeat.teumteumeat.utils.firebase.TeumAnalyticsLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditUserInfoViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val registerUserNameUseCase: RegisterUserNameUseCase,
    private val updateCommuteTimeUseCase: UpdateCommuteTimeUseCase,
    val sessionManager: SessionManager,
    private val analyticsLogger: TeumAnalyticsLogger,
) : ViewModel() {
    companion object {
        private const val MIN_LENGTH = 1
        private const val MAX_LENGTH = 10
        private val ALLOWED_REGEX = Regex("^[가-힣a-zA-Z0-9 ]*$")
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

    fun checkUnsavedChanges(){
        viewModelScope.launch{ // 1️⃣ 이름 등록
            val currentUiState = _uiState.value
            // 현재 변경된 정보가 업다면 저장 확인 팝업창 안 띄움
            currentUiState.apply {
                if(originalCharName == charName &&
                    originalWorkInTime == workInTime &&
                    originalWorkOutTime == workOutTime &&
                    originalUseMinutes == useMinutes
                ){
                    return@launch
                }else{
                    // 저장 확인 팝업창 띄우기
                    _uiState.update {
                        it.copy(
                            isShowSaveDialog = true
                        )
                    }
                }
            }
        }
    }

    fun dismissConfirmationDialog(){
        _uiState.update {
            it.copy(
                isShowSaveDialog = false
            )
        }
    }

    fun saveUserInfo(){
        // 저장 클릭 직후 Activity가 finish되어 viewModelScope가 취소될 수 있으므로
        // 코루틴 진입 전에 동기적으로 발화한다 (settings_change)
        logSettingsChangeIfChanged()

        viewModelScope.launch {
            val nameResult = setUserNameInternal()
            if (nameResult !is ApiResultV2.Success) {
                return@launch
            }

            // 2️⃣ 출퇴근 정보 및 이용 시간 저장
            val commuteResult = saveCommuteInfoInternal()
            if (commuteResult !is ApiResultV2.Success) {
                moveToError(commuteResult)
                return@launch
            }
        }
    }

    /**
     * 원본(original*) 대비 실제 변경된 설정 항목만 settings_change 이벤트로 발화합니다.
     * 변경된 항목이 없으면 발화하지 않습니다.
     */
    private fun logSettingsChangeIfChanged() {
        val state = _uiState.value

        val nicknameChanged = state.charName != state.originalCharName
        // TimeState 전체 비교는 isSelected 플래그 차이로 오판할 수 있어 수집 표현("HH:mm")끼리 비교한다
        val commuteFrom = "${state.originalWorkInTime.toHHmm()}-${state.originalWorkOutTime.toHHmm()}"
        val commuteTo = "${state.workInTime.toHHmm()}-${state.workOutTime.toHHmm()}"
        val commuteChanged = commuteFrom != commuteTo
        val quizChanged = state.useMinutes != state.originalUseMinutes

        analyticsLogger.logSettingsChange(
            nicknameChanged = nicknameChanged,
            commuteTimeFrom = if (commuteChanged) commuteFrom else null,
            commuteTimeTo = if (commuteChanged) commuteTo else null,
            quizCountFrom = if (quizChanged) state.originalUseMinutes else null,
            quizCountTo = if (quizChanged) state.useMinutes else null,
        )
    }

    // Analytics용 "HH:mm" 표기 — toServerTime()의 "HH:mm:00"에서 초 제거
    private fun TimeState.toHHmm(): String = toServerTime().take(5)


    // 서버 전송용: 문제 수(3,5,7,10) → 분(5,7,10,15)
    private fun questionCntToMinutes(cnt: Int): Int = when (cnt) {
        3 -> 5
        5 -> 7
        7 -> 10
        10 -> 15
        else -> cnt
    }

    // 서버 응답용: 분(5,7,10,15) → 문제 수(3,5,7,10)
    private fun minutesToQuestionCnt(minutes: Int): Int = when (minutes) {
        5 -> 3
        7 -> 5
        10 -> 7
        15 -> 10
        else -> minutes
    }

    private suspend fun saveCommuteInfoInternal(): ApiResultV2<Unit> {
        val current = _uiState.value

        val usageTime = questionCntToMinutes(current.useMinutes)

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

        return when (val result = registerUserNameUseCase(state.charName.trim())) {

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
                moveToError(result)
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
                    val questionCnt = minutesToQuestionCnt(data.usageTime)
                    _uiState.update { prev ->
                        val next = prev.copy(
                            isLoading = false,
                            workInTime = workIn,
                            workOutTime = workOut,
                            originalWorkInTime = workIn,
                            originalWorkOutTime = workOut,
                            useMinutes = questionCnt,
                            originalUseMinutes = questionCnt,
                            tempUseMinutes = questionCnt,
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
                    moveToError(result)
                }
            }
        }
    }

    private suspend fun moveToError(result: ApiResultV2<*>) {
        when (result) {
            is ApiResultV2.SessionExpired -> {
                sessionManager.expireSession()
            }

            is ApiResultV2.NetworkError -> {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.uiMessage
                    )
                }
            }

            is ApiResultV2.ServerError -> {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.uiMessage
                    )
                }
            }

            else -> {

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "알 수 없는 오류가 발생했습니다."
                    )
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
                    tempTime = TimeState.firstTime()
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
                        tempTime = TimeState.firstTime()
                    )
                }

                BottomSheetType.WorkOutTime -> {
                    val (hour24, minute) = state.tempTime.to24Hour()
                    state.copy(
                        workOutTime = state.tempTime,
                        isChanged = true,
                        showBottomSheet = false,
                        tempTime = TimeState.firstTime()
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
                tempTime = TimeState.firstTime(),
                tempUseMinutes = 0
            )
        }
    }

    private suspend fun fetchUserName() {
        when (val result = userRepository.getUserName()) {

            is ApiResultV2.Success -> {
                val name = result.data.name
                val trimmedName = name.trim()
                val isValid = trimmedName.length in MIN_LENGTH..MAX_LENGTH &&
                              trimmedName.matches(ALLOWED_REGEX)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        originalCharName = name,
                        charName = name,
                        isNameValid = isValid,
                        nameErrorMessage = if (isValid) "" else "한글, 영문, 숫자만 입력해주세요",
                        violation = if (isValid) NameViolation.None else NameViolation.HasSpecialChar
                    )
                }
            }

            else -> {
                moveToError(result)
            }
        }
    }


    fun onNameTextChanged(input: String) {
        viewModelScope.launch {
            val truncated = if (input.length > MAX_LENGTH) input.take(MAX_LENGTH) else input

            // 앞뒤 공백 제거 후 유효성 판단
            val trimmed = truncated.trim()
            val violation = when {
                trimmed.isEmpty() -> NameViolation.Empty
                !trimmed.matches(ALLOWED_REGEX) -> NameViolation.HasSpecialChar
                else -> NameViolation.None
            }

            val isValid = violation == NameViolation.None && trimmed.length in MIN_LENGTH..MAX_LENGTH

            val message = when (violation) {
                NameViolation.None -> ""
                NameViolation.Empty -> "1자 이상 입력해주세요"
                NameViolation.HasSpecialChar -> "한글, 영문, 숫자만 입력해주세요"
                NameViolation.HasSpace -> ""
                NameViolation.TooLong -> ""
            }

            _uiState.update {
                it.copy(
                    charName = truncated,
                    isNameValid = isValid,
                    nameErrorMessage = message,
                    violation = violation
                )
            }
        }
    }
}
