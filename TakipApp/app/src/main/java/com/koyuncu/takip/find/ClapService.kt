package com.koyuncu.takip.find

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.koyuncu.takip.MainActivity
import kotlin.math.abs

/**
 * Mikrofonu dinleyen foreground servis. 2 kez cirpinca FindAlarm'i tetikler.
 * Alarm calarken dinlemeyi atlar (alarm sesi kendini tetiklemesin diye).
 */
class ClapService : Service() {

    @Volatile private var listening = false
    private var thread: Thread? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startInForeground()
        if (!listening) {
            listening = true
            isListening = true
            thread = Thread { detectLoop() }.also { it.isDaemon = true; it.start() }
        }
        return START_STICKY
    }

    private fun startInForeground() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Cirpma dinleme", NotificationManager.IMPORTANCE_LOW)
            )
        }
        val tap = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notif: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Telefonu Bul aktif")
            .setContentText("Bulmak icin 2 kez cirpin")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setOngoing(true)
            .setContentIntent(tap)
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            startForeground(NOTIF_ID, notif, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
        } else {
            startForeground(NOTIF_ID, notif)
        }
    }

    private fun detectLoop() {
        val sampleRate = 44100
        val minBuf = AudioRecord.getMinBufferSize(
            sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
        )
        if (minBuf <= 0) {
            stopSelf(); return
        }
        val bufSize = maxOf(minBuf, 4096)
        val record = try {
            AudioRecord(
                MediaRecorder.AudioSource.MIC, sampleRate,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufSize
            )
        } catch (t: Throwable) {
            stopSelf(); return
        }
        if (record.state != AudioRecord.STATE_INITIALIZED) {
            try { record.release() } catch (_: Throwable) {}
            stopSelf(); return
        }
        try {
            record.startRecording()
        } catch (t: Throwable) {
            try { record.release() } catch (_: Throwable) {}
            stopSelf(); return
        }

        val buf = ShortArray(bufSize)
        var firstClapAt = 0L
        var clapCount = 0
        var lastOnsetAt = 0L

        while (listening) {
            val n = try { record.read(buf, 0, buf.size) } catch (t: Throwable) { -1 }
            if (n <= 0) continue

            // Alarm calarken yeni cirpma arama (alarm sesi kendini tetiklemesin)
            if (FindAlarm.isRunning()) continue

            var peak = 0
            var i = 0
            while (i < n) {
                val a = abs(buf[i].toInt())
                if (a > peak) peak = a
                i++
            }

            val now = System.currentTimeMillis()
            if (peak > THRESHOLD && now - lastOnsetAt > 200) {
                lastOnsetAt = now
                if (clapCount == 0 || now - firstClapAt > CLAP_WINDOW_MS) {
                    firstClapAt = now
                    clapCount = 1
                } else {
                    clapCount++
                    if (clapCount >= 2) {
                        clapCount = 0
                        mainHandler.post { FindAlarm.start(applicationContext) }
                    }
                }
            }
            if (clapCount > 0 && now - firstClapAt > CLAP_WINDOW_MS) clapCount = 0
        }

        try { record.stop() } catch (_: Throwable) {}
        try { record.release() } catch (_: Throwable) {}
    }

    override fun onDestroy() {
        listening = false
        isListening = false
        try { thread?.interrupt() } catch (_: Throwable) {}
        FindAlarm.stop()
        super.onDestroy()
    }

    companion object {
        @Volatile
        var isListening = false
        private const val CHANNEL_ID = "clap_listen"
        private const val NOTIF_ID = 4711
        private const val THRESHOLD = 20000      // 0..32767 arasi tepe; cirpma ~ yuksek
        private const val CLAP_WINDOW_MS = 1300L // 2 cirpma bu sure icinde olmali
    }
}
