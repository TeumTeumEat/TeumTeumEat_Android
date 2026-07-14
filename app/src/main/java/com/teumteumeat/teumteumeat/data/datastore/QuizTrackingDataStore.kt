package com.teumteumeat.teumteumeat.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

private const val QUIZ_TRACKING_DATASTORE_NAME = "quiz_tracking"

val Context.quizTrackingDataStore: DataStore<Preferences> by preferencesDataStore(
    name = QUIZ_TRACKING_DATASTORE_NAME
)

/**
 * QUIZ-001 `quiz_start` 이벤트의 `entry_type` 판단을 위한 퀴즈 진입/완료 이력 저장소.
 *
 * - `entered_quiz_ids`: QuizScreen 최초 진입 시 documentId 누적
 * - `completed_quiz_ids`: complete-set API 성공 시 documentId 누적
 *
 * 앱 재설치·데이터 초기화 시 이력이 모두 소실되어 entry_type이 "first"로 재발생하는 것은 의도된 동작.
 */
@Singleton
class QuizTrackingDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    companion object {
        private val COMPLETED_QUIZ_IDS = stringSetPreferencesKey("completed_quiz_ids")
        private val ENTERED_QUIZ_IDS = stringSetPreferencesKey("entered_quiz_ids")
        private val TOTAL_QUESTIONS_ANSWERED = intPreferencesKey("total_questions_answered")
    }

    /** complete-set API 성공 시 호출 — documentId를 완료 이력에 추가합니다. */
    suspend fun markQuizCompleted(documentId: String) {
        dataStore.edit { prefs ->
            prefs[COMPLETED_QUIZ_IDS] = (prefs[COMPLETED_QUIZ_IDS] ?: emptySet()) + documentId
        }
    }

    /**
     * QuizScreen 진입 시 entry_type을 판단하고, 최초 진입이면 진입 이력에 기록합니다.
     *
     * 판단 순서:
     * 1. `completed_quiz_ids`에 포함 → "retry"
     * 2. `entered_quiz_ids`에만 포함 → "resume"
     * 3. 둘 다 없음 → "first" (동시에 `entered_quiz_ids`에 추가)
     */
    suspend fun resolveEntryType(documentId: String): String {
        val prefs = dataStore.data
            .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
            .first()
        val completedIds = prefs[COMPLETED_QUIZ_IDS] ?: emptySet()
        val enteredIds = prefs[ENTERED_QUIZ_IDS] ?: emptySet()

        return when {
            completedIds.contains(documentId) -> "retry"
            enteredIds.contains(documentId) -> "resume"
            else -> {
                dataStore.edit { it[ENTERED_QUIZ_IDS] = enteredIds + documentId }
                "first"
            }
        }
    }

    /**
     * QUIZ-002 `quiz_answer_submit`의 `question_no` / User Property
     * `total_questions_answered`용 전역 누적 카운터. 문서 구분 없이 항상 증가하며
     * 재도전(retry) 시에도 초기화되지 않는다.
     */
    suspend fun incrementAndGetTotalQuestionsAnswered(): Int {
        var newValue = 0
        dataStore.edit { prefs ->
            newValue = (prefs[TOTAL_QUESTIONS_ANSWERED] ?: 0) + 1
            prefs[TOTAL_QUESTIONS_ANSWERED] = newValue
        }
        return newValue
    }
}
