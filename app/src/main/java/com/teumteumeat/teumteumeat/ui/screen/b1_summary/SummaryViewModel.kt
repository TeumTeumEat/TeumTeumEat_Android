package com.teumteumeat.teumteumeat.ui.screen.b1_summary

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model.uiMessage
import com.teumteumeat.teumteumeat.data.repository.category.CategoryRepository
import com.teumteumeat.teumteumeat.data.repository.document.DocumentRepository
import com.teumteumeat.teumteumeat.ui.screen.common_screen.UiScreenState
import com.teumteumeat.teumteumeat.utils.Utils
import com.teumteumeat.teumteumeat.utils.Utils.TimeUtil.toMonthDay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@dagger.hilt.android.lifecycle.HiltViewModel
class SummaryViewModel @Inject constructor(
    private val documentRepository: DocumentRepository,
    private val categoryRepository: CategoryRepository,
    val application: Application,
) : ViewModel() {
    private val appContext = application.applicationContext

    private val _uiState = MutableStateFlow(UiStateSummary())
    val uiState = _uiState.asStateFlow()

    private val _screenState =
        MutableStateFlow<UiScreenState>(UiScreenState.Idle)
    val screenState = _screenState.asStateFlow()

    /**
     * 오늘의 남남지식 요약 조회
     */
    fun loadDocumentSummary(goalId: Int, documentId: Int) {
        viewModelScope.launch {
            _uiState.update{
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                )
            }

            if (goalId == -1 || documentId == -1){
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "goalId 나 documentId 가 등록되지 않았습니다."
                    )
                }
            }

            when (val result = documentRepository.getDocumentSummary(goalId, documentId)) {

                is ApiResultV2.Success -> {
                    val summary = result.data

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            title = summary.fileName,          // 화면 타이틀용
                            summary = summary.summary,          // 요약 본문
                            hasSolvedToday = summary.hasSolvedToday,
                            isFirstTime = summary.isFirstTime,
                            errorMessage = null
                        )
                    }
                }

                else -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.uiMessage
                        )
                    }
                }
                /*is ApiResultV2.ServerError -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }

                is ApiResultV2.NetworkError -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "네트워크 연결을 확인해주세요."
                        )
                    }
                }

                is ApiResultV2.SessionExpired -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "로그인이 만료되었습니다. 다시 로그인해주세요."
                        )
                    }
                    // 👉 여기서 로그아웃 이벤트 트리거 가능
                }

                is ApiResultV2.UnknownError -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "알 수 없는 오류가 발생했습니다."
                        )
                    }
                }*/
            }
        }
    }

    fun resetIdleState() {
        _screenState.value = UiScreenState.Idle
    }

    fun loadCategorySummary(categoryId: Int) {
        viewModelScope.launch {

            // 🔵 로딩 시작
            _screenState.value = UiScreenState.Loading
            _uiState.update{
                it.copy(
                    categoryId = categoryId,
                    isLoading = true,
                    errorMessage = null,
                )
            }

            if (categoryId == -1){
                _uiState.update {
                    _screenState.value =
                        UiScreenState.Error("categoryId 가 등록되지 않았습니다.")
                    it.copy(
                        isLoading = false,
                        errorMessage = "categoryId 가 등록되지 않았습니다."
                    )
                }
            }

            // 2️⃣ 레포 호출
            when (
                val result =
                    categoryRepository.getDailyCategoryDocument(categoryId.toLong())
            ) {

                is ApiResultV2.Success -> {
                    val data = result.data

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            summary = data.content,
                            hasSolvedToday = data.hasSolvedToday,
                            isFirstTime = data.isFirstTime,
                            dateText = toMonthDay(data.createdAt),
                            errorMessage = null,
                            categoryDocumentId = data.documentId.toInt(),
                        )
                    }

                    Utils.PrefsUtil.saveDocumentId(appContext, data.documentId.toInt())
                    _screenState.value = UiScreenState.Success
                }

                is ApiResultV2.ServerError,
                is ApiResultV2.NetworkError,
                is ApiResultV2.SessionExpired,
                is ApiResultV2.UnknownError -> {
                    val message = result.uiMessage

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = message
                        )
                    }

                    _screenState.value =
                        UiScreenState.Error(message)
                }
            }
        }
    }

}
