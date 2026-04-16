package com.teumteumeat.teumteumeat.domain.usecase.notification

import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model.map
import com.teumteumeat.teumteumeat.data.repository.user.UserRepository
import com.teumteumeat.teumteumeat.utils.Utils
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * 서버의 푸시 알림 설정값과 기기의 앱 알림 권한 상태를 조합하여
 * 최종적인 알림 활성화 여부를 결정하는 UseCase입니다.
 */
class GetPushNotificationStatusUseCase @Inject constructor(
    private val userRepository: UserRepository,
    @ApplicationContext private val context: android.content.Context
) {
    suspend operator fun invoke(): ApiResultV2<Boolean> {
        val isDevicePermissionGranted = Utils.UiUtils.checkNotificationPermission(context)

        return userRepository.getUserPushEnableSettings().map { response ->
            // 이 시점에서 mapping된 ApiResultV2의 각 message 필드에는
            // 이미 uiMessage에서 정의한 가공된 텍스트가 들어가 있습니다.
            response.pushEnabled && isDevicePermissionGranted
        }
    }
}