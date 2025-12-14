package com.teumteumeat.teumteumeat.ui.screen.a4_main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val viewModel: MainViewModel by viewModels()

        setContent {
            TeumTeumEatTheme {
                HomeCompositionProvider(
                    viewModel = viewModel,
                    context = this.applicationContext,
                    activity = this@MainActivity,
                )
            }
        }
    }


}



