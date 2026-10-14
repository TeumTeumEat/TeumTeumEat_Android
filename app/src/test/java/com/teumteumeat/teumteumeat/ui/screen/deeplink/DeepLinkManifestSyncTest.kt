package com.teumteumeat.teumteumeat.ui.screen.deeplink

import com.teumteumeat.teumteumeat.utils.DeepLinkConstants
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** 매니페스트는 Kotlin 상수를 참조할 수 없어, 두 값이 어긋나면 앱이 설치돼 있어도 링크가 항상 브라우저로 열린다. */
class DeepLinkManifestSyncTest {

    @Test
    fun `매니페스트 App Link host 는 LEAGUE_HOST 와 같다`() {
        // 단위 테스트의 작업 디렉터리는 app 모듈 루트다
        val manifest = File("src/main/AndroidManifest.xml").readText()

        assertTrue(manifest.contains("android:host=\"${DeepLinkConstants.LEAGUE_HOST}\""))
        assertTrue(manifest.contains("android:pathPrefix=\"${DeepLinkConstants.LEAGUE_PATH}\""))
    }
}
