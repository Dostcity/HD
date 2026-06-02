package com.koyuncu.takip.ui.find

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
import com.koyuncu.takip.find.FindAlarm

@Composable
fun FindScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var running by remember { mutableStateOf(FindAlarm.isRunning()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Telefonu Bul", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        Text(
            "Alarm: yüksek ses + flash + titreşim. Telefon sessizde olsa bile çalar. " +
                "(Çırpınca otomatik çalması bir sonraki adımda eklenecek.)",
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(28.dp))
        if (!running) {
            Button(onClick = {
                FindAlarm.start(context)
                running = true
            }) {
                Text("📢  Alarmı Çal (test)")
            }
        } else {
            Button(onClick = {
                FindAlarm.stop()
                running = false
            }) {
                Text("⏹  Durdur")
            }
        }
    }
}
