package com.example.sensorcontrolapp.ui.sensor

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sensorcontrolapp.data.log.CommandLog
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.runtime.State

class SensorStatesViewModel : ViewModel() {

    private val _sensorStates = mutableMapOf<String, MutableStateFlow<Boolean>>()
    private val loggingJobs = mutableMapOf<String, Job>()

    private val _servoAngle = mutableStateOf(0f)
    val servoAngle: State<Float> = _servoAngle

    private val _servoTargetAngle = mutableStateOf(0f)
    val servoTargetAngle: State<Float> = _servoTargetAngle

    val actuatorStroke = MutableStateFlow(0f)           // Anlık stroke yüzdesi
    val actuatorTargetStroke = MutableStateFlow(50f)    // Hedef stroke yüzdesi

    private var currentUser: String? = null
    private var getOutputFunc: (() -> String)? = null
    private var refreshInterval: Int = 1000

    // 🔹 Her sensörün anlık akım değerini tutar
    private val _currentReadings = mutableMapOf<String, MutableStateFlow<Float>>()
    val currentReadings: Map<String, StateFlow<Float>> get() = _currentReadings

    fun getSensorState(sensorKey: String): StateFlow<Boolean> {
        return _sensorStates.getOrPut(sensorKey) { MutableStateFlow(false) }
    }

    fun setSensorState(sensorKey: String, isEnabled: Boolean) {
        _sensorStates.getOrPut(sensorKey) { MutableStateFlow(false) }.value = isEnabled
        if (isEnabled) startSensorLogging(sensorKey) else stopSensorLogging(sensorKey)
    }

    fun setLoggerEnvironment(
        username: String,
        getOutput: () -> String,
        refreshIntervalMs: Int,
        enabledSensors: List<String>
    ) {
        currentUser = username
        getOutputFunc = getOutput
        refreshInterval = refreshIntervalMs

        _sensorStates.values.forEach { it.value = false }

        enabledSensors.forEach { sensorKey ->
            _sensorStates.getOrPut(sensorKey) { MutableStateFlow(false) }.value = true
            startSensorLogging(sensorKey)
        }
    }

    private fun startSensorLogging(sensorKey: String) {
        if (loggingJobs.containsKey(sensorKey)) return
        val username = currentUser ?: return
        val getOutput = getOutputFunc ?: return

        val job = viewModelScope.launch {
            while (getSensorState(sensorKey).value) {
                val output = getOutput()
                val line = output
                    .split("\n")
                    .firstOrNull { it.startsWith(sensorKey, ignoreCase = true) }

                // 🔹 "CUR=xxx" formatından akım bilgisini çek
                line?.let {
                    val currentValue = Regex("CUR=(\\d+(?:\\.\\d+)?)")
                        .find(it)
                        ?.groupValues?.getOrNull(1)
                        ?.toFloatOrNull()
                    currentValue?.let { value ->
                        _currentReadings.getOrPut(sensorKey) { MutableStateFlow(0f) }.value = value
                    }
                }

                CommandLog.log(
                    username = username,
                    command = "${sensorKey}_LOG_AUTO",
                    screen = "BackgroundLogger",
                    sensor = sensorKey,
                    status = "PERIODIC",
                    response = line ?: "NO_DATA"
                )

                delay(refreshInterval.toLong())
            }
        }
        loggingJobs[sensorKey] = job
    }

    private fun stopSensorLogging(sensorKey: String) {
        loggingJobs[sensorKey]?.cancel()
        loggingJobs.remove(sensorKey)
    }

    fun clearAll() {
        loggingJobs.values.forEach { it.cancel() }
        loggingJobs.clear()
        _sensorStates.values.forEach { it.value = false }
        currentUser = null
        getOutputFunc = null
    }

    fun updateServoAngle(newAngle: Float) {
        _servoAngle.value = newAngle
    }

    fun updateServoTargetAngle(newTarget: Float) {
        _servoTargetAngle.value = newTarget
    }

    // 🔹 Manuel akım güncelleme (istenirse)
    fun updateCurrentValue(sensorKey: String, currentValue: Float) {
        _currentReadings.getOrPut(sensorKey) { MutableStateFlow(0f) }.value = currentValue
    }

    fun setActuatorStroke(value: Float) {
        actuatorStroke.value = value
    }

    fun setActuatorTargetStroke(value: Float) {
        actuatorTargetStroke.value = value
    }

    // 🔹 AppNavGraph ve SensorDetailScreen için güvenli erişim
    fun getOrCreateReading(sensorKey: String): MutableStateFlow<Float> {
        return _currentReadings.getOrPut(sensorKey) { MutableStateFlow(0f) }
    }


}

