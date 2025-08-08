package com.example.sensorcontrolapp.ui.config

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sensorcontrolapp.model.UserConfig
import com.example.sensorcontrolapp.model.CurrentRange
import androidx.compose.ui.Alignment
import com.example.sensorcontrolapp.ui.sensor.SensorStatesViewModel

@Composable
fun ConfigScreen(
    initialConfig: UserConfig,
    onSave: (UserConfig) -> Unit,
    onBack: () -> Unit,
    onNavigateToSensorConfig: (String) -> Unit,
    sensorStatesViewModel: SensorStatesViewModel,
    onSendCommand: (String) -> Unit
) {
    var selectedSensors by remember { mutableStateOf(initialConfig.enabledSensors.toMutableList()) }
    var refreshMs by remember { mutableStateOf(initialConfig.defaultSensorRefreshMs.toString()) }

    val currentLimits = remember {
        mutableStateMapOf<String, CurrentRange>().apply { putAll(initialConfig.currentLimits) }
    }

    val sensorOptions = listOf(
        "TEMP" to "Sıcaklık Sensörü",
        "LOAD" to "Yük Hücresi",
        "LOCATION" to "ADXL345 Lokasyon Sensörü",
        "ROT" to "Rotary Encoder",
        "LIN" to "Lineer Potansiyometre",
        "AS5600" to "Manyetik Açı Sensörü",
        "VOLT" to "Gerilim Sensörü",
        "VIBRATION" to "Titreşim Modülü",
        "ADS" to "ADS1115 ADC",
        "ACT" to "Linear Aktüatör",
        "SERVO" to "Servo Motor"
    )

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text("Sensor Settings", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))

        sensorOptions.forEach { (sensorKey, label) ->

            if (!currentLimits.containsKey(sensorKey)) {
                currentLimits[sensorKey] = CurrentRange(0, 1000)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToSensorConfig(sensorKey) }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = selectedSensors.contains(sensorKey),
                    onCheckedChange = { isChecked ->
                        if (isChecked) {
                            selectedSensors = (selectedSensors + sensorKey).distinct().toMutableList()
                        } else {
                            selectedSensors = selectedSensors.filterNot { it == sensorKey }.toMutableList()


                            sensorStatesViewModel.setSensorState(sensorKey, false)
                            onSendCommand("${sensorKey}_OFF")
                        }
                    }
                )
                Text(label, modifier = Modifier.weight(1f))
            }

            // Servo panel
            if (sensorKey == "SERVO" && selectedSensors.contains("SERVO")) {
                ServoControlPanel(
                    onSendCommand = onSendCommand,
                    sensorStatesViewModel = sensorStatesViewModel
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Linear Aktüatör panel
            if (sensorKey == "ACT" && selectedSensors.contains("ACT")) {
                LinearActuatorControlPanel(
                    onSendCommand = onSendCommand,
                    sensorStatesViewModel = sensorStatesViewModel
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = refreshMs,
            onValueChange = { refreshMs = it },
            label = { Text("Refresh Interval (ms)") }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row {
            Button(onClick = {
                val newConfig = initialConfig.copy(
                    enabledSensors = selectedSensors.toList(),
                    defaultSensorRefreshMs = refreshMs.toIntOrNull() ?: 1000,
                    currentLimits = currentLimits.toMap()
                )
                onSave(newConfig)
            }) {
                Text("Save")
            }

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedButton(onClick = onBack) {
                Text("Cancel")
            }
        }
    }
}
