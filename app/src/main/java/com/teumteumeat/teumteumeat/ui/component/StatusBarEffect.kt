package com.teumteumeat.teumteumeat.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.core.view.WindowCompat
import com.teumteumeat.teumteumeat.utils.LocalActivityContext

/**
 * 시스템 다크모드 여부와 무관하게 항상 흰 배경을 쓰는 화면(축하/완료 연출 등)에서
 * 상태바 아이콘이 배경에 묻히지 않도록 어두운 아이콘으로 고정한다.
 * 컴포지션을 벗어나면 진입 전 값으로 복원해 같은 Activity 내 다른 화면에 영향을 주지 않는다.
 */
@Composable
fun ForceLightStatusBarIcons() {
    // ⚠️ Preview는 LocalActivityContext를 제공하지 않아 .current 접근 시 크래시하므로 건너뜀
    if (LocalInspectionMode.current) return

    val activity = LocalActivityContext.current
    DisposableEffect(activity) {
        val controller = WindowCompat.getInsetsController(activity.window, activity.window.decorView)
        val previous = controller.isAppearanceLightStatusBars
        controller.isAppearanceLightStatusBars = true
        onDispose {
            controller.isAppearanceLightStatusBars = previous
        }
    }
}
