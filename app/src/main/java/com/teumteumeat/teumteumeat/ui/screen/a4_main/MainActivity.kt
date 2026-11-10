package com.teumteumeat.teumteumeat.ui.screen.a4_main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import com.teumteumeat.teumteumeat.ui.aa0_base.BaseActivity
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import dagger.hilt.android.AndroidEntryPoint
import java.time.YearMonth

object MainArgs {
    const val KEY_TARGET_SCREEN = "key_target_screen"

    /** true로 전달되면, 홈 진입 시 간식이 없는 상태(SnackState.Available이 아님)라면 쿠폰 충전 팝업을 바로 띄운다. */
    const val KEY_OPEN_COUPON_ON_NO_SNACK = "key_open_coupon_on_no_snack"
}

@AndroidEntryPoint
class MainActivity : BaseActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onRetryClick() {
        viewModel.loadCalendarHistory(YearMonth.now())
        viewModel.triggerRetry()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        Log.d("MainActivity", "onCreate: taskId=$taskId, hash=${hashCode()}")

        val targetScreen = intent
            .getStringExtra(MainArgs.KEY_TARGET_SCREEN)
            ?.let { MainScreenType.valueOf(it) }
        val openCouponOnNoSnack = intent.getBooleanExtra(MainArgs.KEY_OPEN_COUPON_ON_NO_SNACK, false)


        setContent {

            TeumTeumEatTheme {

                // 🔥 최초 진입 시에만 Intent 값 반영
                LaunchedEffect(Unit) {
                    if (targetScreen != null) {
                        viewModel.onScreenChanged(targetScreen, from = "MainActivity")
                    }
                }

                MainCompositionProvider(
                    viewModel = viewModel,
                    context = this.applicationContext,
                    activity = this@MainActivity,
                    openCouponOnNoSnack = openCouponOnNoSnack,
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d("MainActivity", "onNewIntent: taskId=$taskId, hash=${hashCode()}")
        setIntent(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MainActivity", "onDestroy: taskId=$taskId, hash=${hashCode()}")
    }

}