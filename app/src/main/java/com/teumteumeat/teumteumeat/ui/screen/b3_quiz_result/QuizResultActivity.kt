package com.teumteumeat.teumteumeat.ui.screen.b3_quiz_result

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
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
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.LocalActivityContext
import com.teumteumeat.teumteumeat.utils.LocalAppContext
import com.teumteumeat.teumteumeat.utils.LocalQuizResultUiState
import com.teumteumeat.teumteumeat.utils.LocalScreenState
import com.teumteumeat.teumteumeat.utils.LocalViewModelContext
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class QuizResultActivity : ComponentActivity() {
    companion object {
        const val EXTRA_DOCUMENT_ID = "extra_document_id"
        const val INVALID_DOCUMENT_ID = -1L

        const val GOAL_TYPE = "goal_type"
        const val INVAILD_GOAL_TYPE = -1

        fun newIntent(
            context: Context,
            documentId: Long,
        ): Intent {
            return Intent(context, QuizResultActivity::class.java).apply {
                putExtra(EXTRA_DOCUMENT_ID, documentId)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val documentId = intent.getLongExtra(EXTRA_DOCUMENT_ID, INVALID_DOCUMENT_ID)

        if (documentId == INVALID_DOCUMENT_ID) {
            // 필수 값 누락 → 방어 코드
            finish()
            return
        }

        enableEdgeToEdge()
        setContent {
            TeumTeumEatTheme {
                val viewModel : QuizResultViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val navController = rememberNavController()
                val screenState by viewModel.screenState.collectAsStateWithLifecycle()

                // ⭐ 최초 진입 시 API 호출
                LaunchedEffect(Unit) {
                    val nowDate = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
                    viewModel.initArgs(
                        documentId = documentId,
                        date = nowDate
                    )

                    viewModel.initQuizResult()
                }

                CompositionLocalProvider(
                    LocalAppContext provides this.applicationContext,
                    LocalActivityContext provides this@QuizResultActivity,
                    LocalViewModelContext provides viewModel,
                    LocalQuizResultUiState provides uiState,
                    LocalScreenState provides screenState,
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

