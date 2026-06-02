package com.koyuncu.takip.find

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * "Telefonu bul" alarmi: yuksek ses (alarm akisi) + flash yanip sonme + titresim.
 * Tek noktadan start()/stop(). Cesitli hatalara karsi her blok try/catch icinde.
 */
object FindAlarm {
    private var appContext: Context? = null
    private var player: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var cameraManager: CameraManager? = null
    private var torchId: String? = null
    private val handler = Handler(Looper.getMainLooper())
    private var blinkOn = false
    private var running = false
    private var savedAlarmVolume = -1

    fun isRunning(): Boolean = running

    fun start(context: Context) {
        if (running) return
        running = true
        val ctx = context.applicationContext
        appContext = ctx

        // Alarm ses seviyesini sonuna kadar ac (sessizde bile alarm akisi duyulur)
        try {
            val am = ctx.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            savedAlarmVolume = am.getStreamVolume(AudioManager.STREAM_ALARM)
            am.setStreamVolume(
                AudioManager.STREAM_ALARM,
                am.getStreamMaxVolume(AudioManager.STREAM_ALARM),
                0
            )
        } catch (_: Throwable) {
        }

        // Dongulu alarm sesi
        try {
            val uri = RingtoneManager.getActualDefaultRingtoneUri(ctx, RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            player = MediaPlayer().apply {
                setDataSource(ctx, uri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }
        } catch (_: Throwable) {
        }

        // Dongulu titresim
        try {
            val v = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                (ctx.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                ctx.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            vibrator = v
            v.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 600, 400), 0))
        } catch (_: Throwable) {
        }

        // Flash yanip sonme
        try {
            val cm = ctx.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            cameraManager = cm
            torchId = cm.cameraIdList.firstOrNull { id ->
                cm.getCameraCharacteristics(id).get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
            if (torchId != null) handler.post(blinkRunnable)
        } catch (_: Throwable) {
        }
    }

    private val blinkRunnable = object : Runnable {
        override fun run() {
            try {
                torchId?.let {
                    blinkOn = !blinkOn
                    cameraManager?.setTorchMode(it, blinkOn)
                }
            } catch (_: Throwable) {
            }
            handler.postDelayed(this, 350)
        }
    }

    fun stop() {
        if (!running) return
        running = false

        try {
            player?.stop()
            player?.release()
        } catch (_: Throwable) {
        }
        player = null

        handler.removeCallbacks(blinkRunnable)
        try {
            torchId?.let { cameraManager?.setTorchMode(it, false) }
        } catch (_: Throwable) {
        }
        blinkOn = false

        try {
            vibrator?.cancel()
        } catch (_: Throwable) {
        }
        vibrator = null

        // Alarm ses seviyesini eski haline dondur
        try {
            if (savedAlarmVolume >= 0) {
                val am = appContext?.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                am?.setStreamVolume(AudioManager.STREAM_ALARM, savedAlarmVolume, 0)
            }
        } catch (_: Throwable) {
        }
        savedAlarmVolume = -1
    }
}
