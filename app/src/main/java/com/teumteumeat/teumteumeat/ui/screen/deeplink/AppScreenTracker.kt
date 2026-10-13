package com.teumteumeat.teumteumeat.ui.screen.deeplink

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.teumteumeat.teumteumeat.ui.screen.a0_splash.SplashActivity
import com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_5_add_goal.AddGoalActivity
import com.teumteumeat.teumteumeat.ui.screen.b2_quiz.QuizActivity
import com.teumteumeat.teumteumeat.ui.screen.b3_quiz_result.QuizResultActivity
import java.lang.ref.WeakReference

/**
 * 앱 안에서 마지막으로 보인 화면(Activity)을 추적한다.
 * 푸시 알림을 탭했을 때 퀴즈 / 주제 추가 / 새 목표 설정 중이라면 화면을 옮기지 않기 위해 쓴다 ([PushNavigationPolicy]).
 *
 * 스플래시와 딥링크 착지점은 "다음 화면으로 가는 중간 단계"라 추적하지 않는다.
 * Activity 를 약참조로만 들고 있고, 파괴되면 비운다.
 */
object AppScreenTracker : Application.ActivityLifecycleCallbacks {

    /** 진행을 끊으면 안 되는 화면 — 퀴즈 풀이 / 퀴즈 결과 / 주제 추가·새 목표 설정 */
    private val noInterruptScreens: Set<Class<out Activity>> = setOf(
        QuizActivity::class.java,
        QuizResultActivity::class.java,
        AddGoalActivity::class.java,
    )

    private val ignoredScreens: Set<Class<out Activity>> = setOf(
        SplashActivity::class.java,
        DeepLinkRouterActivity::class.java,
    )

    private var lastResumed: WeakReference<Activity>? = null

    private val liveScreen: Activity?
        get() = lastResumed?.get()?.takeIf { !it.isFinishing && !it.isDestroyed }

    /** 앱 안에 살아 있는 화면이 있는지 (콜드 스타트면 false) */
    val hasLiveScreen: Boolean get() = liveScreen != null

    /** 퀴즈 / 주제 추가 / 새 목표 설정처럼 알림을 눌러도 옮기면 안 되는 화면이 마지막 화면인지 */
    val isNoInterruptScreenActive: Boolean
        get() = liveScreen?.let { it.javaClass in noInterruptScreens } == true

    fun register(application: Application) {
        application.registerActivityLifecycleCallbacks(this)
    }

    override fun onActivityResumed(activity: Activity) {
        if (activity.javaClass !in ignoredScreens) lastResumed = WeakReference(activity)
    }

    override fun onActivityDestroyed(activity: Activity) {
        if (lastResumed?.get() === activity) lastResumed = null
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
    override fun onActivityStarted(activity: Activity) = Unit
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivityStopped(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
}
