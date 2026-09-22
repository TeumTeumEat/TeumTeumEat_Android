package com.teumteumeat.teumteumeat.ui.screen.deeplink

import android.net.Uri
import com.teumteumeat.teumteumeat.utils.DeepLinkConstants

/**
 * 딥링크 URI를 어느 화면으로 보낼지 판정하는 순수 Kotlin 로직.
 * Context/Intent에 의존하지 않아 [DeepLinkRouterActivity] 없이도 단위 테스트가 가능하다.
 */
object DeepLinkRouter {

    fun resolve(uri: Uri?, hasRefreshToken: Boolean): DeepLinkDestination {
        if (uri == null) return DeepLinkDestination.Invalid

        val isLeagueLink = uri.scheme == "https" &&
            uri.host == DeepLinkConstants.LEAGUE_HOST &&
            uri.path == DeepLinkConstants.LEAGUE_PATH

        if (!isLeagueLink) return DeepLinkDestination.Invalid

        return if (hasRefreshToken) DeepLinkDestination.League else DeepLinkDestination.LoginThenLeague
    }
}

sealed class DeepLinkDestination {
    data object League : DeepLinkDestination()
    data object LoginThenLeague : DeepLinkDestination()
    data object Invalid : DeepLinkDestination()
}
