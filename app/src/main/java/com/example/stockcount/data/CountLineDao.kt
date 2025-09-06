package com.example.stockcount.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CountLineDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(line: CountLine)

    @Query("UPDATE count_lines SET quantity = quantity + :delta, updatedAt = :updatedAt WHERE ean = :ean")
    suspend fun incrementCount(ean: String, delta: Int, updatedAt: Long)

    @Query("SELECT * FROM count_lines WHERE ean = :ean LIMIT 1")
    suspend fun findByEan(ean: String): CountLine?

    @Query("SELECT * FROM count_lines ORDER BY updatedAt DESC")
    fun getAll(): Flow<List<CountLine>>
}
