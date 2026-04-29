package com.teumteumeat.teumteumeat.ui.component.button

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.teumteumeat.teumteumeat.R
import com.teumteumeat.teumteumeat.ui.component.text.AutoSizeText
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.appTypography


@Composable
fun BaseOutlineButton(
    modifier: Modifier = Modifier,
    text: String = "",
    textStyle: TextStyle = TextStyle(),
    subText: String? = null,
    subTextStyle: TextStyle = TextStyle(),
    isEnabled: Boolean = true,
    color: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit = {},
    contentAligment: Alignment = Alignment.Center,
    btnHeight: Int = 50,
    maxLine: Int = 1,
    overFlowSetting: TextOverflow = TextOverflow.Clip,
    showTrailingArrow: Boolean = false,
) {
    val contentColor =
        if (isEnabled) color else MaterialTheme.colorScheme.onSurfaceVariant
    val outlineColor =
        if (isEnabled) color else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = modifier.fillMaxWidth(),
    ) {
        Button(
            onClick = onClick,
            enabled = isEnabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = contentColor,
                disabledContainerColor = Color.White,
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.5.dp, outlineColor),
            modifier = Modifier
                .height(btnHeight.dp)
                .fillMaxWidth(),
        ) {
            if (subText != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = text,
                        style = textStyle,
                        maxLines = maxLine,
                        overflow = overFlowSetting,
                    )
                    Text(
                        text = subText,
                        style = subTextStyle,
                        maxLines = maxLine,
                        overflow = overFlowSetting,
                    )
                }
            } else if (showTrailingArrow) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = contentAligment,
                    ) {
                        Text(
                            text = text,
                            style = textStyle,
                            maxLines = maxLine,
                            overflow = overFlowSetting,
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = contentColor,
                    )
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = contentAligment,
                ) {
                    Text(
                        text = text,
                        style = textStyle,
                        maxLines = maxLine,
                        overflow = overFlowSetting,
                    )
                }
            }
        }
    }
}


@Composable
fun SelectableBaseOutlineButton(
    modifier: Modifier = Modifier,
    text: String = "",
    textStyle: TextStyle = TextStyle(),
    isSelected: Boolean = true,
    color: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit,
    contentAligment: Arrangement.Horizontal = Arrangement.Center,
    @DrawableRes iconRes: Int? = null,
) {
    val contentColor =
        if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant
    val outlineColor =
        if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = modifier,
        horizontalArrangement = contentAligment,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Button(
            onClick = onClick,
            contentPadding = PaddingValues(horizontal = 17.5.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = contentColor,
                disabledContainerColor = Color.White,
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.5.dp, outlineColor),
            modifier = modifier
                .defaultMinSize(minHeight = 60.dp),
        ) {
            Row(
                modifier = modifier,
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (iconRes != null) {
                    Image(
                        painter = painterResource(id = iconRes),
                        contentDescription = "카테고리 아이콘",
                        Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                AutoSizeText(
                    text = text,
                    modifier = Modifier.weight(1f, fill = false),
                    baseStyle = MaterialTheme.appTypography.btnSemiBold20_h24.copy(
                        color = contentColor,
                    )
                )
            }
        }
    }
}

@DrawableRes
fun getDepth1CategoryIconRes(categoryName: String): Int? {
    return when (categoryName) {
        "IT"           -> R.drawable.ic_category_it
        "스포츠"        -> R.drawable.ic_category_sport
        "경제"          -> R.drawable.ic_category_economy
        "주식"          -> R.drawable.ic_category_stock
        "생활 법률 및 제도" -> R.drawable.ic_category_institution
        "기초 과학"      -> R.drawable.ic_category_science
        "건강"          -> R.drawable.ic_category_health
        "시사 교양"      -> R.drawable.ic_category_current_events
        "맞춤법"        -> R.drawable.ic_category_spelling
        else           -> null
    }
}

@DrawableRes
fun getDepth2CategoryIconRes(categoryName: String): Int? {
    return when (categoryName) {
        // IT
        "앱개발자"              -> R.drawable.ic_cate_two_app_dev
        "웹개발자"              -> R.drawable.ic_cate_two_web
        "DevOps"               -> R.drawable.ic_cate_two_devops
        "데이터베이스"           -> R.drawable.ic_cate_two_database
        "네트워크"              -> R.drawable.ic_cate_two_network
        "PM"                   -> R.drawable.ic_cate_two_pm
        "디자인"                -> R.drawable.ic_cate_two_design

        // 스포츠
        "러닝 & 유산소"          -> R.drawable.ic_cate_two_running
        "웨이트(헬스)"           -> R.drawable.ic_cate_two_weight
        "구기 종목 (축구 & 농구)" -> R.drawable.ic_cate_two_ball_game

        // 경제
        "금융 기초"             -> R.drawable.ic_cate_two_finance
        "통화 정책"             -> R.drawable.ic_cate_two_finance

        // 주식
        "투자 입문"             -> R.drawable.ic_cate_two_investment
        "분석 기초"             -> R.drawable.ic_cate_two_analysis

        // 생활 법률 및 제도
        "주거와 계약"            -> R.drawable.ic_cate_two_property
        "생활과 노동"            -> R.drawable.ic_cate_two_labor

        // 기초 과학
        "물리 & 화학 상식"       -> R.drawable.ic_cate_two_chemistry
        "지구와 우주"            -> R.drawable.ic_cate_two_space

        // 건강
        "식품과 영양"            -> R.drawable.ic_cate_two_food
        "질환과 안전"            -> R.drawable.ic_cate_two_disease

        // 시사 교양
        "지리와 문화"            -> R.drawable.ic_cate_two_map
        "국제 사회"             -> R.drawable.ic_cate_two_society

        // 맞춤법
        "표준어 규정"            -> R.drawable.ic_cate_two_rule
        "실전 언어"             -> R.drawable.ic_cate_two_lang

        // 세금 (로그에서 확인된 경우 추가)
        // "세금 상식"             -> R.drawable.ic_cate_two_tax

        else -> null
    }
}

@Preview(showBackground = true)
@Composable
fun BaseOutlineButtonPreview() {
    TeumTeumEatTheme {

        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            BaseOutlineButton(
                text = "button",
                isEnabled = true,
                onClick = {
                    // Utils.UxUtils.moveActivity(context, LoginActivity::class.java, false)
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            SelectableBaseOutlineButton(
                text = "button",
                isSelected = false,
                onClick = {
                    // Utils.UxUtils.moveActivity(context, LoginActivity::class.java, false)
                },
            )
        }
    }
}
