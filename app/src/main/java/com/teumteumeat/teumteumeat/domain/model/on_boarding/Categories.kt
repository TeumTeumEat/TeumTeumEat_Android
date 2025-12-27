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
                        id = segment,       // UI 식별자
                        name = segment
                    )
                }
                .children
        }

        // 2️⃣ 실제 카테고리 (leaf)
        currentLevel[dto.name] = MutableCategory(
            id = dto.name,                  // UI 식별자
            name = dto.name,
            serverCategoryId = dto.categoryId // ⭐ 서버 ID는 여기!
        )
    }

    return root.values.map { it.toImmutable() }
}

/**
 * 내부 전용 Mutable 모델
 */
private class MutableCategory(
    val id: String,
    val name: String,
    val serverCategoryId: Int? = null,
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



