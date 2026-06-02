package com.koyuncu.takip

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

/**
 * Cok basit, bagimsiz cokme ekrani (FragmentActivity/Compose/biometric KULLANMAZ),
 * ki cokme sebebi ne olursa olsun bu ekran guvenle acilabilsin.
 */
class CrashActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val trace = CrashStore.get(this) ?: "Hata kaydi bulunamadi."

        val messageView = TextView(this).apply {
            text = "UYGULAMA COKTU\n\nLutfen bu ekranin goruntusunu alip gonder:\n\n$trace"
            setTextColor(Color.WHITE)
            setPadding(32, 48, 32, 48)
            textSize = 12f
            setTextIsSelectable(true)
        }
        val scroll = ScrollView(this).apply { addView(messageView) }

        val button = Button(this).apply {
            text = "Temizle ve uygulamayi ac"
            setOnClickListener {
                CrashStore.clear(this@CrashActivity)
                startActivity(
                    Intent(this@CrashActivity, MainActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                )
                finish()
            }
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#1B1B1B"))
            addView(button)
            addView(scroll)
        }
        setContentView(root)
    }
}
