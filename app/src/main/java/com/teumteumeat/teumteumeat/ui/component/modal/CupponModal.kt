package com.teumteumeat.teumteumeat.ui.component.modal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.teumteumeat.teumteumeat.R
import com.teumteumeat.teumteumeat.ui.component.button.BaseFillButton
import com.teumteumeat.teumteumeat.ui.component.button.FillSecondaryButton
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.appTypography
import com.teumteumeat.teumteumeat.utils.extendedColors

// ⚠️ 아래 항목들은 프로젝트의 실제 패키지 경로에 맞게 수정해야 해!
// import com.yourpackage.R // R.drawable.ic_coupon을 사용하기 위해 필요해
// import com.yourpackage.components.FillSecondaryButton // 커스텀 버튼
// import com.yourpackage.components.BaseFillButton // 커스텀 버튼
// import com.yourpackage.theme.appTypography // 커스텀 타이포그래피 (MaterialTheme 확장)

// 🔹 2. 전체화면 프리뷰 (안드로이드 스튜디오 디자인 탭에서 확인용)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AdCouponModalPreview() {
    // 실제 테마(Theme)를 적용하고 싶다면 프로젝트의 테마(ex: MyAppTheme)로 감싸주세요.
    // 프리뷰에서는 다이얼로그의 어두운 배경(Dim)을 시각적으로 확인하기 위해 Box로 시뮬레이션합니다.
    var showDialog by remember { mutableStateOf(false) }


    TeumTeumEatTheme {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.extendedColors.backSurface), // 다이얼로그 뒷배경의 어두운 느낌
            contentAlignment = Alignment.Center
        ) {

            BaseFillButton(onClick = {
                showDialog = true
            })
            AdCouponDialog(
                showDialog = showDialog,
                couponCount = 1,         // 프리뷰 테스트용 데이터
                dailyCouponLimit = 2,
                onDismiss = { showDialog = false },
                onUseCoupon = { /* 프리뷰에서는 동작하지 않음 */ },
                onChargeCoupon = { /* 프리뷰에서는 동작하지 않음 */ },
                isAdLoading = true,
            )

            // 이전에 만든 모달 UI를 Dialog 안에 배치합니다.
            AdCouponModal(
                couponCount = 1,         // 프리뷰 테스트용 데이터
                dailyCouponLimit = 2,
                onClose = { showDialog = false },
                onUseCoupon = { /* 프리뷰에서는 동작하지 않음 */ },
                onChargeCoupon = { /* 프리뷰에서는 동작하지 않음 */ },
                isAdLoading = true,
            )
        }
    }
}

// 🔹 1. 실제 앱에서 사용할 다이얼로그 래퍼 (바깥 영역 클릭 방지 적용)
@Composable
fun AdCouponDialog(
    showDialog: Boolean,
    couponCount: Int,
    dailyCouponLimit: Int,
    onDismiss: () -> Unit,
    onUseCoupon: () -> Unit,
    onChargeCoupon: () -> Unit,
    isAdLoading: Boolean
) {
    if (showDialog) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnClickOutside = false, // ✅ 핵심: 바깥 영역을 클릭해도 닫히지 않게 설정
                dismissOnBackPress = true,      // 안드로이드 기기의 '뒤로 가기' 버튼으로 닫는 것을 허용할지 여부
                usePlatformDefaultWidth = false // 커스텀 패딩을 적용하기 위해 기본 너비 제한 해제
            )
        ) {
            // 이전에 만든 모달 UI를 Dialog 안에 배치합니다.
            AdCouponModal(
                isAdLoading = isAdLoading,
                couponCount = couponCount,
                dailyCouponLimit = dailyCouponLimit,
                onClose = onDismiss, // 모달 내의 닫기(X) 버튼 클릭 시 onDismiss 실행
                onUseCoupon = onUseCoupon,
                onChargeCoupon = onChargeCoupon
            )
        }
    }
}

