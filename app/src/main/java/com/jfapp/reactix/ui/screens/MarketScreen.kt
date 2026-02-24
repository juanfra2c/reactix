package com.jfapp.reactix.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jfapp.reactix.viewmodel.AppViewModel
import com.jfapp.reactix.viewmodel.MarketViewModel

@Composable
fun MarketScreen(
    appVm: AppViewModel,
    marketVm: MarketViewModel,
    onBack: () -> Unit
) {
    val ui by appVm.ui.collectAsState()
    var msg by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Market", style = MaterialTheme.typography.headlineLarge)
            TextButton(onClick = onBack) { Text("Back") }
        }

        Spacer(Modifier.height(8.dp))
        Text("Coins: ${ui.coins} • Selected skin: ${ui.selectedSkin}")

        Spacer(Modifier.height(16.dp))

        Text("Skins", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))

        marketVm.skins.forEach { item ->
            Card(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(item.title, style = MaterialTheme.typography.titleMedium)
                        Text("${item.priceCoins} coins", style = MaterialTheme.typography.bodyMedium)
                    }
                    Button(onClick = {
                        marketVm.buySkin(item) { ok ->
                            msg = if (ok) "Purchased/Selected: ${item.title}" else "Not enough coins"
                        }
                    }) { Text("Buy") }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        msg?.let { Text(it) }

        Spacer(Modifier.height(20.dp))
        Text(
            "Boosts will be added next: using boosts will block ranking submission (fair play).",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}