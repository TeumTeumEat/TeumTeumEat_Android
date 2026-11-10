package com.teumteumeat.teumteumeat.utils.firebase

import com.google.firebase.crashlytics.FirebaseCrashlytics
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Crashlytics 커스텀 Key("키" 탭) / Breadcrumb("로그 및 탐색경로" 탭) 전용 로거.
 *
 * Activity/ViewModel이 [FirebaseCrashlytics]를 직접 참조하지 않도록 래핑한다.
 * [log]/[setCustomKey]는 Fatal 크래시 또는 [recordNonFatal] 호출 시점에만 대시보드로 업로드되며,
 * 그 전까지는 기기 메모리에만 쌓인 상태로 대기한다 — 크래시 없이 세션이 종료되면 유실된다.
 */
@Singleton
class TeumCrashlyticsLogger @Inject constructor(
    private val crashlytics: FirebaseCrashlytics,
) {

    /** "로그 및 탐색경로" 탭에 시간순으로 남는 브레드크럼 */
    fun log(message: String) {
        crashlytics.log(message)
    }

    fun setCustomKey(key: String, value: String) {
        crashlytics.setCustomKey(key, value)
    }

    fun setCustomKey(key: String, value: Long) {
        crashlytics.setCustomKey(key, value)
    }

    fun setCustomKey(key: String, value: Boolean) {
        crashlytics.setCustomKey(key, value)
    }

    fun setUserId(userId: String) {
        crashlytics.setUserId(userId)
    }

    /**
     * 앱을 죽이지는 않지만 원인 파악이 필요한 비정상 상황을 Non-Fatal 이슈로 기록한다.
     * 호출 시점까지 쌓인 [log]/[setCustomKey] 값이 이 이슈와 함께 업로드되어 대시보드에서 확인 가능하다.
     */
    fun recordNonFatal(throwable: Throwable) {
        crashlytics.recordException(throwable)
    }
}
