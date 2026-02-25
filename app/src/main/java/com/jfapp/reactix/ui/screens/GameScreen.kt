package com.jfapp.reactix.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.jfapp.reactix.game.Challenge
import com.jfapp.reactix.game.GameMode
import com.jfapp.reactix.viewmodel.AppViewModel
import com.jfapp.reactix.viewmodel.GameViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.sin

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

    // Countdown + input lock
    var inputEnabled by remember { mutableStateOf(false) }
    var countdown by remember { mutableStateOf<Int?>(null) } // 3,2,1,0(GO), null

    // Flash feedback (local)
    val flashAlpha = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var flashColor by remember { mutableStateOf(Color.Transparent) }

    LaunchedEffect(gameMode) {
        gameVm.start(gameMode)

        inputEnabled = false
        countdown = 3
        delay(450)
        countdown = 2
        delay(450)
        countdown = 1
        delay(450)
        countdown = 0 // GO
        delay(250)
        countdown = null
        inputEnabled = true
    }

    LaunchedEffect(state.isGameOver) {
        if (state.isGameOver) {
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

            val chTop = state.currentChallenge

            // Chip solo para TapColor
            if (chTop is Challenge.TapColor) {
                TargetChip(
                    target = chTop.target,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 14.dp)
                )
            }

            // Overlay texto para TapTimes: x2 / x3 encima del círculo central
            if (chTop is Challenge.TapTimes) {
                Box(
                    modifier = Modifier.matchParentSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "x${chTop.times}",
                        style = MaterialTheme.typography.displayMedium
                    )
                }
            }

            Canvas(
                modifier = Modifier
                    .fillMaxSize()

                    // SWIPE (robusto: acumulamos y decidimos al soltar)
                    .pointerInput(inputEnabled, Unit) {
                        if (!inputEnabled) return@pointerInput

                        var totalDx = 0f
                        var totalDy = 0f

                        detectDragGestures(
                            onDragStart = { totalDx = 0f; totalDy = 0f },
                            onDragCancel = { totalDx = 0f; totalDy = 0f },
                            onDragEnd = {
                                val threshold = 80f
                                if (hypot(totalDx, totalDy) >= threshold) {
                                    val dir = if (abs(totalDx) > abs(totalDy)) {
                                        if (totalDx > 0) Challenge.SwipeDir.RIGHT else Challenge.SwipeDir.LEFT
                                    } else {
                                        if (totalDy > 0) Challenge.SwipeDir.DOWN else Challenge.SwipeDir.UP
                                    }
                                    gameVm.swipe(dir)
                                }
                                totalDx = 0f
                                totalDy = 0f
                            }
                        ) { change, dragAmount ->
                            change.consume()
                            totalDx += dragAmount.x
                            totalDy += dragAmount.y
                        }
                    }

                    // TAP (TapColor hit test + TapTimes círculo central + DontTap penaliza)
                    .pointerInput(inputEnabled, state.currentChallenge, state.tapColorDots, state.tapTimesProgress) {
                        if (!inputEnabled) return@pointerInput

                        detectTapGestures { offset ->
                            val ch = state.currentChallenge

                            // --- TapColor: hit-test círculos ---
                            if (ch is Challenge.TapColor && state.tapColorDots.isNotEmpty()) {
                                val w = size.width
                                val h = size.height
                                val radius = min(w, h) * 0.09f

                                val hit = state.tapColorDots.firstOrNull { dot ->
                                    val cx = dot.nx * w
                                    val cy = dot.ny * h
                                    val dx = offset.x - cx
                                    val dy = offset.y - cy
                                    dx * dx + dy * dy <= radius * radius
                                }

                                if (hit != null) {
                                    val ok = (hit.color == ch.target)
                                    flashColor = if (ok) Color(0xFF27AE60) else Color(0xFFEB5757)

                                    gameVm.tap(hit.color)
                                    scope.launch {
                                        flashAlpha.snapTo(0.22f)
                                        flashAlpha.animateTo(0f, tween(160))
                                    }
                                } else {
                                    flashColor = Color(0xFFEB5757)
                                    gameVm.tap(null)
                                    scope.launch {
                                        flashAlpha.snapTo(0.18f)
                                        flashAlpha.animateTo(0f, tween(140))
                                    }
                                }
                                return@detectTapGestures
                            }

                            // --- TapTimes: SOLO cuenta si tocas el círculo central ---
                            if (ch is Challenge.TapTimes) {
                                val w = size.width
                                val h = size.height
                                val center = Offset(w / 2f, h / 2f)
                                val radius = min(w, h) * 0.14f

                                val dx = offset.x - center.x
                                val dy = offset.y - center.y
                                val inside = (dx * dx + dy * dy) <= radius * radius

                                if (inside) {
                                    flashColor = Color.White.copy(alpha = 0.9f)
                                    gameVm.tap(null)
                                    scope.launch {
                                        flashAlpha.snapTo(0.10f)
                                        flashAlpha.animateTo(0f, tween(110))
                                    }
                                }
                                // fuera: ignoramos (no cuenta)
                                return@detectTapGestures
                            }

                            // --- DontTap: cualquier tap penaliza (engine decide) ---
                            if (ch is Challenge.DontTap) {
                                flashColor = Color(0xFFEB5757)
                                gameVm.tap(null)
                                scope.launch {
                                    flashAlpha.snapTo(0.18f)
                                    flashAlpha.animateTo(0f, tween(140))
                                }
                                return@detectTapGestures
                            }

                            // Otros: tap normal + flash suave
                            flashColor = Color.White.copy(alpha = 0.8f)
                            gameVm.tap(null)
                            scope.launch {
                                flashAlpha.snapTo(0.08f)
                                flashAlpha.animateTo(0f, tween(90))
                            }
                        }
                    }
            ) {
                val w = size.width
                val h = size.height
                val center = Offset(w / 2f, h / 2f)

                when (val ch = state.currentChallenge) {

                    is Challenge.TapColor -> {
                        val radius = min(w, h) * 0.09f

                        state.tapColorDots.forEach { dot ->
                            val cx = dot.nx * w
                            val cy = dot.ny * h

                            val base = when (dot.color) {
                                Challenge.TargetColor.BLUE -> Color(0xFF2F80ED)
                                Challenge.TargetColor.GREEN -> Color(0xFF27AE60)
                                Challenge.TargetColor.YELLOW -> Color(0xFFF2C94C)
                            }

                            // Todos iguales (no revelamos target)
                            val alpha = 0.80f

                            drawCircle(
                                color = Color.White.copy(alpha = 0.18f),
                                radius = radius + 6f,
                                center = Offset(cx, cy)
                            )
                            drawCircle(
                                color = base.copy(alpha = alpha),
                                radius = radius,
                                center = Offset(cx, cy)
                            )
                        }
                    }

                    is Challenge.DontTap -> {
                        // Círculo + X grande
                        val radius = min(w, h) * 0.16f

                        drawCircle(
                            color = Color.White.copy(alpha = 0.10f),
                            radius = radius,
                            center = center
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = 0.22f),
                            radius = radius + 6f,
                            center = center,
                            style = Stroke(width = 10f, cap = StrokeCap.Round)
                        )

                        val xLen = radius * 0.65f
                        drawLine(
                            color = Color.White.copy(alpha = 0.55f),
                            start = Offset(center.x - xLen, center.y - xLen),
                            end = Offset(center.x + xLen, center.y + xLen),
                            strokeWidth = 12f,
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = Color.White.copy(alpha = 0.55f),
                            start = Offset(center.x - xLen, center.y + xLen),
                            end = Offset(center.x + xLen, center.y - xLen),
                            strokeWidth = 12f,
                            cap = StrokeCap.Round
                        )
                    }

                    is Challenge.Swipe -> {
                        // Flecha doble (ya la tenías)
                        val len = size.minDimension * 0.22f
                        val start = center

                        val end = when (ch.dir) {
                            Challenge.SwipeDir.UP -> Offset(center.x, center.y - len)
                            Challenge.SwipeDir.DOWN -> Offset(center.x, center.y + len)
                            Challenge.SwipeDir.LEFT -> Offset(center.x - len, center.y)
                            Challenge.SwipeDir.RIGHT -> Offset(center.x + len, center.y)
                        }

                        drawArrow(
                            start = start,
                            end = end,
                            color = Color.White.copy(alpha = 0.75f),
                            strokeWidth = 14f,
                            headLength = 42f
                        )

                        val end2 = Offset(
                            x = start.x + (end.x - start.x) * 0.75f,
                            y = start.y + (end.y - start.y) * 0.75f
                        )

                        drawArrow(
                            start = start,
                            end = end2,
                            color = Color.White.copy(alpha = 0.35f),
                            strokeWidth = 10f,
                            headLength = 32f
                        )
                    }

                    is Challenge.TapTimes -> {
                        // Círculo central + anillo de progreso (en vez de puntos)
                        val radius = min(w, h) * 0.14f

                        drawCircle(
                            color = Color.White.copy(alpha = 0.10f),
                            radius = radius,
                            center = center
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = 0.22f),
                            radius = radius + 6f,
                            center = center,
                            style = Stroke(width = 10f, cap = StrokeCap.Round)
                        )

                        val progress = (state.tapTimesProgress.toFloat() / ch.times.toFloat())
                            .coerceIn(0f, 1f)

                        drawArc(
                            color = Color.White.copy(alpha = 0.55f),
                            startAngle = -90f,
                            sweepAngle = 360f * progress,
                            useCenter = false,
                            topLeft = Offset(center.x - (radius + 14f), center.y - (radius + 14f)),
                            size = Size((radius + 14f) * 2f, (radius + 14f) * 2f),
                            style = Stroke(width = 12f, cap = StrokeCap.Round)
                        )
                    }

                    null -> Unit
                }
            }

            // Countdown overlay
            if (countdown != null) {
                Box(
                    modifier = Modifier.matchParentSize(),
                    contentAlignment = Alignment.Center
                ) {
                    val text = if (countdown == 0) "GO" else countdown.toString()
                    Text(text = text, style = MaterialTheme.typography.displayLarge)
                }
            }

            // Flash overlay
            if (flashAlpha.value > 0f) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .graphicsLayer(alpha = flashAlpha.value)
                        .background(flashColor)
                )
            }

            // Texto mínimo: solo para retos que NO sean TapColor ni TapTimes (para no mezclar con chip/x2)
            if (chTop !is Challenge.TapColor && chTop !is Challenge.TapTimes) {
                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 14.dp)
                ) {
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
}

