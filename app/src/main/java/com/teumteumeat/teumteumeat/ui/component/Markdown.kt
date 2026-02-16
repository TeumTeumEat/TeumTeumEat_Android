package com.teumteumeat.teumteumeat.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import com.teumteumeat.teumteumeat.utils.appTypography


@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier
) {
    val annotated = remember(markdown) {
        buildAnnotatedString {
            markdown.lines().forEach { line ->
                when {
                    line.startsWith("### ") -> {
                        withStyle(
                            SpanStyle(
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        ) {
                            appendWithBold(line.removePrefix("### "))
                        }
                    }

                    line.startsWith("## ") -> {
                        withStyle(
                            SpanStyle(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            appendWithBold(line.removePrefix("## "))
                        }
                    }

                    line.startsWith("# ") -> {
                        withStyle(
                            SpanStyle(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            appendWithBold(line.removePrefix("# "))
                        }
                    }

                    line.startsWith("- ") -> {
                        appendWithBold("• ${line.removePrefix("- ")}")
                    }

                    line.isBlank() -> {
                        append("")
                    }

                    else -> {
                        appendWithBold(line)
                    }
                }
                append("\n")
            }
        }
    }

    Text(
        text = annotated,
        modifier = modifier,
        style = MaterialTheme.appTypography.bodyMedium16.copy(
            lineHeight = 22.sp
        )
    )
}

private fun AnnotatedString.Builder.appendWithBold(
    text: String
) {
    val regex = Regex("\\*\\*(.*?)\\*\\*")
    var lastIndex = 0

    regex.findAll(text).forEach { match ->
        // bold 이전 일반 텍스트
        append(text.substring(lastIndex, match.range.first))

        // bold 텍스트
        withStyle(
            SpanStyle(fontWeight = FontWeight.Bold)
        ) {
            append(match.groupValues[1])
        }

        lastIndex = match.range.last + 1
    }

    // 남은 텍스트
    if (lastIndex < text.length) {
        append(text.substring(lastIndex))
    }
}


