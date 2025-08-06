package com.example.sensorcontrolapp.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sensorcontrolapp.model.UserConfig

@Composable
fun HomeScreen(
    username: String,
    onLogout: () -> Unit,
    onSendCommand: (String) -> Unit,
    onViewLogs: () -> Unit,
    onSettings: () -> Unit,
    onOpenSensorControl: () -> Unit

) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Welcome, $username!", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(24.dp))







        Button(onClick = onOpenSensorControl) {
            Text("Sensor Control Screen")
        }

        Button(onClick = onSettings){
            Text("Settings")
        }

        Button(onClick = onViewLogs) {
            Text("View Logs")
        }


        Button(onClick = onLogout) {
            Text("Logout")
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}