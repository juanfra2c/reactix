package com.jfapp.reactix.ads

import android.content.Context

class AdsManager(private val context: Context) {

    fun init() {
        // TODO: si usas AdMob aquí inicializas MobileAds.initialize(context)
    }

    fun loadInterstitial() {
        // TODO
    }

    fun loadRewarded() {
        // TODO
    }

    fun showInterstitialIfReady(onClosed: () -> Unit) {
        // STUB: por ahora no muestra nada
        onClosed()
    }

    fun showRewarded(onReward: () -> Unit, onClosed: () -> Unit) {
        // STUB: simula recompensa
        onReward()
        onClosed()
    }
}