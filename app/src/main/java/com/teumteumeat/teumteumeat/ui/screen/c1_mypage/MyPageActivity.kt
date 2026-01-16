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
import com.teumteumeat.teumteumeat.ui.screen.b2_quiz.QuizActivity
import com.teumteumeat.teumteumeat.ui.screen.c2_goal_list.GoalListActivity
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.LocalActivityContext
import com.teumteumeat.teumteumeat.utils.LocalAppContext
import com.teumteumeat.teumteumeat.utils.LocalMyPageUiState
import com.teumteumeat.teumteumeat.utils.LocalViewModelContext
import com.teumteumeat.teumteumeat.utils.Utils
import dagger.hilt.android.AndroidEntryPoint

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
                    val activityContext = LocalActivityContext.current

                    // 최초 진입 시 1회 호출
                    LaunchedEffect(Unit) {
                        // viewModel.loadDocumentSummary(goalId, documentId)
                    }

                    MyPageScreen(
                        uiState = uiState,
                        onBackClick = { finish() },
                        onTopicClick = {
                            Utils.UxUtils.moveActivity(
                                activityContext,
                                GoalListActivity::class.java,
                                exitFlag = false
                            )
                        },
                        onAlarmToggle = {  },
                        onTermsClick = {  },
                        onCustomerCenterClick = {  },
                        onLogoutClick = {
                            Utils.UxUtils.moveActivity(
                                activityContext,
                                LoginActivity::class.java,
                            )
                        }
                    )
                }
            }
        }
    }
}