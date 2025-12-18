package com.teumteumeat.teumteumeat.ui.screen.a1_auth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import com.teumteumeat.teumteumeat.ui.screen.a4_main.MainCompositionProvider
import com.teumteumeat.teumteumeat.ui.screen.a4_main.MainViewModel
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import kotlin.getValue

class LoginActivity : ComponentActivity()  {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val viewModel: MainViewModel by viewModels()

        setContent {
            TeumTeumEatTheme {
                LoginScreen()
//                MainCompositionProvider(
//                    viewModel = viewModel,
//                    context = this.applicationContext,
//                    activity = this@MainActivity,
//                )
            }
        }
    }
}
