package com.teumteumeat.teumteumeat.ui.screen.b2_quiz

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.teumteumeat.teumteumeat.domain.model.common.GoalTypeUiState
import com.teumteumeat.teumteumeat.ui.screen.b1_summary.SummaryActivity
import com.teumteumeat.teumteumeat.ui.screen.b3_quiz_result.QuizResultActivity
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.LocalActivityContext
import com.teumteumeat.teumteumeat.utils.LocalAppContext
import com.teumteumeat.teumteumeat.utils.LocalQuizUiState
import com.teumteumeat.teumteumeat.utils.LocalScreenState
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
                val screenState by viewModel.screenState.collectAsStateWithLifecycle()

                CompositionLocalProvider(
                    LocalAppContext provides this.applicationContext,
                    LocalActivityContext provides this@QuizActivity,
                    LocalViewModelContext provides viewModel,
                    LocalQuizUiState provides uiState,
                    LocalScreenState provides screenState,
                ) {
                    val context = LocalActivityContext.current as QuizActivity
                    val goalTypeUiState: GoalTypeUiState = Utils.PrefsUtil.getGoalType(context)
                    val documentId = Utils.PrefsUtil.getDocumentId(context) ?: -1

                    // 🔹 최초 진입 시 퀴즈 목록 조회
                    LaunchedEffect(Unit) {
                        viewModel.loadQuizzes(documentId, goalTypeUiState)
                    }

                    QuizScreen(
                        uiState = uiState,
                        onBackClick = {
                            viewModel.prevQuiz()
                        },
                        onSelectAnswer = { answer ->
                            viewModel.submitAnswer(answer)
                        },
                        screenState = screenState,
                        onRetryApi = { viewModel.loadQuizzes(documentId, goalTypeUiState) },
                        onGoBeforeScreen = {
                            Utils.UxUtils.moveActivity(
                                context,
                                SummaryActivity::class.java
                            )
                        },
                        onCompleteQuiz = {
                            context.startActivity(
                                QuizResultActivity.newIntent(context, documentId)
                            )
                            finish()
                        }
                    )

                }
            }
        }
    }
}

