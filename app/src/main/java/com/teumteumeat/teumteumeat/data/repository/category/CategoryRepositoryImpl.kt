package com.teumteumeat.teumteumeat.data.repository.category

import com.teumteumeat.teumteumeat.data.api.auth.AuthApiService
import com.teumteumeat.teumteumeat.data.api.category.CategoryApiService
import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model.TokenLocalDataSource
import com.teumteumeat.teumteumeat.data.repository.BaseRepository
import javax.inject.Inject
import com.teumteumeat.teumteumeat.data.network.model_response.DailyCategoryDocument
import com.teumteumeat.teumteumeat.data.network.model_response.toDomain


class CategoryRepositoryImpl @Inject constructor(
    private val categoryApiService: CategoryApiService,
    authApiService: AuthApiService,
    tokenLocalDataSource: TokenLocalDataSource
) : BaseRepository(authApiService, tokenLocalDataSource),
    CategoryRepository {

    override suspend fun getDailyCategoryDocument(
        categoryId: Long
    ): ApiResultV2<DailyCategoryDocument> {

        return safeApiVer2(
            apiCall = {
                categoryApiService.getDailyCategoryDocument(categoryId)
            },
            mapper = { dto ->
                if (dto == null) {
                    error("DailyCategoryDocumentDto is null")
                }
                dto.toDomain()
            }
        )
    }

    /**
     * 오늘의 카테고리 요약글을 생성(POST)합니다.
     */
    override suspend fun createDailyCategoryDocument(
        categoryId: Long
    ): ApiResultV2<DailyCategoryDocument> {
        return safeApiVer2(
            apiCall = {
                categoryApiService.createDailyCategoryDocument(categoryId)
            },
            mapper = { dto ->
                if (dto == null) {
                    error("DailyCategoryDocumentDto is null")
                }
                dto.toDomain()
            }
        )
    }
}
