package com.koyuncu.takip.notify

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.koyuncu.takip.R

object Notifier {
    private const val CHANNEL_ID = "price_drops"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Fiyat Düşüşleri",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Takip edilen ürünlerin fiyatı düştüğünde bildirir" }
            context.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    fun priceDrop(context: Context, id: Int, productName: String, market: String, price: Double) {
        ensureChannel(context)
        val text = "%s — %,.0f TL".format(market, price)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Fiyat düştü: $productName")
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(id, notification)
        } catch (_: SecurityException) {
            // POST_NOTIFICATIONS izni verilmemiş olabilir
        }
    }
}
