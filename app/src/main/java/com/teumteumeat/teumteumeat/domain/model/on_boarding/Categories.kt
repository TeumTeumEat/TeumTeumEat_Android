package com.teumteumeat.teumteumeat.domain.model.on_boarding

import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.Category

data class CategoriesResponseDto(
    val categoryResponses: List<CategoryDto>
)

data class CategoryDto(
    val categoryId: Int,
    val name: String,
    val path: String,
    val description: String?
)


fun List<CategoryDto>.toDomainCategoryTree(): List<Category> {
    val root = mutableMapOf<String, MutableCategory>()

    forEach { dto ->
        val segments = dto.path
            .split("/")
            .filter { it.isNotBlank() }

        var currentLevel = root

        // 1️⃣ path 기반 depth 생성 (UI 전용)
        segments.forEach { segment ->
            currentLevel = currentLevel
                .getOrPut(segment) {
                    MutableCategory(
                        id = segment,
                        name = segment
                    )
                }
                .children
        }

        // 2️⃣ leaf 처리 (⭐ 핵심 수정)
        var leaf = currentLevel[dto.name]
        if (leaf != null) {
            // 이미 존재하면 serverCategoryId만 세팅
            leaf.serverCategoryId = dto.categoryId
        } else {
            // 없으면 새로 생성
            currentLevel[dto.name] = MutableCategory(
                id = dto.name,
                name = dto.name,
                serverCategoryId = dto.categoryId
            )
        }
    }

    return root.values.map { it.toImmutable() }
}

/**
 * 내부 전용 Mutable 모델
 */
private class MutableCategory(
    val id: String,
    val name: String,
    var serverCategoryId: Int? = null,
    val children: MutableMap<String, MutableCategory> = mutableMapOf()
) {
    fun toImmutable(): Category {
        return Category(
            id = id,
            name = name,
            serverCategoryId = serverCategoryId,
            children = children.values.map { it.toImmutable() }
        )
    }
}



