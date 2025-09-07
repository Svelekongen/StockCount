package com.example.stockcount.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant

@Entity(tableName = "count_lines")
data class CountLine(
    @PrimaryKey val ean: String,
    val name: String? = null,
    val quantity: Int = 0,
    val location: String? = null,
    val note: String? = null,
    val updatedAt: Instant
)


