package com.teumteumeat.teumteumeat.domain.model.goal

/**
 * 학습 목표의 타입
 *
 * - CATEGORY : 카테고리 기반 학습 목표
 * - DOCUMENT : 사용자 업로드 pdf 기반 학습 목표
 */
enum class DomainGoalType {
    CATEGORY, DOCUMENT
}