package com.example.sensorcontrolapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.sensorcontrolapp.model.User

@Dao
interface UserDao {

    @Insert
    suspend fun insertUser(user: User)

    // Yeni eklenen, login için:
    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    suspend fun getUser(username: String, password: String): User?

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<User>
}