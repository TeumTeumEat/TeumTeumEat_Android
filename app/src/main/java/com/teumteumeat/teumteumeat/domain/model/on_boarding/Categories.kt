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

        segments.forEach { segment ->
            currentLevel = currentLevel
                .getOrPut(segment) { MutableCategory(id = segment, name = segment) }
                .children
        }

        val leaf = currentLevel[dto.name]
        if (leaf != null) {
            leaf.serverCategoryId = dto.categoryId
        } else {
            currentLevel[dto.name] = MutableCategory(
                id = dto.name,
                name = dto.name,
                serverCategoryId = dto.categoryId
            )
        }
    }

    return root.values.map { it.toImmutable() }
}

fun List<Category>.maxDepth(): Int {
    if (isEmpty()) return 0
    return 1 + (maxOfOrNull { it.children.maxDepth() } ?: 0)
}

fun String.toDepth1CategoryLabel(): String = when (this) {
    "생활 법률 및 제도" -> "생활 법률\n및 제도"
    else -> this
}

fun String.toDepth2CategoryLabel(): String = this

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



