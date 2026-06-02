package com.koyuncu.takip.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.koyuncu.takip.TakipApplication
import com.koyuncu.takip.notify.Notifier

class PriceCheckWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as TakipApplication
        return try {
            val results = app.productRepository.checkAll()
            results.filter { it.dropped }.forEachIndexed { index, r ->
                Notifier.priceDrop(
                    context = applicationContext,
                    id = (r.product.id.toInt() * 100 + index),
                    productName = r.product.name,
                    market = r.lowest.market,
                    price = r.lowest.price
                )
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
