package com.teumteumeat.teumteumeat.ui.screen.b2_1_quiz_result

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onesignal.common.DateUtils
import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model.uiMessage
import com.teumteumeat.teumteumeat.data.repository.document.DocumentRepository
import com.teumteumeat.teumteumeat.data.repository.quiz.QuizRepository
import com.teumteumeat.teumteumeat.ui.screen.b1_summary.UiStateSummary
import com.teumteumeat.teumteumeat.utils.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuizResultViewModel @Inject constructor(
    private val documentRepository: DocumentRepository,
    private val quizRepository: QuizRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiStateQuizResult())
    val uiState = _uiState.asStateFlow()

    fun loadQuizResults(
        type: String,
        id: Int,
        date: String
    ) {
        viewModelScope.launch {

            // 1️⃣ 로딩 시작
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = ""
                )
            }

            // 2️⃣ API 호출
            when (val result = quizRepository.getQuizHistory(type, id, date)) {

                is ApiResultV2.Success -> {
                    val history = result.data

                    val quizzes = history.quizzes
                    val correctCount = quizzes.count { it.isCorrect }

                    // 3️⃣ 성공 상태 반영
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            quizzes = quizzes,
                            createdAt = history.createdAt,
                            correctCount = correctCount
                        )
                    }
                }

                is ApiResultV2.ServerError -> {
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
                            errorMessage = result.message
                        )
                    }
                }

                is ApiResultV2.SessionExpired -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }

                is ApiResultV2.UnknownError -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = null
                )
            }
        }
    }

    /**
     * 오늘의 남남지식 요약 조회
     */
    fun loadDocumentSummary(goalId: Int, documentId: Int) {
        Log.d("TAG", "loadDocumentSummary: $goalId, $documentId")
        viewModelScope.launch {
            _uiState.update{
                it.copy(
                    isLoading = true,
                    errorMessage = "",
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
                    val data = result.data

                    _uiState.update {
                        it.copy(
                            summary = UiStateSummary(
                                isLoading = false,
                                title = data.fileName,
                                dateText = Utils.DateUtil.todayText(), // ⭐ 아래 유틸 참고
                                summary = data.summary,
                                hasSolvedToday = data.hasSolvedToday,
                                isFirstTime = data.isFirstTime,
                                errorMessage = null
                            )
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

}

