package com.teumteumeat.teumteumeat.ui.screen.b1_summary

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
import com.teumteumeat.teumteumeat.domain.model.goal.DomainGoalType
import com.teumteumeat.teumteumeat.ui.screen.b2_quiz.QuizActivity
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.LocalActivityContext
import com.teumteumeat.teumteumeat.utils.LocalAppContext
import com.teumteumeat.teumteumeat.utils.LocalScreenState
import com.teumteumeat.teumteumeat.utils.LocalSummaryUiState
import com.teumteumeat.teumteumeat.utils.LocalViewModelContext
import com.teumteumeat.teumteumeat.utils.Utils
import dagger.hilt.android.AndroidEntryPoint


object SummaryArgs {
    const val KEY_GOAL_ID = "key_goal_id"
    const val KEY_GOAL_TYPE = "key_goal_type"
    const val KEY_DOCUMENT_ID = "key_document_id"
    const val KEY_CATEGORY_ID = "key_category_id"
}

@AndroidEntryPoint
class SummaryActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val goalId = intent.getLongExtra(
            SummaryArgs.KEY_GOAL_ID,
            -1L
        )

        val goalType = intent.getStringExtra(
            SummaryArgs.KEY_GOAL_TYPE
        )?.let {
            DomainGoalType.valueOf(it)
        }

        val documentId = intent.getLongExtra(
            SummaryArgs.KEY_DOCUMENT_ID,
            -1L
        ).takeIf { it != -1L }

        val categoryId = intent.getLongExtra(
            SummaryArgs.KEY_CATEGORY_ID,
            -1L
        ).takeIf { it != -1L }

        // 🔥 여기서 goalType 으로 분기
        when (goalType) {
            DomainGoalType.CATEGORY -> {
                // categoryId 기반 요약글 조회
            }

            DomainGoalType.DOCUMENT -> {
                // documentId 기반 요약글 조회
            }

            null -> {
                // 예외 처리 (잘못된 진입)
                finish()
            }
        }

        setContent {
            TeumTeumEatTheme {

                val viewModel : SummaryViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val screenState by viewModel.screenState.collectAsStateWithLifecycle()

                CompositionLocalProvider(
                    LocalAppContext provides this.applicationContext,
                    LocalActivityContext provides this@SummaryActivity,
                    LocalViewModelContext provides viewModel,
                    LocalSummaryUiState provides uiState,
                    LocalScreenState provides screenState,
                ) {
                    val context = LocalContext.current

                    // 최초 진입 시 1회 호출
                    LaunchedEffect(Unit) {
                        if (goalType != null && goalId != -1L) {
                            viewModel.initSummary(
                                goalId = goalId,
                                goalType = goalType,
                                documentId = documentId,
                                categoryId = categoryId
                            )

                            viewModel.loadSummaryByGoalType()

                        } else {
                            finish()
                        }


                    }

                    SummaryScreen(
                        uiState = uiState,
                        screenState = screenState,
                        onBackClick = { finish() },
                        onQuizClick = {
                            Utils.UxUtils.moveActivity(context, QuizActivity::class.java, exitFlag = true)
                        },
                        onSetIdleScreen = viewModel::resetIdleState,
                        onRetryApi = { viewModel.loadSummaryByGoalType() }
                    )
                }
            }
        }
    }
}