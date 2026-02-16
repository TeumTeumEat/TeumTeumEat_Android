package com.teumteumeat.teumteumeat.ui.component.category_pager

import android.graphics.Color
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
private fun BreadcrumbItem(
    text: String,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentColor = MaterialTheme.colorScheme.primary
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            1.dp,
            contentColor
        ),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(
                start = 12.dp,
                end = 6.dp,
                top = 8.dp,
                bottom = 8.dp
            )
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    contentColor
                ),

                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier.width(5.dp))
            IconButton(
                onClick = onClear,
                modifier = Modifier
                    .size(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "clear breadcrumb",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,

                )
            }
        }
    }
}

@Composable
fun CategoryBreadcrumb(
    depth1Name: String?,
    depth2Name: String?,
    depth3Name: String?,
    onClearDepth1: () -> Unit,
    onClearDepth2: () -> Unit,
    onClearDepth3: () -> Unit,
    modifier: Modifier = Modifier
) {

    if (depth1Name.isNullOrBlank()) return

    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
    ) {
        Spacer(modifier = Modifier.width(20.dp))

        // 🔹 Depth 1
        BreadcrumbItem(
            text = depth1Name,
            onClear = onClearDepth1,
        )

        if (!depth2Name.isNullOrBlank()) {
            Spacer(modifier = Modifier.width(8.dp))
            BreadcrumbSeparator()
            Spacer(modifier = Modifier.width(8.dp))

            // 🔹 Depth 2
            BreadcrumbItem(
                text = depth2Name,
                onClear = onClearDepth2
            )
        }

        if (!depth3Name.isNullOrBlank()) {
            Spacer(modifier = Modifier.width(8.dp))
            BreadcrumbSeparator()
            Spacer(modifier = Modifier.width(8.dp))

            // 🔹 Depth 3
            BreadcrumbItem(
                text = depth3Name,
                onClear = onClearDepth3
            )
        }

        Spacer(modifier = Modifier.width(20.dp))

    }
}

@Composable
private fun BreadcrumbSeparator() {
    Text(
        text = ">",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}