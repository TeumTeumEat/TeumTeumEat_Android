package com.teumteumeat.teumteumeat.data.repository.category

import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model_response.CategoryDocument

interface CategoryRepository {
    suspend fun getDailyCategoryDocument(
        categoryId: Long
    ): ApiResultV2<CategoryDocument>

    suspend fun createDailyCategoryDocument(
        categoryId: Long
    ): ApiResultV2<CategoryDocument>
}
