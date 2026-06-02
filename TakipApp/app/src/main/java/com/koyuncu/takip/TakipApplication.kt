package com.koyuncu.takip

import android.app.Application
import com.koyuncu.takip.data.local.AppDatabase
import com.koyuncu.takip.data.price.FakePriceSource
import com.koyuncu.takip.data.repo.ProductRepository
import com.koyuncu.takip.data.repo.TodoRepository
import com.koyuncu.takip.notify.Notifier
import com.koyuncu.takip.work.WorkScheduler

/**
 * Basit servis locator. (İleride istenirse Hilt'e geçilebilir.)
 */
class TakipApplication : Application() {

    private val db by lazy { AppDatabase.get(this) }
    val todoRepository by lazy { TodoRepository(db.todoDao()) }
    val productRepository by lazy { ProductRepository(db.productDao(), FakePriceSource()) }

    override fun onCreate() {
        super.onCreate()
        Notifier.ensureChannel(this)
        WorkScheduler.schedule(this)
    }
}
