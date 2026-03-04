package com.teumteumeat.teumteumeat.ui.screen.c2_goal_list

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.teumteumeat.teumteumeat.ui.screen.a1_login.LoginActivity
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.LocalActivityContext
import com.teumteumeat.teumteumeat.utils.LocalAppContext
import com.teumteumeat.teumteumeat.utils.LocalGoalListUiState
import com.teumteumeat.teumteumeat.utils.LocalScreenState
import com.teumteumeat.teumteumeat.utils.LocalViewModelContext
import com.teumteumeat.teumteumeat.utils.Utils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class GoalListActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TeumTeumEatTheme {

                val viewModel : GoalListViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val screenState by viewModel.screenState.collectAsStateWithLifecycle()
                val activity = this@GoalListActivity

                val sessionManager = viewModel.sessionManager // 세션메니저 정의

                // 🔥 전역 세션 이벤트 감지
                LaunchedEffect(Unit) {
                    sessionManager.sessionEvent.collectLatest {
                        Utils.UxUtils.moveActivity(activity, LoginActivity::class.java)
                    }
                }

                CompositionLocalProvider(
                    LocalAppContext provides this.applicationContext,
                    LocalActivityContext provides activity,
                    LocalViewModelContext provides viewModel,
                    LocalGoalListUiState provides uiState,
                    LocalScreenState provides screenState,
                ) {

                    GoalListScreen(
                        uiState = uiState,
                        onBackClick = {
                            // 주제 변경이 성공했다면 RESULT_OK를 설정
                            if (uiState.isChanged) {
                                setResult(RESULT_OK)
                            }
                            finish()
                        },
                        onGoalClick = viewModel::onGoalClick,
                        onCancelChangeGoal = viewModel::onCancelChangeGoal,
                        onConfirmChangeGoal = viewModel::onConfirmChangeGoal,
                        onRetryApi = viewModel::loadMyPageData
                    )
                }
            }
        }
    }
}