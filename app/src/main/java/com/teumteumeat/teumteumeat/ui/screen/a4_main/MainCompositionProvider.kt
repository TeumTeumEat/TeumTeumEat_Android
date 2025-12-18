package com.teumteumeat.teumteumeat.ui.screen.a4_main

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.teumteumeat.teumteumeat.R
import com.teumteumeat.teumteumeat.ui.component.DefaultMonoBg
import com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_1_home.UiStateHome
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.LocalActivityContext
import com.teumteumeat.teumteumeat.utils.LocalAppContext
import com.teumteumeat.teumteumeat.utils.LocalMainUiState
import com.teumteumeat.teumteumeat.utils.LocalViewModelContext


@Composable
fun MainCompositionProvider(
    viewModel: MainViewModel,
    context: Context,
    activity: MainActivity,
) {

    val mainUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navHostController = rememberNavController()

    CompositionLocalProvider(
        LocalAppContext provides context,
        LocalActivityContext provides activity,
        LocalMainUiState provides mainUiState,
        LocalViewModelContext provides viewModel,

        ) {
        DefaultMonoBg(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                content = { padding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .systemBarsPadding()
                            .padding(),
                        verticalArrangement = Arrangement.SpaceBetween,
                    ) {
                        /**
                         * 홈화면 타이틀 바
                         */
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    vertical = 19.dp, horizontal = 24.dp,
                                ),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {

                            Image(
                                painter = painterResource(R.drawable.logo_home),
                                contentDescription = "home logo",
                                contentScale = ContentScale.None
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.icon_fire_fill),
                                    contentDescription = "home logo",
                                    contentScale = ContentScale.None
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    "0",
                                    style = MaterialTheme.typography.titleMedium,
                                )
                            }
                            IconButton(
                                onClick = {
                                },
                                modifier = Modifier.size(30.dp),

                                ) {
                                Icon(
                                    modifier = Modifier.padding(0.dp),
                                    imageVector = Icons.Rounded.Settings,
                                    contentDescription = "previous page"
                                )
                            }

                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            MainNavHost(
                                navController = navHostController,
                                startDestination = BottomNavItem.Home.route,
                                modifier = Modifier.padding(),
                            )
                        }
                    }
                },
                bottomBar = {
                    BottomNavigationBar(navHostController)
                }
            )

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

@Preview(showBackground = true)
@Composable
fun HomeMainFramePreview() {

    val fakeViewModel = remember { UiStateHome() }
    val navHostController = rememberNavController()

    TeumTeumEatTheme {
        DefaultMonoBg(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface
        ) {

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    BottomNavigationBar(navHostController)
                },
                content = { padding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .systemBarsPadding()
                            .padding(),
                        verticalArrangement = Arrangement.SpaceBetween,
                    ) {
                        /**
                         * 홈화면 타이틀 바
                         */
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    vertical = 19.dp, horizontal = 24.dp,
                                ),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {

                            Image(
                                painter = painterResource(R.drawable.logo_home),
                                contentDescription = "home logo",
                                contentScale = ContentScale.None
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.icon_fire_fill),
                                    contentDescription = "home logo",
                                    contentScale = ContentScale.None
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    "0",
                                    style = MaterialTheme.typography.titleMedium,
                                )
                            }
                            IconButton(
                                onClick = {
                                },
                                modifier = Modifier.size(30.dp),

                                ) {
                                Icon(
                                    modifier = Modifier.padding(0.dp),
                                    imageVector = Icons.Rounded.Settings,
                                    contentDescription = "previous page"
                                )
                            }

                        }

//                        HomeNavHost(
//                            navController = navHostController,
//                            startDestination = BottomNavItem.Home.route,
//                            modifier = Modifier.padding(),
//                        )
                    }
                }
            )
        }
    }
}