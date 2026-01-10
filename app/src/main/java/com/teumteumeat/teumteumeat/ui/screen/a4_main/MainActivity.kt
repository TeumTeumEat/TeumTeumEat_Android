package com.teumteumeat.teumteumeat.ui.screen.a4_main

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import dagger.hilt.android.AndroidEntryPoint

object MainArgs {
    const val KEY_TARGET_SCREEN = "key_target_screen"
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val targetScreen = intent
            .getStringExtra(MainArgs.KEY_TARGET_SCREEN)
            ?.let { MainScreenType.valueOf(it) }


        setContent {

            TeumTeumEatTheme {
                val viewModel: MainViewModel = hiltViewModel()

                // 🔥 최초 진입 시에만 Intent 값 반영
                LaunchedEffect(Unit) {
                    Log.d("탭 변경 인자 디버깅", "targetScreen: ${targetScreen}")
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
}