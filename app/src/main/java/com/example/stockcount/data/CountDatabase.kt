package com.example.stockcount.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [CountLine::class], version = 1, exportSchema = false)
abstract class CountDatabase : RoomDatabase() {
    abstract fun countLineDao(): CountLineDao

    companion object {
        @Volatile
        private var INSTANCE: CountDatabase? = null

        fun getInstance(context: Context): CountDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    CountDatabase::class.java,
                    "count_database"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
