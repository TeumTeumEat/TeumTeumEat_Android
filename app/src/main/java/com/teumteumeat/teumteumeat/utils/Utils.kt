package com.teumteumeat.teumteumeat.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.net.toUri
import com.google.firebase.messaging.FirebaseMessaging
import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model.uiMessage
import com.teumteumeat.teumteumeat.di.NotificationEntryPoint
import com.teumteumeat.teumteumeat.domain.model.common.GoalTypeUiState
import com.teumteumeat.teumteumeat.domain.model.goal.Difficulty
import com.teumteumeat.teumteumeat.domain.model.goal.DomainGoalType
import com.teumteumeat.teumteumeat.domain.model.on_boarding.TimeState
import com.teumteumeat.teumteumeat.ui.component.AmPm
import dagger.hilt.android.EntryPointAccessors
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import java.io.FileInputStream
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoField
import java.util.Locale
import java.util.Properties

sealed interface NotificationPermissionEvent {
    data object RequestPermission : NotificationPermissionEvent
    data object AlreadyGranted : NotificationPermissionEvent
}

class ContractViolationException(
    override val message: String
) : RuntimeException(message)

class Utils {

    object RepositoryUtils{
        /**
         * Repository mapper 전용 null 처리 유틸
         *
         * - null 이면 error(...) 호출
         * - error 는 IllegalStateException 을 던짐
         * - safeApiVer2 에서 catch 되어 UnknownError 로 변환됨
         *
         * @param requestUrl 디버깅 및 프론트 노출용 메시지에 포함할 API 경로
         */
        inline fun <T : Any> T?.requireNotNullOrError(): T {
            if (this == null) {
                error("서버 응답값이 null 입니다.")
            }
            return this
        }
    }

    object TypeUtils{

        private val DATE_YYYY_MM_DD =
            DateTimeFormatter.ofPattern("yyyy-MM-dd")

        private val DATE_MM_DD = DateTimeFormatter.ofPattern("MM.dd")

        fun String.toMMdd(): String {
            return LocalDateTime.parse(this)
                .toLocalDate()
                .format(DateTimeFormatter.ofPattern("MM.dd"))
        }

        /**
         * LocalDate → yyyy-MM-dd
         * (API query / 서버 요청용)
         */
        fun LocalDate.toYyyyMmDd(): String {
            return this.format(DATE_YYYY_MM_DD)
        }

        /**
         * LocalDate → MM.dd
         * (UI 표시용)
         */
        fun LocalDate.toMmDd(): String {
            return this.format(DATE_MM_DD)
        }

        /**
         * LocalDate → YearMonth
         * (서버 전송용 / 월 단위 요청)
         */
        fun LocalDate.toYearMonth(): YearMonth {
            return YearMonth.from(this)
        }

        /**
         * LocalDateTime을 yyyy-MM-dd 문자열로 변환
         * (API 요청 / 날짜 비교용)
         */
        fun LocalDateTime.toYyyyMmDd(): String {
            return this
                .toLocalDate()
                .format(DATE_YYYY_MM_DD)
        }

        /**
         * 서버에서 내려온 날짜 문자열을 yyyy-MM-dd 형태로 변환
         *
         * 실패 시 빈 문자열 반환
         */
        fun String?.toYyyyMmDdOrEmpty(): String {
            if (this.isNullOrBlank()) return ""

            return try {
                LocalDateTime.parse(this)
                    .toLocalDate()
                    .format(DATE_YYYY_MM_DD)
            } catch (e: DateTimeParseException) {
                ""
            }
        }


        fun Difficulty.toUiText(): String =
            when (this) {
                Difficulty.EASY -> "난이도 하"
                Difficulty.MEDIUM -> "난이도 중"
                Difficulty.HARD -> "난이도 상"
                Difficulty.NONE -> ""
            }

        fun formatDate(date: LocalDate): String {
            return "${date.monthValue}월 ${date.dayOfMonth}일"
        }

    }

    object UiUtils{

        private class NoRippleInteractionSource : MutableInteractionSource {
            override val interactions: Flow<Interaction> = emptyFlow()
            override suspend fun emit(interaction: Interaction) {}
            override fun tryEmit(interaction: Interaction) = true
        }

        @Composable
        fun noRipple(): MutableInteractionSource = remember { NoRippleInteractionSource() }

        fun formatTestTime(timeTaken: Int, examDuration: Int = 0): String {
            val showedTimeSecond = if( examDuration == 0) timeTaken else examDuration - timeTaken
            val minutes = showedTimeSecond / 60
            val seconds = showedTimeSecond % 60
            return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        }

