package com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

private fun openNotificationSetting(activity: Activity) {
    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Intent("android.settings.APP_NOTIFICATION_SETTINGS").apply {
            putExtra("android.provider.extra.APP_PACKAGE", activity.packageName)
        }
    } else {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", activity.packageName, null)
        }
    }
    activity.startActivity(intent)
}

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
            // 🔔 설정 안내 오버레이
            NotificationSettingGuideOverlay(
                uiState = uiState,
                onConfirm = {
                    viewModel.openNotificationSetting()
                },
                onDismiss = {
                    viewModel.closeNotificationDisableGuide()
                }
            )

            /* ------------------------------
         * 2️⃣ 알림 설정 안내 Overlay (최상단)
         * ------------------------------ */
            NotificationSettingGuideOverlay(
                uiState = uiState,
                onConfirm = {
                    // 설정으로 이동
                    openNotificationSetting(activity)
                    viewModel.closeNotificationSettingGuide()
                },
                onDismiss = {
                    viewModel.closeNotificationSettingGuide()
                }
            )

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