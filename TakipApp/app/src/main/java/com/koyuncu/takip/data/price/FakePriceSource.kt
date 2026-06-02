package com.koyuncu.takip.data.price

import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * Geliştirme/test için sahte fiyat üreten kaynak.
 * Her çağrıda fiyatları biraz oynatır ki bildirim akışını test edebilelim.
 * Gerçek kaynaklar hazır olduğunda bunu değiştireceğiz.
 */
class FakePriceSource : PriceSource {

    private val markets = listOf(
        "Hepsiburada", "Trendyol", "Amazon TR", "N11", "DJI Store"
    )

    override suspend fun fetchPrices(query: String): List<MarketPrice> {
        delay(400) // ağ gecikmesi simülasyonu
        val base = 38_000.0 + (query.hashCode() % 5000)
        return markets.map { market ->
            val variation = Random.nextDouble(-0.10, 0.12) // ±%
            val price = (base * (1 + variation)).let { Math.round(it / 10.0) * 10.0 }
            MarketPrice(
                market = market,
                price = price,
                url = "https://example.com/search?q=" + query.replace(" ", "+")
            )
        }
    }
}
