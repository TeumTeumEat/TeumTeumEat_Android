package com.teumteumeat.teumteumeat.data.repository.notice

import com.teumteumeat.teumteumeat.data.api.auth.AuthApiService
import com.teumteumeat.teumteumeat.data.api.notice.NoticeApiService
import com.teumteumeat.teumteumeat.data.mapper.toDomain
import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model.TokenLocalDataSource
import com.teumteumeat.teumteumeat.data.repository.BaseRepository
import com.teumteumeat.teumteumeat.domain.model.notice.NoticePage
import com.teumteumeat.teumteumeat.domain.repository.notice.NoticeRepository
import com.teumteumeat.teumteumeat.utils.Utils.RepositoryUtils.requireNotNullOrError
import javax.inject.Inject

class NoticeRepositoryImpl @Inject constructor(
    private val noticeApiService: NoticeApiService,
    authApiService: AuthApiService,
    tokenLocalDataSource: TokenLocalDataSource
) : BaseRepository(authApiService, tokenLocalDataSource), NoticeRepository {

    override suspend fun getNotices(
        lastNoticeId: Long?,
        size: Int
    ): ApiResultV2<NoticePage> =
        safeApiVer2(
            apiCall = {
                noticeApiService.getNotices(
                    lastNoticeId = lastNoticeId,
                    size = size
                )
            },
            mapper = { data ->
                data.requireNotNullOrError().toDomain()
            }
        )
}
