package com.koyuncu.takip.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM tracked_products ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<TrackedProductEntity>>

    @Query("SELECT * FROM tracked_products")
    suspend fun getAll(): List<TrackedProductEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: TrackedProductEntity): Long

    @Update
    suspend fun update(product: TrackedProductEntity)

    @Delete
    suspend fun delete(product: TrackedProductEntity)

    @Insert
    suspend fun insertHistory(entry: PriceHistoryEntity)

    @Query("SELECT * FROM price_history WHERE productId = :productId ORDER BY timestamp DESC LIMIT :limit")
    fun observeHistory(productId: Long, limit: Int = 50): Flow<List<PriceHistoryEntity>>
}
