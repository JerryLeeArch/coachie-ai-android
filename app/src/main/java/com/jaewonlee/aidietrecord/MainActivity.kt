package com.jaewonlee.aidietrecord

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.jaewonlee.aidietrecord.navigation.AIDietNavHost
import com.jaewonlee.aidietrecord.ui.theme.AIDietRecordTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var darkThemeEnabled by rememberSaveable {
                mutableStateOf(false)
            }
            AIDietRecordTheme(darkTheme = darkThemeEnabled) {
                AIDietNavHost(
                    darkThemeEnabled = darkThemeEnabled,
                    onDarkThemeChange = { darkThemeEnabled = it }
                )
            }
        }
    }
}
