package com.example.stockcount.data.db

import androidx.room.TypeConverter
import kotlinx.datetime.Instant

class InstantConverters {
    @TypeConverter
    fun fromEpoch(epoch: Long?): Instant? = epoch?.let { Instant.fromEpochMilliseconds(it) }

    @TypeConverter
    fun toEpoch(instant: Instant?): Long? = instant?.toEpochMilliseconds()
}


