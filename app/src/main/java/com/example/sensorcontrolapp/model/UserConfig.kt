package com.example.sensorcontrolapp.model

import androidx.compose.foundation.text.input.TextFieldLineLimits


data class UserConfig(
    val enabledSensors: List<String> = listOf(),
    val defaultSensorRefreshMs: Int = 1000,
    val currentLimits: Map<String, CurrentRange> = emptyMap()
)

data class CurrentRange(
    val min: Int = 0,
    val max: Int = 1000
)