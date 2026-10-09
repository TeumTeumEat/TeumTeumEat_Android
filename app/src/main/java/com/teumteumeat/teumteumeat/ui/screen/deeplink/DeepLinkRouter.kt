package com.teumteumeat.teumteumeat.ui.screen.deeplink

import android.net.Uri
import com.teumteumeat.teumteumeat.utils.DeepLinkConstants
import java.net.URI

/**
 * 딥링크 URI를 어느 화면으로 보낼지 판정하는 순수 Kotlin 로직.
 * Context/Intent에 의존하지 않아 [DeepLinkRouterActivity] 없이도 단위 테스트가 가능하다.
 */
object DeepLinkRouter {

    fun resolve(uri: Uri?, hasRefreshToken: Boolean): DeepLinkDestination {
        if (uri == null) return DeepLinkDestination.Invalid

        if (!matchesLeagueLink(uri.scheme, uri.host, uri.path)) return DeepLinkDestination.Invalid

        return if (hasRefreshToken) DeepLinkDestination.League else DeepLinkDestination.LoginThenLeague
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

    private fun matchesLeagueLink(scheme: String?, host: String?, path: String?): Boolean =
        scheme == "https" && host == DeepLinkConstants.LEAGUE_HOST && path == DeepLinkConstants.LEAGUE_PATH
}

sealed class DeepLinkDestination {
    data object League : DeepLinkDestination()
    data object LoginThenLeague : DeepLinkDestination()
    data object Invalid : DeepLinkDestination()
}
