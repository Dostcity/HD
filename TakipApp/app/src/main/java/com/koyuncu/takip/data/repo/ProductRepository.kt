package com.koyuncu.takip.data.repo

import com.koyuncu.takip.data.local.PriceHistoryEntity
import com.koyuncu.takip.data.local.ProductDao
import com.koyuncu.takip.data.local.TrackedProductEntity
import com.koyuncu.takip.data.price.MarketPrice
import com.koyuncu.takip.data.price.OnDeviceScraper
import kotlinx.coroutines.flow.Flow

/** Bir fiyat kontrolünün sonucu: düşüş olduysa bildirim için kullanılır. */
data class PriceCheckResult(
    val product: TrackedProductEntity,
    val lowest: MarketPrice,
    val previousLowest: Double?,
    val dropped: Boolean
)

class ProductRepository(private val dao: ProductDao) {

    val products: Flow<List<TrackedProductEntity>> = dao.observeAll()

    fun history(productId: Long) = dao.observeHistory(productId)

    suspend fun add(name: String, url: String, targetPrice: Double?) {
        dao.insert(
            TrackedProductEntity(
                name = name.trim(),
                query = name.trim(),
                url = url.trim(),
                targetPrice = targetPrice
            )
        )
    }

    suspend fun delete(product: TrackedProductEntity) = dao.delete(product)

    /** Tek bir ürünü (linkinden) kontrol eder, geçmişe yazar, sonucu döner. */
    suspend fun checkProduct(product: TrackedProductEntity): PriceCheckResult? {
        val prices = mutableListOf<MarketPrice>()
        if (product.url.isNotBlank()) {
            OnDeviceScraper.scrape(product.url)?.let { prices.add(it) }
        }
        val lowest = prices.minByOrNull { it.price } ?: return null

        prices.forEach {
            dao.insertHistory(
                PriceHistoryEntity(productId = product.id, market = it.market, price = it.price)
            )
        }

        val previous = product.lastLowestPrice
        val target = product.targetPrice
        val dropped = when {
            target != null -> lowest.price <= target
            previous != null -> lowest.price < previous
            else -> false // ilk ölçüm: referans alınır, bildirim yok
        }

        dao.update(
            product.copy(
                lastLowestPrice = lowest.price,
                lastLowestMarket = lowest.market,
                lastCheckedAt = System.currentTimeMillis()
            )
        )

        return PriceCheckResult(product, lowest, previous, dropped)
    }

    suspend fun checkAll(): List<PriceCheckResult> =
        dao.getAll().mapNotNull { checkProduct(it) }
}
