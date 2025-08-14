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
import kotlinx.coroutines.launch

@Composable
fun LogScreen(
    username: String,
    getUserLogs: suspend (String) -> List<CommandLog>,
    onClearUserLogs: suspend (String) -> Unit,     // ⬅️ EKLENDİ
    onBack: () -> Unit
) {
    var logs by remember { mutableStateOf<List<CommandLog>>(emptyList()) }
    var showConfirm by remember { mutableStateOf(false) } // confirm dialog
    var isDeleting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Kullanıcı adı değiştiğinde logları periyodik çek
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

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Button(onClick = onBack) { Text("Back") }

            // ⬇️ Kullanıcı loglarını sil butonu
            OutlinedButton(
                enabled = !isDeleting,
                onClick = { showConfirm = true }
            ) {
                Text(if (isDeleting) "Clearing..." else "Clear My Logs")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

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

        // ⬇️ Onay penceresi
        if (showConfirm) {
            AlertDialog(
                onDismissRequest = { showConfirm = false },
                confirmButton = {
                    TextButton(onClick = {
                        showConfirm = false
                        isDeleting = true
                        scope.launch {
                            try {
                                onClearUserLogs(username)      // veritabanından sil
                                logs = emptyList()             // UI'ı hemen boşalt
                            } finally {
                                isDeleting = false
                            }
                        }
                    }) { Text("Delete") }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirm = false }) { Text("Cancel") }
                },
                title = { Text("Clear all logs?") },
                text = { Text("This will permanently delete all your command logs.") }
            )
        }
    }
}

// Basit zaman formatlayıcı
fun formatTimestamp(timestamp: Long): String {
    return java.text.SimpleDateFormat("HH:mm:ss dd.MM.yyyy")
        .format(java.util.Date(timestamp))
}
