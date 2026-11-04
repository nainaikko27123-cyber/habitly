package com.naikko.habitly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.naikko.habitly.ui.navigation.HabitlyRoot
import com.naikko.habitly.ui.theme.HabitlyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val startTab = intent.getStringExtra(EXTRA_START_TAB)
        setContent {
            HabitlyTheme {
                HabitlyRoot(startRoute = startTab)
            }
        }
    }

    companion object {
        const val EXTRA_START_TAB = "start_tab"
    }
}
