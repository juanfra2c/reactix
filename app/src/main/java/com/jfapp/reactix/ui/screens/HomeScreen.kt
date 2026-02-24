package com.jfapp.reactix.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jfapp.reactix.viewmodel.AppViewModel

@Composable
fun HomeScreen(
    appVm: AppViewModel,
    onPlayClassic: () -> Unit,
    onPlayDaily: () -> Unit,
    onRanking: () -> Unit,
    onMarket: () -> Unit,
    onSettings: () -> Unit
) {
    val ui by appVm.ui.collectAsState()

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Reactix", style = MaterialTheme.typography.headlineLarge)
            Button(onClick = { appVm.signInPlayGames() }) { Text("Play Games") }
        }

        Spacer(Modifier.height(16.dp))

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Best (Global): ${ui.bestGlobal}", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(6.dp))
                Text("Best (Daily): ${ui.dailyBest}", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                Text("Level ${ui.level} • XP ${ui.xp} • Coins ${ui.coins}")
            }
        }

        Spacer(Modifier.height(18.dp))

        Button(onClick = onPlayClassic, modifier = Modifier.fillMaxWidth().height(54.dp)) {
            Text("PLAY")
        }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(onClick = onPlayDaily, modifier = Modifier.fillMaxWidth().height(54.dp)) {
            Text("DAILY CHALLENGE")
        }

        Spacer(Modifier.height(18.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onRanking, modifier = Modifier.weight(1f)) { Text("Ranking") }
            OutlinedButton(onClick = onMarket, modifier = Modifier.weight(1f)) { Text("Market") }
            OutlinedButton(onClick = onSettings, modifier = Modifier.weight(1f)) { Text("Settings") }
        }
    }
}