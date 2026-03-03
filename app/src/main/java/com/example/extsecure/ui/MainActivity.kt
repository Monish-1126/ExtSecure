package com.example.extsecure.ui

import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.material3.Surface
import com.example.extsecure.broadcast.NetworkReceiver
import com.example.extsecure.ui.screens.ScanScreen
import com.example.extsecure.ui.theme.ExtSecureTheme
import com.example.extsecure.viewmodel.ScanViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: ScanViewModel by viewModels()
    private val networkReceiver = NetworkReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        registerNetworkReceiver()

        setContent {
            ExtSecureTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ScanScreen(scanViewModel = viewModel)
                }
            }
        }
    }

    private fun registerNetworkReceiver() {
        networkReceiver.onNetworkChanged = { isConnected ->
            viewModel.updateNetworkStatus(isConnected)
        }

        @Suppress("DEPRECATION")
        registerReceiver(
            networkReceiver,
            IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        )

        // Seed initial state
        viewModel.updateNetworkStatus(NetworkReceiver.isNetworkAvailable(this))
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(networkReceiver)
    }
}
