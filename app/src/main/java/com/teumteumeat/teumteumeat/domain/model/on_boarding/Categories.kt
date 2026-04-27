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

        // ❌ 4뎁스 규칙 위반 (path 3 초과 → leaf 포함 시 5뎁스)
        if (segments.size > 3) {
            return@forEach
        }

        var currentLevel = root

        // 1️⃣ path 기반 depth 생성 (최대 3뎁스)
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

        // 2️⃣ leaf (항상 4뎁스)
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

fun String.toDepth2CategoryEmoji(): String = when (this) {
    // IT
    "앱개발자"                  -> "📱"
    "웹개발자"                  -> "💻"
    "데이터베이스"              -> "🗄️"
    "DevOps"                   -> "⚙️"
    "PM"                       -> "📋"
    "네트워크"                  -> "📡"
    "디자인"                    -> "🎨"
    // 스포츠
    "러닝 & 유산소"             -> "🏃"
    "웨이트(헬스)"              -> "💪"
    "구기 종목 (축구 & 농구)"   -> "⚽"
    // 경제
    "금융 기초"                 -> "💰"
    // 주식
    "투자 입문"                 -> "📈"
    "분석 기초"                 -> "📊"
    // 생활 법률 및 제도
    "주거와 계약"               -> "🏠"
    "생활과 노동"               -> "⚖️"
    // 기초 과학
    "물리 & 화학 상식"          -> "🔬"
    "지구와 우주"               -> "🌍"
    // 건강
    "식품과 영양"               -> "🥗"
    "질환과 안전"               -> "🏥"
    // 시사 교양
    "지리와 문화"               -> "🗺️"
    "국제 사회"                 -> "🌏"
    // 맞춤법
    "표준어 규정"               -> "📖"
    "실전 언어"                 -> "✍️"
    else                       -> ""
}

fun String.toDepth2CategoryLabel(): String {
    val emoji = toDepth2CategoryEmoji()
    return when (this) {
        "구기 종목 (축구 & 농구)" -> if (emoji.isEmpty()) "구기 종목" else "${emoji}구기 종목\n(축구 & 농구)"
        else -> if (emoji.isEmpty()) this else "$emoji $this"
    }
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



