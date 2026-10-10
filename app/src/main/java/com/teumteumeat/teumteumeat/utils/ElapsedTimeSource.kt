package com.teumteumeat.teumteumeat.utils

/**
 * 부팅 이후 경과 시간(ms) 소스.
 * 기기 시각(벽시계)을 바꾸거나 시간대가 달라도 영향을 받지 않고, 화면이 꺼진 절전 중에도 계속 흐른다.
 * 서버가 내려준 '남은 시간'처럼 상대 시간을 기기 시각과 무관하게 세어야 할 때 쓴다. (구현: [android.os.SystemClock.elapsedRealtime])
 */
fun interface ElapsedTimeSource {
    fun elapsedRealtimeMillis(): Long
}
