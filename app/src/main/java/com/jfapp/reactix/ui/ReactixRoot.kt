package com.jfapp.reactix.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jfapp.reactix.data.AppRepository
import com.jfapp.reactix.data.DataStoreManager
import com.jfapp.reactix.play.PlayGamesManager
import com.jfapp.reactix.ui.screens.*
import com.jfapp.reactix.viewmodel.*

@Composable
fun ReactixRoot() {
    val nav = rememberNavController()
    val ctx = LocalContext.current

    val repo = remember {
        AppRepository(
            dataStore = DataStoreManager(ctx),
            playGames = PlayGamesManager(ctx),
        )
    }

    val appVm: AppViewModel = viewModel(factory = AppViewModelFactory(repo))
    val gameVm: GameViewModel = viewModel(factory = GameViewModelFactory(repo))
    val marketVm: MarketViewModel = viewModel(factory = MarketViewModelFactory(repo))

    NavHost(navController = nav, startDestination = Routes.Splash) {
        composable(Routes.Splash) {
            SplashScreen(onDone = { nav.navigate(Routes.Home) { popUpTo(Routes.Splash) { inclusive = true } } })
        }

        composable(Routes.Home) {
            HomeScreen(
                appVm = appVm,
                onPlayClassic = { nav.navigate("${Routes.Game}?mode=classic") },
                onPlayDaily = { nav.navigate("${Routes.Game}?mode=daily") },
                onRanking = { nav.navigate(Routes.Ranking) },
                onMarket = { nav.navigate(Routes.Market) },
                onSettings = { nav.navigate(Routes.Settings) }
            )
        }

        composable(
            route = "${Routes.Game}?mode={mode}",
            arguments = listOf(navArgument("mode") { type = NavType.StringType; defaultValue = "classic" })
        ) { backStack ->
            val mode = backStack.arguments?.getString("mode") ?: "classic"
            GameScreen(
                mode = mode,
                appVm = appVm,
                gameVm = gameVm,
                onExit = { nav.popBackStack() },
                onGameOver = { nav.navigate(Routes.GameOver) }
            )
        }

        composable(Routes.GameOver) {
            GameOverScreen(
                appVm = appVm,
                onPlayAgain = { nav.navigate("${Routes.Game}?mode=${appVm.lastMode}") { popUpTo(Routes.Home) { inclusive = false } } },
                onHome = { nav.popBackStack(Routes.Home, false) },
                onMarket = { nav.navigate(Routes.Market) },
                onRanking = { nav.navigate(Routes.Ranking) }
            )
        }

        composable(Routes.Ranking) {
            RankingScreen(appVm = appVm, onBack = { nav.popBackStack() })
        }

        composable(Routes.Market) {
            MarketScreen(
                appVm = appVm,
                marketVm = marketVm,
                onBack = { nav.popBackStack() }
            )
        }

        composable(Routes.Settings) {
            SettingsScreen(appVm = appVm, onBack = { nav.popBackStack() })
        }
    }
}