package com.koyuncu.takip.data.repo

import com.koyuncu.takip.data.local.PriceHistoryEntity
import com.koyuncu.takip.data.local.ProductDao
import com.koyuncu.takip.data.local.TrackedProductEntity
import com.koyuncu.takip.data.price.MarketPrice
import com.koyuncu.takip.data.price.OnDeviceScraper
import kotlinx.coroutines.flow.Flow

/** Bir fiyat kontrolünün sonucu (note: ekranda gösterilecek teşhis). */
data class PriceCheckResult(
    val product: TrackedProductEntity,
    val lowest: MarketPrice?,
    val previousLowest: Double?,
    val dropped: Boolean,
    val note: String
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

    /** Ürünü linkinden kontrol eder; her durumda teşhisli sonuç döner. */
    suspend fun checkProduct(product: TrackedProductEntity): PriceCheckResult {
        val res = OnDeviceScraper.scrape(product.url)
        if (res.price == null) {
            return PriceCheckResult(product, null, product.lastLowestPrice, false, res.note)
        }

        val mp = MarketPrice(market = res.market, price = res.price, url = product.url, currency = "TL")
        dao.insertHistory(
            PriceHistoryEntity(productId = product.id, market = mp.market, price = mp.price)
        )

        val previous = product.lastLowestPrice
        val target = product.targetPrice
        val dropped = when {
            target != null -> mp.price <= target
            previous != null -> mp.price < previous
            else -> false // ilk ölçüm: referans alınır, bildirim yok
        }

        dao.update(
            product.copy(
                lastLowestPrice = mp.price,
                lastLowestMarket = mp.market,
                lastCheckedAt = System.currentTimeMillis()
            )
        )

        return PriceCheckResult(product, mp, previous, dropped, res.note)
    }

    suspend fun checkAll(): List<PriceCheckResult> =
        dao.getAll().map { checkProduct(it) }
}
