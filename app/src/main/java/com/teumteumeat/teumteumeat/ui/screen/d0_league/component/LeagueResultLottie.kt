package com.teumteumeat.teumteumeat.ui.screen.d0_league.component

import android.provider.Settings
import androidx.annotation.RawRes
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieClipSpec
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.teumteumeat.teumteumeat.R
import com.teumteumeat.teumteumeat.domain.model.league.LeagueResultTier
import kotlinx.coroutines.delay

// 모달이 뜬 뒤 로티를 재생하기까지의 짧은 대기 시간
private const val LOTTIE_START_DELAY_MS = 300L

/**
 * 트로피 로티(300x300 캔버스)에서 실제로 보이는 영역의 캔버스 좌표.
 * 트로피 몸체는 캔버스 안에서 60% 스케일이라 캔버스 전체를 그대로 채우면 작게 보이므로,
 * 후광·반짝임이 들어오는 가로폭과 받침 하단을 기준으로 모달 영역에 맞춰 키운다.
 * JSON 을 다시 만들면 함께 갱신해야 한다.
 */
internal object TrophyArt {
    const val CANVAS_SIZE = 300f

    // 후광(지름 236)·반짝임이 들어오는 가로폭
    const val ART_WIDTH = 236f

    // 반짝임·후광의 위쪽 끝
    const val ART_TOP = 15f

    // 트로피 받침 하단(가만히 있을 때). 위아래 흔들림(bob)으로 ±3.5 정도 움직인다.
    const val BASE_BOTTOM = 203.4f
}

/** 트로피 로티를 그릴 정사각 캔버스의 크기와, 영역 상단 기준 세로 이동량(dp). */
internal data class TrophyLottiePlacement(val sizeDp: Float, val offsetYDp: Float)

/**
 * 영역(가로 [areaWidthDp] x 세로 [areaHeightDp])에 트로피를 앉힌다.
 * 받침 하단이 영역 맨 아래에 닿도록 배치하므로, 바로 아래 간격(Spacer)이 곧 받침과 다음 요소 사이의 간격이 된다.
 * 배율은 후광 가로폭이 영역 폭을 넘지 않고, 반짝임 위쪽 끝~받침 하단이 영역 높이에 들어오는 값 중 작은 쪽이다.
 */
internal fun trophyLottiePlacement(areaWidthDp: Float, areaHeightDp: Float): TrophyLottiePlacement {
    val scaleByWidth = areaWidthDp / TrophyArt.ART_WIDTH
    val scaleByHeight = areaHeightDp / (TrophyArt.BASE_BOTTOM - TrophyArt.ART_TOP)
    val scale = minOf(scaleByWidth, scaleByHeight)
    return TrophyLottiePlacement(
        sizeDp = TrophyArt.CANVAS_SIZE * scale,
        offsetYDp = areaHeightDp - TrophyArt.BASE_BOTTOM * scale,
    )
}

/**
 * 순위 구간별 로티 정보.
 * 트로피(1~10위)는 intro(등장 1회) 뒤 loop 구간을 반복하고, 캐릭터(순위권 밖)는 1회 재생 후 마지막 프레임에서 정지한다.
 * 프레임 값은 JSON 의 markers(intro/loop)와 같다. JSON 을 다시 만들면 함께 갱신해야 한다.
 * [fitsTrophyArea]가 true 이면 캔버스 비율 대신 [trophyLottiePlacement]로 영역에 맞춰 그린다.
 */
private data class LeagueResultLottieSpec(
    @RawRes val rawRes: Int,
    val canvasAspectRatio: Float,
    val totalFrames: Int,
    val introFrames: IntRange? = null,
    val loopFrames: IntRange? = null,
    // 애니메이션을 끈 설정에서 보여줄 정지 프레임 (모든 요소가 보이는 시점)
    val stillFrame: Int,
    val fitsTrophyArea: Boolean = false,
)

