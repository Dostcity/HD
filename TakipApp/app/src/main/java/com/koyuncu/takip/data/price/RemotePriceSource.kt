package com.koyuncu.takip.data.price

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Fiyatlari GitHub Actions'in urettigi herkese acik prices.json'dan okur.
 *
 * Beklenen bicim:
 * {
 *   "updatedAt": "...",
 *   "products": [
 *     { "query":"DJI Mini 5 Pro", "name":"DJI Mini 5 Pro",
 *       "prices":[ {"market":"Trendyol","price":46099.0,"url":"...","currency":"TL"} ] }
 *   ]
 * }
 *
 * query ya da name (buyuk/kucuk harf duyarsiz) ile eslesir.
 */
class RemotePriceSource(private val jsonUrl: String) : PriceSource {

    override suspend fun fetchPrices(query: String): List<MarketPrice> = withContext(Dispatchers.IO) {
        val text = download(jsonUrl) ?: return@withContext emptyList()
        parse(text, query)
    }

    private fun download(urlStr: String): String? {
        var conn: HttpURLConnection? = null
        return try {
            conn = (URL(urlStr).openConnection() as HttpURLConnection).apply {
                connectTimeout = 15000
                readTimeout = 15000
                requestMethod = "GET"
                setRequestProperty("Accept", "application/json")
                setRequestProperty("Cache-Control", "no-cache")
            }
            if (conn.responseCode in 200..299) {
                conn.inputStream.bufferedReader().use { it.readText() }
            } else {
                null
            }
        } catch (t: Throwable) {
            null
        } finally {
            try { conn?.disconnect() } catch (_: Throwable) {}
        }
    }

    private fun parse(text: String, query: String): List<MarketPrice> {
        return try {
            val products = JSONObject(text).optJSONArray("products") ?: return emptyList()
            val q = query.trim().lowercase()
            for (i in 0 until products.length()) {
                val p = products.optJSONObject(i) ?: continue
                val pq = p.optString("query").trim().lowercase()
                val pn = p.optString("name").trim().lowercase()
                if (pq != q && pn != q) continue
                val arr = p.optJSONArray("prices") ?: return emptyList()
                val list = ArrayList<MarketPrice>()
                for (j in 0 until arr.length()) {
                    val m = arr.optJSONObject(j) ?: continue
                    val price = m.optDouble("price", -1.0)
                    if (price <= 0.0) continue
                    list.add(
                        MarketPrice(
                            market = m.optString("market", "?"),
                            price = price,
                            url = m.optString("url", ""),
                            currency = m.optString("currency", "TL")
                        )
                    )
                }
                return list
            }
            emptyList()
        } catch (t: Throwable) {
            emptyList()
        }
    }
}
