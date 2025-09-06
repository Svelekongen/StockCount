package com.example.stockcount.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "count_lines")
data class CountLine(
    @PrimaryKey val ean: String,
    val name: String,
    val quantity: Int,
    val note: String? = null,
    val location: String? = null,
    val updatedAt: Long
)
