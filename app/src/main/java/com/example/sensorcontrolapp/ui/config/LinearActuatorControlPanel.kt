package com.example.sensorcontrolapp.ui.config

import android.view.MotionEvent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import com.example.sensorcontrolapp.ui.sensor.SensorStatesViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LinearActuatorControlPanel(
    onSendCommand: (String) -> Unit,
    sensorStatesViewModel: SensorStatesViewModel
) {
    var speedMultiplier by remember { mutableStateOf(1f) }
    var speedText by remember { mutableStateOf(speedMultiplier.toString()) }

    var strokeTarget by remember { mutableStateOf(50f) }
    var strokeText by remember { mutableStateOf(strokeTarget.toInt().toString()) }

    var isForwardPressed by remember { mutableStateOf(false) }
    var isBackwardPressed by remember { mutableStateOf(false) }

    // ViewModel’den stroke değerlerini al
    val currentStroke = sensorStatesViewModel.actuatorStroke.value
    val currentTargetStroke = sensorStatesViewModel.actuatorTargetStroke.value

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text("Linear Aktüatör Kontrol", style = MaterialTheme.typography.titleMedium)

        Spacer(modifier = Modifier.height(8.dp))

        // İleri / Durdur / Geri butonları
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Button(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp)
                    .pointerInteropFilter {
                        when (it.action) {
                            MotionEvent.ACTION_DOWN -> {
                                isForwardPressed = true
                                onSendCommand("ACT_FWD_START")
                            }
                            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                isForwardPressed = false
                                onSendCommand("ACT_STOP")
                            }
                        }
                        true
                    },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isForwardPressed) Color.Green else MaterialTheme.colorScheme.primary
                ),
                onClick = {}
            ) { Text("İleri") }

            Button(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp),
                onClick = { onSendCommand("ACT_STOP") }
            ) { Text("Durdur") }

            Button(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp)
                    .pointerInteropFilter {
                        when (it.action) {
                            MotionEvent.ACTION_DOWN -> {
                                isBackwardPressed = true
                                onSendCommand("ACT_BWD_START")
                            }
                            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                isBackwardPressed = false
                                onSendCommand("ACT_STOP")
                            }
                        }
                        true
                    },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isBackwardPressed) Color.Green else MaterialTheme.colorScheme.primary
                ),
                onClick = {}
            ) { Text("Geri") }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Hız Katsayısı
        Text("Hız Katsayısı:")
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = speedText,
                onValueChange = { input ->
                    speedText = input
                    input.toFloatOrNull()?.let { value ->
                        speedMultiplier = value.coerceIn(0.5f, 2f)
                        onSendCommand("ACT_SPEED_MULT=$value")
                    }
                },
                label = { Text("Katsayı") },
                modifier = Modifier.weight(0.4f),
                singleLine = true
            )
            Slider(
                value = speedMultiplier,
                onValueChange = {
                    speedMultiplier = it
                    speedText = it.toString()
                    onSendCommand("ACT_SPEED_MULT=$it")
                },
                valueRange = 0.5f..2f,
                steps = 3,
                modifier = Modifier
                    .weight(0.6f)
                    .padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Hedef Stroke
        Text("Hedef Stroke (%):")
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = strokeText,
                onValueChange = { input ->
                    strokeText = input
                    input.toFloatOrNull()?.let { value ->
                        strokeTarget = value.coerceIn(0f, 100f)
                        onSendCommand("ACT_TARGET=${value.toInt()}")
                    }
                },
                label = { Text("Stroke") },
                modifier = Modifier.weight(0.4f),
                singleLine = true
            )
            Slider(
                value = strokeTarget,
                onValueChange = {
                    strokeTarget = it
                    strokeText = it.toInt().toString()
                    onSendCommand("ACT_TARGET=${it.toInt()}")
                },
                valueRange = 0f..100f,
                modifier = Modifier
                    .weight(0.6f)
                    .padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Mevcut ve Hedef Stroke kartı
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Mevcut Stroke: ${currentStroke}%",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Hedef Stroke: ${currentTargetStroke}%",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
