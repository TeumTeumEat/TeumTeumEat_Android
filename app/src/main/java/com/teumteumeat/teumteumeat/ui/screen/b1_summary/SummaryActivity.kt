package com.teumteumeat.teumteumeat.ui.screen.b1_summary

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.os.bundleOf
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.teumteumeat.teumteumeat.domain.model.goal.DomainGoalType
import com.teumteumeat.teumteumeat.ui.screen.b2_quiz.QuizActivity
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.LocalActivityContext
import com.teumteumeat.teumteumeat.utils.LocalAppContext
import com.teumteumeat.teumteumeat.utils.LocalScreenState
import com.teumteumeat.teumteumeat.utils.LocalSummaryUiState
import com.teumteumeat.teumteumeat.utils.LocalViewModelContext
import com.teumteumeat.teumteumeat.utils.Utils
import dagger.hilt.android.AndroidEntryPoint


object SummaryArgs {
    const val KEY_GOAL_ID = "key_goal_id"
    const val KEY_GOAL_TYPE = "key_goal_type"
    const val KEY_DOCUMENT_ID = "key_document_id"
    const val KEY_CATEGORY_ID = "key_category_id"
}

@AndroidEntryPoint
class SummaryActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val goalId = intent.getLongExtra(
            SummaryArgs.KEY_GOAL_ID,
            -1L
        )

        val goalType = intent.getStringExtra(
            SummaryArgs.KEY_GOAL_TYPE
        )?.let {
            DomainGoalType.valueOf(it)
        }

        val documentId = intent.getLongExtra(
            SummaryArgs.KEY_DOCUMENT_ID,
            -1L
        )
        Log.d("Debug_Summary", "Received documentId from Intent: $documentId")

        val categoryId = intent.getLongExtra(
            SummaryArgs.KEY_CATEGORY_ID,
            -1L
        ).takeIf { it != -1L }

        // 🔥 여기서 goalType 으로 분기
        when (goalType) {
            DomainGoalType.CATEGORY -> {
                // categoryId 기반 요약글 조회
            }

            DomainGoalType.DOCUMENT -> {
                // documentId 기반 요약글 조회
            }

            null -> {
                // 예외 처리 (잘못된 진입)
                finish()
            }
        }

        setContent {
            TeumTeumEatTheme {

                val viewModel: SummaryViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val screenState by viewModel.screenState.collectAsStateWithLifecycle()
                val navController = rememberNavController()

                // 1. 이벤트 수집 전용 LaunchedEffect (Composition 시작 시 즉시 구독)
                LaunchedEffect(viewModel.event) {
                    viewModel.event.collect { event ->
                        Log.d("SummaryActivity", "Event Received: $event")
                        when (event) {
                            is UiEvent.MoveToQuiz -> {

                                when(goalType){
                                    DomainGoalType.CATEGORY -> {
                                        if(uiState.categoryDocumentId != null){
                                            val categoryDocumentId = uiState.categoryDocumentId
                                            val intent = QuizActivity.newIntent(
                                                this@SummaryActivity,
                                                categoryDocumentId.toLong(),
                                                goalType.name
                                            )
                                            startActivity(intent)
                                            finish() // 다음 화면으로 이동 후 현재 화면 종료
                                        }else{
                                            Log.e("SummaryActivity", "Data Missing: Type or ID is invalid")
                                        }
                                    }
                                    DomainGoalType.DOCUMENT -> {
                                        if (documentId != -1L) {
                                            val intent = QuizActivity.newIntent(
                                                this@SummaryActivity,
                                                documentId,
                                                goalType.name
                                            )
                                            startActivity(intent)
                                            finish() // 다음 화면으로 이동 후 현재 화면 종료
                                        } else {
                                            Log.e("SummaryActivity", "Data Missing: Type or ID is invalid")
                                        }
                                    }
                                    null -> {}
                                }

                            }

                            is UiEvent.ShowError -> {
                                Toast.makeText(
                                    this@SummaryActivity,
                                    event.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }

                CompositionLocalProvider(
                    LocalAppContext provides this.applicationContext,
                    LocalActivityContext provides this@SummaryActivity,
                    LocalViewModelContext provides viewModel,
                    LocalSummaryUiState provides uiState,
                    LocalScreenState provides screenState,
                ) {
                    val context = LocalContext.current
                    val activity = LocalActivityContext.current as SummaryActivity

                    // 최초 진입 시 1회 호출
                    LaunchedEffect(Unit) {
                        if (goalType != null && goalId != -1L) {
                            viewModel.initSummary(
                                goalId = goalId,
                                goalType = goalType,
                                documentId = documentId,
                                categoryId = categoryId
                            )
                            // viewModel.loadInitialData()
                        } else {
                            Toast.makeText(
                                context,
                                "요약글 생성에 실패했어요. 다시 목표를 생성 해 주세요.",
                                Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }

                    SummaryNavHost(
                        navController = navController,
                        onGoQuizScreen = {
                            //  false -  가이드 화면으로 이동
                            //  true - 퀴즈 풀러 이동
                            if (!uiState.isQuizGuideSeen) {
                                navController.navigate(SummaryRoute.Guide.route)
                            } else {
                                viewModel.onQuizClick(uiState.isSkipQuizGuideChecked)
                            }
                        }
                    )
                }
            }
        }
    }
}