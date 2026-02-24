package com.jfapp.reactix.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jfapp.reactix.viewmodel.AppViewModel

@Composable
fun GameOverScreen(
    appVm: AppViewModel,
    onPlayAgain: () -> Unit,
    onHome: () -> Unit,
    onMarket: () -> Unit,
    onRanking: () -> Unit
) {
    val ui by appVm.ui.collectAsState()

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("GAME OVER", style = MaterialTheme.typography.headlineLarge)

        Spacer(Modifier.height(16.dp))

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Score: ${ui.lastScore}", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(6.dp))
                Text("Best Global: ${ui.bestGlobal}")
                Text("Best Daily: ${ui.dailyBest}")
                Spacer(Modifier.height(6.dp))
                Text("Boost used: ${ui.lastUsedBoost}")
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(onClick = onPlayAgain, modifier = Modifier.fillMaxWidth().height(54.dp)) {
            Text("PLAY AGAIN")
        }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(onClick = onRanking, modifier = Modifier.fillMaxWidth().height(54.dp)) {
            Text("RANKING")
        }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(onClick = onMarket, modifier = Modifier.fillMaxWidth().height(54.dp)) {
            Text("MARKET")
        }
        Spacer(Modifier.height(10.dp))
        TextButton(onClick = onHome, modifier = Modifier.fillMaxWidth()) { Text("Back to Home") }
    }
}