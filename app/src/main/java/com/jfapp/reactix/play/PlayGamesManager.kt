package com.jfapp.reactix.play

import android.app.Activity
import android.content.Context
import com.google.android.gms.games.AuthenticationResult
import com.google.android.gms.games.PlayGames
import com.google.android.gms.tasks.Task

class PlayGamesManager(private val context: Context) {

    fun signIn(): Task<AuthenticationResult?> {
        // Play Games v2 handles sign-in implicitly; you can still request sign-in.
        return PlayGames.getGamesSignInClient(context as Activity).signIn()
    }

    fun submitGlobalScore(score: Int) {
        if (PlayGamesIds.LEADERBOARD_GLOBAL.startsWith("TODO")) return
        PlayGames.getLeaderboardsClient(context as Activity)
            .submitScore(PlayGamesIds.LEADERBOARD_GLOBAL, score.toLong())
    }

    fun submitDailyScore(score: Int) {
        if (PlayGamesIds.LEADERBOARD_DAILY.startsWith("TODO")) return
        PlayGames.getLeaderboardsClient(context as Activity)
            .submitScore(PlayGamesIds.LEADERBOARD_DAILY, score.toLong())
    }

    fun showGlobalLeaderboard() {
        if (PlayGamesIds.LEADERBOARD_GLOBAL.startsWith("TODO")) return
        PlayGames.getLeaderboardsClient(context as Activity)
            .getLeaderboardIntent(PlayGamesIds.LEADERBOARD_GLOBAL)
            .addOnSuccessListener { intent ->
                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
    }

    fun showDailyLeaderboard() {
        if (PlayGamesIds.LEADERBOARD_DAILY.startsWith("TODO")) return
        PlayGames.getLeaderboardsClient(context as Activity)
            .getLeaderboardIntent(PlayGamesIds.LEADERBOARD_DAILY)
            .addOnSuccessListener { intent ->
                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
    }
}