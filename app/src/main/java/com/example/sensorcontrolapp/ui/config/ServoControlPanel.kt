package com.example.sensorcontrolapp.ui.config

import android.view.MotionEvent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.ui.Alignment
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.graphics.Color
import com.example.sensorcontrolapp.ui.sensor.SensorStatesViewModel







@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ServoControlPanel(
    onSendCommand: (String) -> Unit,
    sensorStatesViewModel: SensorStatesViewModel
) {
    var speedMultiplier by remember { mutableStateOf(1f) }
    var speedText by remember { mutableStateOf(speedMultiplier.toString()) }

    // Buton renk durumları
    var isForwardPressed by remember { mutableStateOf(false) }
    var isBackwardPressed by remember { mutableStateOf(false) }

    // ViewModel’den açı değerlerini al
    val currentAngle = sensorStatesViewModel.servoAngle.value
    val currentTargetAngle = sensorStatesViewModel.servoTargetAngle.value

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text("Servo Kontrol", style = MaterialTheme.typography.titleMedium)

        Spacer(modifier = Modifier.height(8.dp))

        // İleri / Durdur / Geri Butonları
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Button(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp)
                    .pointerInteropFilter {
                        when (it.action) {
                            MotionEvent.ACTION_DOWN -> {
                                isForwardPressed = true
                                onSendCommand("SERVO_FWD_START")
                            }
                            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                isForwardPressed = false
                                onSendCommand("SERVO_STOP")
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
                onClick = { onSendCommand("SERVO_STOP") }
            ) { Text("Durdur") }

            Button(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp)
                    .pointerInteropFilter {
                        when (it.action) {
                            MotionEvent.ACTION_DOWN -> {
                                isBackwardPressed = true
                                onSendCommand("SERVO_BWD_START")
                            }
                            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                isBackwardPressed = false
                                onSendCommand("SERVO_STOP")
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
                        onSendCommand("SERVO_SPEED_MULT=$value")
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
                    onSendCommand("SERVO_SPEED_MULT=$it")
                },
                valueRange = 0.5f..2f,
                steps = 3,
                modifier = Modifier
                    .weight(0.6f)
                    .padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Mevcut ve Hedef Açı kutusu
        androidx.compose.material3.Card(
            modifier = Modifier.fillMaxWidth(),
            colors = androidx.compose.material3.CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Mevcut Açı: ${currentAngle}°",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Hedef Açı: ${currentTargetAngle}°",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

