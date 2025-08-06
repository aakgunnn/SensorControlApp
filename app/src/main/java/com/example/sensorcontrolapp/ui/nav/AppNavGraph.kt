package com.example.sensorcontrolapp.ui.nav

import androidx.compose.runtime.*
import androidx.navigation.*
import androidx.navigation.compose.*
import com.example.sensorcontrolapp.data.*
import com.example.sensorcontrolapp.data.log.CommandLog
import com.example.sensorcontrolapp.ui.config.ConfigScreen
import com.example.sensorcontrolapp.ui.config.SensorConfigScreen
import com.example.sensorcontrolapp.ui.home.HomeScreen
import com.example.sensorcontrolapp.ui.login.LoginScreen
import com.example.sensorcontrolapp.ui.login.LoginViewModel
import com.example.sensorcontrolapp.ui.log.LogScreen
import com.example.sensorcontrolapp.ui.sensor.details.SensorDetailScreen
import com.example.sensorcontrolapp.ui.sensor.SensorStatesViewModel
import com.example.sensorcontrolapp.data.log.CommandLogDao
import com.example.sensorcontrolapp.ui.session.UserSessionViewModel
import com.example.sensorcontrolapp.model.UserConfig
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

@Composable
fun AppNavGraph(
    navController: NavHostController,
    loginViewModel: LoginViewModel,
    usbSerialManager: UsbSerialManager,
    commandLogDao: CommandLogDao,
    configManager: ConfigManager,
    sessionViewModel: UserSessionViewModel,
    sensorStatesViewModel: SensorStatesViewModel,

    onLogout: () -> Unit,
    onSendCommand: (String) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.LOGIN
    ) {
        composable(NavRoutes.LOGIN) {
            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = {
                    val result = loginViewModel.loginState.value
                    if (result is LoginViewModel.LoginResult.Success) {
                        val user = result.user
                        val config = configManager.loadConfig(user.username)
                        sessionViewModel.login(user, config)

                        navController.navigate(NavRoutes.HOME) {
                            popUpTo(NavRoutes.LOGIN) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }

        composable(NavRoutes.HOME) {
            val user = sessionViewModel.currentUser.collectAsState().value
            val config = sessionViewModel.userConfig.collectAsState().value

            if (user != null && config != null) {
                HomeScreen(
                    username = user.username,
                    onLogout = {
                        configManager.saveConfig(user.username, config)
                        sessionViewModel.logout()
                        navController.navigate(NavRoutes.LOGIN) {
                            popUpTo(0)
                        }
                    },
                    onSendCommand = { command ->
                        usbSerialManager.send(command)
                        CommandLog.log(
                            username = user.username,
                            command = command,
                            screen = "HomeScreen"
                        )
                    },
                    onViewLogs = { navController.navigate(NavRoutes.LOGS) },
                    onSettings = { navController.navigate(NavRoutes.CONFIG) },
                    onOpenSensorControl = { navController.navigate(NavRoutes.SENSOR_CONTROL) }
                )
            }
        }

        composable(NavRoutes.CONFIG) {
            val user = sessionViewModel.currentUser.collectAsState().value
            val config = sessionViewModel.userConfig.collectAsState().value

            if (user != null && config != null) {
                ConfigScreen(
                    initialConfig = config,
                    onSave = { newConfig ->
                        sessionViewModel.updateConfig(newConfig)
                        configManager.saveConfig(user.username, newConfig)
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() },
                    onNavigateToSensorConfig = { sensorKey ->
                        println("Navigating to SensorConfigScreen with key: $sensorKey")
                        navController.navigate("${NavRoutes.SENSOR_CONFIG}?sensor=$sensorKey")
                    },
                    sensorStatesViewModel = sensorStatesViewModel,
                    onSendCommand = { command ->
                        usbSerialManager.send(command)
                        CommandLog.log(
                            username = user.username,
                            command = command,
                            screen = "ConfigScreen"
                        )
                    }
                )
            }
        }

        composable(
            route = "${NavRoutes.SENSOR_CONFIG}?sensor={sensor}",
            arguments = listOf(navArgument("sensor") { defaultValue = "" })
        ) { backStackEntry ->
            val sensorKey = backStackEntry.arguments?.getString("sensor") ?: ""
            println("Opened SensorConfigScreen for: $sensorKey")

            SensorConfigScreen(
                sensorKey = sensorKey,
                sensorStatesViewModel = sensorStatesViewModel,
                userConfig = sessionViewModel.userConfig.value ?: UserConfig(),
                onSave = { updatedConfig ->
                    val user = sessionViewModel.currentUser.value
                    if (user != null) {
                        sessionViewModel.updateConfig(updatedConfig)
                        configManager.saveConfig(user.username, updatedConfig)
                    }
                    navController.popBackStack()
                },
                onSendCommand = { usbSerialManager.send(it) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.LOGS) {
            val user = sessionViewModel.currentUser.collectAsState().value
            if (user != null) {
                LogScreen(
                    username = user.username,
                    getUserLogs = { username ->
                        try {
                            commandLogDao.getLogsForUser(username) ?: emptyList()
                        } catch (e: Exception) {
                            emptyList()
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable(
            route = NavRoutes.SENSOR_CONTROL
        ) {
            // Burada sensorKey geçmiyor, doğrudan SensorDetailScreen açılıyor.
            // Eğer sensorKey parametreli olacaksa SENSOR_DETAILS mantığına göre düzenlenebilir.
            val defaultSensorKey = "DEFAULT" // Eğer belirli bir sensör kontrolü açılacaksa
            val readingFlow = sensorStatesViewModel.getOrCreateReading(defaultSensorKey)

            val scope = rememberCoroutineScope()
            val receivedFlow: StateFlow<String> = remember {
                readingFlow.map { it.toString() }
                    .stateIn(scope, SharingStarted.Eagerly, "0")
            }

            SensorDetailScreen(
                sensorKey = defaultSensorKey,
                sensorStatesViewModel = sensorStatesViewModel,
                onSendCommand = { usbSerialManager.send(it) },
                receivedText = receivedFlow,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
