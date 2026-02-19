package com.teumteumeat.teumteumeat.ui.screen.b2_quiz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.teumteumeat.teumteumeat.domain.model.common.GoalTypeUiState
import com.teumteumeat.teumteumeat.ui.screen.a1_login.LoginActivity
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
import kotlinx.coroutines.flow.collectLatest
import kotlin.jvm.java

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
                    val activity = LocalActivityContext.current as QuizActivity
                    val goalTypeUiState: GoalTypeUiState = Utils.PrefsUtil.getGoalType(activity)
                    val documentId = Utils.PrefsUtil.getDocumentId(activity) ?: -1

                    val sessionManager = viewModel.sessionManager // 세션메니저 정의

                    // 🔥 전역 세션 이벤트 감지
                    LaunchedEffect(Unit) {
                        sessionManager.sessionEvent.collectLatest {
                            Utils.UxUtils.moveActivity(activity, LoginActivity::class.java)
                        }
                    }

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
                                activity,
                                SummaryActivity::class.java
                            )
                        },
                        onCompleteQuiz = {
                            activity.startActivity(
                                QuizResultActivity.newIntent(activity, documentId)
                            )
                            finish()
                        }
                    )

                }
            }
        }
    }
}

