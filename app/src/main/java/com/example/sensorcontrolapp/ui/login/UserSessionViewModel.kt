package com.example.sensorcontrolapp.ui.session

import androidx.lifecycle.ViewModel
import com.example.sensorcontrolapp.model.User
import com.example.sensorcontrolapp.model.UserConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class UserSessionViewModel : ViewModel() {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val _userConfig = MutableStateFlow<UserConfig?>(null)
    val userConfig: StateFlow<UserConfig?> = _userConfig

    private val _currentConfig = MutableStateFlow<UserConfig?>(null)
    val currentConfig: StateFlow<UserConfig?> = _currentConfig


    fun login(user: User, config: UserConfig) {
        _currentUser.value = user
        _userConfig.value = config
    }

    fun logout() {
        _currentUser.value = null
        _userConfig.value = null
    }

    fun updateConfig(newConfig: UserConfig) {
        _userConfig.value = newConfig
    }
}
