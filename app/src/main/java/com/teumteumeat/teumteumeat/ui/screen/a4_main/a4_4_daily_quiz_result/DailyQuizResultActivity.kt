package com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_4_daily_quiz_result

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.teumteumeat.teumteumeat.domain.model.common.GoalType
import com.teumteumeat.teumteumeat.domain.model.common.GoalTypeUiState
import com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_3_daily_summary_detail.DailySummaryActivity
import com.teumteumeat.teumteumeat.ui.screen.b3_quiz_result.QuizResultNavHost
import com.teumteumeat.teumteumeat.ui.screen.b3_quiz_result.QuizResultViewModel
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.LocalActivityContext
import com.teumteumeat.teumteumeat.utils.LocalAppContext
import com.teumteumeat.teumteumeat.utils.LocalDailyQuizResultUiState
import com.teumteumeat.teumteumeat.utils.LocalQuizResultUiState
import com.teumteumeat.teumteumeat.utils.LocalViewModelContext
import com.teumteumeat.teumteumeat.utils.Utils
import com.teumteumeat.teumteumeat.utils.Utils.DailySummaryArgs
import com.teumteumeat.teumteumeat.utils.Utils.UxUtils.moveScreenWithDailyItem
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class DailyQuizResultActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val id = intent.getLongExtra(
            DailySummaryArgs.KEY_ID,
            -1L
        )

        val type = intent.getStringExtra(
            DailySummaryArgs.KEY_TYPE
        )?.let {
            GoalType.valueOf(it)
        }

        val date = intent.getStringExtra(
            DailySummaryArgs.KEY_DATE
        )?.let {
            LocalDate.parse(it)
        }


        // 🔐 안전성 체크
        if (id == -1L || type == null || date == null) {
            // todo. 에러 스크린으로 처리학기
            Toast.makeText(applicationContext, "id=${id}, type=${type}, date=${date} null 값이 있스니다.",
                Toast.LENGTH_SHORT).show()
            finish() // 잘못된 진입 방지x
            return
        }

        enableEdgeToEdge()
        setContent {
            TeumTeumEatTheme {
                val viewModel : DailyQuizResultViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val screenState by viewModel.screenState.collectAsStateWithLifecycle()


                // ⭐ 최초 진입 시 API 호출
                LaunchedEffect(Unit) {
                    // todo. daily 퀴즈 결과 로드하기
                    // ✅ ViewModel에 초기값 전달
                    viewModel.initArgs(
                        id = id,
                        type = type,
                        date = date
                    )

                    // ✅ 실제 API 호출
                    viewModel.loadQuizResults()
                }

                CompositionLocalProvider(
                    LocalAppContext provides this.applicationContext,
                    LocalActivityContext provides this@DailyQuizResultActivity,
                    LocalViewModelContext provides viewModel,
                    LocalDailyQuizResultUiState provides uiState,
                ) {
                    val activityContext = LocalActivityContext.current

                    DailyQuizResultScreen(
                        onBack = { finish() },
                        uiState = uiState,
                        screenState = screenState,
                        onViewSummaryClick = {
                            moveScreenWithDailyItem(
                                context = activityContext,
                                targetActivity = DailySummaryActivity::class.java,
                                id = uiState.id ?: return@DailyQuizResultScreen,
                                type = uiState.type ?: return@DailyQuizResultScreen,
                                date = uiState.date ?: return@DailyQuizResultScreen,
                                exitCurrent = true
                            )
                        }
                    )
                }
            }
        }
    }
}

