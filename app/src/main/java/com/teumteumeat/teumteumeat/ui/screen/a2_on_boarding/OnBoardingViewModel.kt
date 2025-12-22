package com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class OnBoardingViewModel(
) : ViewModel() {

    companion object {
        private const val MIN_LENGTH = 1
        private const val MAX_LENGTH = 10
        private val ALLOWED_REGEX = Regex("^[가-힣a-zA-Z0-9]*$")
    }

    private val _uiState = MutableStateFlow<UiStateOnBoardingMain>(UiStateOnBoardingMain())
    val uiState = _uiState.asStateFlow()

    // Flow 값으로 currentPage 읽기
    private val currentPage get() = uiState.value.currentPage
    private val totalPage get() = uiState.value.totalPage



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
                    isValid = isValid,
                    errorMessage =  message,
                    violation = violation
                )
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
