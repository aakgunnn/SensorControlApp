package com.example.sensorcontrolapp.ui.config

import android.view.MotionEvent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Alignment
import com.example.sensorcontrolapp.ui.sensor.SensorStatesViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ServoControlPanel(
    onSendCommand: (String) -> Unit,
    sensorStatesViewModel: SensorStatesViewModel
) {
    var speedMultiplier by remember { mutableStateOf(1f) }
    var speedText by remember { mutableStateOf(speedMultiplier.toString()) }

    // basılı durumlar
    var isForwardPressed by remember { mutableStateOf(false) }
    var isBackwardPressed by remember { mutableStateOf(false) }

    // View erişimi: parent scroll'un intercept etmesini kapatacağız
    val view = LocalView.current

    // ViewModel’den açı değerleri
    val currentAngle = sensorStatesViewModel.servoAngle.value
    val currentTargetAngle = sensorStatesViewModel.servoTargetAngle.value

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text("Servo Kontrol", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        // İleri / Durdur / Geri
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {

            // İleri
            Button(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp)
                    .pointerInteropFilter { ev ->
                        when (ev.action) {
                            MotionEvent.ACTION_DOWN -> {
                                // scroll parent intercept etmesin
                                view.parent?.requestDisallowInterceptTouchEvent(true)
                                isForwardPressed = true
                                onSendCommand("SERVO")
                                onSendCommand("SERVO_FWD_START")
                                true
                            }
                            MotionEvent.ACTION_UP -> {
                                isForwardPressed = false
                                onSendCommand("SERVO")
                                onSendCommand("SERVO_STOP")
                                // tekrar izin ver
                                view.parent?.requestDisallowInterceptTouchEvent(false)
                                true
                            }
                            MotionEvent.ACTION_CANCEL -> {
                                isForwardPressed = false
                                //onSendCommand("SERVO_STOP")
                                view.parent?.requestDisallowInterceptTouchEvent(false)
                                true
                            }
                            else -> true
                        }
                    },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isForwardPressed)
                        MaterialTheme.colorScheme.secondaryContainer
                    else
                        MaterialTheme.colorScheme.primary,
                    contentColor = if (isForwardPressed)
                        MaterialTheme.colorScheme.onSecondaryContainer
                    else
                        MaterialTheme.colorScheme.onPrimary
                ),
                onClick = {}
            ) { Text("İleri") }

            // Durdur
            Button(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp),
                onClick = { onSendCommand("SERVO_STOP") }
            ) { Text("Durdur") }

            // Geri
            Button(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp)
                    .pointerInteropFilter { ev ->
                        when (ev.action) {
                            MotionEvent.ACTION_DOWN -> {
                                view.parent?.requestDisallowInterceptTouchEvent(true)
                                isBackwardPressed = true
                                onSendCommand("SERVO_BWD_START")
                                true
                            }
                            MotionEvent.ACTION_UP -> {
                                isBackwardPressed = false
                                onSendCommand("SERVO_STOP")
                                view.parent?.requestDisallowInterceptTouchEvent(false)
                                true
                            }
                            MotionEvent.ACTION_CANCEL -> {
                                isBackwardPressed = false
                                onSendCommand("SERVO_STOP")
                                view.parent?.requestDisallowInterceptTouchEvent(false)
                                true
                            }
                            else -> true
                        }
                    },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isBackwardPressed)
                        MaterialTheme.colorScheme.secondaryContainer
                    else
                        MaterialTheme.colorScheme.primary,
                    contentColor = if (isBackwardPressed)
                        MaterialTheme.colorScheme.onSecondaryContainer
                    else
                        MaterialTheme.colorScheme.onPrimary
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

        // Mevcut ve Hedef Açı
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Mevcut Açı: ${currentAngle}°", style = MaterialTheme.typography.bodyLarge)
                Text(text = "Hedef Açı: ${currentTargetAngle}°", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
