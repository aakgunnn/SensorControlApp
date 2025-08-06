package com.example.sensorcontrolapp.data.log

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Entity(tableName = "command_logs")
data class CommandLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val command: String,
    val timestamp: Long = System.currentTimeMillis(),
    val screen: String? = null,
    val sensorName: String? = null,
    val status: String = "SUCCESS",
    val response: String? = null  //stm'den gelen veri buraya yazılacak -log ekranında veri gosterimi icin-
) {
    companion object {
        lateinit var dao: CommandLogDao

        fun init(logDao: CommandLogDao) {
            dao = logDao
        }

        fun log(
            username: String,
            command: String,
            screen: String,
            sensor: String? = null,
            status: String = "SUCCESS",
            response: String? = null
        ) {
            val log = CommandLog(
                username = username,
                command = command,
                screen = screen,
                sensorName = sensor,
                status = status,
                response = response
            )
            CoroutineScope(Dispatchers.IO).launch {
                dao.insertLog(log)
            }
        }
    }
}
