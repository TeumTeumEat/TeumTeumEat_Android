package com.teumteumeat.teumteumeat.ui.screen.a1_login

import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.teumteumeat.teumteumeat.BuildConfig
import com.teumteumeat.teumteumeat.R
import com.teumteumeat.teumteumeat.ui.component.DefaultMonoBg
import com.teumteumeat.teumteumeat.ui.component.loading.FullScreenLoading
import com.teumteumeat.teumteumeat.ui.screen.a1_login.webView.KakaoLoginWebViewActivity
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import kotlin.jvm.java

@Composable
fun LoginScreen(
    onGoogleClick: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val shape = RoundedCornerShape(28.dp)
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    // 🔥 이벤트 수신
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {

                is LoginUiEvent.LoginSuccess -> {
                    if (event.isOnboardingCompleted) {
                        // 메인 이동
                        Log.d("Login", "navigate main")
                    } else {
                        // 온보딩 이동
                        Log.d("Login", "navigate onboarding")
                    }
                }

                LoginUiEvent.NeedTermsAgreement -> {
                    Log.d("Login", "navigate terms")
                }
            }
        }
    }



    // ❌ 에러 표시
    uiState.errorMessage?.let { message ->
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }


    TeumTeumEatTheme {
        // 🔄 로딩 표시
        if (uiState.isLoading) {
            FullScreenLoading()
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "한줄 간단소개",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.tertiary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Image(
                    painter = painterResource(id = R.drawable.logo_login),
                    contentDescription = "메인 로고",
                    contentScale = ContentScale.Fit
                )
            }


            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 70.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(Color(0xFFFEE500))
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            Log.d("버튼 탭: ", "카카오 로그인 버튼")
                            val intent = Intent(context, KakaoLoginWebViewActivity::class.java)
                            intent.putExtra(
                                "url",
                                "${BuildConfig.BASE_DOMAIN}oauth2/authorization/kakao"
                            )
                            context.startActivity(intent)
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {

                        // 🔹 카카오 로고
                        Image(
                            painter = painterResource(id = R.drawable.icon_kakao_talk),
                            contentDescription = "KakaoTalk Logo",
                            contentScale = ContentScale.Fit
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        // 🔹 텍스트
                        Text(
                            text = "카카오계정 로그인",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF121212)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(Color(0xFFFFFFF))
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            shape = shape
                        )
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            Log.d("버튼 탭: ", "구글 로그인 버튼")
                            onGoogleClick()
                            // context.startActivity(intent)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {

                        // 🔹 구글 로고
                        Image(
                            painter = painterResource(id = R.drawable.icon_google),
                            contentDescription = "KakaoTalk Logo",
                            contentScale = ContentScale.Fit
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        // 🔹 텍스트
                        Text(
                            text = "구글계정 로그인",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF121212)
                        )
                    }
                }
            }

        }
    }


}




@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    val fakeViewModel : LoginViewModel = hiltViewModel()
    TeumTeumEatTheme {
        DefaultMonoBg(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface
        ) {
            LoginScreen({  }, fakeViewModel)
        }
    }
}

