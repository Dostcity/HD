package com.koyuncu.takip.ui.tracking

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions

@Composable
fun TrackingScreen(vm: TrackingViewModel, modifier: Modifier = Modifier) {
    val products by vm.products.collectAsState()
    val checking by vm.checking.collectAsState()

    var name by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("") }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Ürün adı (örn. DJI Mini 5 Pro)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = url,
            onValueChange = { url = it },
            label = { Text("Ürün linki (Trendyol / Amazon / Akakçe)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )
        OutlinedTextField(
            value = target,
            onValueChange = { target = it },
            label = { Text("Hedef fiyat (opsiyonel, TL)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    vm.add(name, url, target.replace(",", ".").toDoubleOrNull())
                    name = ""; url = ""; target = ""
                },
                modifier = Modifier.weight(1f)
            ) { Text("Takibe ekle") }

            OutlinedButton(onClick = { vm.checkNow() }, enabled = !checking) {
                if (checking) {
                    CircularProgressIndicator(modifier = Modifier.padding(end = 4.dp))
                } else {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                }
                Text("Kontrol et")
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(products, key = { it.id }) { p ->
                ElevatedCard {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(p.name, style = MaterialTheme.typography.titleMedium)
                            val priceLine = p.lastLowestPrice?.let {
                                "En düşük: %,.0f TL — %s".format(it, p.lastLowestMarket ?: "-")
                            } ?: "Henüz kontrol edilmedi"
                            Text(priceLine, style = MaterialTheme.typography.bodyMedium)
                            p.targetPrice?.let {
                                Text(
                                    "Hedef: %,.0f TL".format(it),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        IconButton(onClick = { vm.delete(p) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Sil")
                        }
                    }
                }
            }
        }
    }
}
