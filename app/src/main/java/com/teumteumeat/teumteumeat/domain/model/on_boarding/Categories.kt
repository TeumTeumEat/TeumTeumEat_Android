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

fun List<CategoryDto>.toCategoryTree(): List<Category> {
    val rootMap = mutableMapOf<String, Category>()

    forEach { dto ->
        val segments = dto.path
            .split("/")
            .filter { it.isNotBlank() }

        var currentLevel = rootMap

        segments.forEachIndexed { index, name ->
            val id = segments
                .take(index + 1)
                .joinToString(separator = "/")

            val existing = currentLevel[id]

            if (existing == null) {
                val newCategory = Category(
                    id = id,
                    name = name,
                    children = emptyList()
                )
                currentLevel[id] = newCategory
            }

            // 다음 depth로 이동
            val currentCategory = currentLevel[id]!!
            currentLevel = currentCategory.children
                .associateBy { it.id }
                .toMutableMap()
        }
    }

    return buildTree(rootMap)
}

private fun buildTree(map: Map<String, Category>): List<Category> {
    return map.values.map { category ->
        category.copy(
            children = buildTree(
                category.children.associateBy { it.id }
            )
        )
    }
}



