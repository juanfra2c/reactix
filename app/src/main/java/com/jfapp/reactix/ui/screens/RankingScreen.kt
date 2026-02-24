package com.jfapp.reactix.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jfapp.reactix.viewmodel.AppViewModel

@Composable
fun RankingScreen(appVm: AppViewModel, onBack: () -> Unit) {
    var tab by remember { mutableStateOf(0) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Ranking", style = MaterialTheme.typography.headlineLarge)
            TextButton(onClick = onBack) { Text("Back") }
        }

        Spacer(Modifier.height(12.dp))

        TabRow(selectedTabIndex = tab) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Daily") })
            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Global") })
        }

        Spacer(Modifier.height(16.dp))

        if (tab == 0) {
            Button(onClick = { appVm.showDailyLeaderboard() }, modifier = Modifier.fillMaxWidth()) {
                Text("Open Daily Leaderboard (Play Games)")
            }
        } else {
            Button(onClick = { appVm.showGlobalLeaderboard() }, modifier = Modifier.fillMaxWidth()) {
                Text("Open Global Leaderboard (Play Games)")
            }
        }

        Spacer(Modifier.height(18.dp))
        Text(
            "IDs are TODO until you paste real leaderboard IDs in PlayGamesIds.kt.",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}