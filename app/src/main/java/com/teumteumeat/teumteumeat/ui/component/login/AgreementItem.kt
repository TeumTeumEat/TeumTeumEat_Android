package com.teumteumeat.teumteumeat.ui.component.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.teumteumeat.teumteumeat.ui.component.CheckBoxCircle

@Composable
fun AgreementItem(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    // ✅ 기존 단순 텍스트용 (선택)
    text: String = "",
    isLink: Boolean = false,
    onTextClick: (() -> Unit)? = null,

    // ✅ 복잡한 UI를 위한 슬롯
    content: (@Composable () -> Unit) = {},
) {
    Row(
        Modifier
            .wrapContentWidth()
            .padding(vertical = 12.dp)
            .clickable { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        CheckBoxCircle(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )

        Spacer(modifier = Modifier.width(8.dp))

        content()
    }
}
