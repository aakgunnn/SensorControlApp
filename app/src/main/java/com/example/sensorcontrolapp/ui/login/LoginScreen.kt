package com.example.sensorcontrolapp.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,   //viewmodel referansı
    onLoginSuccess: () -> Unit //    !!!!!ileri asamada kullanilacak
) {
    // !State'ler //  kullacinin giris bilgilerini tutar
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val loginState by viewModel.loginState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Login", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        //Compose'da metin kutusu
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))


        // Login ve Register butonları
        Row {
            Button(onClick = {
                viewModel.login(username, password) // Giriş işlemi
            }) {
                Text("Login")
            }

            Spacer(modifier = Modifier.width(12.dp))

            OutlinedButton(onClick = {
                viewModel.register(username, password) // Kayıt işlemi
            }) {
                Text("Register")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        // Hata ya da basariyi göster
        when (val state = loginState) {
            is LoginViewModel.LoginResult.Failure -> {
                Text(state.message, color = MaterialTheme.colorScheme.error)
            }
            is LoginViewModel.LoginResult.Success -> {
                onLoginSuccess() // Giriş başarılı!
                viewModel.resetLoginState()
            }
            LoginViewModel.LoginResult.Idle -> {}
        }
    }
}