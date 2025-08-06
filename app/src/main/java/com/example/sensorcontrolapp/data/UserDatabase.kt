package com.example.sensorcontrolapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.sensorcontrolapp.data.log.CommandLog
import com.example.sensorcontrolapp.data.log.CommandLogDao
import com.example.sensorcontrolapp.model.User
import com.example.sensorcontrolapp.data.UserDao

@Database(entities = [User::class, CommandLog::class], version = 2)
abstract class UserDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun commandLogDao(): CommandLogDao

    companion object {
        @Volatile
        private var INSTANCE: UserDatabase? = null

        fun getInstance(context: Context): UserDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    UserDatabase::class.java,
                    "sensor_user_database"
                )
                    .fallbackToDestructiveMigration() // Eski veritabanı schema uymazsa sıfırlar
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