@Composable
private fun TargetChip(target: Challenge.TargetColor, modifier: Modifier = Modifier) {
    val targetColor = when (target) {
        Challenge.TargetColor.BLUE -> Color(0xFF2F80ED)
        Challenge.TargetColor.GREEN -> Color(0xFF27AE60)
        Challenge.TargetColor.YELLOW -> Color(0xFFF2C94C)
    }

    Surface(
        modifier = modifier,
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("TAP", style = MaterialTheme.typography.titleMedium)
            Canvas(Modifier.size(16.dp)) { drawCircle(color = targetColor) }
        }
    }
}

@Composable
private fun instructionText(state: com.jfapp.reactix.game.GameState): String {
    val ch = state.currentChallenge ?: return "..."
    return when (ch) {
        is Challenge.TapColor -> "TAP"
        is Challenge.DontTap -> "DON'T TAP"
        is Challenge.Swipe -> "SWIPE"
        is Challenge.TapTimes -> "TAP x${ch.times}"
    }
}

@Composable
private fun hintText(): String = "Tap • Swipe"

private fun DrawScope.drawArrow(
    start: Offset,
    end: Offset,
    color: Color,
    strokeWidth: Float,
    headLength: Float,
    headAngleDeg: Float = 28f
) {
    // cuerpo
    drawLine(
        color = color,
        start = start,
        end = end,
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )

    val angle = atan2(end.y - start.y, end.x - start.x)
    val headAngle = Math.toRadians(headAngleDeg.toDouble()).toFloat()

    val x1 = end.x - headLength * cos(angle - headAngle)
    val y1 = end.y - headLength * sin(angle - headAngle)
    val x2 = end.x - headLength * cos(angle + headAngle)
    val y2 = end.y - headLength * sin(angle + headAngle)

    // alas
    drawLine(
        color = color,
        start = end,
        end = Offset(x1, y1),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
    drawLine(
        color = color,
        start = end,
        end = Offset(x2, y2),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
}