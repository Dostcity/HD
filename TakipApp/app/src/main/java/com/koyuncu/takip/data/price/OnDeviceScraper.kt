package com.koyuncu.takip.data.price

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/** Kazima sonucu + teshis bilgisi. */
data class ScrapeResult(
    val price: Double?,
    val market: String,
    val status: Int,
    val htmlLen: Int,
    val blocked: Boolean,
    val note: String
)

/**
 * Cihaz uzerinde, verilen urun sayfasini cekip fiyatini cikarir.
 * Masaustu Chrome kimligi kullanir (bulutta calisan buydu).
 */
object OnDeviceScraper {

    private const val UA =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36"

    private data class Dl(val status: Int, val body: String?, val error: String?)

    suspend fun scrape(url: String): ScrapeResult = withContext(Dispatchers.IO) {
        if (url.isBlank()) return@withContext ScrapeResult(null, "-", 0, 0, false, "link yok")
        val market = marketName(url)
        val dl = download(url)
        if (dl.body == null) {
            val n = if (dl.status < 0) "ag hatasi: ${dl.error ?: "?"}" else "HTTP ${dl.status}"
            return@withContext ScrapeResult(null, market, dl.status, 0, false, n)
        }
        if (looksBlocked(dl.status, dl.body)) {
            return@withContext ScrapeResult(null, market, dl.status, dl.body.length, true, "engellendi (HTTP ${dl.status})")
        }
        val price = extractPrice(dl.body)
        if (price == null) {
            return@withContext ScrapeResult(
                null, market, dl.status, dl.body.length, false,
                "HTTP ${dl.status}, fiyat bulunamadi (${dl.body.length} bayt)"
            )
        }
        ScrapeResult(price, market, dl.status, dl.body.length, false, "OK %,.0f TL".format(price))
    }

    fun marketName(url: String): String {
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

    private fun download(urlStr: String): Dl {
        var conn: HttpURLConnection? = null
        return try {
            conn = (URL(urlStr).openConnection() as HttpURLConnection).apply {
                connectTimeout = 25000
                readTimeout = 25000
                instanceFollowRedirects = true
                useCaches = false
                requestMethod = "GET"
                setRequestProperty("User-Agent", UA)
                setRequestProperty(
                    "Accept",
                    "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"
                )
                setRequestProperty("Accept-Language", "tr-TR,tr;q=0.9,en;q=0.8")
            }
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val body = stream?.bufferedReader()?.use { it.readText() }
            Dl(code, body, null)
        } catch (t: Throwable) {
            val msg = (t.javaClass.simpleName + ": " + (t.message ?: "")).take(140)
            Dl(-1, null, msg)
        } finally {
            try { conn?.disconnect() } catch (_: Throwable) {}
        }
    }

    private fun looksBlocked(status: Int, html: String): Boolean {
        if (status == 403 || status == 429 || status == 503) return true
        val low = html.lowercase()
        return listOf("datadome", "px-captcha", "/security/", "captcha", "are you a human")
            .any { it in low }
    }

    private fun extractPrice(html: String): Double? {
        for (key in listOf("discountedPrice", "sellingPrice", "originalPrice")) {
            val m = Regex("\"$key\"\\s*:\\s*\\{[^{}]*?\"value\"\\s*:\\s*([0-9]+(?:\\.[0-9]+)?)").find(html)
            if (m != null) m.groupValues[1].toDoubleOrNull()?.let { if (it > 0) return it }
        }
        val ld = Regex(
            "<script[^>]*type=\"application/ld\\+json\"[^>]*>(.*?)</script>",
            setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE)
        )
        for (m in ld.findAll(html)) {
            priceFromJsonLd(m.groupValues[1].trim())?.let { return it }
        }
        val meta = Regex(
            "<meta[^>]+(?:property|name)=\"product:price:amount\"[^>]+content=\"([0-9.,]+)\""
        ).find(html)
        if (meta != null) parseTrNumber(meta.groupValues[1])?.let { if (it > 0) return it }
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
