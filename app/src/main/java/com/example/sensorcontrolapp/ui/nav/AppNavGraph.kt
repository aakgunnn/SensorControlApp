package com.example.sensorcontrolapp.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.sensorcontrolapp.data.ConfigManager
import com.example.sensorcontrolapp.data.UsbSerialManager
import com.example.sensorcontrolapp.data.log.CommandLog
import com.example.sensorcontrolapp.data.log.CommandLogDao
import com.example.sensorcontrolapp.model.UserConfig
import com.example.sensorcontrolapp.ui.config.ConfigScreen
import com.example.sensorcontrolapp.ui.config.SensorConfigScreen
import com.example.sensorcontrolapp.ui.home.HomeScreen
import com.example.sensorcontrolapp.ui.log.LogScreen
import com.example.sensorcontrolapp.ui.login.LoginScreen
import com.example.sensorcontrolapp.ui.login.LoginViewModel
import com.example.sensorcontrolapp.ui.sensor.SensorControlScreen
import com.example.sensorcontrolapp.ui.sensor.SensorStatesViewModel
import com.example.sensorcontrolapp.ui.sensor.details.SensorDetailScreen
import com.example.sensorcontrolapp.ui.session.UserSessionViewModel
import androidx.compose.runtime.LaunchedEffect



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

        composable(NavRoutes.SENSOR_CONTROL) {
            val user = sessionViewModel.currentUser.collectAsState().value
            val config = sessionViewModel.userConfig.collectAsState().value
            val receivedText = usbSerialManager.receivedData

            if (user != null && config != null) {
                LaunchedEffect(Unit) {
                    sensorStatesViewModel.setLoggerEnvironment(
                        username = user.username,
                        getOutput = { receivedText.value },
                        refreshIntervalMs = config.defaultSensorRefreshMs,
                        enabledSensors = config.enabledSensors
                    )
                }
                SensorControlScreen(
                    onSendCommand = { command ->
                        usbSerialManager.send(command)
                        CommandLog.log(
                            username = user.username,
                            command = command,
                            screen = "SensorControlScreen"
                        )
                    },
                    onBack = { navController.popBackStack() },
                    receivedText = receivedText,
                    onNavigateToSensorDetail = { sensorKey ->
                        navController.navigate("${NavRoutes.SENSOR_DETAIL}/$sensorKey")
                    },
                    sensorStatesViewModel = sensorStatesViewModel
                )
            }


        }

        composable(
            route = "${NavRoutes.SENSOR_DETAIL}/{sensorKey}",
            arguments = listOf(navArgument("sensorKey") { defaultValue = "" })
        ) { backStackEntry ->
            val sensorKey = backStackEntry.arguments?.getString("sensorKey") ?: ""
            val receivedText = usbSerialManager.receivedData

            SensorDetailScreen(
                sensorKey = sensorKey,
                onBack = { navController.popBackStack() },
                onSendCommand = { usbSerialManager.send(it) },
                receivedText = receivedText,
                sensorStatesViewModel = sensorStatesViewModel,
                sessionViewModel = sessionViewModel
            )
        }






    }
}
