package com.teumteumeat.teumteumeat.ui.screen.a0_splash

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.teumteumeat.teumteumeat.R
import com.teumteumeat.teumteumeat.ui.component.DefaultMonoBg
import com.teumteumeat.teumteumeat.ui.screen.a1_login.LoginActivity
import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.OnBoardingActivity
import com.teumteumeat.teumteumeat.ui.screen.a4_main.MainActivity
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.Utils

@Composable
fun SplashScreen(
    viewModel: SplashViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val context = LocalContext.current

    val v2Color = MaterialTheme.colorScheme.primary

    val isPlaying = remember { mutableStateOf(true) }
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.splash_animation)
    )

    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1, // 🔥 1번만 재생
        isPlaying = isPlaying.value // 🔥 핵심
    )

    // ✅ 애니메이션 종료 감지
    LaunchedEffect(progress) {
        if (progress == 1f) {
            Log.d("SplashDebug", "animation finished")
            viewModel.onAnimationFinished()
        }
    }

    // ✅ 네비게이션 처리 (단발성)
    LaunchedEffect(uiState.nextRoute) {
        when (uiState.nextRoute) {
            SplashRoute.ON_BOARDING -> {
                Utils.UxUtils.moveActivity(context, OnBoardingActivity::class.java)
            }
            SplashRoute.MAIN -> {
                Utils.UxUtils.moveActivity(context, MainActivity::class.java)
            }
            SplashRoute.LOGIN -> {
                Utils.UxUtils.moveActivity(context, LoginActivity::class.java)
            }

            null -> {}
        }
    }

    // 🔥 상태에 따른 단발성 네비게이션
    /*LaunchedEffect(uiState) {
        when (uiState) {
            is SplashUiState.Success -> {
                Utils.UxUtils.moveActivity(
                    context,
                    MainActivity::class.java,
                    exitFlag = true
                )
            }

            is SplashUiState.Error -> {
                val message = uiState.message
                Log.e("Splash", "소셜 로그인 실패: $message")

                Utils.UxUtils.moveActivity(
                    context,
                    LoginActivity::class.java,
                    exitFlag = true
                )
            }
            else-> {}

        }
    }*/

    TeumTeumEatTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                // 애니매이션으로 변경이후 v2Color로 변경 예정
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center,

        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                )
                /*Image(
                    painter = painterResource(id = R.drawable.logo_login),
                    contentDescription = "메인 로고",
                    contentScale = ContentScale.Fit
                )*/
            }
        }
    }
}




@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    TeumTeumEatTheme {
        DefaultMonoBg(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface
        ) {
/*            SplashScreen(
                viewModel = ,
                onSuccess = TODO(),
                onFail = TODO()
            )*/
        }
    }
}

