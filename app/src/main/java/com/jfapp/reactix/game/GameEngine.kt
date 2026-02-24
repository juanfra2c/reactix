package com.jfapp.reactix.game

import kotlin.math.max
import kotlin.random.Random

class GameEngine {

    private var rng: Random = Random(System.currentTimeMillis())
    private var challengeMs: Long = GameConfig.INITIAL_CHALLENGE_MS
    private var challengesDone: Int = 0

    fun start(mode: GameMode, seed: Long? = null): GameState {
        rng = if (seed != null) Random(seed) else Random(System.currentTimeMillis())
        challengeMs = GameConfig.INITIAL_CHALLENGE_MS
        challengesDone = 0
        val s0 = GameState(mode = mode, isRunning = true)
        return nextChallenge(s0, nowMs())
    }

    fun tick(state: GameState, nowMs: Long): GameState {
        if (!state.isRunning || state.isGameOver) return state

        // If the challenge time is over, decide outcome:
        if (nowMs >= state.challengeEndsAtMs) {
            // If it was DontTap and player survived => success
            state.currentChallenge?.let { ch ->
                return when (ch) {
                    is Challenge.DontTap -> onSuccess(state, nowMs)
                    // Otherwise: they failed to do required action in time
                    else -> onFail(state)
                }
            }
        }
        return state
    }

    fun onTap(state: GameState, nowMs: Long, tappedColor: Challenge.TargetColor? = null): GameState {
        if (!state.isRunning || state.isGameOver) return state

        // If it expects no tap, any tap fails
        if (state.expectsNoTap) return onFail(state)

        // TapTimes
        if (state.expectsTapTimes > 0) {
            val prog = state.tapTimesProgress + 1
            val s2 = state.copy(tapTimesProgress = prog)
            return if (prog >= state.expectsTapTimes) onSuccess(s2, nowMs) else s2
        }

        // TapColor
        val expected = state.expectsTapColor
        return if (expected != null && tappedColor == expected) {
            onSuccess(state, nowMs)
        } else {
            onFail(state)
        }
    }

    fun onSwipe(state: GameState, nowMs: Long, dir: Challenge.SwipeDir): GameState {
        if (!state.isRunning || state.isGameOver) return state
        if (state.expectsNoTap) return onFail(state)
        val expected = state.expectsSwipe ?: return onFail(state)
        return if (expected == dir) onSuccess(state, nowMs) else onFail(state)
    }

    fun useBoost(state: GameState, type: BoostType): GameState {
        if (!state.isRunning || state.isGameOver) return state
        return when (type) {
            BoostType.EXTRA_REVIVE -> state.copy(usedBoostInRun = true, reviveAvailable = true)
            BoostType.DOUBLE_COINS_THIS_RUN -> state.copy(usedBoostInRun = true) // handled at finalize
            BoostType.SHIELD_ONE_FAIL -> state.copy(usedBoostInRun = true) // V1.1: implement
        }
    }

    fun tryRevive(state: GameState): GameState {
        if (state.isRunning && state.isGameOver && state.reviveAvailable && !state.didRevive) {
            // revive puts you back into running, new challenge
            val resurrected = state.copy(isGameOver = false, isRunning = true, didRevive = true)
            return nextChallenge(resurrected, nowMs())
        }
        return state
    }

    private fun onSuccess(state: GameState, nowMs: Long): GameState {
        val newScore = state.score + 1
        val newCombo = state.combo + 1
        val coins = state.coinsEarned + GameConfig.COINS_PER_POINT
        val xp = state.xpEarned + GameConfig.XP_PER_POINT

        val speedup = (newScore % GameConfig.SPEEDUP_EVERY_N == 0)
        if (speedup) {
            challengeMs = max(GameConfig.MIN_CHALLENGE_MS, challengeMs - GameConfig.SPEEDUP_STEP_MS)
        }

        val s2 = state.copy(score = newScore, combo = newCombo, coinsEarned = coins, xpEarned = xp)
        return nextChallenge(s2, nowMs)
    }

    private fun onFail(state: GameState): GameState {
        return state.copy(isRunning = false, isGameOver = true, combo = 0)
    }

    private fun nextChallenge(state: GameState, nowMs: Long): GameState {
        val ch = Challenge.random(rng)
        challengesDone++

        val endsAt = nowMs + challengeMs

        return when (ch) {
            is Challenge.TapColor -> state.copy(
                currentChallenge = ch,
                challengeEndsAtMs = endsAt,
                expectsNoTap = false,
                expectsTapColor = ch.target,
                expectsSwipe = null,
                expectsTapTimes = 0,
                tapTimesProgress = 0,
            )

            is Challenge.DontTap -> state.copy(
                currentChallenge = ch,
                challengeEndsAtMs = nowMs + ch.durationMs,
                expectsNoTap = true,
                expectsTapColor = null,
                expectsSwipe = null,
                expectsTapTimes = 0,
                tapTimesProgress = 0,
            )

            is Challenge.Swipe -> state.copy(
                currentChallenge = ch,
                challengeEndsAtMs = endsAt,
                expectsNoTap = false,
                expectsTapColor = null,
                expectsSwipe = ch.dir,
                expectsTapTimes = 0,
                tapTimesProgress = 0,
            )

            is Challenge.TapTimes -> state.copy(
                currentChallenge = ch,
                challengeEndsAtMs = endsAt,
                expectsNoTap = false,
                expectsTapColor = null,
                expectsSwipe = null,
                expectsTapTimes = ch.times,
                tapTimesProgress = 0,
            )
        }
    }

    private fun nowMs(): Long = System.currentTimeMillis()
}

enum class BoostType {
    EXTRA_REVIVE,
    DOUBLE_COINS_THIS_RUN,
    SHIELD_ONE_FAIL
}