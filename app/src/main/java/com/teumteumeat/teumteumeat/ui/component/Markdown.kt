package com.teumteumeat.teumteumeat.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.teumteumeat.teumteumeat.utils.appTypography

// ── 공개 컴포저블 ────────────────────────────────────────────────────────────────

/**
 * 마크다운 텍스트를 렌더링하는 컴포저블.
 *
 * ### 지원 문법
 * - 헤딩: `# H1` / `## H2` / `### H3`
 * - 굵기: `**bold**`
 * - 목록: `- item` / `* item` → `•` 불릿, 항목별 줄바꿈
 * - 인라인 코드: `` `code` `` → 밝은 회색 둥근 배경 + 빨간 텍스트(가운데 정렬)
 * - 코드 블록: ` ```language\n코드\n``` ` → 상단 언어 레이블 표시
 *
 * ### 스트리밍 대응
 * SSE 수신 중 닫히지 않은 마커(`**`, `` ` ``, ` ``` `)는 화면에 노출하지 않고,
 * 문법이 완성되면 해당 서식으로 전환한다. 닫는 ` ``` ` 미수신 코드 블록도 부분 렌더링한다.
 */
@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier
) {
    val segments = remember(markdown) { parseSegments(markdown) }

    Column(modifier = modifier) {
        segments.forEachIndexed { index, segment ->
            when (segment) {
                is ParsedSegment.TextBlock -> MarkdownTextBlock(segment.text)
                is ParsedSegment.CodeBlock -> {
                    // 다음 블록이 헤딩으로 시작할 때만 한 줄 정도 띄워 구분한다. 그 외에는 붙인다.
                    val nextStartsWithHeading = (segments.getOrNull(index + 1) as? ParsedSegment.TextBlock)
                        ?.text?.trimStart()?.startsWith("#") == true
                    CodeBlockView(
                        language = segment.language,
                        code = segment.code,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 7.dp, bottom = if (nextStartsWithHeading) 20.dp else 7.dp)
                    )
                }
            }
        }
    }
}

// ── 텍스트 블록 (헤딩·목록·문단·인라인 코드) ─────────────────────────────────────────

@Composable
private fun MarkdownTextBlock(text: String) {
    val (annotated, inlineContent) = remember(text) {
        val map = LinkedHashMap<String, InlineTextContent>()
        buildBlockAnnotation(text, map) to map
    }

    if (annotated.isBlank() && inlineContent.isEmpty()) return

    Text(
        text = annotated,
        inlineContent = inlineContent,
        style = MaterialTheme.appTypography.bodyMedium16.copy(lineHeight = 22.sp)
    )
}

// ── 코드 블록 UI ─────────────────────────────────────────────────────────────────

@Composable
private fun CodeBlockView(
    language: String,
    code: String,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(8.dp)

    Column(
        modifier = modifier
            .clip(shape)
            .border(1.dp, Color(0xFFDDDDDD), shape)
    ) {
        // 언어 레이블 (상단 좌측)
        if (language.isNotBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE8E8E8))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = language,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = Color(0xFF555555)
                )
            }
        }

        // 코드 영역 (수평 스크롤)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF8F8F8))
        ) {
            Box(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(12.dp)
            ) {
                Text(
                    text = code,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    color = Color(0xFF333333),
                    softWrap = false
                )
            }
        }
    }
}

// ── 파싱: 코드 블록 분리 ──────────────────────────────────────────────────────────

private sealed interface ParsedSegment {
    data class TextBlock(val text: String) : ParsedSegment
    data class CodeBlock(val language: String, val code: String) : ParsedSegment
}

/**
 * 마크다운 문자열을 텍스트 블록과 코드 블록으로 분리한다.
 * 열기 ` ``` ` 부터 닫기 ` ``` ` (또는 스트림 끝)까지 코드 블록으로 수집한다.
 */
private fun parseSegments(markdown: String): List<ParsedSegment> {
    val result = mutableListOf<ParsedSegment>()
    val lines = markdown.split("\n")
    val textBuf = StringBuilder()
    var i = 0

    while (i < lines.size) {
        val line = lines[i]
        if (line.trimStart().startsWith("```")) {
            // 코드 블록 앞 텍스트의 후행 빈 줄을 제거해 코드 블록이 앞 문단에 붙어 보이도록 한다.
            val pending = textBuf.toString().trim('\n')
            if (pending.isNotEmpty()) result.add(ParsedSegment.TextBlock(pending))
            textBuf.clear()

            val language = line.trimStart().removePrefix("```").trim()
            val codeBuf = StringBuilder()
            i++
            while (i < lines.size && !lines[i].trimStart().startsWith("```")) {
                codeBuf.append(lines[i]).append("\n")
                i++
            }
            result.add(ParsedSegment.CodeBlock(language, codeBuf.toString().trimEnd('\n')))
        } else {
            textBuf.append(line).append("\n")
        }
        i++
    }

    val pending = textBuf.toString().trim('\n')
    if (pending.isNotEmpty()) result.add(ParsedSegment.TextBlock(pending))
    return result
}

// ── 어노테이션 빌더 (헤딩·목록·인라인) ─────────────────────────────────────────────

private val HEADING_REGEX = Regex("^(#{1,3})\\s*(.*)")

