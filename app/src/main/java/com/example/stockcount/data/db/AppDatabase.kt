package com.example.stockcount.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.stockcount.data.entity.CountLine

@Database(entities = [CountLine::class], version = 1, exportSchema = false)
@TypeConverters(InstantConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun countLineDao(): CountLineDao
}


