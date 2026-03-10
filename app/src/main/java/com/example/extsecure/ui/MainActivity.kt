package com.example.extsecure.ui

import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.*
import com.example.extsecure.broadcast.NetworkReceiver
import com.example.extsecure.ui.screens.DetailScreen
import com.example.extsecure.ui.screens.HistoryScreen
import com.example.extsecure.ui.screens.ScanScreen
import com.example.extsecure.ui.screens.SettingsScreen
import com.example.extsecure.ui.theme.ExtSecureTheme
import com.example.extsecure.viewmodel.ScanViewModel
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : ComponentActivity() {

    private val viewModel: ScanViewModel by viewModels()

    // BroadcastReceiver instance used as the single source of network updates.
    private var networkReceiver: NetworkReceiver? = null

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Register BroadcastReceiver for connectivity changes.
        networkReceiver = NetworkReceiver { isConnected ->
            viewModel.updateNetworkStatus(isConnected)
        }
        @Suppress("DEPRECATION")
        registerReceiver(
            networkReceiver,
            IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        )

        // Set initial network status on app launch.
        viewModel.updateNetworkStatus(NetworkReceiver.checkConnectivity(this))

        setContent {
            // 0 = System, 1 = Light, 2 = Dark
            var themeMode by rememberSaveable { mutableIntStateOf(0) }
            val isDark = when (themeMode) {
                1 -> false
                2 -> true
                else -> isSystemInDarkTheme()
            }

            ExtSecureTheme(darkTheme = isDark) {
                val navController = rememberNavController()
                val currentBackStack by navController.currentBackStackEntryAsState()
                val currentRoute = currentBackStack?.destination?.route
                val isDetailScreen = currentRoute?.startsWith("detail/") == true
                val isSettingsScreen = currentRoute == "settings"
                val isSubScreen = isDetailScreen || isSettingsScreen
                val isNetworkAvailable by viewModel.isNetworkAvailable.collectAsState()

                Scaffold(
                    topBar = {
                        Column {
                            CenterAlignedTopAppBar(
                                title = {
                                    Text(
                                        text = when {
                                            isDetailScreen -> "Scan Details"
                                            isSettingsScreen -> "Settings"
                                            else -> "ExtSecure"
                                        },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    )
                                },
                                navigationIcon = {
                                    if (isSubScreen) {
                                        IconButton(onClick = { navController.popBackStack() }) {
                                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                        }
                                    } else {
                                        IconButton(onClick = {}) {
                                            Icon(Icons.Default.Shield, contentDescription = "Logo")
                                        }
                                    }
                                },
                                actions = {
                                    if (!isSubScreen) {
                                        IconButton(onClick = {
                                            navController.navigate("settings") {
                                                popUpTo("home")
                                            }
                                        }) {
                                            Icon(
                                                Icons.Default.Settings,
                                                contentDescription = "Settings",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                },
                                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.background,
                                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                                    navigationIconContentColor = MaterialTheme.colorScheme.primary
                                )
                            )

                            // ── Network warning banner (driven by BroadcastReceiver) ──
                            AnimatedVisibility(
                                visible = !isNetworkAvailable,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Surface(
                                    color = MaterialTheme.colorScheme.errorContainer,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "⚠\uFE0F No internet connection — scans won't work",
                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                        }
                    },
                    bottomBar = {
                        if (!isSubScreen) {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface
                            ) {
                                NavigationBarItem(
                                    selected = currentRoute == "home",
                                    onClick = {
                                        navController.navigate("home") {
                                            popUpTo("home") { inclusive = true }
                                        }
                                    },
                                    label = { Text("Scan") },
                                    icon = { Icon(Icons.Default.Search, null) }
                                )
                                NavigationBarItem(
                                    selected = currentRoute == "history",
                                    onClick = {
                                        navController.navigate("history") {
                                            popUpTo("home")
                                        }
                                    },
                                    label = { Text("History") },
                                    icon = { Icon(Icons.Default.History, null) }
                                )
                            }
                        }
                    }
                ) { padding ->
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier.padding(padding),
                        enterTransition = {
                            fadeIn(animationSpec = tween(300)) +
                                slideInHorizontally(animationSpec = tween(300)) { it / 4 }
                        },
                        exitTransition = {
                            fadeOut(animationSpec = tween(300)) +
                                slideOutHorizontally(animationSpec = tween(300)) { -it / 4 }
                        },
                        popEnterTransition = {
                            fadeIn(animationSpec = tween(300)) +
                                slideInHorizontally(animationSpec = tween(300)) { -it / 4 }
                        },
                        popExitTransition = {
                            fadeOut(animationSpec = tween(300)) +
                                slideOutHorizontally(animationSpec = tween(300)) { it / 4 }
                        }
                    ) {
                        composable("home") {
                            ScanScreen(viewModel, navController)
                        }
                        composable("history") {
                            HistoryScreen(viewModel, navController)
                        }
                        composable("detail/{id}") { backStackEntry ->
                            val id = backStackEntry.arguments?.getString("id") ?: ""
                            DetailScreen(id, viewModel, navController)
                        }
                        composable("settings") {
                            SettingsScreen(
                                viewModel = viewModel,
                                themeMode = themeMode,
                                onThemeModeChanged = { themeMode = it }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister BroadcastReceiver to avoid leaks.
        networkReceiver?.let {
            try {
                unregisterReceiver(it)
            } catch (_: IllegalArgumentException) {
                // Already unregistered.
            }
        }
        networkReceiver = null
    }
}