@Composable
fun AdCouponModal(
    isAdLoading: Boolean = false,
    couponCount: Int = 0,
    dailyCouponLimit: Int = 10,
    onClose: () -> Unit,
    onUseCoupon: () -> Unit,
    onChargeCoupon: () -> Unit
) {

    val theme = MaterialTheme.extendedColors

    val hasCuppon = couponCount > 0


    // 기존 BaseModal의 구조를 활용하되, 중간에 상세 UI를 주입합니다.
    Surface(
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 40.dp)
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = MaterialTheme.extendedColors.modalShadow,
                spotColor = MaterialTheme.extendedColors.modalShadow,
            ),
    ) {
        // ✅ 좌상단 모달 닫기 버튼 배치를 위해 전체를 Box로 감싸기
        Box(modifier = Modifier.fillMaxWidth()) {

            Column(
                // 버튼 영역과 겹치지 않도록 Top 패딩을 조금 더 줍니다. (필요시 조절)
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 🔹 1. 타이틀
                Text(
                    text = "광고 보고 퀴즈 더 풀기",
                    style = MaterialTheme.appTypography.titleBold22,
                    color = theme.textPrimary
                )

                // ✅ 텍스트에 카운트 변수 적용
                Text(
                    text = "사용 가능 쿠폰 $couponCount/${dailyCouponLimit}",
                    style = MaterialTheme.appTypography.captionRegular14
                        .copy(color = theme.textSecondary),
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 🔹 2. 회색 안내 박스
                Surface(
                    color = theme.btnGray100,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_coupon),
                            contentDescription = null,
                            tint = theme.btnGray400,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = buildAnnotatedString {
                                withStyle(
                                    style = SpanStyle(
                                        fontSize = 12.sp,
                                        color = theme.textPointBlue,
                                        fontWeight = FontWeight.Bold,
                                    )
                                ) {
                                    append("퀴즈 쿠폰")
                                }
                                append("으로 하루 한번만 가능했던\n틈틈잇 퀴즈를 추가로 풀 수 있어요!")
                            },
                            style = MaterialTheme.appTypography.lableMedium12_h14,
                            color = theme.textPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 🔹 3. 불렛 포인트 리스트
                val bulletText = """
                    • 하루 최대 10번 사용 가능해요.
                    • 광고로 받은 쿠폰은 당일까지 사용 가능
                """.trimIndent()

                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = bulletText,
                        color = theme.textGhost,
                        // ✅ 앞서 적용했던 폰트 여백 제거 속성은 그대로 유지합니다.
                        style = MaterialTheme.appTypography.captionRegular12.copy(
                            lineHeight = 18.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 🔹 4. 하단 버튼
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FillSecondaryButton(
                        text = "쿠폰 사용",
                        textStyle = MaterialTheme.appTypography.btnSemiBold18_h24,
                        isEnabled = hasCuppon,
                        isModalBtn = true,
                        modifier = Modifier.weight(1f),
                        onClick = onUseCoupon,
                        conerRadius = 16.dp
                    )

                    BaseFillButton(
                        // 로딩 중일 때는 텍스트를 비우거나 "로딩 중..."으로 변경
                        text = if (isAdLoading) "" else {
                            when (dailyCouponLimit) {
                                0 -> "쿠폰 충전"
                                in 1..couponCount -> "미리 충전하기"
                                else -> "미리 충전하기"
                            }
                        },
                        isLoading = isAdLoading,
                        textStyle = MaterialTheme.appTypography.btnSemiBold18_h24,
                        isEnabled = dailyCouponLimit > 0 && couponCount < dailyCouponLimit,
                        isModalBtn = true,
                        modifier = Modifier.weight(1f),
                        conerRadius = 16.dp,
                        onClick = onChargeCoupon
                    )
                }
            }

            // ✅ 수정됨: 명확한 리플 효과와 터치 영역을 제공하는 IconButton 위젯 사용
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(5.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_close),
                    contentDescription = "닫기",
                    tint = theme.textTeritory,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}