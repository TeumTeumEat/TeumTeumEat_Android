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
import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.enum_type.GoalType
import com.teumteumeat.teumteumeat.ui.screen.b2_quiz.QuizActivity
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.LocalActivityContext
import com.teumteumeat.teumteumeat.utils.LocalAppContext
import com.teumteumeat.teumteumeat.utils.LocalScreenState
import com.teumteumeat.teumteumeat.utils.LocalSummaryUiState
import com.teumteumeat.teumteumeat.utils.LocalViewModelContext
import com.teumteumeat.teumteumeat.utils.Utils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SummaryActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
                    val goalId = Utils.PrefsUtil.getGoalId(context) ?: -1
                    val documentId = Utils.PrefsUtil.getDocumentId(context) ?: -1
                    val categoryId = Utils.PrefsUtil.getCategoryId(context) ?: -1
                    val userGoalType = Utils.PrefsUtil.getGoalType(context)

                    // 최초 진입 시 1회 호출
                    LaunchedEffect(Unit) {
                        when(userGoalType) {
                            GoalType.DOCUMENT -> {
                                viewModel.loadDocumentSummary(goalId, documentId)
                            }
                            GoalType.CATEGORY -> {
                                viewModel.loadCategorySummary(categoryId)
                            }
                            GoalType.NONE -> {}
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
                        onRetryApi = { viewModel.loadCategorySummary(categoryId) }
                    )
                }
            }
        }
    }
}