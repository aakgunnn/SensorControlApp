package com.example.sensorcontrolapp.ui.log

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sensorcontrolapp.data.log.CommandLog
import androidx.compose.material3.HorizontalDivider
import kotlinx.coroutines.delay




@Composable
fun LogScreen(
    username: String,
    getUserLogs: suspend (String) -> List<CommandLog>,
    onBack: () -> Unit
) {
    var logs by remember { mutableStateOf<List<CommandLog>>(emptyList()) }

    // Kullanıcı adı değiştiğinde logları yeniden çek
    LaunchedEffect(username) {
        while (true) {
            logs = getUserLogs(username)
            delay(5_000) // her 5 saniyede bir güncelle
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text("Command Logs for $username", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onBack, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("Back")
        }

        LazyColumn {
            items(logs) { log ->
                Text(text = "${formatTimestamp(log.timestamp)} — ${log.command}")
                log.screen?.let { Text(text = "From: $it") }
                log.sensorName?.let { Text(text = "Sensor: $it") }
                Text(text = "Status: ${log.status}")
                log.response?.let { Text(text = "Response: $it") }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            }
        }

        Spacer(modifier = Modifier.height(16.dp))


    }
}


// Basit zaman formatlayıcı
fun formatTimestamp(timestamp: Long): String {
    return java.text.SimpleDateFormat("HH:mm:ss dd.MM.yyyy").format(java.util.Date(timestamp))
}