        /**
         * 주어진 문자열을 지정된 최대 문자 수(`maxCharsPerLine`)와 최대 라인 수(`maxLines`)에 맞게 포맷팅합니다.
         *
         * - 각 줄은 최대 `maxCharsPerLine` 개의 문자로 제한됩니다.
         * - 총 라인은 `maxLines`을 초과하지 않습니다.
         * - 만약 `text`가 지정된 제한을 초과하면 `...`을 추가하여 생략합니다.
         *
         * @param text 포맷팅할 원본 문자열
         * @param maxCharsPerLine 한 줄에 허용되는 최대 문자 수
         * @param maxLines 표시할 최대 줄 수
         * @return 지정된 문자 및 줄 제한에 맞춰 포맷된 문자열
         */
        fun formatTextWithLineLimit(text: String, maxCharsPerLine: Int, maxLines: Int): String {
            val words = text.split(" ") // 단어 단위로 분리
            val result = mutableListOf<String>()
            var currentLine = ""

            for (word in words) {
                if ((currentLine.length + word.length) <= maxCharsPerLine) {
                    if (currentLine.isNotEmpty()) currentLine += " "
                    currentLine += word
                } else {
                    result.add(currentLine)
                    currentLine = word
                }
                if (result.size >= maxLines) break // 최대 줄 수를 초과하면 종료
            }

            if (currentLine.isNotEmpty() && result.size < maxLines) {
                result.add(currentLine)
            }

            return result.joinToString("\n") // 줄바꿈으로 연결하여 반환
        }

        /**
         * 서버 날짜 포맷("yyyy-MM-dd")을
         * UI 표시용 포맷으로 변환
         */
        fun String.toUiDateFormat(
            outputPattern: String = "yyyy.MM.dd"
        ): String {
            return try {
                val serverFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val uiFormatter = DateTimeFormatter.ofPattern(outputPattern)

                LocalDate
                    .parse(this, serverFormatter)
                    .format(uiFormatter)
            } catch (e: Exception) {
                this // 파싱 실패 시 원본 반환 (안전장치)
            }
        }

        private val TIME_FORMATTER: DateTimeFormatter =
            DateTimeFormatter.ofPattern("HH:mm:ss")

        fun isValidTime(time: String?): Boolean {
            if (time.isNullOrBlank()) return false

            return try {
                LocalTime.parse(time, TIME_FORMATTER)
                true
            } catch (e: DateTimeParseException) {
                false
            }
        }

        fun TimeState.to24HourString(): String? {
            val hour24 = when (amPm) {
                AmPm.AM -> {
                    if (hour == 12) 0 else hour
                }
                AmPm.PM -> {
                    if (hour == 12) 12 else hour + 12
                }
            }

            // 🚫 24시 이상 차단
            if (hour24 !in 0..23) return null

            return "%02d:%02d:00".format(hour24, minute)
        }

        fun TimeState.to24Hour(): Pair<Int, Int> {
            val hour24 = when (amPm) {
                AmPm.AM -> {
                    if (hour == 12) 0 else hour
                }
                AmPm.PM -> {
                    if (hour == 12) 12 else hour + 12
                }
            }.coerceIn(0, 23) // 🔒 24시 초과 방지

            return hour24 to minute
        }


        fun TimeState.normalizeTo12Hour(): TimeState {

            val hour12 = when (hour) {
                0 -> 12
                in 1..12 -> hour
                in 13..23 -> hour - 12
                else -> 12
            }

            val newAmPm = if (hour in 0..11) AmPm.AM else AmPm.PM

            return copy(
                hour = hour12,
                amPm = newAmPm
            )
        }

        /**
         * 앱의 알림이 실제로 사용자에게 전달될 수 있는 상태인지 통합 확인합니다.
         * 1. 시스템 설정에서 앱 알림이 켜져 있는지 확인
         * 2. Android 13 이상일 경우 POST_NOTIFICATIONS 권한 허용 여부 확인
         *
         * @return 알림 권한과 설정이 모두 허용된 경우 true
         */
        fun checkNotificationPermission(context: Context): Boolean {
            val isSystemEnabled = areAppNotificationsEnabled(context)
            val isRuntimeGranted = isPostNotificationsGranted(context)

            return isSystemEnabled && isRuntimeGranted
        }

        fun isPostNotificationsGranted(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= 33) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        }

