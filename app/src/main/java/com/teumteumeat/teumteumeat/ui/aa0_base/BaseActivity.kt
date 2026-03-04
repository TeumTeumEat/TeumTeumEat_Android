package com.teumteumeat.teumteumeat.ui.aa0_base

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.teumteumeat.teumteumeat.ui.screen.a1_login.LoginActivity
import com.teumteumeat.teumteumeat.ui.screen.common_screen.ErrorState
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.monitor.NetworkConnection
import android.util.Log


abstract class BaseActivity : AppCompatActivity() {
    private lateinit var networkConnection: NetworkConnection
    private var composeErrorView: ComposeView? = null

    // 각 액티비티에서 구현해야 할 재시도 로직 (예: viewModel.fetchData())
    abstract fun onRetryClick()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        networkConnection = NetworkConnection(this)
        observeNetwork()
    }

    override fun onStart() {
        super.onStart()
        // 다른 화면에 있다가 돌아올 때(onRestart -> onStart) 마다 체크합니다.
        checkInitialNetworkState()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        checkInitialNetworkState()
    }

    private fun checkInitialNetworkState() {
        // LiveData의 관찰 시작 전, 현재 상태를 직접 확인
        val isConnected = networkConnection.isCurrentlyConnected

        Log.d("NetworkConnection", "checkInitialNetworkState called, isConnected: $isConnected")

        // 연결되지 않은 상태라면 즉시 에러 UI 표시
        if (!isConnected) {
            if (this is LoginActivity) {
                Toast.makeText(this, "네트워크 연결을 확인해 주세요.", Toast.LENGTH_SHORT).show()
            } else {
                Log.d("NetworkConnection", "set up error view")
                handleNetworkErrorComposeView(false)
            }
        }
    }

    private fun observeNetwork() {
        networkConnection.observe(this) { isConnected ->
            if (this is LoginActivity) {
                // 1. 로그인 액티비티: 토스트만 표시
                if (!isConnected) {
                    Toast.makeText(this, "네트워크 연결을 확인해 주세요.", Toast.LENGTH_SHORT).show()
                }
            } else {
                // 2. 그 외 액티비티: Compose 모달 표시/제거
                handleNetworkErrorComposeView(isConnected)
            }
        }
    }

    private fun handleNetworkErrorComposeView(isConnected: Boolean) {
        if (!isConnected) {
            if (composeErrorView == null) {
                // ComposeView를 동적으로 생성하여 최상단에 추가
                composeErrorView = ComposeView(this).apply {
                    // 중요: 아래 설정들이 터치 이벤트가 하단 뷰로 전달되는 것을 방지합니다.
                    isClickable = true
                    isFocusable = true

                    // ComposeView의 생명주기 관리 최적화
                    setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

                    setContent {
                        // 사용자 정의 테마로 감싸주세요 (예: MyTheme)
                        TeumTeumEatTheme {
                            MaterialTheme {
                                com.teumteumeat.teumteumeat.ui.component.FullScreenErrorModal(
                                    errorState = ErrorState(
                                        title = "네트워크 연결 끊김",
                                        description = "인터넷 연결이 원활하지 않습니다.\n네트워크 설정을 확인하고 다시 시도해주세요.",
                                        retryLabel = "다시 시도",
                                        onRetry = {
                                            // 추상 함수 호출 -> 자식 액티비티의 로직 실행
                                            checkConnectionAndRetry()
                                        }
                                    ),
                                    isShowBackBtn = false,
                                    onBack = { finish() } // 뒤로가기 시 액티비티 종료 등
                                )
                            }
                        }
                    }
                }

                // 현재 액티비티의 최상단 레이아웃에 추가
                addContentView(
                    composeErrorView,
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                )
            }
            composeErrorView?.visibility = View.VISIBLE
        }
    }

    /**
     * 재시도 버튼 클릭 시 호출되는 함수
     */
    private fun checkConnectionAndRetry() {
        if (networkConnection.isCurrentlyConnected) {
            // 1. 연결이 확인되면 그제서야 뷰를 숨깁니다.
            composeErrorView?.visibility = View.GONE
            // 2. 자식 액티비티의 데이터 로드 로직 실행
            onRetryClick()
        } else {
            // 아직 연결되지 않았다면 경고 메시지만 표시하고 뷰를 유지합니다.
            Toast.makeText(this, "네트워크 연결이 여전히 불안정합니다.", Toast.LENGTH_SHORT).show()
        }
    }
}