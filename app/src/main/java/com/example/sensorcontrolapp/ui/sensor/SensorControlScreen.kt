package com.example.sensorcontrolapp.ui.sensor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState








data class SensorItem(
    val key: String,
    val displayName: String
)

val sensorList = listOf(
    SensorItem("LOAD", "Yük Hücresi"),
    SensorItem("TEMP", "Sıcaklık Sensörü"),
    SensorItem("LOCATION", "ADXL345 Lokasyon"),
    SensorItem("ROT", "Rotary Encoder"),
    SensorItem("LIN", "Lineer Potansiyometre"),
    SensorItem("AS5600", "Manyetik Açı Sensörü"),
    SensorItem("VOLT", "Gerilim Sensörü"),
    SensorItem("VIBRATION", "Titreşim Modülü"),
    SensorItem("ADS", "ADS1115 ADC"),
    SensorItem("ACT", "Linear Aktüatör")
)

@Composable
fun SensorControlScreen(
    onSendCommand: (String) -> Unit,
    onBack: () -> Unit,
    receivedText: kotlinx.coroutines.flow.StateFlow<String>,
    onNavigateToSensorDetail: (String) -> Unit,
    sensorStatesViewModel: SensorStatesViewModel
) {
    val currentOutput = receivedText.collectAsState().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Sensör Kontrol Paneli", style = MaterialTheme.typography.titleMedium)

        Spacer(modifier = Modifier.height(12.dp))

        sensorList.forEach { sensor ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToSensorDetail(sensor.key) }
                    .padding(vertical = 6.dp)
            ) {
                Text(
                    text = sensor.displayName,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Filled.ArrowForward,
                    contentDescription = "Detay"
                )

            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("STM32'den Gelen Veri:", style = MaterialTheme.typography.titleMedium)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            currentOutput
                .split("\n")
                .filter { it.isNotBlank() }
                .forEach { line ->
                    Text(text = line)
                }
        }


        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onBack) {
            Text("Geri Dön")
        }
    }
}
