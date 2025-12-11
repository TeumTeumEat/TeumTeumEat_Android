package com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme

class OnBoardingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val viewModel: OnBoardingViewModel by viewModels()

        setContent {
            TeumTeumEatTheme {
                OnBoardingCompositionProvider(
                    viewModel = viewModel,
                    context = this.applicationContext,
                    activity = this@OnBoardingActivity
                )
            }
        }
    }


}



