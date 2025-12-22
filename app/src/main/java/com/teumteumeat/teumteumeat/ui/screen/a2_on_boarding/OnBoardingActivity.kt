package com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.hilt.navigation.compose.hiltViewModel
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnBoardingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        setContent {
            TeumTeumEatTheme {
                val viewModel: OnBoardingViewModel = hiltViewModel()
                OnBoardingCompositionProvider(
                    viewModel = viewModel,
                    context = this.applicationContext,
                    activity = this@OnBoardingActivity
                )
            }
        }
    }


}



