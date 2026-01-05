package com.teumteumeat.teumteumeat.ui.screen.b3_quiz_result

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.enum_type.GoalType
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.LocalActivityContext
import com.teumteumeat.teumteumeat.utils.LocalAppContext
import com.teumteumeat.teumteumeat.utils.LocalQuizResultUiState
import com.teumteumeat.teumteumeat.utils.LocalViewModelContext
import com.teumteumeat.teumteumeat.utils.Utils
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class QuizResultActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TeumTeumEatTheme {
                val viewModel : QuizResultViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val navController = rememberNavController()

                val goalId = Utils.PrefsUtil.getGoalId(applicationContext) ?: 0
                val documentId = Utils.PrefsUtil.getDocumentId(applicationContext) ?: 0
                val goalType = Utils.PrefsUtil.getGoalType(applicationContext)
                // ✅ 현재 날짜 (yyyy-MM-dd)
                val nowDate = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
                val userGoalType = Utils.PrefsUtil.getGoalType(applicationContext)
                val categoryId = Utils.PrefsUtil.getCategoryId(applicationContext) ?: -1


                // ⭐ 최초 진입 시 API 호출
                LaunchedEffect(Unit) {
                    viewModel.loadQuizResults(
                        type = goalType.name,
                        id = documentId,
                        date = nowDate
                    )
                    when(userGoalType) {
                        GoalType.DOCUMENT -> {
                            viewModel.loadDocumentSummary(goalId, documentId)
                        }
                        GoalType.CATEGORY -> {
                            viewModel.loadCategorySummary(categoryId)
                        }
                        GoalType.NONE -> TODO()
                    }
                }

                CompositionLocalProvider(
                    LocalAppContext provides this.applicationContext,
                    LocalActivityContext provides this@QuizResultActivity,
                    LocalViewModelContext provides viewModel,
                    LocalQuizResultUiState provides uiState,
                ) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize()
                    ) { @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter") it
                        QuizResultNavHost(navController)
                    }
                }
            }
        }
    }
}

