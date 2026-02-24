package com.jfapp.reactix.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jfapp.reactix.data.AppRepository
import com.jfapp.reactix.game.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

class GameViewModel(private val repo: AppRepository) : ViewModel() {

    private val engine = GameEngine()

    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    private var loopJob: Job? = null

    fun start(mode: GameMode) {
        loopJob?.cancel()

        val seed = if (mode == GameMode.DAILY) dailySeed() else null
        _state.value = engine.start(mode, seed)

        loopJob = viewModelScope.launch {
            while (true) {
                delay(16) // ~60fps tick
                _state.value = engine.tick(_state.value, System.currentTimeMillis())
                if (_state.value.isGameOver) {
                    break
                }
            }
        }
    }

    fun stop() {
        loopJob?.cancel()
    }

    fun tap(tappedColor: Challenge.TargetColor?) {
        _state.value = engine.onTap(_state.value, System.currentTimeMillis(), tappedColor)
    }

    fun swipe(dir: Challenge.SwipeDir) {
        _state.value = engine.onSwipe(_state.value, System.currentTimeMillis(), dir)
    }

    fun useBoost(type: BoostType) {
        _state.value = engine.useBoost(_state.value, type)
    }

    fun revive() {
        _state.value = engine.tryRevive(_state.value)
    }

    private fun dailySeed(): Long {
        val epochDay = LocalDate.now(ZoneId.systemDefault()).toEpochDay()
        // stable seed per day
        return (epochDay.toString() + GameConfig.DAILY_SEED_SALT).hashCode().toLong()
    }
}