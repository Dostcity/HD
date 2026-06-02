package com.koyuncu.takip.data.price

/**
 * Bir ürünün farklı pazaryerlerindeki fiyatlarını döndüren kaynak.
 * İleride her pazaryeri (Hepsiburada, Trendyol, Amazon TR, N11, DJI Store...)
 * için ayrı implementasyon eklenip burada birleştirilecek.
 */
interface PriceSource {
    suspend fun fetchPrices(query: String): List<MarketPrice>
}
