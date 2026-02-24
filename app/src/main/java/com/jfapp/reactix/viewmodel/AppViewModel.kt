package com.jfapp.reactix.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jfapp.reactix.data.AppRepository
import com.jfapp.reactix.game.GameMode
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

data class UiAppState(
    val bestGlobal: Int = 0,
    val dailyBest: Int = 0,
    val coins: Int = 0,
    val xp: Int = 0,
    val level: Int = 1,
    val removeAds: Boolean = false,
    val selectedSkin: String = "default",

    val lastScore: Int = 0,
    val lastMode: String = "classic",
    val lastUsedBoost: Boolean = false,
)

class AppViewModel(private val repo: AppRepository) : ViewModel() {

    private val _ui = MutableStateFlow(UiAppState())
    val ui: StateFlow<UiAppState> = _ui.asStateFlow()

    // for navigation convenience
    val lastMode: String get() = _ui.value.lastMode

    init {
        viewModelScope.launch {
            repo.persisted.collect { p ->
                _ui.update {
                    it.copy(
                        bestGlobal = p.bestGlobal,
                        dailyBest = p.dailyBest,
                        coins = p.coins,
                        xp = p.xp,
                        level = p.level,
                        removeAds = p.removeAds,
                        selectedSkin = p.selectedSkin
                    )
                }
            }
        }
    }

    fun signInPlayGames() {
        repo.signInPlayGames()
    }

    fun showDailyLeaderboard() = repo.showDailyLeaderboard()
    fun showGlobalLeaderboard() = repo.showGlobalLeaderboard()

    fun setLastRun(score: Int, mode: GameMode, usedBoost: Boolean) {
        _ui.update {
            it.copy(
                lastScore = score,
                lastMode = if (mode == GameMode.DAILY) "daily" else "classic",
                lastUsedBoost = usedBoost
            )
        }
    }

    fun applyAndPersistRun(mode: GameMode, score: Int, coinsEarned: Int, xpEarned: Int, usedBoost: Boolean) {
        viewModelScope.launch {
            val current = _ui.value

            val epochDay = LocalDate.now(ZoneId.systemDefault()).toEpochDay()

            val newBestGlobal = maxOf(current.bestGlobal, score)
            val newDailyBest = if (mode == GameMode.DAILY) maxOf(current.dailyBest, score) else current.dailyBest

            // Simple level calc: every 250 XP -> +1
            val newTotalXp = current.xp + xpEarned + if (mode == GameMode.DAILY) 50 else 0
            val newLevel = maxOf(1, (newTotalXp / 250) + 1)

            val countsForRanking = !usedBoost

            // Persist
            if (mode == GameMode.DAILY) {
                repo.saveDaily(
                    epochDay = epochDay,
                    dailyBest = newDailyBest,
                    coinsAdd = coinsEarned,
                    xpAdd = xpEarned + 50,
                    level = newLevel
                )
            } else {
                repo.saveClassic(
                    bestGlobal = newBestGlobal,
                    coinsAdd = coinsEarned,
                    xpAdd = xpEarned,
                    level = newLevel
                )
            }

            // Submit scores to Play Games (only if countsForRanking)
            if (countsForRanking) {
                if (mode == GameMode.DAILY) repo.submitDaily(score) else repo.submitGlobal(score)
            }

            // Keep last run
            setLastRun(score, mode, usedBoost)
        }
    }
}