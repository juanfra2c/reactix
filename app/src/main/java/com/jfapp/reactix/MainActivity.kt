package com.jfapp.reactix

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.jfapp.reactix.ui.ReactixRoot
import com.jfapp.reactix.ui.theme.ReactixTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ReactixTheme {
                ReactixRoot()
            }
        }
    }
}