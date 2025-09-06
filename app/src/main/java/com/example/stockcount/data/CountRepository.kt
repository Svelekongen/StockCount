package com.example.stockcount.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

class CountRepository private constructor(
    private val dao: CountLineDao
) {
    suspend fun upsert(line: CountLine) = dao.upsert(line)

    suspend fun increment(ean: String, delta: Int) =
        dao.incrementCount(ean, delta, System.currentTimeMillis())

    suspend fun findByEan(ean: String): CountLine? = dao.findByEan(ean)

    fun getAll(): Flow<List<CountLine>> = dao.getAll()

    companion object {
        @Volatile
        private var INSTANCE: CountRepository? = null

        fun getInstance(context: Context): CountRepository {
            return INSTANCE ?: synchronized(this) {
                val db = CountDatabase.getInstance(context)
                CountRepository(db.countLineDao()).also { INSTANCE = it }
            }
        }
    }
}
