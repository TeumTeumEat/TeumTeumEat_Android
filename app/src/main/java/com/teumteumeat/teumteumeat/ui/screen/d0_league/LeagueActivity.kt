package com.teumteumeat.teumteumeat.ui.screen.d0_league

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.teumteumeat.teumteumeat.BuildConfig
import com.teumteumeat.teumteumeat.ui.screen.a1_login.LoginActivity
import com.teumteumeat.teumteumeat.ui.screen.a4_main.MainActivity
import com.teumteumeat.teumteumeat.ui.screen.a4_main.MainArgs
import com.teumteumeat.teumteumeat.ui.screen.d0_league.debug.LeagueTimeOffsetDebugPanel
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.Utils
import com.teumteumeat.teumteumeat.utils.firebase.TeumAnalyticsLogger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
class LeagueActivity : ComponentActivity() {

    companion object {
        /** 리그 진입 경로(`league_view` 의 source) 를 담는 Intent extra 키. 값은 TeumAnalyticsEvent.LeagueView.SOURCE_* */
        const val EXTRA_SOURCE = "extra_league_source"

        /**
         * @param source 진입 경로 — TeumAnalyticsEvent.LeagueView.SOURCE_HOME / SOURCE_HISTORY.
         *   null 이면 경로를 알 수 없는 진입이라 `league_view` 를 기록하지 않는다.
         */
        fun newIntent(context: Context, source: String? = null): Intent {
            return Intent(context, LeagueActivity::class.java).apply {
                source?.let { putExtra(EXTRA_SOURCE, it) }
            }
        }
    }

    @Inject
    lateinit var analyticsLogger: TeumAnalyticsLogger

    // singleTask 라 이미 떠 있는 인스턴스로 재진입하면 onCreate 가 아니라 onNewIntent 로 들어온다.
    // 그때마다 값을 올려 Compose 쪽(LaunchedEffect)에서 ViewModel 에 재조회를 요청한다.
    private var reenterCount by mutableIntStateOf(0)

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        reenterCount++
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 새로 생성될 때만 1회 기록한다. Activity 재생성(savedInstanceState != null)과
        // 이미 떠 있는 리그로의 재진입(onNewIntent)에서는 기록하지 않는다.
        if (savedInstanceState == null) {
            intent.getStringExtra(EXTRA_SOURCE)?.let { analyticsLogger.logLeagueView(it) }
        }

        // 이 화면의 배경(연노랑→흰색 그라데이션)은 시스템 다크모드와 무관하게 항상 밝으므로,
        // 상태바 아이콘도 시스템 테마를 따라가지 않고 항상 어두운 색으로 고정한다.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
            ),
        )

        setContent {
            TeumTeumEatTheme {
                val viewModel: LeagueViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val screenState by viewModel.screenState.collectAsStateWithLifecycle()

                // 이미 떠 있는 리그 화면으로 재진입(onNewIntent)하면 최신 랭킹으로 갱신한다.
                LaunchedEffect(reenterCount) {
                    if (reenterCount > 0) viewModel.onReenter()
                }

                // 🔥 전역 세션 이벤트 감지
                LaunchedEffect(Unit) {
                    viewModel.sessionManager.sessionEvent.collectLatest {
                        Utils.UxUtils.moveActivity(this@LeagueActivity, LoginActivity::class.java, clearTask = true)
                    }
                }

                LaunchedEffect(Unit) {
                    viewModel.uiEvent.collectLatest { event ->
                        when (event) {
                            is LeagueUiEvent.ShareLeague -> {
                                analyticsLogger.logLeagueShare(event.source, event.resultTier)
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, event.shareText)
                                }
                                startActivity(Intent.createChooser(sendIntent, null))
                            }

                            is LeagueUiEvent.ShowToast ->
                                Toast.makeText(this@LeagueActivity, event.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    LeagueScreen(
                        uiState = uiState,
                        screenState = screenState,
                        onBackClick = { finish() },
                        onShareClick = { viewModel.requestShare() },
                        onInfoClick = { viewModel.onGuideClick() },
                        onRaiseRankClick = {
                            // 홈으로 이동 — 홈에 간식이 없는 상태(SnackState.Available 아님)라면
                            // 이동 직후 쿠폰 충전 팝업을 바로 띄운다 (MainCompositionProvider에서 처리).
                            Utils.UxUtils.moveActivity(
                                context = this@LeagueActivity,
                                destinationActivity = MainActivity::class.java,
                                extras = Bundle().apply {
                                    putBoolean(MainArgs.KEY_OPEN_COUPON_ON_NO_SNACK, true)
                                },
                            )
                        },
                        onRetryApi = { viewModel.loadLeague() },
                        onResultModalDismiss = { viewModel.onResultModalDismiss() },
                        onResultShareClick = { viewModel.requestResultShare() },
                        onGuideDismiss = { viewModel.onGuideDismiss() },
                    )

                    // [dev/local 전용] 리그 시각 오프셋 테스트 패널 — 디버그 빌드에서만 노출한다.
                    if (BuildConfig.DEBUG) {
                        LeagueTimeOffsetDebugPanel(
                            resetRemainingSeconds = uiState.resetRemainingSeconds,
                            onOffsetChanged = { viewModel.loadLeague() },
                            onResultSeenRecordCleared = {
                                viewModel.resetDismissedResults()
                                viewModel.loadLeague()
                            },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .statusBarsPadding()
                                .padding(top = 56.dp, end = 8.dp),
                        )
                    }
                }
            }
        }
    }
}
