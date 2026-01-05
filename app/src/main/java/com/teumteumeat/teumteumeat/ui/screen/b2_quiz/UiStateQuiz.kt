package com.teumteumeat.teumteumeat.ui.screen.b2_quiz

import com.teumteumeat.teumteumeat.data.network.model_response.UserQuiz
import com.teumteumeat.teumteumeat.ui.screen.common_screen.ErrorState

data class UiStateQuiz(
    val isLoading: Boolean = false,

    // 서버에서 받은 전체 퀴즈 (메모리에 유지)
    val quizzes: List<QuizCardUiState> = emptyList(),

    // 현재 화면에 보여줄 퀴즈 index
    val currentIndex: Int = 0,

    val errorMessage: String? = null,

    val isCompleted: Boolean = false,
    
    val errorState: ErrorState = ErrorState(
        title = "",
        description = "",
        retryLabel = "",
        onRetry = { }
    )

) {
    val currentQuiz: QuizCardUiState?
        get() = quizzes.getOrNull(currentIndex)

    val currentStep: Int
        get() = currentIndex + 1 // ⭐ UI용 (1-based)

    val totalSteps: Int
        get() = quizzes.size
}

data class SubmitQuizResult(
    val isCorrect: Boolean,
    val correctAnswer: String,
    val explanation: String
)

data class QuizCardUiState(
    val quizId: Int,
    val question: String,
    val options: List<String>,
    val type: QuizType,

    // 사용자 선택
    val selectedAnswer: String? = null,

    // 제출 결과
    val isSubmitted: Boolean = false,
    val isCorrect: Boolean? = null,

    // 카드 단위 로딩
    val isSubmitting: Boolean = false
)

fun UserQuiz.toUiState(): QuizCardUiState {
    return QuizCardUiState(
        quizId = quizId,
        question = question,
        options = options,
        type = type
    )
}

/*
data class QuizItemUiState(
    val quizId: Long,
    val question: String,
    val options: List<String>,
    val answer: String,
    val type: QuizType,
    val explanation: String,

    // 🔽 사용자 입력
    val selectedAnswer: String? = null,
    val isAnswered: Boolean = false,

    // 🔽 서버 제출 결과
    val isCorrect: Boolean? = null,
    val serverCorrectAnswer: String? = null,
    val*//* serverExplanation: String? = null
)*/
/*
data class Quiz(
    val id: Long,
    val question: String,
    val options: List<String>,
    val correctAnswer: String,
    val type: QuizType,
    val explanation: String
)

fun Quiz.toUiState(): QuizItemUiState {
    return QuizItemUiState(
        quizId = id,
        question = question,
        options = options,
        answer = correctAnswer,
        type = type,
        explanation = explanation,

        // UI 초기 상태
        selectedAnswer = null,
        isAnswered = false,
        isCorrect = null,
        serverCorrectAnswer = null,
        serverExplanation = null
    )
}*/

enum class QuizType {
    OX,
    MCQ;
    companion object {
        fun from(value: String): QuizType {
            return when {
                value.contains("OX", ignoreCase = true) -> OX
                value.contains("MCQ", ignoreCase = true) -> MCQ
                else -> OX // 기본값(원하면 MCQ로 바꿔도 됨)
            }
        }
    }
}
/*
data class UiStateUserQuiz(
    val isLoading: Boolean = false,
    val quiz: UserQuizUiState? = null,
    val errorMessage: String? = null,
    val isFinished: Boolean = false
)

data class UserQuizUiState(
    val quizId: Long,
    val question: String,
    val options: List<String>,
    val type: QuizType,

    val selectedAnswer: String? = null,
    val isAnswered: Boolean = false
)

fun UserQuiz.toUiState(): UserQuizUiState {
    return UserQuizUiState(
        quizId = quizId,
        question = question,
        options = options,
        type = type
    )
}*/




