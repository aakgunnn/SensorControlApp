package com.example.sensorcontrolapp.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sensorcontrolapp.data.UserDao
import com.example.sensorcontrolapp.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(private val userDao: UserDao) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginResult>(LoginResult.Idle)
    val loginState: StateFlow<LoginResult> = _loginState

    fun login(username: String, password: String) {
        viewModelScope.launch {
            val user = userDao.getUser(username, password)
            if (user != null) {
                _loginState.value = LoginResult.Success(user)
            } else {
                _loginState.value = LoginResult.Failure("Invalid credentials")
            }

        }
    }

    fun register(username: String, password: String) {
        viewModelScope.launch {
            val existing = userDao.getUserByUsername(username)
            if (existing != null) {
                _loginState.value = LoginResult.Failure("User already exists")
                return@launch
            }

            val newUser = User(username = username, password = password)
            userDao.insertUser(newUser)
            _loginState.value = LoginResult.Success(newUser)
        }
    }

    fun resetState() {
        _loginState.value = LoginResult.Idle
    }

    fun resetLoginState(){
        _loginState.value = LoginResult.Idle
    }

    sealed class LoginResult {
        data object Idle : LoginResult()
        data class Success(val user: User) : LoginResult()
        data class Failure(val message: String) : LoginResult()
    }
}