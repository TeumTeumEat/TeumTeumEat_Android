package com.teumteumeat.teumteumeat.ui.screen.d0_league.component

import android.provider.Settings
import androidx.annotation.RawRes
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
 * 순위 구간별 로티 정보.
 * 금트로피(1~10위)는 intro(등장 1회) 뒤 loop 구간을 반복하고, 캐릭터(순위권 밖)는 1회 재생 후 마지막 프레임에서 정지한다.
 * 프레임 값은 JSON 의 markers(intro/loop)와 같다. JSON 을 다시 만들면 함께 갱신해야 한다.
 */
private data class LeagueResultLottieSpec(
    @RawRes val rawRes: Int,
    val canvasAspectRatio: Float,
    val totalFrames: Int,
    val introFrames: IntRange? = null,
    val loopFrames: IntRange? = null,
    // 애니메이션을 끈 설정에서 보여줄 정지 프레임 (모든 요소가 보이는 시점)
    val stillFrame: Int,
)

// 1~10위 공통 금트로피 spec. JSON markers: intro 0..36 / loop 36..126
private val GoldTrophyLottieSpec = LeagueResultLottieSpec(
    rawRes = R.raw.trophy_gold_league_result, canvasAspectRatio = 280f / 190f, totalFrames = 126,
    introFrames = 0..36, loopFrames = 36..126, stillFrame = 70,
)

private fun LeagueResultTier.lottieSpec(): LeagueResultLottieSpec = when (this) {
    // 2~10위도 1위와 같은 금트로피 로티를 쓴다. (문구만 다르다)
    LeagueResultTier.FIRST, LeagueResultTier.SECOND -> GoldTrophyLottieSpec

    LeagueResultTier.OUT_OF_RANK -> LeagueResultLottieSpec(
        rawRes = R.raw.league_result_disappointed, canvasAspectRatio = 490f / 270f, totalFrames = 192,
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

    LottieAnimation(
        composition = compositionResult.value,
        progress = {
            if (animationsEnabled) animationState.progress
            else spec.stillFrame.toFloat() / spec.totalFrames
        },
        modifier = modifier
            .aspectRatio(spec.canvasAspectRatio),
    )
}
