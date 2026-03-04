package com.teumteumeat.teumteumeat.ui.screen.b2_quiz

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.teumteumeat.teumteumeat.domain.model.common.GoalTypeUiState
import com.teumteumeat.teumteumeat.ui.aa0_base.BaseActivity
import com.teumteumeat.teumteumeat.ui.screen.a1_login.LoginActivity
import com.teumteumeat.teumteumeat.ui.screen.b3_quiz_result.QuizResultActivity
import com.teumteumeat.teumteumeat.ui.screen.common_screen.UiScreenState
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
class QuizActivity : BaseActivity() {
    private val viewModel: QuizViewModel by viewModels()

    private var documentId: Long = -1
    private var goalType: String = ""

    override fun onRetryClick() {
        viewModel.loadQuizzes(documentId, GoalTypeUiState.fromString(goalType))
    }

    companion object {
        const val EXTRA_DOCUMENT_ID = "documentId"
        const val INVALID_DOCUMENT_ID = -1L

        const val GOAL_TYPE = "goalType"

        fun newIntent(
            context: Context,
            documentId: Long,
            goalType: String,
        ): Intent {
            return Intent(context, QuizActivity::class.java).apply {
                putExtra(EXTRA_DOCUMENT_ID, documentId)
                putExtra(GOAL_TYPE, goalType)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        documentId = intent.getLongExtra(EXTRA_DOCUMENT_ID, INVALID_DOCUMENT_ID)
        goalType = intent.getStringExtra(GOAL_TYPE) ?: ""

        if (documentId == INVALID_DOCUMENT_ID || goalType == "") {
            Log.d("QuizActivity", "documentId: $documentId, goalType: $goalType")
        }

        enableEdgeToEdge()
        setContent {
            TeumTeumEatTheme {
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

                    val sessionManager = viewModel.sessionManager // 세션메니저 정의

                    // 🔥 전역 세션 이벤트 감지
                    LaunchedEffect(Unit) {
                        sessionManager.sessionEvent.collectLatest {
                            Utils.UxUtils.moveActivity(activity, LoginActivity::class.java)
                        }
                    }

                    // 🔹 최초 진입 시 퀴즈 목록 조회
                    LaunchedEffect(Unit) {
                        viewModel.loadQuizzes(documentId, GoalTypeUiState.fromString(goalType))
                    }

                    BackHandler {
                        when(screenState){
                            UiScreenState.Success -> {
                                viewModel.prevQuiz()
                            }
                            is UiScreenState.Error, UiScreenState.Idle -> {
                                finish()
                            }
                            UiScreenState.Loading -> {}
                        }
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
                        onRetryApi = {
                            viewModel.loadQuizzes(documentId, GoalTypeUiState.fromString(goalType))
                        },
                        onCompleteQuiz = {
                            val documentId = viewModel.documentId.toInt()

                            viewModel.completeQuiz()

                            activity.startActivity(
                                QuizResultActivity.newIntent(activity, documentId)
                            )

                            finish()
                        },
                        onDismissExitDialog = { viewModel.dismissExitDialog() },
                        onDestroyActivity = { finish() },
                    )

                }
            }
        }
    }
}

