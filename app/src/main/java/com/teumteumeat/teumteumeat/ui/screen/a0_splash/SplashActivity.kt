package com.teumteumeat.teumteumeat.ui.screen.a0_splash

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.teumteumeat.teumteumeat.ui.screen.deeplink.DeepLinkRouter
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.DeepLinkConstants
import com.teumteumeat.teumteumeat.utils.LocalActivityContext
import com.teumteumeat.teumteumeat.utils.LocalAppContext
import com.teumteumeat.teumteumeat.utils.LocalViewModelContext
import dagger.hilt.android.AndroidEntryPoint

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : ComponentActivity()  {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("SplashActivity", "onCreate")
        enableEdgeToEdge()

        // 푸시 알림 탭 등으로 리그 딥링크(link extra)를 들고 들어왔는지 — 진행도 분기 뒤 홈 → 리그로 이동한다.
        val isLeagueEntry = DeepLinkRouter.isLeagueLink(
            intent.getStringExtra(DeepLinkConstants.PUSH_DATA_KEY_LINK)
        )

        setContent {
            TeumTeumEatTheme {
                 val viewModel: SplashViewModel = hiltViewModel()
                CompositionLocalProvider(
                    LocalAppContext provides this.applicationContext,
                    LocalActivityContext provides this,
                    LocalViewModelContext provides viewModel,

                ){
                    SplashScreen(
                        viewModel = viewModel,
                        isLeagueEntry = isLeagueEntry,
                    )
                }
            }
        }

    }
}
