package com.teumteumeat.teumteumeat.ui.screen.c1_mypage

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
import com.teumteumeat.teumteumeat.ui.screen.a1_login.LoginActivity
import com.teumteumeat.teumteumeat.ui.screen.c2_goal_list.GoalListActivity
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.LocalActivityContext
import com.teumteumeat.teumteumeat.utils.LocalAppContext
import com.teumteumeat.teumteumeat.utils.LocalMyPageUiState
import com.teumteumeat.teumteumeat.utils.LocalViewModelContext
import com.teumteumeat.teumteumeat.utils.Utils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class MyPageActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TeumTeumEatTheme {

                val viewModel : MyPageViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                CompositionLocalProvider(
                    LocalAppContext provides this.applicationContext,
                    LocalActivityContext provides this@MyPageActivity,
                    LocalViewModelContext provides viewModel,
                    LocalMyPageUiState provides uiState,
                ) {
                    val context = LocalContext.current
                    val activity = LocalActivityContext.current

                    val sessionManager = viewModel.sessionManager // 세션메니저 정의

                    // 🔥 전역 세션 이벤트 감지
                    LaunchedEffect(Unit) {
                        sessionManager.sessionEvent.collectLatest {
                            Utils.UxUtils.moveActivity(activity, LoginActivity::class.java)
                        }
                    }

                    MyPageScreen(
                        uiState = uiState,
                        onBackClick = { finish() },
                        onTopicClick = {
                            Utils.UxUtils.moveActivity(
                                activity,
                                GoalListActivity::class.java,
                                exitFlag = false
                            )
                        },
                        onAlarmToggle = {  },
                        onTermsClick = {  },
                        onCustomerCenterClick = {  },
                        onLogoutClick = {
                            Utils.UxUtils.moveActivity(
                                activity,
                                LoginActivity::class.java,
                            )
                        }
                    )
                }
            }
        }
    }
}