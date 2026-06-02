package com.koyuncu.takip.data.price

/** Tek bir pazaryerinden gelen fiyat. */
data class MarketPrice(
    val market: String,
    val price: Double,
    val url: String,
    val currency: String = "TL"
)
