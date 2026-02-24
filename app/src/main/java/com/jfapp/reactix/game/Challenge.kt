package com.jfapp.reactix.game

import kotlin.random.Random

sealed class Challenge {
    data class TapColor(val target: TargetColor) : Challenge()
    data class DontTap(val durationMs: Long) : Challenge()
    data class Swipe(val dir: SwipeDir) : Challenge()
    data class TapTimes(val times: Int) : Challenge()

    enum class TargetColor { BLUE, GREEN, YELLOW }
    enum class SwipeDir { UP, DOWN, LEFT, RIGHT }

    companion object {
        fun random(rng: Random): Challenge {
            return when (rng.nextInt(4)) {
                0 -> TapColor(TargetColor.entries[rng.nextInt(TargetColor.entries.size)])
                1 -> DontTap(durationMs = 900L + rng.nextLong(0, 700))
                2 -> Swipe(SwipeDir.entries[rng.nextInt(SwipeDir.entries.size)])
                else -> TapTimes(times = if (rng.nextBoolean()) 2 else 3)
            }
        }
    }
}