        fun areAppNotificationsEnabled(context: Context): Boolean {
            return NotificationManagerCompat.from(context).areNotificationsEnabled()
        }

    }

    object DailySummaryArgs {
        const val KEY_ID = "key_id"
        const val KEY_TYPE = "key_type"
        const val KEY_DATE = "key_date"
    }


    object UxUtils{

        fun moveScreenWithDailyItem(
            context: Context,
            targetActivity: Class<out Activity>,   // ✅ 이동 대상 Activity
            id: Long,
            type: DomainGoalType,
            date: LocalDate,
            exitCurrent: Boolean = false
        ) {
            val intent = Intent(
                context,
                targetActivity
            ).apply {
                putExtra(DailySummaryArgs.KEY_ID, id)
                putExtra(DailySummaryArgs.KEY_TYPE, type.name)
                putExtra(DailySummaryArgs.KEY_DATE, date.toString()) // yyyy-MM-dd
            }


            context.startActivity(intent)

            if (exitCurrent && context is Activity) {
                context.finish()
            }
        }

        /**
         * 모달이 열려 있을 때와 닫혀 있을 때의 백 버튼 동작을 처리하는 함수입니다.
         *
         * - 모달이 열려 있다면(`isSheetOpen`이 `true`), `handleSheetState()`를 호출하여 모달을 닫습니다.
         * - 모달이 닫혀 있다면(`isSheetOpen`이 `false`), `handleMainBackBtn()`을 호출하여 기본 백 버튼 동작을 실행합니다.
         *
         * @param handleSheetState 모달 상태를 변경하는 함수 (모달 닫기)
         * @param isSheetOpen 현재 모달이 열려 있는지 여부 (`true`이면 모달이 열려 있음)
         * @param handleMainBackBtn 기본 백 버튼 동작을 실행하는 함수 (예: 화면 종료)
         */
        fun handleModalBackPress(
            handleSheetState : () -> Unit,
            isSheetOpen: Boolean = false,
            handleMainBackBtn : () -> Unit,
        ) {
            if (isSheetOpen) {
                handleSheetState()
            } else {
                handleMainBackBtn()
            }
        }

        /**
         * 액티비티 전환을 수행하는 함수.
         *
         * - 지정된 대상 액티비티(`destinatedActivity`)로 이동합니다.
         * - `exitFlag`가 `true`이면 현재 액티비티를 종료합니다.
         *
         * @param context 현재 컨텍스트 (보통 `Activity` 또는 `ApplicationContext`)
         * @param destinationActivity 이동할 대상 액티비티의 클래스 (`Class<out Activity>`)
         * @param exitFlag `true`이면 현재 액티비티를 종료하고, `false`이면 종료하지 않음 (기본값: `true`)
         */
        fun moveActivity(
            context: Context,
            destinationActivity: Class<out Activity>,
            exitFlag: Boolean = true,
            extras: Bundle? = null // 데이터를 담을 Bundle 추가
        ){
            val intent = Intent(context, destinationActivity).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                extras?.let {putExtras(it)}
            }
            context.startActivity(intent)

            if (exitFlag && context is Activity){
                context.finish()
            }
        }

        fun isNotificationPermissionRequired(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        }

        // 1단계: Context 확장 함수 (UI 전용)
        fun Context.extractFileName(uri: Uri): String {
            var name = "unknown"
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0 && cursor.moveToFirst()) {
                    name = cursor.getString(index)
                }
            }
            return name
        }

        fun openExternalBrowser(
            context: Context,
            url: String
        ) {
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            context.startActivity(intent)
        }

    }

    object ConfigUtils {

        fun getBaseUrl(): String {
            val properties = Properties()
            val file = java.io.File("local.properties") // 프로젝트 루트의 local.properties 파일
            if (file.exists()) {
                properties.load(FileInputStream(file))
            }
            return properties.getProperty("BASE_URL", "")
        }
    }



    /*object NetworkUtil {
        suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): ApiResult<T> {
            return try {
                val response = apiCall()
                if (response.isSuccessful) {
                    response.body()?.let {
                        ApiResult.Success(it)
                    } ?: ApiResult.Error(NetworkError.Unknown, "Response body is null")
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessageCode = try {
                        val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                        errorResponse.messageCode
                    } catch (e: Exception) {
                        "HTTP ${response.code()} - ${response.message()}"
                    }
                    ApiResult.Error(NetworkError.ServerError, errorMessageCode)
                }
            } catch (e: UnknownHostException) {
                ApiResult.Error(NetworkError.NoInternet, "No internet connection")
            } catch (e: SocketTimeoutException) {
                ApiResult.Error(NetworkError.Timeout, "Request timed out")
            } catch (e: HttpException) {
                ApiResult.Error(NetworkError.HttpExceptionError, "HttpException error: ${e.message()}")
            } catch (e: ConnectException) {  // 🔹 서버 연결 실패 예외 처리 추가
                ApiResult.Error(NetworkError.NoInternet, "NO_INTERNET")
            } catch (e: IOException) {
                ApiResult.Error(NetworkError.Unknown, "UNKNOWN")
            }
        }
    }*/

    object TimeUtil {

        fun TimeState.Companion.fromServerTime(
            time: String // "HH:mm:ss"
        ): TimeState {
            val parts = time.split(":")
            val hour24 = parts[0].toInt()
            val minute = parts[1].toInt()

            return if (hour24 < 12) {
                TimeState(
                    hour = if (hour24 == 0) 12 else hour24,
                    minute = minute,
                    amPm = AmPm.AM
                )
            } else {
                TimeState(
                    hour = if (hour24 == 12) 12 else hour24 - 12,
                    minute = minute,
                    amPm = AmPm.PM
                )
            }
        }

        fun todayText(): String {
            val today = java.time.LocalDate.now()
            return "${today.monthValue}월 ${today.dayOfMonth}일"
        }

        fun String.toTimeState(): TimeState {
            val (h, m) = this.split(":").map { it.toInt() }

            val amPm = if (h < 12) AmPm.AM else AmPm.PM
            val hour12 = when {
                h == 0 -> 12
                h > 12 -> h - 12
                else -> h
            }

            return TimeState(
                hour = hour12,
                minute = m,
                amPm = amPm
            )
        }

        private val serverFormatter: DateTimeFormatter =
            DateTimeFormatterBuilder()
                .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
                // 🔑 소수점 이하를 1~9자리까지 허용
                .appendFraction(
                    ChronoField.NANO_OF_SECOND,
                    1,   // 최소 1자리
                    9,   // 최대 9자리 (나노초)
                    true // 소수점(.) 포함
                )
                .toFormatter()

        fun toMonthDay(createdAt: String): String {
            return try {
                Log.d("파싱 오류 분석", "createdAt: ${createdAt}")
                val dateTime = LocalDateTime.parse(createdAt, serverFormatter)
                "${dateTime.monthValue}월 ${dateTime.dayOfMonth}일"
            } catch (e: Exception) {
                // 파싱 실패 시 안전 장치
                Log.d("파싱 오류 분석", "error: ${e.toString()}")
                ""
            }
        }


    }

    object FcmTokenStore {

        private const val PREF_NAME = "fcm_token_pref"
        private const val KEY_FCM_TOKEN = "key_fcm_token"

        fun save(context: Context, token: String) {
            Log.e("FCM_TOKEN_TRACE", "💾 토큰 저장 = $token")

            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit {
                    putString(KEY_FCM_TOKEN, token)
                    commit()
                }
        }

        fun get(context: Context): String? {
            val token = context
                .getSharedPreferences("fcm_token_pref", Context.MODE_PRIVATE)
                .getString("key_fcm_token", null)

            Log.e("FCM_TOKEN_TRACE", "📤 저장된 토큰 조회 = $token")

            return token
        }

        fun clear(context: Context) {
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .remove(KEY_FCM_TOKEN)
                .apply()
        }
    }

    object FcmTokenSyncUtil {

        /**
         * ✅ 이미 확보된 token 을 서버로 전송
         * - onNewToken
         * - 로그인 성공
         * - 푸시 ON
         */
        fun syncWithServer(context: Context, token: String) {
            val entryPoint = EntryPointAccessors.fromApplication(
                context.applicationContext,
                NotificationEntryPoint::class.java
            )

            val repository = entryPoint.notificationRepository()

            CoroutineScope(Dispatchers.IO).launch {
                val result = repository.registerDeviceToken(
                    token = token,
                    deviceType = "ANDROID"
                )

                when (result) {
                    is ApiResultV2.Success -> {
                        Log.d("FCM_SYNC", "✅ 서버 토큰 등록 성공")
                    }
                    else -> {
                        Log.e(
                            "FCM_SYNC",
                            "❌ 서버 토큰 등록 실패: ${result.uiMessage}"
                        )
                    }
                }
            }
        }

        /**
         * ✅ App 시작 시:
         * - Firebase 에서 최신 token 조회
         * - 로컬 저장값과 비교
         * - 다르면 서버 재전송
         */
        fun checkAndSyncOnAppStart(context: Context) {
            FirebaseMessaging.getInstance().token
                .addOnSuccessListener { latestToken ->

                    val savedToken = FcmTokenStore.get(context)

                    Log.d("FCM_SYNC", "🚀 AppStart token(latest)=$latestToken")
                    Log.d("FCM_SYNC", "🚀 AppStart token(saved)=$savedToken")

                    if (latestToken.isNullOrBlank()) return@addOnSuccessListener

                    // 1️⃣ 로컬에 없으면 저장
                    if (savedToken != latestToken) {
                        FcmTokenStore.save(context, latestToken)

                        // 2️⃣ 서버와 불일치 → 재전송
                        syncWithServer(context, latestToken)
                    }

                }
                .addOnFailureListener { e ->
                    Log.e("FCM_SYNC", "❌ AppStart 토큰 조회 실패", e)
                }
        }
    }


    object InfoUtil{
        fun getAppVersion(context: Context): String {
            return try {
                val packageInfo = context.packageManager.getPackageInfo(
                    context.packageName,
                    0
                )
                "v${packageInfo.versionName}"
            } catch (e: Exception) {
                "v1.0.0"
            }
        }
    }

    enum class USER_REGISTER_STATE { LOGIN, ADD_INFO, LEVEL_EXAM, MAIN }

    // 유저의 언어 정보값을 SharedPref 에 저장하는 Utile 함수 만들기
    object PrefsUtil {
        private const val PREF_NAME = "user_prefs"
        private const val KEY_LANGUAGE = "user_language"
        private const val ID = "user_id"
        private const val NICK_NAME = "user_nick_name"
        private const val EXAM_MONTH = "user_exam_month"
        private const val KEY_GOAL_ID = "goal_id"
        private const val KEY_CATEGORY_ID = "category_id"
        private const val KEY_DOCUMENT_ID = "document_id"
        private const val KEY_GOAL_TYPE = "goal_type"

        // 유저가 한번이라도 요약글을 최초로 생성하였는지
        private const val KEY_SNACK_CONSUMED_DATE = "snack_consumed_date"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"


        /**
         * 온보딩 완료 상태 저장
         */
        fun setOnboardingCompleted(context: Context, isCompleted: Boolean) {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, isCompleted).apply()
        }

        /**
         * 온보딩 완료 여부 확인
         */
        fun isOnboardingCompleted(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            return prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
        }

        /**
         * 오늘 이미 요약글을 사용했는지
         */
        fun isSnackConsumedToday(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val savedDate = prefs.getString(KEY_SNACK_CONSUMED_DATE, null)
            val today = LocalDate.now().toString()
            return savedDate == today
        }

        /**
         * 요약글 사용 처리 (Consumed)
         */
        fun markSnackConsumedToday(context: Context) {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putString(
                    KEY_SNACK_CONSUMED_DATE,
                    LocalDate.now().toString()
                )
                .apply()
        }

        /**
         * 자정 초기화
         */
        fun clearSnackState(context: Context) {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .remove(KEY_SNACK_CONSUMED_DATE)
                .apply()
        }

        // 🔔 알림 권한 한 번이라도 거부했는지
        private const val KEY_NOTIFICATION_DENIED_ONCE = "notification_denied_once"

        private val IS_USER_REGISTER_STATE = USER_REGISTER_STATE.LOGIN.name

        fun saveGoalType(context: Context, goalTypeUiState: GoalTypeUiState) {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putString(KEY_GOAL_TYPE, goalTypeUiState.name)
                .apply()
        }

        fun getGoalType(context: Context): GoalTypeUiState {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val value = prefs.getString(KEY_GOAL_TYPE, null)

            return runCatching {
                if (value != null) GoalTypeUiState.valueOf(value)
                else GoalTypeUiState.NONE
            }.getOrElse {
                GoalTypeUiState.NONE
            }
        }

        fun clearGoalType(context: Context) {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            prefs.edit {
                remove(KEY_GOAL_TYPE)
            }
        }

        fun saveDocumentId(context: Context, documentId: Int) {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putInt(KEY_DOCUMENT_ID, documentId)
                .commit()
        }

        fun getDocumentId(context: Context): Int? {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val value = prefs.getInt(KEY_DOCUMENT_ID, -1)
            return if (value == -1) null else value
        }

        fun clearDocumentId(context: Context) {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            prefs.edit {
                remove(KEY_DOCUMENT_ID)
            }
        }


        fun saveGoalId(context: Context, goalId: Int) {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putInt(KEY_GOAL_ID, goalId)
                .commit() // 🔴 동기 저장
        }


        fun getGoalId(context: Context): Int? {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val value = prefs.getInt(KEY_GOAL_ID, -1)
            return if (value == -1) null else value
        }


        fun clearGoalId(context: Context) {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            prefs.edit {
                remove(KEY_GOAL_ID)
            }
        }


        fun saveCategoryId(context: Context, categoryId: Int) {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putInt(KEY_CATEGORY_ID, categoryId)
                .commit()
        }


        fun getCategoryId(context: Context): Int? {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val value = prefs.getInt(KEY_CATEGORY_ID, -1)
            return if (value == -1) null else value
        }


        fun clearCategoryId(context: Context) {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            prefs.edit {
                remove(KEY_CATEGORY_ID)
            }
        }


        /**
         * 🔴 알림 권한을 한 번이라도 거부했을 때 저장
         */
        fun saveNotificationDeniedOnce(context: Context) {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            prefs.edit {
                putBoolean(KEY_NOTIFICATION_DENIED_ONCE, true)
            }
        }

        /**
         * 🔍 알림 권한 거부 이력이 있는지 확인
         */
        fun hasNotificationDeniedOnce(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            return prefs.getBoolean(KEY_NOTIFICATION_DENIED_ONCE, false)
        }

        /**
         * 🔄 알림 권한 거부 이력 초기화
         * - 최초 권한 요청 상태로 되돌림
         */
        fun clearNotificationDeniedOnce(context: Context) {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            prefs.edit {
                remove(KEY_NOTIFICATION_DENIED_ONCE)
                // 또는 putBoolean(KEY_NOTIFICATION_DENIED_ONCE, false)
            }
        }

        // 유저 id 저장
        fun saveUserId(context: Context, userId: String) {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            prefs.edit { putString(ID, userId) }
        }

        // 유저 id 불러오기
        fun getUserId(context: Context): String {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            return prefs.getString(ID, "no_login_user") ?: "no_login_user"
        }

        // 유저 닉네임 저장
        fun saveUserNick(context: Context, nickName: String) {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            prefs.edit { putString(NICK_NAME, nickName) }
        }

        // 유저 닉네임 불러오기
        fun getUserNick(context: Context): String {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            return prefs.getString(NICK_NAME, "no_login_user") ?: "no_login_user"
        }

        // 유저 응시월 저장
        fun saveUserExamMonth(context: Context, examMonth: Int) {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            prefs.edit { putInt(EXAM_MONTH, examMonth) }
        }

        // 유저 닉네임 불러오기
        fun getUserExamMonth(context: Context): Int {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            return prefs.getInt(EXAM_MONTH, 0)
        }

        // 언어 저장
        fun saveUserLanguage(context: Context, languageCode: String) {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            prefs.edit { putString(KEY_LANGUAGE, languageCode) }
        }

        // 언어 불러오기 (기본값 "ko")
        fun getUserLanguage(context: Context): String {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            return prefs.getString(KEY_LANGUAGE, "VI") ?: "VI"
        }

        // 오프라인일때: 유저 시작 지점 지정
        fun saveUserRegisterState(context: Context, registerState: String) {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            prefs.edit { putString(IS_USER_REGISTER_STATE, registerState) }
        }

        // 오프라인일때: 유저 시작 지점 가져오기
        fun getUserRegisterState(context: Context): String {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            return prefs.getString(IS_USER_REGISTER_STATE, USER_REGISTER_STATE.LOGIN.name) ?: USER_REGISTER_STATE.LOGIN.name
        }
    }


    object UpdateUtils{
        fun moveToPlayStore(activity: Activity) {
            val packageName = activity.packageName
            val intent = Intent(
                Intent.ACTION_VIEW,
                "market://details?id=$packageName".toUri()
            )
            // market:// 실패(디바이스에 PlayStore 없음 등) 대비
            val fallback = Intent(
                Intent.ACTION_VIEW,
                "https://play.google.com/store/apps/details?id=$packageName".toUri()
            )

            try {
                activity.startActivity(intent)
            } catch (e: Exception) {
                activity.startActivity(fallback)
            }
        }
    }
}