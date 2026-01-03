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
import com.teumteumeat.teumteumeat.ui.screen.b2_quiz.QuizActivity
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.LocalActivityContext
import com.teumteumeat.teumteumeat.utils.LocalAppContext
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

                CompositionLocalProvider(
                    LocalAppContext provides this.applicationContext,
                    LocalActivityContext provides this@SummaryActivity,
                    LocalViewModelContext provides viewModel,
                    LocalSummaryUiState provides uiState,
                ) {
                    val context = LocalContext.current
                    val goalId = Utils.PrefsUtil.getGoalId(context) ?: -1
                    val documentId = Utils.PrefsUtil.getDocumentId(context) ?: -1
                    val categoryId = Utils.PrefsUtil.getCategoryId(context) ?: -1

                    // 최초 진입 시 1회 호출
                    LaunchedEffect(Unit) {
                        viewModel.loadDocumentSummary(goalId, documentId)
                    }

                    SummaryScreen(
                        uiState = uiState,
                        onBackClick = { finish() },
                        onQuizClick = {
                            // TODO 퀴즈 화면 이동
                            Utils.UxUtils.moveActivity(context, QuizActivity::class.java, exitFlag = true)
                        }
                    )
                }
            }
        }
    }
}