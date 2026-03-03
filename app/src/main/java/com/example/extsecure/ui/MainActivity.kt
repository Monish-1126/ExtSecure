package com.example.extsecure.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.compose.*
import com.example.extsecure.broadcast.NetworkUtil
import com.example.extsecure.ui.screens.HomeScreen
import com.example.extsecure.ui.screens.HistoryScreen
import com.example.extsecure.ui.theme.ExtSecureTheme
import com.example.extsecure.viewmodel.ScanViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: ScanViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ExtSecureTheme {

                val navController = rememberNavController()
                val currentBackStack by navController.currentBackStackEntryAsState()
                val currentRoute = currentBackStack?.destination?.route

                // Update network state once on launch
                viewModel.updateNetworkStatus(
                    NetworkUtil.isNetworkAvailable(this)
                )

                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                selected = currentRoute == "home",
                                onClick = { navController.navigate("home") },
                                label = { Text("Home") },
                                icon = { Icon(Icons.Default.Search, null) }
                            )
                            NavigationBarItem(
                                selected = currentRoute == "history",
                                onClick = { navController.navigate("history") },
                                label = { Text("History") },
                                icon = { Icon(Icons.Default.History, null) }
                            )
                        }
                    }
                ) { padding ->
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = androidx.compose.ui.Modifier.padding(padding)
                    ) {
                        composable("home") {
                            HomeScreen(viewModel)
                        }
                        composable("history") {
                            HistoryScreen(viewModel)
                        }
                    }
                }
            }
        }
    }
}