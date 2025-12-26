package com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.teumteumeat.teumteumeat.ui.component.CustomProgressBar
import com.teumteumeat.teumteumeat.ui.component.DefaultMonoBg
import com.teumteumeat.teumteumeat.ui.component.SizeAnimationInvisible
import com.teumteumeat.teumteumeat.utils.LocalActivityContext
import com.teumteumeat.teumteumeat.utils.LocalAppContext
import com.teumteumeat.teumteumeat.utils.LocalOnBoardingMainUiState
import com.teumteumeat.teumteumeat.utils.LocalViewModelContext


@Composable
fun OnBoardingCompositionProvider(
    viewModel: OnBoardingViewModel,
    context: Context,
    activity: OnBoardingActivity,
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navHostController = rememberNavController()

    CompositionLocalProvider(
        LocalAppContext provides context,
        LocalActivityContext provides activity,
        LocalOnBoardingMainUiState provides uiState,
        LocalViewModelContext provides viewModel,

        ) {
        DefaultMonoBg(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
            ) {
                Row(
                    modifier = Modifier.padding(
                        horizontal = 24.dp,
                    ),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically,
                ) {

                    SizeAnimationInvisible(
                        isVisible = uiState.currentPage > 0
                    ) {
                        IconButton(
                            onClick = {
                                viewModel.prevPage()
                                navHostController.popBackStack()
                            },

                            ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                                contentDescription = "previous page",
                                modifier = Modifier
                                    .size(40.dp)
                                    .padding(0.dp),
                            )
                        }
                    }


                    CustomProgressBar(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        currentStep = uiState.currentPage,
                        totalSteps = uiState.totalPage,
                    )


                    Text(
                        "${uiState.currentPage}/${uiState.totalPage}",
                        maxLines = 1,
                        softWrap = false
                    )
                }
                OnBoardingNavHost(
                    navController = navHostController,
                )
            }
        }

        /*viewModel?.let { viewModel ->
            val splashResult by viewModel..collectAsState()
            splashResult?.let { result ->
                CompositionLocalProvider(LocalSplashResult provides result) {
                    // Log.d(activity.TAG, "Result: welComeResult")
                }
            } ?: Scaffold(content = { paddingValues ->
                DefaultMonoBg(
                    innerPadding = paddingValues,
                    color = MaterialTheme.colorScheme.surface
                )
            })
        }*/
    }

}