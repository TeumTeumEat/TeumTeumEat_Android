package com.teumteumeat.teumteumeat.ui.screen.a4_main.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.teumteumeat.teumteumeat.R
import com.teumteumeat.teumteumeat.utils.appTypography
import com.teumteumeat.teumteumeat.utils.extendedColors

@Composable
fun StampCountBadgeStateful(
    modifier: Modifier,
    count: Int,
    isActive: Boolean = true,
    title: String,
) {
    val theme = MaterialTheme.extendedColors
    val typo = MaterialTheme.appTypography

    val bgColor = theme.btnFillSecondary
    val textColor = theme.textPointBlue

    Row(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(50))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {

        Text(title,
            style = typo.bodyMedium14.copy(textColor)
        )

        Spacer(Modifier.width(6.dp))

        Icon(
            painter = painterResource(id = R.drawable.icon_stamp),
            contentDescription = "",
            tint = textColor,
            modifier = Modifier.size(24.dp)
        )

        Spacer(Modifier.width(4.dp))

        Text("$count",
            // todo. 추후 폰트 조정 필요, AppleSDGothicNeoB00, regular 20 h24
            style = typo.titleBold20.copy(textColor)
        )
    }
}
