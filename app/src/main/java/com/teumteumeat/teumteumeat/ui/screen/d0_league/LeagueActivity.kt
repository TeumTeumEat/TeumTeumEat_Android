package com.teumteumeat.teumteumeat.ui.screen.d0_league

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.teumteumeat.teumteumeat.ui.screen.a1_login.LoginActivity
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.Utils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class LeagueActivity : ComponentActivity() {

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, LeagueActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

                // 🔥 전역 세션 이벤트 감지
                LaunchedEffect(Unit) {
                    viewModel.sessionManager.sessionEvent.collectLatest {
                        Utils.UxUtils.moveActivity(this@LeagueActivity, LoginActivity::class.java, clearTask = true)
                    }
                }

                LeagueScreen(
                    uiState = uiState,
                    screenState = screenState,
                    onBackClick = { finish() },
                    onShareClick = {
                        // TODO: 리그 결과 공유 기능 연동
                    },
                    onInfoClick = {
                        // TODO: 리그 안내(정책) 모달 연동
                    },
                    onRaiseRankClick = {
                        // TODO: 순위 올리기(퀴즈 이동 등) 플로우 연동
                    },
                    onRetryApi = { viewModel.loadLeague() },
                )
            }
        }
    }
}
