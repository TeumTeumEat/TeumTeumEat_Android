package com.teumteumeat.teumteumeat.ui.screen.c3_edit_user_info

import android.os.Bundle
import android.util.Log
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
import com.teumteumeat.teumteumeat.utils.LocalEditUserInfoUiState
import com.teumteumeat.teumteumeat.utils.LocalViewModelContext
import com.teumteumeat.teumteumeat.utils.Utils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class EditUserInfoActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TeumTeumEatTheme {

                val viewModel : EditUserInfoViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                CompositionLocalProvider(
                    LocalAppContext provides this.applicationContext,
                    LocalActivityContext provides this@EditUserInfoActivity,
                    LocalViewModelContext provides viewModel,
                    LocalEditUserInfoUiState provides uiState,
                ) {

                    val sessionManager = viewModel.sessionManager // 세션메니저 정의
                    val activity = LocalActivityContext.current

                    // 🔥 전역 세션 이벤트 감지
                    LaunchedEffect(Unit) {
                        sessionManager.sessionEvent.collectLatest {
                            Utils.UxUtils.moveActivity(activity, LoginActivity::class.java)
                        }
                    }

                    EditUserInfoScreen(
                        uiState = uiState,
                        onBackClick = { finish() },
                        onInfoSaveClick = { viewModel.saveUserInfo() },
                    )
                }
            }
        }
    }

    override fun onBackPressed() {
        Log.d("BackDebug", "Activity onBackPressed()")
        super.onBackPressed()
    }
}