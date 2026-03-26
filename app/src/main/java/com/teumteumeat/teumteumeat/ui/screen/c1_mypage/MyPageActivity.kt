package com.teumteumeat.teumteumeat.ui.screen.c1_mypage

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.teumteumeat.teumteumeat.ui.aa0_base.BaseActivity
import com.teumteumeat.teumteumeat.ui.screen.a1_login.LoginActivity
import com.teumteumeat.teumteumeat.ui.screen.c2_goal_list.GoalListActivity
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.LocalActivityContext
import com.teumteumeat.teumteumeat.utils.LocalAppContext
import com.teumteumeat.teumteumeat.utils.LocalMyPageUiState
import com.teumteumeat.teumteumeat.utils.LocalScreenState
import com.teumteumeat.teumteumeat.utils.LocalViewModelContext
import com.teumteumeat.teumteumeat.utils.Utils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class MyPageActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TeumTeumEatTheme {

                val viewModel: MyPageViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val screenState by viewModel.screenState.collectAsStateWithLifecycle()

                // 🔹 Activity 결과를 받기 위한 런처 정의
                val goalListLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) { result ->
                    if (result.resultCode == RESULT_OK) {
                        // 성공적으로 변경하고 돌아왔다면 데이터 재로딩!
                        viewModel.loadMyPageData()
                    }
                }

                CompositionLocalProvider(
                    LocalAppContext provides this.applicationContext,
                    LocalActivityContext provides this@MyPageActivity,
                    LocalViewModelContext provides viewModel,
                    LocalMyPageUiState provides uiState,
                    LocalScreenState provides screenState,
                ) {
                    val context = LocalContext.current
                    val activity = LocalActivityContext.current

                    val sessionManager = viewModel.sessionManager // 세션메니저 정의

                    // 🔹 알림 권한 런처
                    val permissionLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestPermission()
                    ) { isGranted ->
                        viewModel.onNotificationPermissionResult(isGranted)
                    }

                    // 🔔 알림 권한 요청 트리거 감지
                    LaunchedEffect(uiState.requestNotificationPermission) {
                        if (uiState.requestNotificationPermission) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }

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
                            // 🔹 기존 Utils 사용 대신 런처를 통해 Activity 실행
                            val intent = Intent(activity, GoalListActivity::class.java)
                            goalListLauncher.launch(intent)
                        },
                        onLogoutClick = {
                            viewModel.logout(
                                onSuccess = {
                                    Utils.UxUtils.moveActivity(
                                        activity,
                                        LoginActivity::class.java,
                                    )
                                },
                                onError = { message ->
                                }
                            )

                        },
                        onAlarmToggle = {
                            viewModel.toogleAlarm(!uiState.isAlarmEnabled)
                        },
                        onTermsClick = { },
                        onCustomerCenterClick = { },
                        onWithdroawClick = { viewModel.withdrawUser() },
                        onNotificationGuideConfirm = {
                            openNotificationSetting(activity)
                            viewModel.closeNotificationGuide()
                        },
                        onNotificationGuideDismiss = {
                            viewModel.closeNotificationGuide()
                        },
                        onRetryClick = {
                            viewModel.loadMyPageData()
                        }
                    )
                }
            }
        }
    }

    override fun onRetryClick() {
        // ViewModel을 찾을 수 없으므로(onCreate 내부 지역변수), 
        // MyPageScreen의 onRetryClick 콜백에서 직접 viewModel.loadMyPageData()를 호출하도록 처리했습니다.
    }

    private fun openNotificationSetting(activity: Activity) {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, activity.packageName)
        }
        activity.startActivity(intent)
    }
}