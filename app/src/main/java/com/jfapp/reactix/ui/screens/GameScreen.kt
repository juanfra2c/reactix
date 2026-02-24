package com.jfapp.reactix.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.jfapp.reactix.game.Challenge
import com.jfapp.reactix.game.GameMode
import com.jfapp.reactix.viewmodel.AppViewModel
import com.jfapp.reactix.viewmodel.GameViewModel
import kotlin.math.abs
import androidx.compose.foundation.gestures.detectTapGestures

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    mode: String,
    appVm: AppViewModel,
    gameVm: GameViewModel,
    onExit: () -> Unit,
    onGameOver: () -> Unit
) {
    val gameMode = if (mode == "daily") GameMode.DAILY else GameMode.CLASSIC
    val state by gameVm.state.collectAsState()

    LaunchedEffect(gameMode) {
        gameVm.start(gameMode)
    }

    LaunchedEffect(state.isGameOver) {
        if (state.isGameOver) {
            // persist + submit score (if no boost)
            appVm.applyAndPersistRun(
                mode = gameMode,
                score = state.score,
                coinsEarned = state.coinsEarned,
                xpEarned = state.xpEarned,
                usedBoost = state.usedBoostInRun
            )
            onGameOver()
        }
    }

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(if (gameMode == GameMode.DAILY) "Daily" else "Classic") },
            navigationIcon = {
                TextButton(onClick = {
                    gameVm.stop()
                    onExit()
                }) { Text("Exit") }
            },
            actions = { Text("Score ${state.score}") }
        )

        Spacer(Modifier.height(6.dp))

        Box(Modifier.fillMaxSize().padding(12.dp)) {

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val w = size.width
                            val x = offset.x
                            val color = when {
                                x < w / 3f -> Challenge.TargetColor.BLUE
                                x < 2f * w / 3f -> Challenge.TargetColor.GREEN
                                else -> Challenge.TargetColor.YELLOW
                            }
                            gameVm.tap(color)
                        }
                    }
            ) {
                // Draw challenge "prompt"
                val center = Offset(size.width / 2f, size.height / 2f)

                // Simple HUD-like prompt (text will be added later as native text)
                // For now we draw shapes representing the current challenge.
                when (val ch = state.currentChallenge) {
                    is Challenge.TapColor -> {
                        // Draw 3 columns - highlight target column
                        val colW = size.width / 3f
                        val targetIndex = when (ch.target) {
                            Challenge.TargetColor.BLUE -> 0
                            Challenge.TargetColor.GREEN -> 1
                            Challenge.TargetColor.YELLOW -> 2
                        }
                        for (i in 0..2) {
                            val left = i * colW
                            val top = size.height * 0.35f
                            val h = size.height * 0.3f
                            drawRect(
                                color = androidx.compose.ui.graphics.Color.White.copy(alpha = if (i == targetIndex) 0.35f else 0.10f),
                                topLeft = Offset(left + 12f, top),
                                size = androidx.compose.ui.geometry.Size(colW - 24f, h)
                            )
                        }
                    }

                    is Challenge.DontTap -> {
                        // Big "danger" ring
                        drawCircle(
                            color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.14f),
                            radius = size.minDimension * 0.33f,
                            center = center
                        )
                    }

                    is Challenge.Swipe -> {
                        // Arrow-like line
                        val len = size.minDimension * 0.18f
                        val end = when (ch.dir) {
                            Challenge.SwipeDir.UP -> Offset(center.x, center.y - len)
                            Challenge.SwipeDir.DOWN -> Offset(center.x, center.y + len)
                            Challenge.SwipeDir.LEFT -> Offset(center.x - len, center.y)
                            Challenge.SwipeDir.RIGHT -> Offset(center.x + len, center.y)
                        }
                        drawLine(
                            color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.5f),
                            start = center,
                            end = end,
                            strokeWidth = 10f
                        )
                    }

                    is Challenge.TapTimes -> {
                        // Draw N dots
                        val n = ch.times
                        val gap = 46f
                        val startX = center.x - (gap * (n - 1) / 2f)
                        for (i in 0 until n) {
                            drawCircle(
                                color = androidx.compose.ui.graphics.Color.White.copy(alpha = if (i < state.tapTimesProgress) 0.55f else 0.18f),
                                radius = 16f,
                                center = Offset(startX + i * gap, center.y)
                            )
                        }
                    }

                    null -> {}
                }
            }

            // Minimal instruction overlay
            Column(Modifier.fillMaxWidth()) {
                Text(
                    text = instructionText(state),
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = hintText(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

// --- Helpers (no dependencies) ---

@Composable
private fun instructionText(state: com.jfapp.reactix.game.GameState): String {
    val ch = state.currentChallenge ?: return "..."
    return when (ch) {
        is Challenge.TapColor -> "TAP: ${ch.target.name}"
        is Challenge.DontTap -> "DON'T TAP"
        is Challenge.Swipe -> "SWIPE: ${ch.dir.name}"
        is Challenge.TapTimes -> "TAP x${ch.times}"
    }
}

@Composable
private fun hintText(): String =
    "Tap anywhere (left/center/right) • Swipe in any direction"

