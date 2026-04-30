package com.teumteumeat.teumteumeat.domain.model

// domain/model/RequestPromptOption.kt

/**
 * 요청 프롬프트 선택지 모델
 * @param id 고유 식별자
 * @param label UI에 표시될 텍스트
 */
data class RequestPromptOption(
    val id: String,
    val label: String,
)

/** 기본 제공 선택지 목록 */
val defaultRequestPromptOptions: List<RequestPromptOption> = listOf(
    RequestPromptOption("commute",      "출퇴근길에 가볍게 풀 수 있게 만들어주세요."),
    RequestPromptOption("step_by_step", "기초부터 차근차근 개념을 익히고 싶어요."),
    RequestPromptOption("trend",        "최신 트렌드나 뉴스 위주로 구성해주세요."),
    RequestPromptOption("interview",    "면접에 도움이 되는 내용으로 만들어주세요."),
    RequestPromptOption("exam",         "시험 대비용 문제 위주로 만들어주세요."),
    RequestPromptOption("practical",    "실무에서 바로 쓸 수 있게 구성해주세요."),
    RequestPromptOption("example",      "이론보다 예시 중심으로 배우고 싶어요."),
    RequestPromptOption("compare",      "헷갈리기 쉬운 개념을 비교/정리해주세요."),
    RequestPromptOption("trivia",       "짧고 핵심만 담긴 상식 위주로 구성해주세요."),
    RequestPromptOption("deep",         "심화 개념까지 깊이 있게 다뤄주세요."),
)