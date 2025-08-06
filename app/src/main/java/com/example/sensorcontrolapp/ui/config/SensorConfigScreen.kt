package com.example.sensorcontrolapp.ui.config

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sensorcontrolapp.model.UserConfig
import com.example.sensorcontrolapp.model.CurrentRange
import com.example.sensorcontrolapp.ui.sensor.SensorStatesViewModel
import androidx.compose.runtime.collectAsState

@Composable
fun SensorConfigScreen(
    sensorKey: String,
    sensorStatesViewModel: SensorStatesViewModel,
    userConfig: UserConfig,
    onSave: (UserConfig) -> Unit,
    onSendCommand: (String) -> Unit,
    onBack: () -> Unit
) {
    // Limits
    val mutableLimits = remember {
        mutableStateMapOf<String, CurrentRange>().apply { putAll(userConfig.currentLimits) }
    }
    if (!mutableLimits.containsKey(sensorKey)) {
        mutableLimits[sensorKey] = CurrentRange(0, 1000)
    }

    // --- Sensör Değerleri ---
    val minValueInput = remember { mutableStateOf(mutableLimits[sensorKey]?.min?.toString() ?: "0") }
    val maxValueInput = remember { mutableStateOf(mutableLimits[sensorKey]?.max?.toString() ?: "1000") }
    val currentValueReading = sensorStatesViewModel.currentReadings[sensorKey]?.collectAsState(initial = 0f)

    // --- Akım Limitleri ---
    val minCurrentInput = remember { mutableStateOf("0") }
    val maxCurrentInput = remember { mutableStateOf("1000") }
    val currentCurrentReading = sensorStatesViewModel.currentReadings[sensorKey]?.collectAsState(initial = 0f)

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {

        // ---------------- Sensör Değerleri ----------------
        Text("$sensorKey Değer Limitleri", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = minValueInput.value,
            onValueChange = {
                minValueInput.value = it
                mutableLimits[sensorKey] = mutableLimits[sensorKey]?.copy(min = it.toIntOrNull() ?: 0)
                    ?: CurrentRange(min = it.toIntOrNull() ?: 0)
            },
            label = { Text("Min Değer") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = maxValueInput.value,
            onValueChange = {
                maxValueInput.value = it
                mutableLimits[sensorKey] = mutableLimits[sensorKey]?.copy(max = it.toIntOrNull() ?: 1000)
                    ?: CurrentRange(max = it.toIntOrNull() ?: 1000)
            },
            label = { Text("Max Değer") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = (currentValueReading?.value ?: 0f).toString(),
            onValueChange = {},
            label = { Text("Anlık Değer") },
            readOnly = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))


        // ---------------- Akım Limitleri ----------------
        Text("$sensorKey Akım Limitleri", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = minCurrentInput.value,
            onValueChange = { minCurrentInput.value = it },
            label = { Text("Min Akım (mA)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = maxCurrentInput.value,
            onValueChange = { maxCurrentInput.value = it },
            label = { Text("Max Akım (mA)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = (currentCurrentReading?.value ?: 0f).toString(),
            onValueChange = {},
            label = { Text("Anlık Akım (mA)") },
            readOnly = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ---------------- Kaydet & Geri ----------------
        Row {
            Button(onClick = {
                val updatedConfig = userConfig.copy(currentLimits = mutableLimits.toMap())

                // STM32'ye limit komutları gönder
                val minVal = minValueInput.value.toIntOrNull() ?: 0
                val maxVal = maxValueInput.value.toIntOrNull() ?: 1000
                val minCurr = minCurrentInput.value.toIntOrNull() ?: 0
                val maxCurr = maxCurrentInput.value.toIntOrNull() ?: 1000

                // Sensör değer limitleri
                onSendCommand("${sensorKey}_MIN_VAL=$minVal")
                onSendCommand("${sensorKey}_MAX_VAL=$maxVal")

                // Akım limitleri
                onSendCommand("${sensorKey}_MIN_CURR=$minCurr")
                onSendCommand("${sensorKey}_MAX_CURR=$maxCurr")

                onSave(updatedConfig)
            }) {
                Text("Kaydet")
            }
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedButton(onClick = onBack) {
                Text("Geri")
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        // ---------------- Servo Motor Ekstra Kontroller ----------------
        if (sensorKey == "SERVO") {
            Spacer(modifier = Modifier.height(12.dp))
            Text("Servo Parametreleri", style = MaterialTheme.typography.titleMedium)

            // İvme
            val servoAcc = remember { mutableStateOf("150") }
            OutlinedTextField(
                value = servoAcc.value,
                onValueChange = { servoAcc.value = it },
                label = { Text("İvme (us/s²)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Maksimum hız
            val servoSpeed = remember { mutableStateOf("800") }
            OutlinedTextField(
                value = servoSpeed.value,
                onValueChange = { servoSpeed.value = it },
                label = { Text("Max Hız (us/s)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            ServoControlPanel(
                onSendCommand = onSendCommand,
                sensorStatesViewModel = sensorStatesViewModel
            )

            // Kaydet butonunda gönder
            Button(onClick = {
                onSendCommand("SERVO_ACC=${servoAcc.value}")
                onSendCommand("SERVO_SPEED=${servoSpeed.value}")
            }) {
                Text("Servo Parametrelerini Kaydet")
            }
        }
        // Linear Actuator ek kontroller
        if (sensorKey == "ACT") {
            Text("Actuator Parametreleri", style = MaterialTheme.typography.titleMedium)
            val actAcc = remember { mutableStateOf("200") }
            val actSpeed = remember { mutableStateOf("800") }

            OutlinedTextField(
                value = actAcc.value,
                onValueChange = { actAcc.value = it },
                label = { Text("İvme (us/s²)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = actSpeed.value,
                onValueChange = { actSpeed.value = it },
                label = { Text("Max Hız (us/s)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            LinearActuatorControlPanel(onSendCommand, sensorStatesViewModel)

            Button(onClick = {
                onSendCommand("ACT_ACC=${actAcc.value}")
                onSendCommand("ACT_SPEED=${actSpeed.value}")
            }) {
                Text("Actuator Parametrelerini Kaydet")
            }
        }
    }
}
