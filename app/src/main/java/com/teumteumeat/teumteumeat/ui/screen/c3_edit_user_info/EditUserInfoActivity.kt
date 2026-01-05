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
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.LocalActivityContext
import com.teumteumeat.teumteumeat.utils.LocalAppContext
import com.teumteumeat.teumteumeat.utils.LocalEditUserInfoUiState
import com.teumteumeat.teumteumeat.utils.LocalViewModelContext
import dagger.hilt.android.AndroidEntryPoint

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

                    // 최초 진입 시 1회 호출
                    LaunchedEffect(Unit) {
                    }

                    EditUserInfoScreen(
                        uiState = uiState,
                        onBackClick = { finish() },
                        onInfoSaveClick = {},
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