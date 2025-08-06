package com.example.sensorcontrolapp.data

import android.content.Context
import com.example.sensorcontrolapp.model.UserConfig
import com.google.gson.Gson
import java.io.File

class ConfigManager(private val context: Context) {

    private val gson = Gson()

    fun saveConfig(username: String, config: UserConfig) {
        val json = gson.toJson(config)
        getUserFile(username).writeText(json)
    }

    fun loadConfig(username: String): UserConfig {
        val file = getUserFile(username)
        return if (file.exists()) {
            val json = file.readText()
            gson.fromJson(json, UserConfig::class.java)
        } else {
            UserConfig() // varsayılan config
        }
    }

    private fun getUserFile(username: String): File {
        val dir = File(context.filesDir, "configs")
        if (!dir.exists()) dir.mkdirs()
        return File(dir, "$username.json")
    }
}
