package com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_3_daily_summary_detail

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.teumteumeat.teumteumeat.domain.model.common.DomainGoalType_v1
import com.teumteumeat.teumteumeat.domain.model.goal.DomainGoalType
import com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_4_daily_quiz_result.DailyQuizResultActivity
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.LocalActivityContext
import com.teumteumeat.teumteumeat.utils.LocalAppContext
import com.teumteumeat.teumteumeat.utils.LocalDailySummaryUiState
import com.teumteumeat.teumteumeat.utils.LocalScreenState
import com.teumteumeat.teumteumeat.utils.LocalViewModelContext
import com.teumteumeat.teumteumeat.utils.Utils.DailySummaryArgs
import com.teumteumeat.teumteumeat.utils.Utils.UxUtils.moveScreenWithDailyItem
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate


@AndroidEntryPoint
class DailySummaryActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        val id = intent.getLongExtra(
            DailySummaryArgs.KEY_ID,
            -1L
        )

        val type = intent.getStringExtra(
            DailySummaryArgs.KEY_TYPE
        )?.let {
            DomainGoalType.valueOf(it)
        }

        val date = intent.getStringExtra(
            DailySummaryArgs.KEY_DATE
        )?.let {
            LocalDate.parse(it)
        }


        // 🔐 안전성 체크
        if (id == -1L || type == null || date == null) {
            Toast.makeText(applicationContext, "id=${id}, type=${type}, date=${date} null 값이 있스니다.",
                Toast.LENGTH_SHORT).show()
            finish() // 잘못된 진입 방지
            return
        }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TeumTeumEatTheme {

                val viewModel : DailySummaryViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val screenState by viewModel.screenState.collectAsStateWithLifecycle()

                CompositionLocalProvider(
                    LocalAppContext provides this.applicationContext,
                    LocalActivityContext provides this@DailySummaryActivity,
                    LocalViewModelContext provides viewModel,
                    LocalDailySummaryUiState provides uiState,
                    LocalScreenState provides screenState,
                ) {
                    val activityContext = LocalActivityContext.current as DailySummaryActivity

                    // 최초 진입 시 1회 호출
                    LaunchedEffect(Unit) {
                        // ✅ ViewModel에 초기값 전달
                        viewModel.initArgs(
                            id = id,
                            type = type,
                            date = date
                        )

                        // ✅ 실제 API 호출
                        viewModel.loadSummary()
                    }

                    DailySummaryScreen(
                        uiState = uiState,
                        screenState = screenState,
                        onBackClick = { finish() },
                        onViewQuizResultClick = {
                            moveScreenWithDailyItem(
                                context = activityContext,
                                targetActivity = DailyQuizResultActivity::class.java,
                                id = uiState.id ?: return@DailySummaryScreen,
                                type = uiState.type ?: return@DailySummaryScreen,
                                date = uiState.date ?: return@DailySummaryScreen,
                                exitCurrent = true
                            )
                        },
                        onSetIdleScreen = { finish() },
                        onRetryApi = { viewModel.loadSummary() }
                    )
                }
            }
        }
    }
}