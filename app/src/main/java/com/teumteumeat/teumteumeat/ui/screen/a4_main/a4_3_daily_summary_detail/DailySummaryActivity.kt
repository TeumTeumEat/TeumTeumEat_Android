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
import com.teumteumeat.teumteumeat.utils.firebase.TeumCrashlyticsLogger
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import javax.inject.Inject


@AndroidEntryPoint
class DailySummaryActivity : ComponentActivity() {

    @Inject
    lateinit var crashlyticsLogger: TeumCrashlyticsLogger

    override fun onCreate(savedInstanceState: Bundle?) {
        // ✅ super.onCreate()는 항상 가장 먼저 호출한다 — 아래에서 finish()로 조기 반환하더라도
        // 이 호출이 누락되면 SuperNotCalledException으로 크래시가 발생한다.
        super.onCreate(savedInstanceState)

        val id = intent.getLongExtra(
            DailySummaryArgs.KEY_ID,
            -1L
        )

        val rawType = intent.getStringExtra(DailySummaryArgs.KEY_TYPE)
        val type = rawType?.let {
            runCatching { DomainGoalType.valueOf(it) }.getOrNull()
        }

        val rawDate = intent.getStringExtra(DailySummaryArgs.KEY_DATE)
        val date = rawDate?.let {
            runCatching { LocalDate.parse(it) }.getOrNull()
        }


        // 🔐 안전성 체크
        if (id == -1L || type == null || date == null) {
            // ✅ 실사용자 발생 시 Crashlytics 대시보드(키/로그 탭)에서 원인 추적이 가능하도록 기록
            crashlyticsLogger.setCustomKey(KEY_EXTRA_ID, id)
            crashlyticsLogger.setCustomKey(KEY_EXTRA_TYPE, rawType ?: "null")
            crashlyticsLogger.setCustomKey(KEY_EXTRA_DATE, rawDate ?: "null")
            crashlyticsLogger.log("DailySummaryActivity 잘못된 intent extra로 진입 차단")
            crashlyticsLogger.recordNonFatal(
                IllegalStateException(
                    "DailySummaryActivity invalid intent extras: id=$id, type=$rawType, date=$rawDate"
                )
            )

            Toast.makeText(applicationContext, "id=${id}, type=${type}, date=${date} null 값이 있스니다.",
                Toast.LENGTH_SHORT).show()
            finish() // 잘못된 진입 방지
            return
        }

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

    companion object {
        private const val KEY_EXTRA_ID = "daily_summary_extra_id"
        private const val KEY_EXTRA_TYPE = "daily_summary_extra_type"
        private const val KEY_EXTRA_DATE = "daily_summary_extra_date"
    }
}