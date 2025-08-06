package com.example.sensorcontrolapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.example.sensorcontrolapp.data.ConfigManager
import com.example.sensorcontrolapp.data.UsbSerialManager
import com.example.sensorcontrolapp.data.UserDatabase
import com.example.sensorcontrolapp.ui.login.LoginViewModel
import com.example.sensorcontrolapp.ui.nav.AppNavGraph
import com.example.sensorcontrolapp.ui.theme.SensorControlAppTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sensorcontrolapp.data.log.CommandLog
import com.example.sensorcontrolapp.ui.sensor.SensorStatesViewModel
import com.example.sensorcontrolapp.ui.session.UserSessionViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue






class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userDao = UserDatabase.getInstance(applicationContext).userDao()
        val loginViewModel = LoginViewModel(userDao)
        val usbSerialManager = UsbSerialManager(applicationContext)
        val commandLogDao = UserDatabase.getInstance(applicationContext).commandLogDao()
        val configManager = ConfigManager(applicationContext)

        CommandLog.init(commandLogDao)
        usbSerialManager.findAndConnect()

        setContent {

            SensorControlAppTheme {
                val navController = rememberNavController()
                val sessionViewModel = viewModel<UserSessionViewModel>()
                val sensorStatesViewModel = viewModel<SensorStatesViewModel>()

                // Logger ortamı ayarla (state değişikliklerini dinlemeden)
                val username by sessionViewModel.currentUser.collectAsState()
                val config by sessionViewModel.userConfig.collectAsState()

                sensorStatesViewModel.setLoggerEnvironment(
                    username = username?.username.orEmpty(),
                    getOutput = { usbSerialManager.receivedData.value },
                    refreshIntervalMs = sessionViewModel.userConfig.value?.defaultSensorRefreshMs ?: 1000,
                    enabledSensors = sessionViewModel.userConfig.value?.enabledSensors ?: emptyList()
                )
                AppNavGraph(
                    navController = navController,
                    loginViewModel = loginViewModel,
                    usbSerialManager = usbSerialManager,
                    commandLogDao = commandLogDao,
                    configManager = configManager,
                    sessionViewModel = sessionViewModel,
                    sensorStatesViewModel = sensorStatesViewModel,
                    onLogout = {
                        sessionViewModel.logout()
                        sensorStatesViewModel.clearAll()
                    },
                    onSendCommand = {command ->
                        usbSerialManager.send(command)
                        CommandLog.log(
                            username = username?.username.orEmpty(),
                            command = command,
                            screen = "MainActivity"
                        )
                    }
                )
            }
        }
    }
}
