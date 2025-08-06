package com.example.sensorcontrolapp.model

data class SensorItem(
    val key: String,          // e.g., "TEMP"
    val displayName: String,  // e.g., "Sıcaklık Sensörü"
    val supportsDetail: Boolean = false  // özel ekran var mı
)