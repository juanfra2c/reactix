package com.jfapp.reactix.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jfapp.reactix.viewmodel.AppViewModel

@Composable
fun SettingsScreen(appVm: AppViewModel, onBack: () -> Unit) {
    val ui by appVm.ui.collectAsState()

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Settings", style = MaterialTheme.typography.headlineLarge)
            TextButton(onClick = onBack) { Text("Back") }
        }

        Spacer(Modifier.height(16.dp))
        Text("Remove ads: ${ui.removeAds}")
        Spacer(Modifier.height(8.dp))
        Text("More settings (sound/vibration/lang) next.")
    }
}