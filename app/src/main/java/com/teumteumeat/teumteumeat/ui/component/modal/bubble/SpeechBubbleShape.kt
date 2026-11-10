package com.teumteumeat.teumteumeat.ui.component.modal.bubble

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

// 🎯 일체형 커스텀 도형 (말풍선 본체 + 꼬리)
// HomeSpeechBubble, LeagueSpeechBubble이 공유하는 말풍선 셰이프
class SpeechBubbleShape(
    private val cornerRadius: Dp,
    private val tailWidth: Dp,
    private val tailHeight: Dp,
    private val tailPaddingEnd: Dp,
    private val tailTipRadius: Dp = 8.dp // 💡 둥근 정도를 설정하는 새로운 변수 (기본값 4.dp)
) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        return Outline.Generic(Path().apply {
            val tw = with(density) { tailWidth.toPx() }
            val th = with(density) { tailHeight.toPx() }
            val tpe = with(density) { tailPaddingEnd.toPx() }

            // 꼬리 끝 둥글기가 꼬리 높이보다 커지지 않도록 제한합니다.
            val ttr = with(density) { tailTipRadius.toPx() }.coerceAtMost(th)

            val bodyTop = th // 꼬리 아래부터 본체 시작
            val bodyBottom = size.height
            val bodyLeft = 0f
            val bodyRight = size.width

            // Figma의 corner radius "999" 트릭과 동일하게, 본체의 짧은 변 절반을 넘지 않도록
            // clamp하여 큰 값을 넣으면 항상 완전한 필(pill) 모양이 되도록 만듭니다.
            val cr = with(density) { cornerRadius.toPx() }
                .coerceAtMost((bodyBottom - bodyTop) / 2f)
                .coerceAtMost((bodyRight - bodyLeft) / 2f)

            val tailTipX = bodyRight - tpe - (tw / 2f)
            val tailLeftX = bodyRight - tpe - tw
            val tailRightX = bodyRight - tpe

            // 💡 둥근 모서리를 시작하고 끝낼 좌표를 계산합니다.
            // 꼬리 높이(th) 대비 둥글기(ttr)의 비율을 구합니다.
            val ratio = ttr / th

            // 곡선이 시작되는 X, Y 좌표 (꼭짓점 도달 전)
            val curveStartX = tailTipX - (tailTipX - tailLeftX) * ratio
            val curveStartY = ttr

            // 곡선이 끝나는 X, Y 좌표 (꼭짓점을 넘어선 후)
            val curveEndX = tailTipX + (tailRightX - tailTipX) * ratio
            val curveEndY = ttr

            // 좌상단부터 시계방향으로 그립니다.
            moveTo(bodyLeft + cr, bodyTop)

            // 윗변과 꼬리
            lineTo(tailLeftX, bodyTop)

            // 1. 꼭짓점 바로 아래(curveStartY)까지 직선을 긋습니다.
            lineTo(curveStartX, curveStartY)

            // 2. 원래의 뾰족한 끝(tailTipX, 0f)을 제어점으로 삼아 부드러운 곡선을 그립니다.
            quadraticBezierTo(
                x1 = tailTipX, y1 = 0f, // 꺾이는 기준점 (원래의 뾰족한 끝)
                x2 = curveEndX, y2 = curveEndY // 곡선이 도착할 점
            )

            // 3. 꼬리 오른쪽 밑동으로 내려옵니다.
            lineTo(tailRightX, bodyTop)

            lineTo(bodyRight - cr, bodyTop)

            // 우상단 모서리
            arcTo(Rect(bodyRight - 2*cr, bodyTop, bodyRight, bodyTop + 2*cr), -90f, 90f, false)
            // 우측변
            lineTo(bodyRight, bodyBottom - cr)
            // 우하단 모서리
            arcTo(Rect(bodyRight - 2*cr, bodyBottom - 2*cr, bodyRight, bodyBottom), 0f, 90f, false)
            // 아랫변
            lineTo(bodyLeft + cr, bodyBottom)
            // 좌하단 모서리
            arcTo(Rect(bodyLeft, bodyBottom - 2*cr, bodyLeft + 2*cr, bodyBottom), 90f, 90f, false)
            // 좌측변
            lineTo(bodyLeft, bodyTop + cr)
            // 좌상단 모서리
            arcTo(Rect(bodyLeft, bodyTop, bodyLeft + 2*cr, bodyTop + 2*cr), 180f, 90f, false)

            close() // 도형 닫기
        })
    }
}
