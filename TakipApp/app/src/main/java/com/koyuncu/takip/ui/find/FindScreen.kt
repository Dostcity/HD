package com.koyuncu.takip.ui.find

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.koyuncu.takip.find.ClapService
import com.koyuncu.takip.find.FindAlarm

@Composable
fun FindScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var listening by remember { mutableStateOf(ClapService.isListening) }
    var alarmRunning by remember { mutableStateOf(FindAlarm.isRunning()) }

    val micPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startClapService(context)
            listening = true
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Telefonu Bul", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        Text(
            "Çırpınca bul: telefonu bulmak için 2 kez çırp. Arka planda dinler " +
                "(kalıcı bildirim görünür, pili biraz harcar).",
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        if (!listening) {
            Button(onClick = {
                val granted = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
                if (granted) {
                    startClapService(context)
                    listening = true
                } else {
                    micPermission.launch(Manifest.permission.RECORD_AUDIO)
                }
            }) {
                Text("👏  Çırpınca bulmayı başlat")
            }
        } else {
            Button(onClick = {
                stopClapService(context)
                listening = false
            }) {
                Text("⏹  Dinlemeyi durdur")
            }
        }

        Spacer(Modifier.height(36.dp))

        Text(
            "Alarmı doğrudan test et (ses + flash + titreşim):",
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        if (!alarmRunning) {
            Button(onClick = {
                FindAlarm.start(context)
                alarmRunning = true
            }) {
                Text("📢  Alarmı Çal (test)")
            }
        } else {
            Button(onClick = {
                FindAlarm.stop()
                alarmRunning = false
            }) {
                Text("⏹  Alarmı Durdur")
            }
        }
    }
}

private fun startClapService(context: Context) {
    ContextCompat.startForegroundService(context, Intent(context, ClapService::class.java))
}

private fun stopClapService(context: Context) {
    context.stopService(Intent(context, ClapService::class.java))
}
