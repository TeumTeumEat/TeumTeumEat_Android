package com.teumteumeat.teumteumeat.domain.model.on_boarding

import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.Category

data class CategoriesResponseDto(
    val categoryResponses: List<CategoryDto>
)

data class CategoryDto(
    val categoryId: Long,
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

        // 1️⃣ path 기반 depth 생성
        segments.forEach { segment ->
            currentLevel = currentLevel
                .getOrPut(segment) { MutableCategory(segment, segment) }
                .children
        }

        // 2️⃣ 실제 카테고리(name)를 마지막 depth의 child로 추가
        currentLevel[dto.name] = MutableCategory(
            id = dto.categoryId.toString(),
            name = dto.name
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
    val children: MutableMap<String, MutableCategory> = mutableMapOf()
) {
    fun toImmutable(): Category {
        return Category(
            id = id,
            name = name,
            children = children.values.map { it.toImmutable() }
        )
    }
}



