package com.teumteumeat.teumteumeat.ui.screen.c4_notice_list

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.teumteumeat.teumteumeat.ui.aa0_base.BaseActivity
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * 공지사항 목록 화면 진입점. 내 정보 > 기타 > 공지사항에서 진입한다.
 */
@AndroidEntryPoint
class NoticeListActivity : BaseActivity() {

    private val viewModel: NoticeListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TeumTeumEatTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                NoticeListScreen(
                    uiState = uiState,
                    onBackClick = { finish() },
                    onNoticeClick = {
                        // TODO: 공지 상세 화면 미정 (디자인 확인 필요)
                    },
                    onLoadMore = viewModel::loadNextPage,
                    onRetryClick = viewModel::loadFirstPage,
                )
            }
        }
    }

    override fun onRetryClick() {
        viewModel.loadFirstPage()
    }
}
