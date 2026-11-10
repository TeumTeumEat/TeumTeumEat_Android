package com.teumteumeat.teumteumeat.ui.screen.deeplink

import android.net.Uri
import com.teumteumeat.teumteumeat.utils.DeepLinkConstants
import java.net.URI

/**
 * 딥링크 URI를 어느 화면으로 보낼지 판정하는 순수 Kotlin 로직.
 * Context/Intent에 의존하지 않아 [DeepLinkRouterActivity] 없이도 단위 테스트가 가능하다.
 */
object DeepLinkRouter {

    /**
     * 리그 공유 링크(App Link)인지 판정한다. 로그인/온보딩 진행도에 따른 분기는 스플래시가 담당한다.
     */
    fun resolve(uri: Uri?): DeepLinkDestination {
        if (uri == null) return DeepLinkDestination.Invalid

        return if (matchesLeagueLink(uri.scheme, uri.host, uri.path)) {
            DeepLinkDestination.League
        } else {
            DeepLinkDestination.Invalid
        }
    }

    /**
     * FCM data payload 의 `link` 값이 리그 딥링크인지 판정한다. (스플래시가 진행도 분기 뒤 리그로 이동할지 결정할 때 쓴다)
     * Android 의 [Uri] 에 의존하지 않는 순수 Kotlin 이라 단위 테스트가 가능하다.
     */
    fun isLeagueLink(link: String?): Boolean {
        if (link.isNullOrBlank()) return false
        val uri = runCatching { URI(link.trim()) }.getOrNull() ?: return false
        return matchesLeagueLink(uri.scheme, uri.host, uri.path)
    }

    /**
     * 푸시 알림 탭이 리그 화면 진입인지 판정한다. `link` 가 리그 딥링크이거나 `type` 이 리그 알림이면 true.
     * 서버가 `link` 없이 `type` 만 보내도 리그로 진입할 수 있다. 알 수 없는 type 은 false (홈 진입).
     */
    fun isLeaguePush(link: String?, type: String?): Boolean =
        isLeagueLink(link) || PushType.fromValue(type)?.opensLeague == true

    private fun matchesLeagueLink(scheme: String?, host: String?, path: String?): Boolean =
        scheme == "https" && host == DeepLinkConstants.LEAGUE_HOST && path == DeepLinkConstants.LEAGUE_PATH
}

sealed class DeepLinkDestination {
    data object League : DeepLinkDestination()
    data object Invalid : DeepLinkDestination()
}
