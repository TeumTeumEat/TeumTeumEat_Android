@file:JvmName("TypeKt")

package com.teumteumeat.teumteumeat.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.teumteumeat.teumteumeat.R

val Pretendard = FontFamily(
    Font(R.font.pretendard_bold, FontWeight.Bold),
    Font(R.font.pretendard_semi_bold, FontWeight.SemiBold),
    Font(R.font.pretendard_medium, FontWeight.Medium),
    Font(R.font.pretendard_regular, FontWeight.Normal),
)

@Immutable
data class AppTypography(

    // title
    val titleBold24: TextStyle,
    val titleBold22: TextStyle,
    val titleBold20: TextStyle,

    // subtitle
    val subtitleSemiBold20: TextStyle,
    val subtitleSemiBold18: TextStyle,
    val subtitleSemiBold16: TextStyle,

    // body
    val bodyMedium16: TextStyle,
    val bodyMedium14: TextStyle,

    // caption
    val captionRegular14: TextStyle,
    val captionRegular12: TextStyle,

    // button
    val buttonBold20: TextStyle,
    val buttonBold18: TextStyle,
    val btnSemiBold18: TextStyle,
    val titleSemiBold24: TextStyle,
    val bodyMedium16Reg: TextStyle,
    val bodyMedium14Reg: TextStyle,
    val titleBold32: TextStyle,
    val captionSemiBold16: TextStyle,
    val bodySemiBold18: TextStyle,
    val btnSemiBold20_h24: TextStyle,
    val lableMedium12_h14: TextStyle,
    val btnSemiBold18_h24: TextStyle,
    val btnSemiBold20_h24_g50: TextStyle,
    val btnSemiBold18_h24_g50: TextStyle,
    val btnBold20_h24: TextStyle,
)

private val DefaultTextColor = Black100

private val BaseTextStyle = TextStyle(
    fontFamily = Pretendard,
    color = DefaultTextColor
)
val DefaultAppTypography = AppTypography(

    // title
    titleBold32 = BaseTextStyle.copy(
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
    ),
    titleBold24 = BaseTextStyle.copy(
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 24.sp,
    ),
    titleBold22 = BaseTextStyle.copy(
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 22.sp,
    ),
    titleBold20 = BaseTextStyle.copy(
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 20.sp,
    ),
    titleSemiBold24 = BaseTextStyle.copy(
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 24.sp,
    ),

    // subtitle
    subtitleSemiBold20 = BaseTextStyle.copy(
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 20.sp,
    ),
    subtitleSemiBold18 = BaseTextStyle.copy(
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 18.sp,
    ),
    subtitleSemiBold16 = BaseTextStyle.copy(
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 16.sp,
    ),

    // body
    bodySemiBold18 = BaseTextStyle.copy(
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
    ),
    bodyMedium16 = BaseTextStyle.copy(
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
    ),
    bodyMedium14 = BaseTextStyle.copy(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 14.sp,
    ),
    bodyMedium16Reg = BaseTextStyle.copy(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 16.sp,
    ),
    bodyMedium14Reg = BaseTextStyle.copy(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 14.sp,
    ),

    lableMedium12_h14 = BaseTextStyle.copy(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 14.sp,
    ),

    // caption
    captionSemiBold16 = BaseTextStyle.copy(
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
    ),
    captionRegular14 = BaseTextStyle.copy(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 14.sp,
    ),
    captionRegular12 = BaseTextStyle.copy(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 12.sp,
    ),

    // button
    buttonBold20 = BaseTextStyle.copy(
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
    ),
    btnBold20_h24 = BaseTextStyle.copy(
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 24.sp,
    ),
    buttonBold18 = BaseTextStyle.copy(
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 18.sp,
    ),
    btnSemiBold20_h24 = BaseTextStyle.copy(
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 24.sp,
    ),
    btnSemiBold20_h24_g50 = BaseTextStyle.copy(
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 24.sp,
        color = Gray40
    ),
    btnSemiBold18 = BaseTextStyle.copy(
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
    ),
    btnSemiBold18_h24 = BaseTextStyle.copy(
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
    ),
    btnSemiBold18_h24_g50 = BaseTextStyle.copy(
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        color = Gray40
    ),

)



// Set of Material typography styles to start with
val Typography = Typography(

    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
    ),

    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
    ),

    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
    ),

    // subtitle 을 hadline 글꼴로 대체
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
    ),

    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
    ),

    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
    ),

    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
    ),

    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W400,
        fontSize = 14.sp,
    ),

    // display 를 caption 옵션으로 사용
    displayMedium =  TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W400,
        fontSize = 14.sp,
    ),

    displaySmall =  TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W400,
        fontSize = 12.sp,
    ),

    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
    ),

    labelSmall =TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
    ),

    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)