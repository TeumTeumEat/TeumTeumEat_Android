package com.teumteumeat.teumteumeat.ui.screen.deeplink

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.teumteumeat.teumteumeat.ui.screen.a0_splash.SplashActivity
import com.teumteumeat.teumteumeat.ui.screen.a4_main.MainActivity
import com.teumteumeat.teumteumeat.utils.DeepLinkConstants
import com.teumteumeat.teumteumeat.utils.Utils

/**
 * App Link(https://teumteumeat.co.kr/league)의 유일한 착지점.
 * 화면을 그리지 않고 URI를 판정해 목적지로 이동시킨 뒤 즉시 종료된다.
 *
 * 리그 링크는 스플래시로 보내 로그인/온보딩 진행도를 확인한다 — 로그인·온보딩을 마친 유저만 홈 → 리그로 이동하고,
 * 미로그인이면 로그인 화면, 온보딩 중이면 온보딩 화면까지만 이동해 사용자가 진행 과정을 그대로 이어갈 수 있다.
 */
class DeepLinkRouterActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when (DeepLinkRouter.resolve(intent.data)) {
            is DeepLinkDestination.League -> {
                val splashIntent = Intent(this, SplashActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    putExtra(DeepLinkConstants.EXTRA_LEAGUE_DEEPLINK, true)
                }
                startActivity(splashIntent)
                finish()
            }

            is DeepLinkDestination.Invalid -> {
                Utils.UxUtils.moveActivity(this, MainActivity::class.java, clearTask = false)
            }
        }
    }
}
