package com.example.stockcount.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.stockcount.data.entity.CountLine
import kotlinx.coroutines.flow.Flow

@Dao
interface CountLineDao {
    @Query("SELECT * FROM count_lines ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<CountLine>>

    @Query("SELECT * FROM count_lines WHERE ean = :ean LIMIT 1")
    suspend fun getByEan(ean: String): CountLine?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(line: CountLine)

    @Update
    suspend fun update(line: CountLine)

    @Delete
    suspend fun delete(line: CountLine)

    @Query("DELETE FROM count_lines WHERE ean = :ean")
    suspend fun deleteByEan(ean: String)

    @Query("SELECT * FROM count_lines WHERE ean LIKE :q OR name LIKE :q ORDER BY updatedAt DESC")
    fun search(q: String): Flow<List<CountLine>>
}


