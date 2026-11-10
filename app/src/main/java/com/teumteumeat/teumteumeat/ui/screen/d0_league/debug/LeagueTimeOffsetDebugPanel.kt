package com.teumteumeat.teumteumeat.ui.screen.d0_league.debug

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

// 앱 테마의 onSurface 가 옅어 패널 글자가 잘 보이지 않으므로 디버그 패널 전용 글자색을 고정한다.
private val PanelTextColor = Color(0xFF1F2937)

/**
 * [dev/local 전용] 리그 시각 오프셋(`X-League-Time-Offset`) 테스트 패널. `BuildConfig.DEBUG` 일 때만 노출한다.
 *
 * @param resetRemainingSeconds 화면에 표시 중인 리셋까지 남은 시간(초) — '리셋 N초 전' 프리셋 계산에 쓴다.
 * @param onOffsetChanged 오프셋을 바꾼 직후 호출된다. 호출부는 새 오프셋으로 리그를 다시 조회한다.
 * @param onResultSeenRecordCleared '결과 확인 기록 초기화'에 성공한 직후 호출된다. 호출부는 결과 모달을 다시 조회한다.
 */
@Composable
fun LeagueTimeOffsetDebugPanel(
    resetRemainingSeconds: Long,
    onOffsetChanged: () -> Unit,
    onResultSeenRecordCleared: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LeagueTimeOffsetViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var expanded by rememberSaveable { mutableStateOf(false) }

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        shadowElevation = 4.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TextButton(onClick = { expanded = !expanded }) {
                Text("DEV 시간 오프셋 ${uiState.appliedOffsetSeconds?.let { "${it}s" } ?: "OFF"} ${if (expanded) "▲" else "▼"}")
            }

            if (expanded) {
                OutlinedTextField(
                    value = uiState.input,
                    onValueChange = viewModel::onInputChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("오프셋(초, 음수 가능)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PanelTextColor,
                        unfocusedTextColor = PanelTextColor,
                        unfocusedLabelColor = PanelTextColor,
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        enabled = uiState.canApplyInput,
                        onClick = { if (viewModel.applyInput()) onOffsetChanged() },
                    ) { Text("적용") }
                    OutlinedButton(
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PanelTextColor),
                        onClick = {
                            viewModel.clearOffset()
                            onOffsetChanged()
                        },
                    ) { Text("해제") }
                }
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        viewModel.applyPresetBeforeReset(resetRemainingSeconds)
                        onOffsetChanged()
                    },
                ) { Text("리셋 ${DEFAULT_SECONDS_BEFORE_RESET}초 전으로 이동") }
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PanelTextColor),
                    onClick = {
                        viewModel.clearResultSeenRecord { success ->
                            if (success) {
                                Toast.makeText(context, "결과 확인 기록을 초기화했어요", Toast.LENGTH_SHORT).show()
                                onResultSeenRecordCleared()
                            } else {
                                Toast.makeText(context, "결과 확인 기록 초기화에 실패했어요", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                ) { Text("결과 확인 기록 초기화") }
                Text(
                    text = "현재 남은 시간 ${resetRemainingSeconds}s · 프리셋은 적용 중인 오프셋에 누적됩니다",
                    style = MaterialTheme.typography.bodySmall,
                    color = PanelTextColor,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }
        }
    }
}
