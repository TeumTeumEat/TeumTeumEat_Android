package com.teumteumeat.teumteumeat.ui.screen.deeplink

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.os.bundleOf
import com.teumteumeat.teumteumeat.data.network.model.TokenLocalDataSource
import com.teumteumeat.teumteumeat.ui.screen.a1_login.LoginActivity
import com.teumteumeat.teumteumeat.ui.screen.a4_main.MainActivity
import com.teumteumeat.teumteumeat.ui.screen.d0_league.LeagueActivity
import com.teumteumeat.teumteumeat.utils.DeepLinkConstants
import com.teumteumeat.teumteumeat.utils.Utils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * App Link(https://teumteumeat.com/league)의 유일한 착지점.
 * 화면을 그리지 않고 URI를 판정해 목적지로 이동시킨 뒤 즉시 종료된다.
 */
@AndroidEntryPoint
class DeepLinkRouterActivity : ComponentActivity() {

    @Inject
    lateinit var tokenLocalDataSource: TokenLocalDataSource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val hasRefreshToken = tokenLocalDataSource.getRefreshToken() != null

        when (DeepLinkRouter.resolve(intent.data, hasRefreshToken)) {
            is DeepLinkDestination.League -> {
                // 백스택에 MainActivity를 남겨 League에서 뒤로가기 시 메인으로 돌아가도록 한다
                Utils.UxUtils.moveActivityWithBackStack(
                    this,
                    backStackRootActivity = MainActivity::class.java,
                    destinationActivity = LeagueActivity::class.java,
                )
            }

            is DeepLinkDestination.LoginThenLeague -> {
                Utils.UxUtils.moveActivity(
                    this,
                    LoginActivity::class.java,
                    clearTask = false,
                    extras = bundleOf(DeepLinkConstants.EXTRA_PENDING_LEAGUE_REDIRECT to true),
                )
            }

            is DeepLinkDestination.Invalid -> {
                Utils.UxUtils.moveActivity(this, MainActivity::class.java, clearTask = false)
            }
        }
    }
}
