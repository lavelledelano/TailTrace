

package com.example.tailtrace

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

@Composable
fun MainScaffold(vm: AppViewModel) {
    val nav = rememberNavController()
    val route = nav.currentBackStackEntryAsState().value?.destination?.route
    val settings = vm.settings.collectAsState().value

    val requestLocation = rememberLocationRequester { la, ln -> vm.setLocation(la, ln) }
    LaunchedEffect(Unit) { requestLocation() }
    LaunchedEffect(vm.lat, vm.lng, settings?.radius) { vm.loadNearby() }

    val tabs = listOf(
        Triple("map", "Map", Icons.Default.Map),
        Triple("cases", "Cases", Icons.Default.Pets),
        Triple("settings", "Settings", Icons.Default.Settings),
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (route == "map" || route == "cases" || route == "settings") {
                NavigationBar {
                    tabs.forEach { (r, label, icon) ->
                        NavigationBarItem(
                            selected = route == r,
                            onClick = { nav.navigate(r) { popUpTo("map"); launchSingleTop = true } },
                            icon = { Icon(icon, label) },
                            label = { Text(label) },
                        )
                    }
                }
            }
        },
    ) { pad ->
        NavHost(nav, startDestination = "map", modifier = Modifier.padding(pad)) {
            composable("map") { Placeholder("Map (coming soon)") }
            composable("cases") { CasesScreen(vm, nav) }
            composable("create") { CreateCaseScreen(vm, nav) }
            composable("detail/{id}") { DetailScreen(vm, nav, it.arguments!!.getString("id")!!) }
            composable("sighting/{id}") { SightingScreen(vm, nav, it.arguments!!.getString("id")!!) }
            composable("settings") { SettingsScreen(vm) }
        }
    }
}

@Composable
fun Placeholder(title: String, extra: @Composable () -> Unit = {}) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        Alignment.CenterHorizontally,
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        extra()
    }
}