package com.teumteumeat.teumteumeat.ui.screen.a1_login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.AuthError
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.common.model.KakaoSdkError
import com.kakao.sdk.user.UserApiClient
import com.teumteumeat.teumteumeat.R
import com.teumteumeat.teumteumeat.data.repository.login.SocialLoginRepository
import com.teumteumeat.teumteumeat.ui.screen.common_screen.AuthBlockingOverlay
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.LocalActivityContext
import com.teumteumeat.teumteumeat.utils.LocalAppContext
import com.teumteumeat.teumteumeat.utils.LocalLoginUiState
import com.teumteumeat.teumteumeat.utils.LocalViewModelContext
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException

@AndroidEntryPoint
class LoginActivity : ComponentActivity()  {
    private lateinit var googleClient: GoogleSignInClient

    private val viewModel: LoginViewModel by viewModels()


    override fun onResume() {
        super.onResume()
        Log.d("KakaoLife", "onResume")
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d("KakaoLife", "onNewIntent: $intent")
    }

    override fun onPause() {
        super.onPause()
        Log.d("KakaoLife", "onPause")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("KakaoLife", "onCreate")
        enableEdgeToEdge()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.google_web_client_id))
            .requestEmail()
            .build()
        googleClient = GoogleSignIn.getClient(this, gso)

        val launcher =
            registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
        ) { result ->

            // ⭐ 어떤 경우든 일단 해제
            viewModel.endAuthBlocking()

            Log.d("GoogleLogin", "resultCode=${result.resultCode}")
            Log.d("GoogleLogin", "data=${result.data}")
            if (result.resultCode != RESULT_OK) {
                Log.e("GoogleLogin", "RESULT NOT OK")
                return@registerForActivityResult
            }

            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                Log.d("GoogleLogin", "task=$task")

                val account = task.getResult(ApiException::class.java)
                Log.d("GoogleLogin", "account=$account")

                val idToken = account.idToken
                Log.d("GoogleLogin", "idToken=$idToken")

                viewModel.loginWithGoogle(idToken.toString())

            } catch (e: ApiException) {
                Log.e("GoogleLogin", "ApiException code=${e.statusCode}", e)
                viewModel.setAuthErrorPage("statusCode: ${e.statusCode}\n message: ${e.message}\n cause: ${e.stackTrace}")
            }
        }

        setContent {
            TeumTeumEatTheme {
                val viewModel: LoginViewModel = hiltViewModel()
                val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

                CompositionLocalProvider(
                    LocalAppContext provides this.applicationContext,
                    LocalActivityContext provides this,
                    LocalViewModelContext provides viewModel,
                    LocalLoginUiState provides uiState,
                ){
                    Box(modifier = Modifier.fillMaxSize()) {
                        LoginScreen(
                            onGoogleLoginClick = {
                                viewModel.setCurrentPendingLogin(SocialProvider.GOOGLE)
                                viewModel.startAuthBlocking() // ⭐ UI 잠금
                                launcher.launch(googleClient.signInIntent)
                            },
                            onKakaoLoginClick = {
                                viewModel.setCurrentPendingLogin(SocialProvider.KAKAO)
                                viewModel.startAuthBlocking()
                                loginWithKakao(
                                    context = this@LoginActivity,
                                    onSuccess = { idToken, authCode ->
                                        Log.d(
                                            "kakao 로그인",
                                            "idToken: ${idToken}, authCode: ${authCode}"
                                        )
                                        viewModel.loginWithKakaoServer(idToken, authCode)
                                    },
                                    onError = { error ->
                                        viewModel.endAuthBlocking()
                                        when (error) {
                                            KakaoLoginError.UserCancelled -> {
                                                // 아무 처리 안 함
                                            }

                                            KakaoLoginError.NetworkError -> {
                                                showToast("네트워크 상태를 확인해주세요")
                                            }

                                            KakaoLoginError.KakaoAppNotAvailable -> {
                                                showToast("카카오 계정 로그인을 시도해주세요")
                                            }

                                            KakaoLoginError.AuthFailed -> {
                                                showToast("로그인에 실패했어요. 다시 시도해주세요")
                                                // todo. 카카오 로그인 에러 디버깅 텍스트 세팅
                                                viewModel.setAuthErrorPage(error.toString())
                                            }

                                            is KakaoLoginError.Unknown -> {
                                                Log.d("Kakao Login: ", "error: ${error.throwable}")
                                                showToast("알 수 없는 오류가 발생했어요")
                                            }
                                        }
                                    }
                                )
                            },
                            viewModel = viewModel
                        )

                        // 🚫 인증 대기 오버레이
                        if (uiState.isAuthBlocking) {
                            AuthBlockingOverlay()
                        }
                    }
                }
            }
        }
    }

    fun showToast(message: String){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


    fun loginWithKakao(
        context: Context,
        onSuccess: (idToken: String, authCode: String?) -> Unit,
        onError: (KakaoLoginError) -> Unit
    ) {
        val callback: (OAuthToken?, Throwable?) -> Unit =
            callback@{ token, error ->
                Log.d("KakaoRedirect", "callback entered. token=$token, error=$error")
                // ⭐⭐⭐ 핵심: 인증 종료 → UI 잠금 해제
                viewModel.endAuthBlocking()

                // 1️⃣ 에러 우선 처리
                if (error != null) {
                    logKakaoError(t = error)
                    val kakaoError = when (error) {

                        // ✅ 사용자 취소
                        is ClientError -> {
                            when (error.reason) {
                                ClientErrorCause.Cancelled ->
                                    KakaoLoginError.UserCancelled

                                ClientErrorCause.NotSupported ->
                                    KakaoLoginError.KakaoAppNotAvailable

                                ClientErrorCause.IllegalState ->
                                    KakaoLoginError.AuthFailed

                                else ->
                                    KakaoLoginError.Unknown(error)
                            }
                        }

                        // ✅ SDK 내부 오류
                        is KakaoSdkError -> {
                            KakaoLoginError.AuthFailed

                        }

                        // ✅ 네트워크 계열
                        is IOException ->
                            KakaoLoginError.NetworkError

                        // ✅ 그 외
                        else ->
                            KakaoLoginError.Unknown(error)
                    }

                    onError(kakaoError)
                    return@callback
                }

                // 2️⃣ 토큰 성공
                if (token != null) {
                    val idToken = token.idToken
                    Log.d("kakaologin", "idToken: ${idToken}")

                    // ⚠️ OIDC 미설정 등으로 idToken 이 없는 경우
                    if (idToken.isNullOrEmpty()) {
                        onError(KakaoLoginError.AuthFailed)
                        return@callback
                    }

                    onSuccess(
                        idToken,
                        token.accessToken // 서버에서 authCode 대용
                    )
                } else {
                    onError(KakaoLoginError.AuthFailed)
                }
        }

        // ✅ 1차: 카카오톡 로그인 시도
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
            UserApiClient.instance.loginWithKakaoTalk(
                context = context,
                callback = { token, error ->

                    // 🔥 해결책 1의 핵심
                    if (
                        error is AuthError &&
                        error.response.error == "NotSupportError"
                    ) {
                        Log.d("KakaoFlow", "⚠️ NotSupportError → fallback to WEB")

                        // 👉 카카오톡은 있으나 계정 로그인 안 됨 → 계정 로그인으로 fallback
                        UserApiClient.instance.loginWithKakaoAccount(
                            context = context,
                            callback = callback
                        )
                        return@loginWithKakaoTalk
                    }

                    // 그 외 경우는 공통 콜백으로 처리
                    callback(token, error)
                }
            )
        } else {
            // ✅ 카카오톡 미설치 → 바로 웹로그인
            // showToast("카카오톡 앱이 필요합니다")
            Log.d("KakaoLogin", "KakaoTalk app not installed → web login")
            UserApiClient.instance.loginWithKakaoAccount(
                context = context,
                callback = callback
            )
        }
    }

    private fun logKakaoError(tag: String = "KakaoLogin", t: Throwable) {
        // ✅ 가장 기본: 타입/메시지/스택
        Log.e(tag, "type=${t::class.qualifiedName}")
        Log.e(tag, "message=${t.message}")
        Log.e(tag, "localizedMessage=${t.localizedMessage}")
        Log.e(tag, "cause=${t.cause?.javaClass?.name} / ${t.cause?.message}")
        Log.e(tag, "stacktrace", t)

        // ✅ KakaoSdkError면 추가로 toString()도 찍어보기 (내부 정보가 더 나오는 케이스가 있음)
        if (t is com.kakao.sdk.common.model.KakaoSdkError) {
            android.util.Log.e(tag, "KakaoSdkError.toString=${t.toString()}")
        }
    }



}
