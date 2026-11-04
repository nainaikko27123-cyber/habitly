package com.naikko.habitly.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.naikko.habitly.ui.calendar.CalendarScreen
import com.naikko.habitly.ui.clock.ClockScreen
import com.naikko.habitly.ui.habits.TodayScreen
import com.naikko.habitly.ui.icons.HabitlyIcons
import com.naikko.habitly.ui.notes.NotesScreen
import com.naikko.habitly.viewmodel.AppViewModelProvider
import com.naikko.habitly.viewmodel.StopwatchViewModel

/** The four bottom-bar destinations. */
enum class TopLevelDestination(val route: String, val label: String, val icon: ImageVector) {
    Today("today", "Today", HabitlyIcons.Today),
    Calendar("calendar", "Calendar", HabitlyIcons.Calendar),
    Clock("clock", "Clock", HabitlyIcons.Timer),
    Notes("notes", "Notes", HabitlyIcons.Notes);

    companion object {
        fun fromRoute(route: String?) = entries.firstOrNull { it.route == route } ?: Today
    }
}

@Composable
fun HabitlyRoot(startRoute: String? = null) {
    val navController = rememberNavController()
    // Activity-scoped so the stopwatch keeps running while you browse other tabs.
    val stopwatch: StopwatchViewModel = viewModel(factory = AppViewModelProvider.Factory)

    Scaffold(bottomBar = { HabitlyBottomBar(navController) }) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = TopLevelDestination.fromRoute(startRoute).route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { fadeIn(tween(220)) + scaleIn(tween(220), initialScale = 0.98f) },
            exitTransition = { fadeOut(tween(120)) },
            popEnterTransition = { fadeIn(tween(220)) },
            popExitTransition = { fadeOut(tween(120)) },
        ) {
            composable(TopLevelDestination.Today.route) { TodayScreen() }
            composable(TopLevelDestination.Calendar.route) { CalendarScreen() }
            composable(TopLevelDestination.Clock.route) { ClockScreen(stopwatch) }
            composable(TopLevelDestination.Notes.route) { NotesScreen() }
        }
    }
}

@Composable
private fun HabitlyBottomBar(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val current = backStackEntry?.destination
    NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainerLow) {
        TopLevelDestination.entries.forEach { destination ->
            val selected = current?.hierarchy?.any { it.route == destination.route } == true
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(destination.route) {
                        // Standard bottom-nav behaviour: one copy of each tab, state kept per tab.
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(destination.icon, contentDescription = null) },
                label = { Text(destination.label) },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            )
        }
    }
}
