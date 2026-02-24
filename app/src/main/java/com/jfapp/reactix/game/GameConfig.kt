package com.jfapp.reactix.game

object GameConfig {
    // Partida: micro-retos cada X ms, que luego se acorta
    const val INITIAL_CHALLENGE_MS: Long = 2400
    const val MIN_CHALLENGE_MS: Long = 1100
    const val SPEEDUP_EVERY_N: Int = 6
    const val SPEEDUP_STEP_MS: Long = 120

    // Ventana máxima para reaccionar (si no haces nada cuando toca, fallas)
    const val ACTION_TIMEOUT_MS: Long = 1500

    // Daily
    const val DAILY_SEED_SALT: String = "REACTIX_DAILY"

    // Economy
    const val COINS_PER_POINT: Int = 1
    const val XP_PER_POINT: Int = 2
    const val XP_DAILY_BONUS: Int = 50

    // Boost rules
    const val BOOST_BLOCKS_RANKING: Boolean = true
}