package com.example.stockcount.data

import android.content.Context
import androidx.room.Room
import com.example.stockcount.data.db.AppDatabase
import com.example.stockcount.data.db.CountLineDao
import com.example.stockcount.data.entity.CountLine
import com.example.stockcount.domain.Ean13
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class CountRepository private constructor(
    private val dao: CountLineDao,
    private val now: () -> Instant = { Clock.System.now() }
) {
    fun observeAll(): Flow<List<CountLine>> = dao.observeAll()

    fun search(query: String): Flow<List<CountLine>> = dao.search("%${query.trim()}%")

    suspend fun getItem(ean: String): CountLine? = dao.getByEan(ean)

    suspend fun scan(ean: String) {
        require(Ean13.isValid(ean)) { "Invalid EAN-13" }
        val existing = dao.getByEan(ean)
        val ts = now()
        if (existing == null) {
            dao.upsert(CountLine(ean = ean, quantity = 1, updatedAt = ts))
        } else {
            dao.upsert(existing.copy(quantity = existing.quantity + 1, updatedAt = ts))
        }
    }

    suspend fun undoScan(ean: String) {
        val existing = dao.getByEan(ean) ?: return
        val newQty = (existing.quantity - 1).coerceAtLeast(0)
        val ts = now()
        dao.upsert(existing.copy(quantity = newQty, updatedAt = ts))
    }

    suspend fun save(line: CountLine) {
        require(line.quantity >= 0) { "Quantity must be >= 0" }
        dao.upsert(line.copy(updatedAt = now()))
    }

    suspend fun delete(ean: String) = dao.deleteByEan(ean)

    companion object {
        @Volatile private var instance: CountRepository? = null
        fun get(context: Context): CountRepository =
            instance ?: synchronized(this) {
                val db = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "stockcount.db"
                ).build()
                CountRepository(db.countLineDao()).also { instance = it }
            }
    }
}