private fun LeagueResultTier.lottieSpec(): LeagueResultLottieSpec = when (this) {
    // JSON markers: intro 0..40 / loop 40..160. 정지 프레임 66 은 반짝임 3개가 보이는 시점이다.
    LeagueResultTier.FIRST -> LeagueResultLottieSpec(
        rawRes = R.raw.league_result_trophy_gold, canvasAspectRatio = 1f, totalFrames = 160,
        introFrames = 0..40, loopFrames = 40..160, stillFrame = 66, fitsTrophyArea = true,
    )

    LeagueResultTier.SECOND -> LeagueResultLottieSpec(
        rawRes = R.raw.league_result_trophy_silver, canvasAspectRatio = 1f, totalFrames = 160,
        introFrames = 0..40, loopFrames = 40..160, stillFrame = 66, fitsTrophyArea = true,
    )

    LeagueResultTier.OUT_OF_RANK -> LeagueResultLottieSpec(
        rawRes = R.raw.league_result_sit_melt, canvasAspectRatio = 490f / 270f, totalFrames = 192,
        stillFrame = 192,
    )
}

/**
 * 리그 결과 모달의 로티. 모달이 뜬 뒤 짧은 딜레이를 두고 재생하며, 화면에서 사라지면(컴포지션 이탈) 재생도 멈춘다.
 * 시스템 애니메이션이 꺼져 있으면 재생하지 않고 정지 프레임만 보여주고, 로티 로드에 실패하면 아무것도 그리지 않는다.
 */
@Composable
fun LeagueResultLottie(
    tier: LeagueResultTier,
    modifier: Modifier = Modifier,
) {
    val spec = remember(tier) { tier.lottieSpec() }
    val compositionResult = rememberLottieComposition(LottieCompositionSpec.RawRes(spec.rawRes))
    if (compositionResult.isFailure) return

    val context = LocalContext.current
    val animationsEnabled = remember {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) != 0f
    }

    var isStarted by remember { mutableStateOf(false) }
    var isIntroDone by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(LOTTIE_START_DELAY_MS)
        isStarted = true
    }

    val intro = spec.introFrames
    val loop = spec.loopFrames
    val isLooping = intro != null && loop != null
    val inLoopPhase = isLooping && isIntroDone

    val animationState = animateLottieCompositionAsState(
        composition = compositionResult.value,
        isPlaying = isStarted && animationsEnabled,
        iterations = if (inLoopPhase) LottieConstants.IterateForever else 1,
        clipSpec = when {
            inLoopPhase -> LottieClipSpec.Frame(loop!!.first, loop.last)
            isLooping -> LottieClipSpec.Frame(intro!!.first, intro.last)
            else -> null
        },
    )

    // intro 구간이 끝나면 loop 구간 반복으로 전환한다.
    LaunchedEffect(animationState.isAtEnd, animationState.isPlaying) {
        if (isLooping && !isIntroDone && animationState.isAtEnd && !animationState.isPlaying) {
            isIntroDone = true
        }
    }

    val composition = compositionResult.value
    val progress = {
        if (animationsEnabled) animationState.progress
        else spec.stillFrame.toFloat() / spec.totalFrames
    }

    if (spec.fitsTrophyArea) {
        // 모달이 정해 준 영역 안에서 받침 하단을 영역 맨 아래에 맞춰 그린다.
        // 캔버스가 영역보다 커서 requiredSize 를 쓰면 내용이 세로 중앙으로 밀리므로, 위치를 직접 지정한다.
        BoxWithConstraints(modifier = modifier) {
            val placement = trophyLottiePlacement(maxWidth.value, maxHeight.value)
            LottieAnimation(
                composition = composition,
                progress = progress,
                modifier = Modifier
                    .fillMaxSize()
                    .layout { measurable, constraints ->
                        val sizePx = placement.sizeDp.dp.roundToPx()
                        val placeable = measurable.measure(Constraints.fixed(sizePx, sizePx))
                        layout(constraints.maxWidth, constraints.maxHeight) {
                            placeable.place(
                                x = (constraints.maxWidth - sizePx) / 2,
                                y = placement.offsetYDp.dp.roundToPx(),
                            )
                        }
                    },
            )
        }
    } else {
        LottieAnimation(
            composition = composition,
            progress = progress,
            modifier = modifier.aspectRatio(spec.canvasAspectRatio),
        )
    }
}
