package com.koyuncu.takip.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "price_history")
data class PriceHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val market: String,
    val price: Double,
    val timestamp: Long = System.currentTimeMillis()
)
