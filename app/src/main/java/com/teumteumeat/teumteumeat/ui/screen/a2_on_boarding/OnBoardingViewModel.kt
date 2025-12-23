package com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teumteumeat.teumteumeat.data.network.model.ApiResult
import com.teumteumeat.teumteumeat.domain.model.on_boarding.TimeState
import com.teumteumeat.teumteumeat.domain.usecase.on_boarding.UpdateCommuteTimeUseCase
import com.teumteumeat.teumteumeat.domain.usecase.on_boarding.RegisterUserNameUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnBoardingViewModel @Inject constructor(
    val updateCommuteTimeUseCase: UpdateCommuteTimeUseCase,
    val registerUserNameUseCase: RegisterUserNameUseCase
) : ViewModel() {

    companion object {
        private const val MIN_LENGTH = 1
        private const val MAX_LENGTH = 10
        private val ALLOWED_REGEX = Regex("^[가-힣a-zA-Z0-9]*$")
    }

    private val _uiState = MutableStateFlow<UiStateOnBoardingMain>(UiStateOnBoardingMain())
    val uiState = _uiState.asStateFlow()

    fun onMinuteSelected(minute: Int) {
        _uiState.update {
            it.copy(selectedMinute = minute)
        }
    }

    // Flow 값으로 currentPage 읽기
    private val currentPage get() = uiState.value.currentPage
    private val totalPage get() = uiState.value.totalPage

    fun updateCommuteTime() {
        viewModelScope.launch {
            val current = _uiState.value

            // 1️⃣ 로딩 시작
            _uiState.value = current.copy(
                isLoading = true,
                errorMessage = ""
            )

            val result = updateCommuteTimeUseCase(
                start = current.workInTime,
                end = current.workOutTime,
                usageTime = 10 // 필요 시 uiState 값으로 교체
            )

            // 2️⃣ 결과 처리
            when (result) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        isSetWorkInTime = true,
                        isSetWorkOutTime = true
                    )
                }

                is ApiResult.SessionExpired -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                    // 🔔 여기서 로그인 화면 이동 이벤트 트리거 가능
                }

                is ApiResult.NetworkError -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "네트워크 연결을 확인해주세요."
                    )
                }

                is ApiResult.ServerError -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                            ?: "요청 처리 중 오류가 발생했습니다."
                    )
                }

                else -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "알 수 없는 오류가 발생했습니다."
                    )
                }
            }
        }
    }



    /**
     * 🔹 화면 진입 시 호출
     * 🔹 권한 상태만 조회 (팝업 ❌)
     */
    fun syncNotificationPermission(isGranted: Boolean) {
        _uiState.update {
            it.copy(
                isNotificationGranted = isGranted,
                isNotificationChecked = isGranted
            )
        }
    }

    /**
     * 🔹 체크박스 / 버튼 클릭 시 호출
     */
    fun onNotificationOptionClicked() {
        val state = _uiState.value

        // 이미 허용됨 → 바로 체크
        if (state.isNotificationGranted) {
            _uiState.update {
                it.copy(isNotificationChecked = true)
            }
            return
        }

        // 아직 허용 안 됨 → 권한 요청 트리거
        _uiState.update {
            it.copy(requestNotificationPermission = true)
        }
    }

    /**
     * 🔹 OneSignal 권한 요청 결과 콜백
     */
    fun onNotificationPermissionResult(granted: Boolean) {
        _uiState.update {
            it.copy(
                isNotificationGranted = granted,
                isNotificationChecked = granted,
                requestNotificationPermission = false
            )
        }
    }

    /**
     * 🔹 이벤트 소비 (중복 호출 방지)
     */
    fun consumeNotificationPermissionRequest() {
        _uiState.update {
            it.copy(requestNotificationPermission = false)
        }
    }

    /* -----------------------------
     * 체크박스 토글
     * ----------------------------- */

    fun onAgreementCheckedChange(checked: Boolean) {
        _uiState.update {
            it.copy(isCheckedAgreement = checked)
        }
    }

    // 필요 시 토글 방식도 가능
    fun toggleAgreement() {
        _uiState.update {
            it.copy(isCheckedAgreement = !it.isCheckedAgreement)
        }
    }

    /* -----------------------------
     * BottomSheet 제어
     * ----------------------------- */

    fun openTimeSheet(type: TimeType) {
        _uiState.update {
            it.copy(
                showBottomSheet = true,
                currentTimeType = type
            )
        }
    }

    fun closeTimeSheet() {
        _uiState.update {
            it.copy(
                showBottomSheet = false,
                currentTimeType = TimeType.NOTTING
            )
        }
    }

    /* -----------------------------
     * 시간 변경
     * ----------------------------- */

    fun onTimeChanged(newTime: TimeState) {
        _uiState.update { state ->
            when (state.currentTimeType) {
                TimeType.OUT -> state.copy(
                    workOutTime = newTime,
                    isSetWorkOutTime = true
                )

                TimeType.IN -> state.copy(
                    workInTime = newTime,
                    isSetWorkInTime = true
                )

                TimeType.NOTTING -> state.copy()
            }
        }
    }

    /* -----------------------------
     * BottomSheet에 주입할 현재 시간
     * ----------------------------- */

    fun getCurrentTime(): TimeState {
        return when (uiState.value.currentTimeType) {
            TimeType.OUT -> uiState.value.workOutTime
            TimeType.IN -> uiState.value.workInTime
            TimeType.NOTTING -> TimeState.amTime()
        }
    }

    /* -----------------------------
     * BottomSheet 타이틀
     * ----------------------------- */

    fun getSheetTitle(): String {
        return when (uiState.value.currentTimeType) {
            TimeType.OUT -> "집을 나오는 시간"
            TimeType.IN -> "집을 들어가는 시간"
            TimeType.NOTTING -> "시간"
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

            val isValid = violation == NameViolation.None && trimmedToMax.length in MIN_LENGTH..MAX_LENGTH

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
                    errorMessage =  message,
                    violation = violation
                )
            }
        }
    }

    fun onConfirmClick() {
        val state = _uiState.value
        if (!state.isNameValid || state.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            when (val result =
                registerUserNameUseCase(state.charName)
            ) {

                is ApiResult.Success<*> -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSuccess = true,
                        )
                    }
                }

                is ApiResult.ServerError<*> -> {
                    /*val nameErrorMessage =
                        result.details
                            ?.toDomain()
                            ?.filter { it.field == "name" }
                            ?.joinToString("\n") { it.message }*/

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isNameValid = false,
                            errorMessage = result.message
                        )
                    }
                }

                is ApiResult.SessionExpired -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }

                is ApiResult.NetworkError -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }

                is ApiResult.UnknownError -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }



    fun nextPage() {
        if (currentPage < totalPage) {
            Log.d("1증가 전", "증가함, ${currentPage}/${totalPage}")
            viewModelScope.launch {
                _uiState.update { currentState ->
                    currentState.copy(
                        currentPage = currentPage + 1
                    )
                }
            }
            Log.d("1증가 후", "증가함, ${currentPage}/${totalPage}")
        }
    }

    fun prevPage() {
        if (currentPage > 0) {
            Log.d("1감소 전", "감소함, ${currentPage}/${totalPage}")
            viewModelScope.launch {
                _uiState.update { currentState ->
                    currentState.copy(
                        currentPage = currentPage - 1
                    )
                }
            }
            Log.d("1감소 후", "감소함, ${currentPage}/${totalPage}")
        }
    }
}
