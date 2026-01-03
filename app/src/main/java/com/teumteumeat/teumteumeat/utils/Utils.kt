package com.teumteumeat.teumteumeat.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.teumteumeat.teumteumeat.domain.model.on_boarding.TimeState
import com.teumteumeat.teumteumeat.ui.component.AmPm
import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.enum_type.GoalType
import java.io.FileInputStream
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale
import java.util.Properties

sealed interface NotificationPermissionEvent {
    data object RequestPermission : NotificationPermissionEvent
    data object AlreadyGranted : NotificationPermissionEvent
}

class Utils {

    object UiUtils{

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

        fun TimeState.normalizeTo12Hour(): TimeState {
            // 이미 정상 범위면 그대로
            if (hour in 1..12) return this

            val hour12 = when {
                hour == 0 -> 12
                hour in 1..12 -> hour
                hour in 13..23 -> hour - 12
                else -> 12
            }

            val newAmPm = if (hour in 0..11) AmPm.AM else AmPm.PM

            return copy(
                hour = hour12,
                amPm = newAmPm
            )
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

    object UxUtils{

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
            exitFlag: Boolean = true
        ){
            val intent = Intent(context, destinationActivity).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
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

    object DateUtil {

        fun todayText(): String {
            val today = java.time.LocalDate.now()
            return "${today.monthValue}월 ${today.dayOfMonth}일"
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


        // 🔔 알림 권한 한 번이라도 거부했는지
        private const val KEY_NOTIFICATION_DENIED_ONCE = "notification_denied_once"

        private val IS_USER_REGISTER_STATE = USER_REGISTER_STATE.LOGIN.name

        fun saveGoalType(context: Context, goalType: GoalType) {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putString(KEY_GOAL_TYPE, goalType.name)
                .apply()
        }

        fun getGoalType(context: Context): GoalType {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val value = prefs.getString(KEY_GOAL_TYPE, null)

            return runCatching {
                if (value != null) GoalType.valueOf(value)
                else GoalType.NONE
            }.getOrElse {
                GoalType.NONE
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



}