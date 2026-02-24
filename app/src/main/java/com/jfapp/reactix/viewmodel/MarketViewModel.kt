package com.jfapp.reactix.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jfapp.reactix.data.AppRepository
import kotlinx.coroutines.launch

data class MarketItem(
    val id: String,
    val title: String,
    val priceCoins: Int,
)

class MarketViewModel(private val repo: AppRepository) : ViewModel() {

    val skins = listOf(
        MarketItem("skin_default", "Default", 0),
        MarketItem("skin_neon_blue", "Neon Blue", 250),
        MarketItem("skin_neon_green", "Neon Green", 250),
        MarketItem("skin_red_pulse", "Red Pulse", 400),
    )

    fun buySkin(item: MarketItem, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val ok = if (item.priceCoins == 0) true else repo.spendCoins(item.priceCoins)
            if (ok) repo.setSkin(item.id)
            onResult(ok)
        }
    }
}