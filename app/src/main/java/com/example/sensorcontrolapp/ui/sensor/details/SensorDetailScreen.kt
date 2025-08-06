package com.example.sensorcontrolapp.ui.sensor.details

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sensorcontrolapp.ui.sensor.SensorStatesViewModel
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import com.example.sensorcontrolapp.data.log.CommandLog
import com.example.sensorcontrolapp.ui.session.UserSessionViewModel
import com.example.sensorcontrolapp.model.UserConfig
import kotlinx.coroutines.delay
import androidx.hilt.navigation.compose.hiltViewModel // ← Hilt kullanılacaksa bu gerekiyor
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow


@Composable
fun SensorDetailScreen(
    sensorKey: String,
    onBack: () -> Unit,
    onSendCommand: (String) -> Unit,
    receivedText: StateFlow<String>,
    sensorStatesViewModel: SensorStatesViewModel = viewModel(),
    sessionViewModel: UserSessionViewModel = hiltViewModel() // Hilt ile enjekte

) {
    val user by sessionViewModel.currentUser.collectAsState()
    val config: UserConfig? by sessionViewModel.userConfig.collectAsState()

    val currentUserState = rememberUpdatedState(user)
    val currentConfigState = rememberUpdatedState(config)
    val safeFlow = receivedText ?: MutableStateFlow("")
    val currentOutput = safeFlow.collectAsState().value ?: ""
    val outputLines = if (currentOutput.isNotEmpty()) {
        currentOutput.split("\n")
    } else {
        emptyList()
    }

    val isEnabled by sensorStatesViewModel.getSensorState(sensorKey).collectAsState(initial = false)
    val isSensorAllowed = config?.enabledSensors?.contains(sensorKey) == true
    val displayName = getSensorDisplayName(sensorKey)

    var hasLoggedResponse by remember { mutableStateOf(false) }

    // İlk veri geldiğinde bir kere logla
    LaunchedEffect(currentOutput) {
        val responseLine = outputLines.firstOrNull { it.startsWith(sensorKey, ignoreCase = true) }
        val isSensorActive = sensorStatesViewModel.getSensorState(sensorKey).value

        if (!hasLoggedResponse && isSensorActive && currentUserState.value != null && !responseLine.isNullOrBlank()) {
            CommandLog.log(
                username = currentUserState.value!!.username,
                command = "${sensorKey}_LOG",
                screen = "SensorDetailScreen",
                sensor = sensorKey,
                status = "ONESHOT",
                response = responseLine
            )
            hasLoggedResponse = true
        }
    }

    // Her 10 saniyede bir logla (logout olana kadar)
    LaunchedEffect(sensorKey) {
        while (true) {
            delay(1000) ///// veri yazma sikligi

            val user = sessionViewModel.currentUser.value
            val isSensorActive = sensorStatesViewModel.getSensorState(sensorKey).value
            val responseLine = currentOutput
                .split("\n")
                .firstOrNull { it.startsWith(sensorKey, ignoreCase = true) }

            if (user != null && isSensorActive && responseLine != null) {
                CommandLog.log(
                    username = user.username,
                    command = "${sensorKey}_AUTO_LOG",
                    screen = "SensorDetailScreen",
                    sensor = sensorKey,
                    status = "PERIODIC",
                    response = responseLine
                )
            }
        }
    }



    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(displayName, style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Sensörü Aktif Et", modifier = Modifier.weight(1f))
            Switch(
                checked = isEnabled,
                onCheckedChange = { checked ->
                    if (isSensorAllowed) {
                        sensorStatesViewModel.setSensorState(sensorKey, checked)
                        if (checked) {
                            onSendCommand(sensorKey)
                            onSendCommand("${sensorKey}_ON")
                        } else {
                            onSendCommand("${sensorKey}_OFF")
                        }
                    }
                },
                enabled = isSensorAllowed
            )
        }

        if (!isSensorAllowed){
            Text("Bu sensor yapılandırma ayarlarında pasif.",color = Color.Red)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("STM32'den Gelen Veri:", style = MaterialTheme.typography.titleMedium)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            currentOutput
                .split("\n")
                .filter { it.startsWith(sensorKey, ignoreCase = true) }
                .forEach { line -> Text(text = line) }
        }

        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = onBack,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Geri Dön")
        }
    }
}
fun getSensorDisplayName(sensorKey: String): String {
    return when (sensorKey.uppercase()) {
        "LOAD" -> "Yük Hücresi"
        "TEMP" -> "Sıcaklık Sensörü"
        "LOCATION" -> "ADXL345 Lokasyon Sensörü"
        "ROT" -> "Rotary Encoder"
        "LIN" -> "Lineer Potansiyometre"
        "AS5600" -> "Manyetik Açı Sensörü"
        "VOLT" -> "Gerilim Sensörü"
        "VIBRATION" -> "Titreşim Modülü"
        "ADS" -> "ADS1115 ADC"
        "ACT" -> "Linear Aktüatör"
        else -> sensorKey
    }
}
