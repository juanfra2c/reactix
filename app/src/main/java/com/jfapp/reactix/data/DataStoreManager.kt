package com.jfapp.reactix.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.ds by preferencesDataStore("reactix_prefs")

class DataStoreManager(private val context: Context) {

    private object Keys {
        val bestGlobal = intPreferencesKey("best_global")
        val coins = intPreferencesKey("coins")
        val xp = intPreferencesKey("xp")
        val level = intPreferencesKey("level")
        val lastDailyEpochDay = longPreferencesKey("last_daily_epoch_day")
        val dailyBest = intPreferencesKey("daily_best")
        val removeAds = booleanPreferencesKey("remove_ads")
        val selectedSkin = stringPreferencesKey("selected_skin")
    }

    val state: Flow<PersistedState> = context.ds.data.map { p ->
        PersistedState(
            bestGlobal = p[Keys.bestGlobal] ?: 0,
            coins = p[Keys.coins] ?: 0,
            xp = p[Keys.xp] ?: 0,
            level = p[Keys.level] ?: 1,
            lastDailyEpochDay = p[Keys.lastDailyEpochDay] ?: 0L,
            dailyBest = p[Keys.dailyBest] ?: 0,
            removeAds = p[Keys.removeAds] ?: false,
            selectedSkin = p[Keys.selectedSkin] ?: "default",
        )
    }

    suspend fun update(block: (MutablePreferences) -> Unit) {
        context.ds.edit { prefs -> block(prefs) }
    }

    suspend fun saveRunResult(bestGlobal: Int, coinsAdd: Int, xpAdd: Int, level: Int) {
        context.ds.edit { p ->
            p[Keys.bestGlobal] = bestGlobal
            p[Keys.coins] = (p[Keys.coins] ?: 0) + coinsAdd
            p[Keys.xp] = (p[Keys.xp] ?: 0) + xpAdd
            p[Keys.level] = level
        }
    }

    suspend fun saveDaily(epochDay: Long, dailyBest: Int, coinsAdd: Int, xpAdd: Int, level: Int) {
        context.ds.edit { p ->
            p[Keys.lastDailyEpochDay] = epochDay
            p[Keys.dailyBest] = dailyBest
            p[Keys.coins] = (p[Keys.coins] ?: 0) + coinsAdd
            p[Keys.xp] = (p[Keys.xp] ?: 0) + xpAdd
            p[Keys.level] = level
        }
    }

    suspend fun setRemoveAds(value: Boolean) {
        context.ds.edit { p -> p[Keys.removeAds] = value }
    }

    suspend fun spendCoins(amount: Int): Boolean {
        var ok = false
        context.ds.edit { p ->
            val c = p[Keys.coins] ?: 0
            if (c >= amount) {
                p[Keys.coins] = c - amount
                ok = true
            }
        }
        return ok
    }

    suspend fun setSkin(skin: String) {
        context.ds.edit { p -> p[Keys.selectedSkin] = skin }
    }
}

data class PersistedState(
    val bestGlobal: Int,
    val coins: Int,
    val xp: Int,
    val level: Int,
    val lastDailyEpochDay: Long,
    val dailyBest: Int,
    val removeAds: Boolean,
    val selectedSkin: String,
)