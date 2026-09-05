package com.teumteumeat.teumteumeat.ui.screen.a1_login.webView

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.teumteumeat.teumteumeat.ui.screen.a4_main.MainActivity
import com.teumteumeat.teumteumeat.utils.firebase.TeumCrashlyticsLogger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class KakaoLoginWebViewActivity : AppCompatActivity() {

    private val viewModel: KakaoLoginViewModel by viewModels()

    @Inject
    lateinit var crashlyticsLogger: TeumCrashlyticsLogger

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔐 안전성 체크 — "url" extra 없이 진입하면 로그인을 진행할 수 없으므로 원인 기록 후 종료
        val url = intent.getStringExtra(EXTRA_URL)
        if (url == null) {
            crashlyticsLogger.log("KakaoLoginWebViewActivity '$EXTRA_URL' extra 누락으로 진입 차단")
            crashlyticsLogger.recordNonFatal(
                IllegalStateException("KakaoLoginWebViewActivity launched without '$EXTRA_URL' intent extra")
            )
            finish()
            return
        }

        val webView = WebView(this)

        setContentView(webView)

        observeUiState()
        setupWebView(webView)

        webView.loadUrl(url)
    }

    private fun setupWebView(webView: WebView) {
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true

        webView.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val uri = request?.url ?: return false

                // ✅ OAuth Redirect 성공 URL
                if (
                    uri.host == "api.teumteumeat.co.kr" &&
                    uri.path == "/api/v1/users/auth/success"
                ) {
                    val accessToken = uri.getQueryParameter("accessToken")
                    val refreshToken = uri.getQueryParameter("refreshToken")

                    when {
                        !accessToken.isNullOrBlank() && !refreshToken.isNullOrBlank() -> {
                            viewModel.onKakaoAuthCodeReceived(
                                accessToken = accessToken,
                                refreshToken = refreshToken
                            )
                            navigateToMain()
                        }

                        else -> {
                            viewModel.onKakaoLoginFailed("unknown_response")
                            Log.d("웹뷰 로그인 절차: ", "로그인 실패")
                        }
                    }
                    return true
                }


                return false
            }
        }
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is KakaoLoginUiState.Success -> {
                            Log.d("웹뷰 로그인 절차: ", "웹뷰 리다이렉트 성공 -> 엑티비티 이동 시작")
                            // ✅ 로그인 성공 → 메인 화면 이동
                            navigateToMain()
                        }

                        else -> Unit
                    }
                }
            }
        }
    }

    private fun navigateToMain() {
        startActivity(
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        )
        finish()
    }

    companion object {
        private const val EXTRA_URL = "url"
    }
}
