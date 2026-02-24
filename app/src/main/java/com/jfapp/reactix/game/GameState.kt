package com.jfapp.reactix.game

data class GameState(
    val mode: GameMode = GameMode.CLASSIC,
    val isRunning: Boolean = false,
    val isGameOver: Boolean = false,
    val score: Int = 0,
    val combo: Int = 0,
    val coinsEarned: Int = 0,
    val xpEarned: Int = 0,

    val currentChallenge: Challenge? = null,
    val challengeEndsAtMs: Long = 0L,

    // input requirements:
    val expectsNoTap: Boolean = false,
    val expectsTapColor: Challenge.TargetColor? = null,
    val expectsSwipe: Challenge.SwipeDir? = null,
    val expectsTapTimes: Int = 0,
    val tapTimesProgress: Int = 0,

    // boosts
    val usedBoostInRun: Boolean = false,
    val reviveAvailable: Boolean = true,
    val didRevive: Boolean = false,
)