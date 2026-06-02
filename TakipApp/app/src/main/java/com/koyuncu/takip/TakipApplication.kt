package com.koyuncu.takip

import android.app.Application
import android.content.Intent
import android.os.Build
import android.os.Process
import com.koyuncu.takip.data.local.AppDatabase
import com.koyuncu.takip.data.repo.ProductRepository
import com.koyuncu.takip.data.repo.TodoRepository
import com.koyuncu.takip.notify.Notifier
import com.koyuncu.takip.work.WorkScheduler
import java.io.PrintWriter
import java.io.StringWriter
import kotlin.system.exitProcess

/**
 * Basit servis locator. (İleride istenirse Hilt'e geçilebilir.)
 */
class TakipApplication : Application() {

    private val db by lazy { AppDatabase.get(this) }
    val todoRepository by lazy { TodoRepository(db.todoDao()) }
    val productRepository by lazy { ProductRepository(db.productDao()) }

    override fun onCreate() {
        super.onCreate()
        installCrashHandler()
        Notifier.ensureChannel(this)
        WorkScheduler.schedule(this)
    }

    /** Yakalanmayan hatayi kaydeder ve cokme ekranini acmaya calisir. */
    private fun installCrashHandler() {
        Thread.setDefaultUncaughtExceptionHandler { _, e ->
            try {
                val sw = StringWriter()
                e.printStackTrace(PrintWriter(sw))
                val header = "Android API ${Build.VERSION.SDK_INT} / ${Build.MANUFACTURER} ${Build.MODEL}\n\n"
                CrashStore.save(this, header + sw.toString())
            } catch (_: Throwable) {
            }
            try {
                startActivity(
                    Intent(this, CrashActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                )
            } catch (_: Throwable) {
            }
            Process.killProcess(Process.myPid())
            exitProcess(10)
        }
    }
}
