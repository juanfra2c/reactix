package com.jfapp.reactix.data

import com.jfapp.reactix.play.PlayGamesManager
import java.time.LocalDate
import java.time.ZoneId

class AppRepository(
    private val dataStore: DataStoreManager,
    private val playGames: PlayGamesManager,
) {
    val persisted = dataStore.state

    fun signInPlayGames() = playGames.signIn()

    fun submitGlobal(score: Int) = playGames.submitGlobalScore(score)
    fun submitDaily(score: Int) = playGames.submitDailyScore(score)

    fun showGlobalLeaderboard() = playGames.showGlobalLeaderboard()
    fun showDailyLeaderboard() = playGames.showDailyLeaderboard()

    suspend fun applyRunResult(
        mode: String,
        score: Int,
        coinsEarned: Int,
        xpEarned: Int,
        usedBoost: Boolean,
        bestGlobalCurrent: Int,
        dailyBestCurrent: Int,
    ): AppliedResult {

        val newBestGlobal = maxOf(bestGlobalCurrent, score)
        val epochDay = LocalDate.now(ZoneId.systemDefault()).toEpochDay()

        // Level calc: simple XP thresholds
        val totalXpBefore = persistedValueXpCache // not used; we compute in VM
        // Level computed in VM, repository receives it
        // (kept simple: VM handles)

        // Ranking rules:
        val countsForRanking = !usedBoost

        return AppliedResult(
            newBestGlobal = newBestGlobal,
            countsForRanking = countsForRanking,
            epochDay = epochDay,
        )
    }

    suspend fun saveClassic(bestGlobal: Int, coinsAdd: Int, xpAdd: Int, level: Int) {
        dataStore.saveRunResult(bestGlobal, coinsAdd, xpAdd, level)
    }

    suspend fun saveDaily(epochDay: Long, dailyBest: Int, coinsAdd: Int, xpAdd: Int, level: Int) {
        dataStore.saveDaily(epochDay, dailyBest, coinsAdd, xpAdd, level)
    }

    suspend fun spendCoins(amount: Int) = dataStore.spendCoins(amount)
    suspend fun setSkin(skin: String) = dataStore.setSkin(skin)
    suspend fun setRemoveAds(value: Boolean) = dataStore.setRemoveAds(value)
}

// Simple result carrier
data class AppliedResult(
    val newBestGlobal: Int,
    val countsForRanking: Boolean,
    val epochDay: Long,
)

// Placeholder to avoid confusion in this snippet.
private const val persistedValueXpCache: Int = 0