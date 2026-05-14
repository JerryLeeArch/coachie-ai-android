package com.jaewonlee.aidietrecord

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.jaewonlee.aidietrecord.navigation.AIDietNavHost
import com.jaewonlee.aidietrecord.ui.theme.AIDietRecordTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AIDietRecordTheme {
                AIDietNavHost()
            }
        }
    }
}
