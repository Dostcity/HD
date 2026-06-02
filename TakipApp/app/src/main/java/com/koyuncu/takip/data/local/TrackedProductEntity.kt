package com.koyuncu.takip.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Takip edilen ürün.
 * query: pazaryerlerinde aranacak metin (örn. "DJI Mini 5 Pro")
 * targetPrice: bu fiyatın altına düşünce bildirim (null ise herhangi bir düşüş)
 * lastLowestPrice: en son bilinen en düşük fiyat (karşılaştırma için)
 */
@Entity(tableName = "tracked_products")
data class TrackedProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val query: String,
    val url: String = "",
    val targetPrice: Double? = null,
    val lastLowestPrice: Double? = null,
    val lastLowestMarket: String? = null,
    val lastCheckedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
