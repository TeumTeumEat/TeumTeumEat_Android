package com.teumteumeat.teumteumeat.ui.component.quiz_card

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.teumteumeat.teumteumeat.ui.component.canvas_icon.CircleOutlineIcon
import com.teumteumeat.teumteumeat.ui.component.canvas_icon.XOutlineIcon
import com.teumteumeat.teumteumeat.utils.extendedColors

enum class BtnType { ACCEPT, REJECT,}

data class QuizButtonStyle(
    val background: Color,
    val iconColor: Color,
)

@Composable
fun resolveQuizButtonStyle(
    cardStatus: CardStatus,
    btnType: BtnType,
): QuizButtonStyle {

    val theme = MaterialTheme.extendedColors

    return when (cardStatus) {

        CardStatus.Default -> {
            if (btnType == BtnType.ACCEPT) {
                QuizButtonStyle(
                    background = theme.primaryContainer,   // 연한 파랑
                    iconColor = theme.primary
                )
            } else {
                QuizButtonStyle(
                    background = theme.errorContainer,     // 연한 빨강
                    iconColor = theme.error
                )
            }
        }

        CardStatus.Accept -> {
            if (btnType == BtnType.ACCEPT) {
                QuizButtonStyle(
                    background = theme.primary,            // 진한 파랑
                    iconColor = theme.textOnPrimary
                )
            } else {
                QuizButtonStyle(
                    background = theme.unableContainer,     // 회색
                    iconColor = theme.unableContent
                )
            }
        }

        CardStatus.Reject -> {
            if (btnType == BtnType.REJECT) {
                QuizButtonStyle(
                    background = theme.error,              // 진한 빨강
                    iconColor = theme.textOnError
                )
            } else {
                QuizButtonStyle(
                    background = theme.unableContainer,     // 회색
                    iconColor = theme.unableContent
                )
            }
        }
    }
}


@Composable
fun QuizAnswerButton(
    modifier: Modifier,
    background: Color,
    onClick: () -> Unit,
    isBtnType: BtnType,
    iconColor: Color,
) {

    Surface(
        modifier = modifier
            .height(69.dp),
        shape = RoundedCornerShape(24.dp),
        color = background,
        onClick = onClick
    ) {
        Box(
            contentAlignment = Alignment.Center

        ) {
            when(isBtnType) {
                BtnType.ACCEPT -> {
                    CircleOutlineIcon(
                        strokeWidth = 7.dp,
                        color = iconColor,
                    )
                }
                BtnType.REJECT -> {
                    XOutlineIcon(
                        strokeWidth = 7.dp,
                        color = iconColor,
                    )

                }
            }

        }
    }
}