/**
 * 줄바꿈 없이 이어 붙은 리스트 항목을 분리하기 위한 패턴.
 *
 * 서버가 항목 사이에 `\n` 을 보내지 않고 `...앞 항목.* **다음 제목**` 처럼
 * 굵은 제목으로 시작하는 불릿을 바로 이어 붙이는 경우가 있다.
 * 비공백 문자 뒤에 오는 "단일 `*` + `**`" 를 줄바꿈 + 불릿으로 치환한다.
 * (줄 시작 불릿은 앞이 `\n`(공백)이므로 매칭되지 않아 중복 줄바꿈이 생기지 않는다.)
 */
private val LIST_BREAK_REGEX = Regex("(?<=\\S)\\s*\\*(?=\\s*\\*\\*)")

/** 완성된 `**bold**` 또는 `` `code` `` 토큰. */
private val INLINE_REGEX = Regex("\\*\\*(.+?)\\*\\*|`([^`]+)`")

/**
 * 한 줄이 리스트 항목이면 불릿을 제거한 내용을, 아니면 null 을 반환한다.
 *
 * - `- item`              → `item`
 * - `* item`              → `item`
 * - `* **title**` / `***title**` → `**title**`  (선두 `*` 가 홀수면 첫 `*` 가 불릿, 나머지는 굵기 마커)
 * - `**bold**`            → null (선두 `*` 가 짝수 → 불릿 아님, 굵은 문단)
 */
private fun parseListItem(line: String): String? {
    val s = line.trimStart()
    when {
        s.startsWith("-") && (s.length == 1 || s[1] == ' ') -> return s.drop(1).trimStart()
        s.startsWith("*") -> {
            val stars = s.takeWhile { it == '*' }.length
            if (stars % 2 == 1) return s.drop(1).trimStart()
        }
    }
    return null
}

private fun buildBlockAnnotation(
    text: String,
    inlineContent: MutableMap<String, InlineTextContent>
): AnnotatedString = buildAnnotatedString {
    var codeSeq = 0
    // 줄바꿈 없이 이어 붙은 리스트 항목을 먼저 줄 단위로 분리
    val normalized = text.replace(LIST_BREAK_REGEX, "\n*")
    val lines = normalized.split("\n")
    lines.forEachIndexed { index, line ->
        val listContent = parseListItem(line)
        when {
            line.isBlank() -> { /* 빈 줄 → 문단 간격 */ }

            line.startsWith("#") -> {
                val match = HEADING_REGEX.find(line)
                val level = match?.groupValues?.get(1)?.length ?: 1
                val content = match?.groupValues?.get(2).orEmpty()
                val size = when (level) {
                    1 -> 20.sp
                    2 -> 18.sp
                    else -> 17.sp
                }
                val weight = if (level <= 2) FontWeight.Bold else FontWeight.SemiBold
                withStyle(SpanStyle(fontSize = size, fontWeight = weight)) {
                    codeSeq = appendInline(content, inlineContent, codeSeq)
                }
            }

            listContent != null -> {
                append("•  ")
                codeSeq = appendInline(listContent, inlineContent, codeSeq)
            }

            else -> {
                codeSeq = appendInline(line, inlineContent, codeSeq)
            }
        }
        // 마지막 줄 뒤에는 개행을 넣지 않아 코드 블록/다음 요소와의 후행 간격을 없앤다.
        if (index < lines.lastIndex) append("\n")
    }
}

/**
 * 한 줄의 인라인 마크다운(`**bold**`, `` `code` ``)을 처리한다.
 * 완성되지 않은 마커는 [stripDangling]으로 제거하여 스트리밍 중 노출을 막는다.
 *
 * @return 다음 인라인 코드에 사용할 시퀀스 번호
 */
private fun AnnotatedString.Builder.appendInline(
    text: String,
    inlineContent: MutableMap<String, InlineTextContent>,
    startSeq: Int
): Int {
    var seq = startSeq
    var last = 0
    INLINE_REGEX.findAll(text).forEach { match ->
        append(stripDangling(text.substring(last, match.range.first)))
        if (match.value.startsWith("**")) {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append(match.groupValues[1])
            }
        } else {
            val code = match.groupValues[2]
            val id = "code_${seq++}"
            inlineContent[id] = inlineCodeChip(code)
            appendInlineContent(id, code)
        }
        last = match.range.last + 1
    }
    if (last < text.length) append(stripDangling(text.substring(last)))
    return seq
}

/** 짝이 맞지 않는(미완성) 마크다운 마커 제거 — 완성 전까지 순수 텍스트만 노출. */
private fun stripDangling(text: String): String =
    text.replace("**", "").replace("`", "")


/**
 * 인라인 코드 칩. 밝은 회색 둥근 배경 위에 빨간 모노스페이스 텍스트를 배치한다.
 *
 * - Placeholder.height = 2.2em: 일반 줄(22sp)보다 높아 칩이 들어간 줄에 위아래 여백이 생긴다.
 * - 바깥 Box fillMaxSize + Center: 칩을 Placeholder 세로 중앙에 배치하여 위아래 공간을 확보한다.
 * - 외부 좌우 여백은 추가하지 않는다(바깥 Box에 horizontal padding 없음).
 */
private fun inlineCodeChip(code: String): InlineTextContent =
    InlineTextContent(
        placeholder = Placeholder(
            width = (code.length * 0.65f + 0.8f).em,
            height = 2.2.em,
            placeholderVerticalAlign = PlaceholderVerticalAlign.Center
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFEAEAEA))
                    .padding(horizontal = 5.dp, vertical = 1.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = code,
                    color = Color(0xFFD32F2F),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
