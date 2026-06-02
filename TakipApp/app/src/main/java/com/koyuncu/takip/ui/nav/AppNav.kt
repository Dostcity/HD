package com.koyuncu.takip.ui.nav

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.koyuncu.takip.ui.todo.TodoScreen
import com.koyuncu.takip.ui.todo.TodoViewModel
import com.koyuncu.takip.ui.tracking.TrackingScreen
import com.koyuncu.takip.ui.tracking.TrackingViewModel

private sealed class Dest(val route: String, val label: String, val icon: ImageVector) {
    data object Todo : Dest("todo", "Yapılacaklar", Icons.AutoMirrored.Filled.List)
    data object Tracking : Dest("tracking", "Fiyat Takip", Icons.Filled.TrendingDown)
}

@Composable
fun AppNav(
    todoVmFactory: () -> TodoViewModel,
    trackingVmFactory: () -> TrackingViewModel
) {
    val navController = rememberNavController()
    val items = listOf(Dest.Todo, Dest.Tracking)

    Scaffold(
        bottomBar = {
            NavigationBar {
                val backStack by navController.currentBackStackEntryAsState()
                val current = backStack?.destination
                items.forEach { dest ->
                    NavigationBarItem(
                        selected = current?.hierarchy?.any { it.route == dest.route } == true,
                        onClick = {
                            navController.navigate(dest.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(dest.icon, contentDescription = dest.label) },
                        label = { Text(dest.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(navController = navController, startDestination = Dest.Todo.route) {
            composable(Dest.Todo.route) {
                val vm: TodoViewModel = viewModel(factory = simpleFactory(todoVmFactory))
                TodoScreen(vm, Modifier.padding(padding))
            }
            composable(Dest.Tracking.route) {
                val vm: TrackingViewModel = viewModel(factory = simpleFactory(trackingVmFactory))
                TrackingScreen(vm, Modifier.padding(padding))
            }
        }
    }
}
