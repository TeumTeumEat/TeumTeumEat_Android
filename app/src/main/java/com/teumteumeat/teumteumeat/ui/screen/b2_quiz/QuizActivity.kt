package com.teumteumeat.teumteumeat.ui.screen.b2_quiz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.enum_type.GoalType
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.LocalActivityContext
import com.teumteumeat.teumteumeat.utils.LocalAppContext
import com.teumteumeat.teumteumeat.utils.LocalQuizUiState
import com.teumteumeat.teumteumeat.utils.LocalViewModelContext
import com.teumteumeat.teumteumeat.utils.Utils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class QuizActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TeumTeumEatTheme {
                val viewModel : QuizViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                CompositionLocalProvider(
                    LocalAppContext provides this.applicationContext,
                    LocalActivityContext provides this@QuizActivity,
                    LocalViewModelContext provides viewModel,
                    LocalQuizUiState provides uiState,
                ) {
                    val context = LocalContext.current
                    val goalType: GoalType = Utils.PrefsUtil.getGoalType(context)
                    val documentId = Utils.PrefsUtil.getDocumentId(context) ?: -1
                    val categoryId = Utils.PrefsUtil.getCategoryId(context) ?: -1

                    // 🔹 최초 진입 시 퀴즈 목록 조회
                    LaunchedEffect(Unit) {
                        viewModel.loadQuizzes(documentId, goalType)
                    }

                    // 🔹 모든 문제를 다 풀었을 때
//                    if (uiState.currentIndex >= uiState.quizzes.size &&
//                        uiState.quizzes.isNotEmpty()
//                    ) {
///*                        onFinishQuiz()
//                        return*/
//                    }

                    QuizScreen(
                        uiState = uiState,
                        onSelectAnswer = { answer ->
                            viewModel.submitAnswer(answer)
                        },
                        onBackClick = {
                            viewModel.prevQuiz()
                        }
                    )

                }
            }
        }
    }
}

