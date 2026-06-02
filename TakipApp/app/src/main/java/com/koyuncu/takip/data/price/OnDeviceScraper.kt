package com.koyuncu.takip.data.price

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Cihaz uzerinde, verilen urun sayfasini cekip fiyatini cikarir.
 * Telefonun ev IP'sinden istek atildigi icin bot korumalarina daha az takilir.
 * Coklu yedekli ayrıştırma: inline state (discountedPrice/sellingPrice),
 * JSON-LD offers.price, meta product:price:amount.
 */
object OnDeviceScraper {

    private const val UA =
        "Mozilla/5.0 (Linux; Android 14; SM-S928B) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/125.0.0.0 Mobile Safari/537.36"

    suspend fun scrape(url: String): MarketPrice? = withContext(Dispatchers.IO) {
        if (url.isBlank()) return@withContext null
        val html = download(url) ?: return@withContext null
        val price = extractPrice(html) ?: return@withContext null
        MarketPrice(market = marketName(url), price = price, url = url, currency = "TL")
    }

    private fun marketName(url: String): String {
        val u = url.lowercase()
        return when {
            "trendyol" in u -> "Trendyol"
            "hepsiburada" in u -> "Hepsiburada"
            "amazon" in u -> "Amazon TR"
            "akakce" in u -> "Akakçe"
            "cimri" in u -> "Cimri"
            "dolap" in u -> "Dolap"
            "n11" in u -> "N11"
            else -> "Web"
        }
    }

    private fun download(urlStr: String): String? {
        var conn: HttpURLConnection? = null
        return try {
            conn = (URL(urlStr).openConnection() as HttpURLConnection).apply {
                connectTimeout = 20000
                readTimeout = 20000
                instanceFollowRedirects = true
                requestMethod = "GET"
                setRequestProperty("User-Agent", UA)
                setRequestProperty(
                    "Accept",
                    "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"
                )
                setRequestProperty("Accept-Language", "tr-TR,tr;q=0.9,en;q=0.8")
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

    private fun extractPrice(html: String): Double? {
        // 1) Inline state: discountedPrice / sellingPrice / originalPrice {value: N}
        for (key in listOf("discountedPrice", "sellingPrice", "originalPrice")) {
            val m = Regex("\"$key\"\\s*:\\s*\\{[^{}]*?\"value\"\\s*:\\s*([0-9]+(?:\\.[0-9]+)?)").find(html)
            if (m != null) {
                m.groupValues[1].toDoubleOrNull()?.let { if (it > 0) return it }
            }
        }
        // 2) JSON-LD offers.price
        val ld = Regex(
            "<script[^>]*type=\"application/ld\\+json\"[^>]*>(.*?)</script>",
            setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE)
        )
        for (m in ld.findAll(html)) {
            priceFromJsonLd(m.groupValues[1].trim())?.let { return it }
        }
        // 3) meta product:price:amount
        val meta = Regex(
            "<meta[^>]+(?:property|name)=\"product:price:amount\"[^>]+content=\"([0-9.,]+)\""
        ).find(html)
        if (meta != null) {
            parseTrNumber(meta.groupValues[1])?.let { if (it > 0) return it }
        }
        return null
    }

    private fun priceFromJsonLd(raw: String): Double? {
        return try {
            val items: List<JSONObject> = if (raw.trimStart().startsWith("[")) {
                val arr = JSONArray(raw)
                (0 until arr.length()).mapNotNull { arr.optJSONObject(it) }
            } else {
                listOf(JSONObject(raw))
            }
            for (d in items) {
                val offers = d.opt("offers")
                val offerList: List<JSONObject> = when (offers) {
                    is JSONArray -> (0 until offers.length()).mapNotNull { offers.optJSONObject(it) }
                    is JSONObject -> listOf(offers)
                    else -> emptyList()
                }
                for (o in offerList) {
                    val p = o.opt("price")
                    val value = (p as? Number)?.toDouble()
                        ?: p?.toString()?.replace(",", ".")?.toDoubleOrNull()
                    if (value != null && value > 0) return value
                }
            }
            null
        } catch (t: Throwable) {
            null
        }
    }

    private fun parseTrNumber(v: String): Double? {
        val s = when {
            v.contains(",") && v.contains(".") -> v.replace(".", "").replace(",", ".")
            v.contains(",") -> v.replace(",", ".")
            else -> v
        }
        return s.toDoubleOrNull()
    }
}
