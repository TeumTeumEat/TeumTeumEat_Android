package com.teumteumeat.teumteumeat.ui.screen.a4_main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import dagger.hilt.android.AndroidEntryPoint
import java.time.YearMonth
import javax.inject.Inject

object MainArgs {
    const val KEY_TARGET_SCREEN = "key_target_screen"
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    @Inject
    lateinit var appResumeNotifier: AppResumeNotifier

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Log.d("MainActivity", "onCreate: taskId=$taskId, hash=${hashCode()}")

        val targetScreen = intent
            .getStringExtra(MainArgs.KEY_TARGET_SCREEN)
            ?.let { MainScreenType.valueOf(it) }


        setContent {

            TeumTeumEatTheme {

                // 🔥 최초 진입 시에만 Intent 값 반영
                LaunchedEffect(Unit) {
                    viewModel.loadCalendarHistory(YearMonth.now())
                    if (targetScreen != null) {
                        viewModel.onScreenChanged(targetScreen, from = "MainActivity")
                    }
                }

                MainCompositionProvider(
                    viewModel = viewModel,
                    context = this.applicationContext,
                    activity = this@MainActivity,
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        appResumeNotifier.notifyResume()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d("MainActivity", "onNewIntent: taskId=$taskId, hash=${hashCode()}")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MainActivity", "onDestroy: taskId=$taskId, hash=${hashCode()}")
    }

}