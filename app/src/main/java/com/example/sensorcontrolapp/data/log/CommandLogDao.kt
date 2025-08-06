package com.example.sensorcontrolapp.data.log

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface CommandLogDao {
    @Insert
    suspend fun insertLog(log: CommandLog)

    @Query("SELECT * FROM command_logs WHERE username = :username ORDER BY timestamp DESC")
    suspend fun getLogsForUser(username: String): List<CommandLog>
}